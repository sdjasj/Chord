package org.fisco.bcos.sdk.demo.fuzz;

import com.google.common.util.concurrent.RateLimiter;
import org.checkerframework.checker.units.qual.A;

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
    public static Random random = new Random(System.currentTimeMillis());

    public static final Logger logger = Logger.getLogger(CrossFuzzer.class.getName());

    public ContractModel contractModel;
    public ArrayList<CrossCallModel> crossCallModels;
    public ArrayList<ContractModel> seedPool;
    public Transaction[] transactions;
    public ArrayList<Double> tpsOfEachRound = new ArrayList<>();
    public ArrayList<Double> tpsOfDiscussion = new ArrayList<>();

    public ArrayList<Double> threshold = new ArrayList<Double>() {
        {
            add(0.1);
            add(0.3);
            add(0.5);
        }
    };


    public CrossFuzzer() {
        this.contractModel = new ContractModel();
        this.seedPool = new ArrayList<>();
        this.transactions = new Transaction[Constant.TX_COUNT + Constant.CROSS_TX_COUNT * Constant.CROSS_CONTRACT_NUM];
        init();
        initCrossModel();
    }

    public void init() {
        Fuzzer.initLogger();
        initContract();
        contractModel.generateTransactions();
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
                                crossCallModel.deploy();
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

    public void testFuzz(int QPS) throws InterruptedException {
        System.out.println("start fuzzing at " + System.currentTimeMillis() + " .......");
        RateLimiter limiter = RateLimiter.create(QPS);
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

//        getTransactions();
        contractModel.collector.flush();

        contractModel.bar = Bar.generate(Constant.TX_COUNT + Constant.CROSS_TX_COUNT * Constant.CROSS_CONTRACT_NUM,
                contractModel.collector);

        IntStream.range(0, Constant.TX_COUNT + Constant.CROSS_TX_COUNT * Constant.CROSS_CONTRACT_NUM)
                .parallel()
                .forEach(
                        i -> {
                            //TODO: bug: if exec after sleep, the qps will be very slow, need to find out the reason and fix it
//                                limiter.acquire();
//                                try {
//                                    contractTransactions[i].execAfterSleep();
//                                } catch (InterruptedException e) {
//                                    throw new RuntimeException(e);
//                                }
                            //not fixed qps now, just as fast as possible
                            limiter.acquire();

                            try {
                                transactions[i].exec();
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
        IntStream.range(0, Constant.TX_COUNT + Constant.CROSS_TX_COUNT * Constant.CROSS_CONTRACT_NUM)
                        .parallel()
                                .forEach(
                                        i -> {
                                            totalCostTime.addAndGet(transactions[i].executeTime);
                                        }
                                );
        double averageLatency = totalCostTime.doubleValue() / (double) (Constant.TX_COUNT + Constant.CROSS_TX_COUNT * Constant.CROSS_CONTRACT_NUM);

        Util.appendToFile("re_tps.log", String.format("QPS:%d time:%d  tps:%f  tpsNoError:%f latency:%f\n",
                QPS,
                System.currentTimeMillis(),
                tps,
                contractModel.collector.getTpsWithoutError(),
                averageLatency)
        );

        contractModel.generateTransactions();
//        updateSeed(contractModel, tps);
        contractModel.check();
        System.out.println("fuzzing round finish!!!!!!!!!!!!!");
        System.out.println("now mutate contract for explore more combination");
        mutateSeed();
        contractModel.generateTransactions();
        crossCallModels.forEach(
                CrossCallModel::generateTransactions
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
        try {
            Util.executeCommand("rm /root/java-sdk-demo/dist/contracts/solidity/*");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("mutate successfully!!!!!!");
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

//        getTransactions();
        contractModel.collector.flush();

        contractModel.bar = Bar.generate(Constant.TX_COUNT + Constant.CROSS_TX_COUNT * Constant.CROSS_CONTRACT_NUM,
                contractModel.collector);

        IntStream.range(0, Constant.TX_COUNT + Constant.CROSS_TX_COUNT * Constant.CROSS_CONTRACT_NUM)
                .parallel()
                .forEach(
                        i -> {
                            //TODO: bug: if exec after sleep, the qps will be very slow, need to find out the reason and fix it
//                                limiter.acquire();
//                                try {
//                                    contractTransactions[i].execAfterSleep();
//                                } catch (InterruptedException e) {
//                                    throw new RuntimeException(e);
//                                }
                            //not fixed qps now, just as fast as possible
                            limiter.acquire();

                            try {
                                transactions[i].exec();
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
        Util.appendToFile("tps.log", String.format("time:%d  tps:%f  tpsNoError:%f\n",
                System.currentTimeMillis(),
                tps,
                contractModel.collector.getTpsWithoutError())
        );

        contractModel.generateTransactions();
//        updateSeed(contractModel, tps);
        contractModel.check();
        System.out.println("fuzzing round finish!!!!!!!!!!!!!");
        System.out.println("now mutate contract for explore more combination");
        mutateSeed();
        contractModel.generateTransactions();
        crossCallModels.forEach(
                CrossCallModel::generateTransactions
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

        if (tpsOfDiscussion.isEmpty()) {
            tpsOfDiscussion.add(tps);
        } else {
            double tpsOfRound0 = tpsOfDiscussion.get(0);
            double totSumOfTps = 0;
            for (Double aDouble : tpsOfDiscussion) {
                totSumOfTps += aDouble;
            }
            double averageTps = totSumOfTps / tpsOfDiscussion.size();
            Util.appendToFile("discussion_tps.log", String.format("time:%d tps:%f tpsOfRound0:%f averageTps:%f\n", System.currentTimeMillis(), tps, tpsOfRound0, averageTps));
            double deltaOfRound0 = (tpsOfRound0 - tps) / tpsOfRound0;
            double deltaOfAverage = (averageTps - tps) / averageTps;
            for (Double val : threshold) {
                if (deltaOfRound0 >= val) {
                    Util.appendToFile(String.format("%f_round0_fp.log", val), String.format("time:%d tps:%f tpsOfRound0:%f delta:%f%%\n", System.currentTimeMillis(), tps, tpsOfRound0, deltaOfRound0));
                }
                if (deltaOfAverage >= val) {
                    Util.appendToFile(String.format("%f_average_fp.log", val), String.format("time:%d tps:%f tpsOfAverage:%f delta:%f%%\n", System.currentTimeMillis(), tps, averageTps, deltaOfAverage));
                }
            }
            tpsOfDiscussion.add(tps);
        }

        try {
            Util.executeCommand("rm /root/java-sdk-demo/dist/contracts/solidity/*");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("mutate successfully!!!!!!");
    }

    public void initContract() {
        System.out.println("init for fuzzing.......");
        contractModel.writeToLocal();
        try {
            contractModel.deploy();
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

}
