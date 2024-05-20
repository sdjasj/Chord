package org.example;

import com.google.common.util.concurrent.RateLimiter;

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

public class FuzzerWithNotConflict {
    public static ArrayList<String> privateKeys = new ArrayList<>();

    public static Random random = new Random(System.currentTimeMillis());

    public static final Logger logger = Logger.getLogger(FuzzerWithNotConflict.class.getName());

    public static ArrayList<Class<?>> transactionList = new ArrayList<>();

    public ContractOne contractOne;
//    public ContractTwo contractTwo;
//    public ContractThree contractThree;

    public Transaction[] transactions;
    public ArrayList<Seed> seedPools = new ArrayList<>();

    public double min_tps = Double.MAX_VALUE;

    public Collector collector = new Collector();


    static {
        transactionList.add(TransactionOne.class);
    }

    public static String getRandomPrivateKey() {
        return privateKeys.get(random.nextInt(privateKeys.size()));
    }

    public FuzzerWithNotConflict() {
        init();
    }

    public void init() {
        ReadPrivateKeys();
        associateKeys();
        FuzzerWithNotConflict.initLogger();
        initContracts();
    }

    public void updateSeed(Transaction[] transactions, double tps) {
        if (seedPools.size() < Constant.SEED_COUNTS) {
            seedPools.add(new Seed(transactions, tps));
            return;
        }
        int idx = -1;
        double minv = tps;
        for (int i = 0; i < seedPools.size(); i++) {
            if (seedPools.get(i).tps > minv) {
                idx = i;
                minv = seedPools.get(i).tps;
            }
        }
        if (idx != -1) {
            System.out.println(tps);
            seedPools.set(idx, new Seed(transactions, tps));
        }

    }

    public void specifySeed() {
        if (seedPools.isEmpty()) {
            return;
        }
        this.transactions = seedPools.get(random.nextInt(seedPools.size())).transactions;
    }

    public void mutateSeed() {
        IntStream.range(0, Constant.TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            transactions[i].mutate();
                        });
    }


    public void fuzz() throws InterruptedException {
        System.out.println("start fuzzing at " + System.currentTimeMillis() + " .......");
        RateLimiter limiter = RateLimiter.create(Constant.QPS);
        collector.flush();
//        specifySeed();

        //mutate the seed
//        mutateSeed();


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

                            transactions[i].exec();
                        });
//        double tps = utilItems.getTPS();
        AtomicLong totalTxTime = new AtomicLong();
        IntStream.range(0, Constant.TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            totalTxTime.addAndGet(transactions[i].executeTime);
                        }
                );
        double tps = totalTxTime.doubleValue() / (double) Constant.TX_COUNT;
//        updateSeed(transactions, tps);
        contractOne.check();
//        contractTwo.check();
//        contractThree.check();
        System.out.println("fuzzing round finish!!!!!!!!!!!!!");
    }

    public void slowFuzz() throws InterruptedException {
        System.out.println("start fuzzing at " + System.currentTimeMillis() + " .......");
        collector.flush();
        IntStream.range(0, Constant.TX_COUNT)
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

                            transactions[i].exec();
                        });
//        double tps = utilItems.getTPS();
        AtomicLong totalTxTime = new AtomicLong();
        IntStream.range(0, Constant.TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            totalTxTime.addAndGet(transactions[i].executeTime);
                        }
                );
        double tps = totalTxTime.doubleValue() / (double) Constant.TX_COUNT;
        //TODO: now just mutate, not save the seeds
        if (true) {
            min_tps = tps;
            IntStream.range(0, Constant.TX_COUNT)
                    .parallel()
                    .forEach(
                            i -> {
                                transactions[i].mutate();
                            });
        }
        contractOne.check();
//        contractTwo.check();
//        contractThree.check();
        System.out.println("fuzzing round finish!!!!!!!!!!!!!");
    }

    public Transaction getTransactions() {
        if (transactionList == null || transactionList.isEmpty()) {
            throw new IllegalArgumentException("Transaction list is null or empty");
        }

        int randomIndex = random.nextInt(transactionList.size());
        Class<?> selectedClass = transactionList.get(randomIndex);

        try {
//            if (selectedClass.equals(TransactionOne.class)) {
//                return (Transaction) selectedClass.
//                        getDeclaredConstructor(ContractOne.class, Collector.class).
//                        newInstance(contractOne, collector);
//            }
////            else if (selectedClass.equals(TransactionTwo.class)) {
////                return (Transaction) selectedClass.
////                        getDeclaredConstructor(ContractTwo.class, Collector.class).
////                        newInstance(contractTwo, collector);
////            } else if (selectedClass.equals(TransactionThree.class)) {
////                return (Transaction) selectedClass.
////                        getDeclaredConstructor(ContractThree.class, Collector.class).
////                        newInstance(contractThree, collector);
////            }
//            else {
//                System.out.println("fatal error.............");
//            }
            return (Transaction) selectedClass.
                    getDeclaredConstructor(ContractOne.class, Collector.class).
                    newInstance(contractOne, collector);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error instantiating class: " + selectedClass.getSimpleName(), e);
        }
    }

    public void initContracts() {
        System.out.println("init for fuzzing.......");

        System.out.println("init contract one.....");
        contractOne = new ContractOne(Constant.CONTRACT_NUM);
        contractOne.init();
        System.out.println("init contract one successfully...");

//        System.out.println("init contract two.....");
//        contractTwo = new ContractTwo(Constant.CONTRACT_NUM);
//        contractTwo.init();
//        System.out.println("init contract two successfully...");
//
//        System.out.println("init contract three...");
//        contractThree = new ContractThree(Constant.CONTRACT_NUM);
//        contractThree.init();
//        System.out.println("init contract three successfully...");

        System.out.println("generate transaction......");
        transactions = new Transaction[Constant.TX_COUNT];
        IntStream.range(0, Constant.TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            transactions[i] = getTransactions();
                        }
                );
        System.out.println("generate transaction finish.... total count is " + Constant.TX_COUNT);

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
