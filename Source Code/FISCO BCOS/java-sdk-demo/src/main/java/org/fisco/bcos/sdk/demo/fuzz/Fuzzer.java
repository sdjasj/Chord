package org.fisco.bcos.sdk.demo.fuzz;

import com.google.common.util.concurrent.RateLimiter;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;

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


public class Fuzzer {

    public static Random random = new Random(System.currentTimeMillis());

    public static final Logger logger = Logger.getLogger(Fuzzer.class.getName());

    public ContractModel contractModel;
    public ArrayList<ContractModel> seedPool;




    public Fuzzer() {
        this.contractModel = new ContractModel();
        this.seedPool = new ArrayList<>();
        init();
    }

    public void init() {
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
    }

    public void testFuzz(int QPS, int ROUND_TIMES) throws InterruptedException {
        System.out.println("start fuzzing at " + System.currentTimeMillis() + " .......");
        RateLimiter limiter = RateLimiter.create(QPS);
        contractModel.bar = Bar.generate(Constant.TX_COUNT, contractModel.collector);
//        specifySeed();
//
//        //mutate the seed
//        if (!seedPool.isEmpty()) {
//            mutateSeed();
//            initContract();
//        }
        contractModel.collector.flush();


//        getTransactions();

        IntStream.range(0, Constant.TX_COUNT)
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
                                contractModel.transactions[i].exec();
                                contractModel.bar.sendedBar.step();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
//        double tps = utilItems.getTPS();

        contractModel.bar.transactionLatch.await();
        contractModel.collector.setTotalTime();
        contractModel.bar.processEnd();
        AtomicLong totalTxTime = new AtomicLong();
        IntStream.range(0, Constant.TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            totalTxTime.addAndGet(contractModel.transactions[i].executeTime);
                        }
                );
        double tps = totalTxTime.doubleValue() / (double) Constant.TX_COUNT;
        contractModel.generateTransactions();
//        updateSeed(contractModel, tps);
        contractModel.check();
        System.out.println("fuzzing round finish!!!!!!!!!!!!!");
        System.out.println("now mutate contract for explore more combination");
        mutateSeed();
        initContract();
        contractModel.generateTransactions();
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
        contractModel.bar = Bar.generate(Constant.TX_COUNT, contractModel.collector);
//        specifySeed();
//
//        //mutate the seed
//        if (!seedPool.isEmpty()) {
//            mutateSeed();
//            initContract();
//        }
        contractModel.collector.flush();


//        getTransactions();

        IntStream.range(0, Constant.TX_COUNT)
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
                                contractModel.transactions[i].exec();
                                contractModel.bar.sendedBar.step();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
//        double tps = utilItems.getTPS();

        contractModel.bar.transactionLatch.await();
        contractModel.collector.setTotalTime();
        contractModel.bar.processEnd();
        AtomicLong totalTxTime = new AtomicLong();
        IntStream.range(0, Constant.TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            totalTxTime.addAndGet(contractModel.transactions[i].executeTime);
                        }
                );
        double tps = totalTxTime.doubleValue() / (double) Constant.TX_COUNT;
        contractModel.generateTransactions();
//        updateSeed(contractModel, tps);
        contractModel.check();
        System.out.println("fuzzing round finish!!!!!!!!!!!!!");
        System.out.println("now mutate contract for explore more combination");
        mutateSeed();
        initContract();
        contractModel.generateTransactions();
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

