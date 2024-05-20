package org.example.model;

import com.google.common.util.concurrent.RateLimiter;
import org.example.Constant;
import org.example.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.IntStream;

public class CrossFuzzer {
    public static ArrayList<String> privateKeys = new ArrayList<>();

    public static Random random = new Random(System.currentTimeMillis());

    public static final Logger logger = Logger.getLogger(org.example.Fuzzer.class.getName());

    public ContractModel contractModel;
    public ArrayList<CrossCallModel> crossCallModels;
    public ArrayList<ContractModel> seedPool;
    public Transaction[] transactions;
    public ArrayList<Double> tpsOfEachRound = new ArrayList<>();

    public static String getRandomPrivateKey() {
        return privateKeys.get(random.nextInt(privateKeys.size()));
    }

    public CrossFuzzer() {
        this.contractModel = new ContractModel();
        this.seedPool = new ArrayList<>();
        this.transactions = new Transaction[Constant.TX_COUNT + Constant.CROSS_TX_COUNT * Constant.CROSS_CONTRACT_NUM];
        init();
        initCrossModel();
    }

    public void initCrossModel() {
        System.out.println("init crossModel ....");
        crossCallModels = new ArrayList<>();
        IntStream.range(0, Constant.CROSS_CONTRACT_NUM)
                .forEach(
                        i -> {
                            CrossCallModel crossCallModel = new CrossCallModel(contractModel);
                            crossCallModel.init();
                            try {
                                crossCallModel.deploy(getRandomPrivateKey());
                            } catch (IOException e) {
                                System.out.println("deploy contract " + crossCallModel.name + " error!!!!!");
                                throw new RuntimeException(e);
                            }
                            crossCallModel.generateTransactions();
                            crossCallModels.add(crossCallModel);
                        }
                );
        System.out.println("init crossModel finish");
    }

    public void init() {
        ReadPrivateKeys();
        associateKeys();
        Fuzzer.initLogger();
        initContract();
        contractModel.generateTransactions();
    }

    public void updateSeed(ContractModel contractModel, double tps) {
        if (seedPool.size() < Constant.SEED_COUNTS) {
            seedPool.add(contractModel);
            return;
        }
        int idx = -1;
        double minv = tps;
        for (int i = 0; i < seedPool.size(); i++) {
            if (seedPool.get(i).tps > minv) {
                idx = i;
                minv = seedPool.get(i).tps;
            }
        }
        if (idx != -1) {
            System.out.println(tps);
            seedPool.set(idx, contractModel);
        }

    }

    public void specifySeed() {
        if (seedPool.isEmpty()) {
            return;
        }
        this.contractModel = seedPool.get(Util.random.nextInt(seedPool.size()));
    }

    public void mutateSeed() {
        ContractModel newSeed = Util.deepCopy(contractModel);
        newSeed.clear();
        newSeed.mutate();
        newSeed.generateContract();
        this.contractModel = newSeed;
        initContract();
        initCrossModel();
    }

    public void testFuzz(int QPS, String fileName) throws InterruptedException {
        System.out.println("start fuzzing at " + System.currentTimeMillis() + " .......");
        RateLimiter limiter = RateLimiter.create(QPS);

//        specifySeed();
//
//        //mutate the seed
//        if (!seedPool.isEmpty()) {
//            mutateSeed();
//            initContract();
//        }

        IntStream.range(0, Math.min(Constant.TX_COUNT, 10*QPS))
                .parallel()
                .forEach(
                        i -> {
                            transactions[i] = contractModel.transactions[i];
                        }
                );
        for (int i = 0; i < Constant.CROSS_CONTRACT_NUM; i++) {
            int bg = Math.min(Constant.TX_COUNT, 10*QPS) + i * Math.min(Constant.CROSS_TX_COUNT, QPS);
            int finalI = i;
            IntStream.range(bg, bg + Math.min(Constant.CROSS_TX_COUNT, QPS))
                    .parallel()
                    .forEach(
                            j -> {
                                transactions[j] = crossCallModels.get(finalI).crossTransactions[j - bg];
                            }
                    );
        }

        contractModel.collector.flush();
        contractModel.bar = Bar.generate(Math.min(Constant.TX_COUNT, 10*QPS) + Math.min(Constant.CROSS_TX_COUNT, QPS) * Constant.CROSS_CONTRACT_NUM,
                contractModel.collector);
//        getTransactions();
        IntStream.range(0, Math.min(Constant.TX_COUNT, 10*QPS) +  Math.min(Constant.CROSS_TX_COUNT, QPS) * Constant.CROSS_CONTRACT_NUM)
                .parallel()
                .forEach(
                        i -> {

                            //not fixed qps now, just as fast as possible
                            limiter.acquire();

                            try {
                                if (transactions[i] instanceof CrossTransaction) {
                                    ((CrossTransaction) transactions[i]).exec();
                                } else {
                                    transactions[i].exec();
                                }
                                contractModel.bar.sendedBar.step();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
//        double tps = utilItems.getTPS();
        contractModel.bar.transactionLatch.await();
        contractModel.collector.setTotalTime();
        contractModel.bar.processEnd();
        double tps = contractModel.collector.getTPS();
        AtomicLong totalCostTime = new AtomicLong();
        IntStream.range(0, Math.min(Constant.TX_COUNT, 10*QPS) +  Math.min(Constant.CROSS_TX_COUNT, QPS) * Constant.CROSS_CONTRACT_NUM)
                .parallel()
                .forEach(
                        i -> {
                            totalCostTime.addAndGet(transactions[i].executeTime);
                        }
                );
        double averageLatency = totalCostTime.doubleValue() / (double) (Math.min(Constant.TX_COUNT, 10*QPS) +  Math.min(Constant.CROSS_TX_COUNT, QPS) * Constant.CROSS_CONTRACT_NUM);

        Util.appendToFile(fileName, String.format("QPS:%d time:%d  tps:%f  tpsNoError:%f latency:%f\n",
                QPS,
                System.currentTimeMillis(),
                tps,
                contractModel.collector.getTpsWithoutError(),
                averageLatency)
        );
        if (tpsOfEachRound.isEmpty()) {
            tpsOfEachRound.add(tps);
        } else {
            if (tpsOfEachRound.get(tpsOfEachRound.size() - 1) > tps) {
                tpsOfEachRound.add(tps);
            } else {
                tpsOfEachRound.clear();
            }
        }
        if (tpsOfEachRound.size() > Constant.MAX_TPS_DESCEND_TIMES) {
            System.out.println("tps descends continuously, maybe liveness bug occurs");
        }
        if (tps < Constant.MIN_TPS_TOLERANT) {
            System.out.println("tps is less than min tolerant tps, maybe bug occurs");
        }
//        updateSeed(contractModel, tps);
        mutateSeed();
        contractModel.check();
        System.out.println("fuzzing round finish!!!!!!!!!!!!!");
        contractModel.collector.report();

    }


    public void fuzz() throws InterruptedException {
        System.out.println("start fuzzing at " + System.currentTimeMillis() + " .......");
        RateLimiter limiter = RateLimiter.create(Constant.QPS);

//        specifySeed();
//
//        //mutate the seed
//        if (!seedPool.isEmpty()) {
//            mutateSeed();
//            initContract();
//        }

        IntStream.range(0, Constant.TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            transactions[i] = contractModel.transactions[i];
                        }
                            );
        for (int i = 0; i < Constant.CROSS_CONTRACT_NUM; i++) {
            int bg = Constant.TX_COUNT + i * Constant.CROSS_TX_COUNT;
            int finalI = i;
            IntStream.range(bg, bg + Constant.CROSS_TX_COUNT)
                    .parallel()
                    .forEach(
                            j -> {
                                transactions[j] = crossCallModels.get(finalI).crossTransactions[j - bg];
                            }
                    );
        }

        contractModel.collector.flush();
        contractModel.bar = Bar.generate(Constant.TX_COUNT + Constant.CROSS_TX_COUNT * Constant.CROSS_CONTRACT_NUM,
                contractModel.collector);
//        getTransactions();
        IntStream.range(0, Constant.TX_COUNT + Constant.CROSS_TX_COUNT * Constant.CROSS_CONTRACT_NUM)
                .parallel()
                .forEach(
                        i -> {

                            //not fixed qps now, just as fast as possible
                            limiter.acquire();

                            try {
                                if (transactions[i] instanceof CrossTransaction) {
                                    ((CrossTransaction) transactions[i]).exec();
                                } else {
                                    transactions[i].exec();
                                }
                                contractModel.bar.sendedBar.step();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
//        double tps = utilItems.getTPS();
        contractModel.bar.transactionLatch.await();
        contractModel.collector.setTotalTime();
        contractModel.bar.processEnd();
        double tps = contractModel.collector.getTPS();
        Util.appendToFile("re_tps.log", String.format("time:%d  tps:%f  tpsNoError:%f\n",
                System.currentTimeMillis(),
                tps,
                contractModel.collector.getTpsWithoutError())
        );
        if (tpsOfEachRound.isEmpty()) {
            tpsOfEachRound.add(tps);
        } else {
            if (tpsOfEachRound.get(tpsOfEachRound.size() - 1) > tps) {
                tpsOfEachRound.add(tps);
            } else {
                tpsOfEachRound.clear();
            }
        }
        if (tpsOfEachRound.size() > Constant.MAX_TPS_DESCEND_TIMES) {
            System.out.println("tps descends continuously, maybe liveness bug occurs");
        }
        if (tps < Constant.MIN_TPS_TOLERANT) {
            System.out.println("tps is less than min tolerant tps, maybe bug occurs");
        }
//        updateSeed(contractModel, tps);
        mutateSeed();
        contractModel.check();
        System.out.println("fuzzing round finish!!!!!!!!!!!!!");
        contractModel.collector.report();

    }

    public void initContract() {
        System.out.println("init for fuzzing.......");
        contractModel.writeToLocal();
        try {
            contractModel.deploy(getRandomPrivateKey());
        } catch (IOException e) {
            System.out.println("deploy contract " + contractModel.name + " error!!!!!");
            throw new RuntimeException(e);
        }
        System.out.println("init finish !!!!!!!!!!!");
    }

    public static void initLogger() {
        FileHandler fileHandler = null;
        try {
            fileHandler = new FileHandler("myLogFile.log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 设置文件处理器的日志格式
        SimpleFormatter formatter = new SimpleFormatter();
        fileHandler.setFormatter(formatter);

        // 将文件处理器添加到Logger中
        logger.addHandler(fileHandler);

        // 设置日志级别
        logger.setLevel(Level.INFO);
    }

    public void associateKeys() {
        if (privateKeys.isEmpty()) {
            ReadPrivateKeys();
        }
        String cmd = "seid tx evm associate-address %s";
        IntStream.range(0, privateKeys.size())
                .parallel()
                .forEach(
                        i -> {
                            if (!privateKeys.get(i).startsWith("-")) {
                                try {
                                    Util.executeCommand(String.format(cmd, privateKeys.get(i)));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        }
                );
        System.out.println("every account has init fund.....");
    }

    public void ReadPrivateKeys() {
        File accountFileDirectory = new File(Constant.ACCOUNT_FILE);

        if (!accountFileDirectory.exists() || !accountFileDirectory.isDirectory()) {
            System.err.println("Account file directory does not exist or is not a directory.");
            return;
        }

        File[] files = accountFileDirectory.listFiles();

        if (files == null) {
            System.err.println("Error listing files in the account file directory.");
            return;
        }

        for (File file : files) {
            if (file.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String content = reader.readLine();
                    if (content != null) {
                        content = content.trim();
                        if (!content.startsWith("-")) {
                            privateKeys.add(content.trim());
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error reading private key from file: " + file.getAbsolutePath());
                    e.printStackTrace();
                }
            }
        }
    }

}
