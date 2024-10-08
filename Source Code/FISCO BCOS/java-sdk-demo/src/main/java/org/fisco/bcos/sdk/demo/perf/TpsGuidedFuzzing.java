package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.*;
import org.fisco.bcos.sdk.demo.fuzz.Util;
import org.fisco.bcos.sdk.demo.perf.tiger.ConstVal;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

class ShardingUtil {
    public static ShardingService shardingService;
    public static int shardNum;
    public static Random random = new Random(System.currentTimeMillis());

    public static void linkShard(String address) throws ContractException {
        int idx = random.nextInt(shardNum);
        String shardName = "testShard" + idx;
        shardingService.linkShard(shardName, address);
//        System.out.println(
//                "====== ShardingOk ParaTestTwo, deploy success to shard: "
//                        + shardName
//                        + ", address: "
//                        + address);
    }
}

class UtilItems {
    public Collector collector;
    public ProgressBar sendedBar;
    public ProgressBar receivedBar;
    public ProgressBar errorBar;
    CountDownLatch transactionLatch;

    public UtilItems(Collector collector, ProgressBar sendedBar, ProgressBar receivedBar, ProgressBar errorBar, CountDownLatch transactionLatch) {
        this.collector = collector;
        this.sendedBar = sendedBar;
        this.receivedBar = receivedBar;
        this.errorBar = errorBar;
        this.transactionLatch = transactionLatch;
    }

    public void processEnd() {
        sendedBar.close();
        receivedBar.close();
        errorBar.close();
        collector.report();
    }

    public double getTPS() {
        long totalTime = System.currentTimeMillis() - collector.getStartTimestamp();
        return (double) collector.getTotal() / ((double) totalTime / 1000);
    }

    public static UtilItems generate(int txCount) {
        Collector collector = new Collector();
        collector.setTotal(txCount);
        ProgressBar sendedBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(txCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(txCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar errorBar =
                new ProgressBarBuilder()
                        .setTaskName("Errors :")
                        .setInitialMax(txCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        CountDownLatch transactionLatch = new CountDownLatch(txCount);
        return new UtilItems(collector, sendedBar, receivedBar, errorBar, transactionLatch);
    }
}
class ClassParaTestTwo {
    public Client client;
    public int userCount;

    public UtilItems utilItems;
    public ContractSharedOne[] contractSharedOnes;
    public ContractSharedTwo[] contractSharedTwos;
    public ContractSharedThree[] contractSharedThrees;
    public AtomicLong[] summaryOne;
    public AtomicLong[] summaryTwo;
    public AtomicLong[] summaryThree;

    ClassParaTestTwo(int userCount, Client client) {
        this.client = client;
        this.userCount = userCount;
        init();
    }

    public void init() {
        contractSharedOnes = new ContractSharedOne[userCount];
        contractSharedTwos = new ContractSharedTwo[userCount];
        contractSharedThrees = new ContractSharedThree[userCount];
        summaryOne = new AtomicLong[userCount];
        summaryTwo = new AtomicLong[userCount];
        summaryThree = new AtomicLong[userCount];

        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ContractSharedOne contractSharedOne;
                            try {
                                contractSharedOne =
                                        ContractSharedOne.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());
                                contractSharedOne.setEnableDAG(true);
                                ShardingUtil.linkShard(contractSharedOne.getContractAddress());
                                summaryOne[i] = new AtomicLong();
                                contractSharedOnes[i] = contractSharedOne;
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });

        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ContractSharedTwo contractSharedTwo;
                            try {
                                contractSharedTwo =
                                        ContractSharedTwo.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());
                                contractSharedTwo.setEnableDAG(true);
                                ShardingUtil.linkShard(contractSharedTwo.getContractAddress());
                                contractSharedTwos[i] = contractSharedTwo;
                                summaryTwo[i] = new AtomicLong();
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });

        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ContractSharedThree contractSharedThree;
                            try {
                                contractSharedThree =
                                        ContractSharedThree.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());
                                contractSharedThree.setEnableDAG(true);
                                ShardingUtil.linkShard(contractSharedThree.getContractAddress());
                                contractSharedThrees[i] = contractSharedThree;
                                summaryThree[i] = new AtomicLong();
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });
    }

    public void check() throws InterruptedException {
        System.out.println("start check ClassParaTestTwo.......");
        int cnt = 0;
        AtomicBoolean flag = new AtomicBoolean();
        while (cnt < 5) {
            IntStream.range(0, userCount)
                    .parallel()
                    .forEach(
                            i -> {
                                try {
                                    final long expectShareOneVal = contractSharedOnes[i].get().longValue();
                                    final long expectShareTwoVal = contractSharedTwos[i].get().longValue();
                                    final long expectShareThreeVal = contractSharedThrees[i].get().longValue();
                                    final long shareOneVal = summaryOne[i].longValue();
                                    final long shareTwoVal = summaryTwo[i].longValue();
                                    final long shareThreeVal = summaryThree[i].longValue();
                                    if (shareOneVal != expectShareOneVal) {
                                        System.out.println(
                                                "Check failed! shareOne["
                                                        + i
                                                        + "] balance: "
                                                        + shareOneVal
                                                        + " not equal to expected: "
                                                        + expectShareOneVal);
                                        flag.set(true);
                                    }
                                    if (shareTwoVal != expectShareTwoVal) {
                                        System.out.println(
                                                "Check failed! shareTwo["
                                                        + i
                                                        + "] balance: "
                                                        + shareTwoVal
                                                        + " not equal to expected: "
                                                        + expectShareTwoVal);
                                        flag.set(true);
                                    }
                                    if (shareThreeVal != expectShareThreeVal) {
                                        System.out.println(
                                                "Check failed! shareThree["
                                                        + i
                                                        + "] balance: "
                                                        + shareThreeVal
                                                        + " not equal to expected: "
                                                        + expectShareThreeVal);
                                        flag.set(true);
                                    }
                                } catch (ContractException e) {
                                    e.printStackTrace();
                                }
                            });
            if (flag.get()) {
                System.out.println("\n\n\nCheck ParaTestTwo " + cnt + " times but failed, maybe bugs occur");
                flag.set(false);
                cnt++;
                Thread.sleep(600000);
            } else {
                break;
            }
        }
        System.out.println("check finish!!!!!!!!!!!!!!!!");
    }
}

class TransactionParaTestTwo extends ContractTransaction {
    public ClassParaTestTwo contractsPool;
    public int typeOfRW;
    public int typeOfCall;

    public int indexA;
    public int indexB;
    public int value;
    public static Random random = new Random(System.currentTimeMillis());


    public TransactionParaTestTwo(ClassParaTestTwo contractsPool) {
        this.contractsPool = contractsPool;
        this.typeOfRW = random.nextInt(2);
        this.typeOfCall = random.nextInt(6);
        this.value = random.nextInt(1000);
        this.indexA = random.nextInt(contractsPool.userCount);
        this.indexB = random.nextInt(contractsPool.userCount);
        this.sleep_time = random.nextInt(ConstVal.MAX_SLEEP_TIME);
    }

    public TransactionCallback callBack(
            long now,
            AtomicLong[] summary,
            int idx,
            long val
    ) {
        return new TransactionCallback() {
            @Override
            public void onResponse(TransactionReceipt receipt) {
                long cost = System.currentTimeMillis() - now;
                executeTime = cost;
                contractsPool.utilItems.collector.onMessage(receipt, cost);

                contractsPool.utilItems.receivedBar.step();
                if (!receipt.isStatusOK()) {
                    contractsPool.utilItems.errorBar.step();
                    // System.out.println(receipt.getStatus());
                } else {
                    summary[idx].addAndGet(val);
                }
                contractsPool.utilItems.transactionLatch.countDown();
            }
        };
    }


    public TransactionCallback callBack(
            long now
    ) {
        return new TransactionCallback() {
            @Override
            public void onResponse(TransactionReceipt receipt) {
                long cost = System.currentTimeMillis() - now;
                executeTime = cost;
                contractsPool.utilItems.collector.onMessage(receipt, cost);
                contractsPool.utilItems.receivedBar.step();
                if (!receipt.isStatusOK()) {
                    contractsPool.utilItems.errorBar.step();
                    // System.out.println(receipt.getStatus());
                }
                contractsPool.utilItems.transactionLatch.countDown();
            }
        };
    }


    @Override
    public void exec() {
        long now = System.currentTimeMillis();
        switch (typeOfRW) {
            case 0:
                switch (typeOfCall) {
                    case 0:
                        contractsPool.contractSharedOnes[indexA].setTwo(
                                contractsPool.contractSharedTwos[indexB].getContractAddress(),
                                BigInteger.valueOf(value),
                                callBack(now, contractsPool.summaryTwo, indexB, value));
                        break;
                    case 1:
                        contractsPool.contractSharedOnes[indexA].setThree(
                                contractsPool.contractSharedThrees[indexB].getContractAddress(),
                                BigInteger.valueOf(value),
                                callBack(now, contractsPool.summaryThree, indexB, value));
                        break;
                    case 2:
                        contractsPool.contractSharedTwos[indexA].setOne(
                                contractsPool.contractSharedOnes[indexB].getContractAddress(),
                                BigInteger.valueOf(value),
                                callBack(now, contractsPool.summaryOne, indexB, value));
                        break;
                    case 3:
                        contractsPool.contractSharedTwos[indexA].setThree(
                                contractsPool.contractSharedThrees[indexB].getContractAddress(),
                                BigInteger.valueOf(value),
                                callBack(now, contractsPool.summaryThree, indexB, value));
                        break;
                    case 4:
                        contractsPool.contractSharedThrees[indexA].setOne(
                                contractsPool.contractSharedOnes[indexB].getContractAddress(),
                                BigInteger.valueOf(value),
                                callBack(now, contractsPool.summaryOne, indexB, value));
                        break;
                    case 5:
                        contractsPool.contractSharedThrees[indexA].setTwo(
                                contractsPool.contractSharedTwos[indexB].getContractAddress(),
                                BigInteger.valueOf(value),
                                callBack(now, contractsPool.summaryTwo, indexB, value));
                        break;
                }
                break;
            case 1:
                switch (random.nextInt(6)) {
                    case 0:
                        contractsPool.contractSharedOnes[indexA].getTwo(
                                contractsPool.contractSharedTwos[indexB].getContractAddress(),
                                callBack(now));
                        break;
                    case 1:
                        contractsPool.contractSharedOnes[indexA].getThree(
                                contractsPool.contractSharedThrees[indexB].getContractAddress(),
                                callBack(now));
                        break;
                    case 2:
                        contractsPool.contractSharedTwos[indexA].getOne(
                                contractsPool.contractSharedOnes[indexB].getContractAddress(),
                                callBack(now));
                        break;
                    case 3:
                        contractsPool.contractSharedTwos[indexA].getThree(
                                contractsPool.contractSharedThrees[indexB].getContractAddress(),
                                callBack(now));
                        break;
                    case 4:
                        contractsPool.contractSharedThrees[indexA].getOne(
                                contractsPool.contractSharedOnes[indexB].getContractAddress(),
                                callBack(now));
                        break;
                    case 5:
                        contractsPool.contractSharedThrees[indexA].getTwo(
                                contractsPool.contractSharedTwos[indexB].getContractAddress(),
                                callBack(now));
                        break;
                }
                break;
        }
    }

    @Override
    public void mutate() {
        this.typeOfRW = random.nextInt(2);
        this.typeOfCall = random.nextInt(6);
        this.value += random.nextInt(20000);
        this.indexA = random.nextInt(contractsPool.userCount);
        this.indexB = random.nextInt(contractsPool.userCount);
        this.sleep_time = random.nextInt(ConstVal.MAX_SLEEP_TIME);
    }
}

class TransactionParaTestOne extends ContractTransaction {
    public ClassParaTestOne contractsPool;
    public int typeOfCall;
    public int index;
        public int val;

    public TransactionParaTestOne(ClassParaTestOne contractsPool) {
        this.contractsPool = contractsPool;
        this.typeOfCall = TpsGuidedFuzzing.random.nextInt(4);
        this.index = TpsGuidedFuzzing.random.nextInt(contractsPool.userCount);
        this.val = TpsGuidedFuzzing.random.nextInt(1000);
        this.sleep_time = TpsGuidedFuzzing.random.nextInt(ConstVal.MAX_SLEEP_TIME);
    }

    public TransactionCallback callBack(
            long now,
            AtomicLong summary,
            int val
    ) {
        return new TransactionCallback() {
            @Override
            public void onResponse(TransactionReceipt receipt) {
                long cost = System.currentTimeMillis() - now;
                executeTime =  cost;
                contractsPool.utilItems.collector.onMessage(receipt, cost);

                contractsPool.utilItems.receivedBar.step();
                if (!receipt.isStatusOK()) {
                    contractsPool.utilItems.errorBar.step();
                    // System.out.println(receipt.getStatus());
                } else {
                    summary.addAndGet(val);
                }
                contractsPool.utilItems.transactionLatch.countDown();
            }
        };
    }

    @Override
    void exec() {
        long now = System.currentTimeMillis();
        switch (typeOfCall) {
            case 0:
                contractsPool.contracts[index].addA(
                        BigInteger.valueOf(val), callBack(now, contractsPool.A[index], val));
                break;
            case 1:
                contractsPool.contracts[index].addB(
                        BigInteger.valueOf(val),
                        callBack(now, contractsPool.B[index], val));

                break;
            case 2:
                contractsPool.contracts[index].addC(
                        BigInteger.valueOf(val),
                        callBack(now, contractsPool.C[index], val));

                break;
            case 3:
                contractsPool.contracts[index].addD(
                        BigInteger.valueOf(val),
                        callBack(now, contractsPool.D[index], val));

                break;
            case 4:
                contractsPool.contracts[index].addAB(
                        BigInteger.valueOf(val),
                        BigInteger.valueOf(val),
                        new TransactionCallback() {
                            @Override
                            public void onResponse(TransactionReceipt receipt) {
                                long cost = System.currentTimeMillis() - now;
                                contractsPool.utilItems.collector.onMessage(receipt, cost);

                                contractsPool.utilItems.receivedBar.step();
                                if (!receipt.isStatusOK()) {
                                    contractsPool.utilItems.errorBar.step();
                                    // System.out.println(receipt.getStatus());
                                } else {
                                    contractsPool.A[index].addAndGet(val);
                                    contractsPool.B[index].addAndGet(val);
                                }
                                contractsPool.utilItems.transactionLatch.countDown();
                            }
                        });
        }
    }

    @Override
    void mutate() {
        this.typeOfCall = TpsGuidedFuzzing.random.nextInt(4);
        this.index = TpsGuidedFuzzing.random.nextInt(contractsPool.userCount);
        this.val = TpsGuidedFuzzing.random.nextInt(2000);
        this.sleep_time = TpsGuidedFuzzing.random.nextInt(ConstVal.MAX_SLEEP_TIME);
    }
}



class ClassParaTestOne {
    public Client client;
    public UtilItems utilItems;
    public int userCount;

    ParallelTest1[] contracts;

    public AtomicLong[] A;
    public AtomicLong[] B;
    public AtomicLong[] C;
    public AtomicLong[] D;

    public ClassParaTestOne(int userCount, Client client) throws ContractException {
        this.userCount = userCount;
        this.client = client;
        this.A = new AtomicLong[userCount];
        this.B = new AtomicLong[userCount];
        this.C = new AtomicLong[userCount];
        this.D = new AtomicLong[userCount];
        this.contracts = new ParallelTest1[userCount];
        init();
    }

    public void init() {
        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ParallelTest1 contract;
                            try {
                                contract = ParallelTest1.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
                                contract.setEnableDAG(true);
                                ShardingUtil.linkShard(contract.getContractAddress());
                                this.A[i] = new AtomicLong();
                                this.B[i] = new AtomicLong();
                                this.C[i] = new AtomicLong();
                                this.D[i] = new AtomicLong();
                                contracts[i] = contract;
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });
    }

    public void check() throws InterruptedException {
        System.out.println("start check ClassParaTestOne.........");
        int cnt = 0;
        AtomicBoolean flag = new AtomicBoolean(false);
        while (cnt < 5) {
            int finalCnt = cnt;
            IntStream.range(0, userCount)
                    .parallel()
                    .forEach(
                            i -> {
                                try {
                                    if (A[i].longValue() != contracts[i].A().longValue()) {
                                        flag.set(true);
                                        System.out.println(
                                                "Check failed! Time "
                                                        + finalCnt
                                                        + " for state_A: "
                                                        + contracts[i].A().longValue()
                                                        + " not equal to expected: "
                                                        + A[i].longValue());
                                    }
                                    if (B[i].longValue() != contracts[i].B().longValue()) {
                                        flag.set(true);
                                        System.out.println(
                                                "Check failed! Time "
                                                        + finalCnt
                                                        + " for state_B: "
                                                        + contracts[i].B().longValue()
                                                        + " not equal to expected: "
                                                        + B[i].longValue());
                                    }
                                    if (C[i].longValue() != contracts[i].C().longValue()) {
                                        flag.set(true);
                                        System.out.println(
                                                "Check failed! Time "
                                                        + finalCnt
                                                        + " for state_C: "
                                                        + contracts[i].C().longValue()
                                                        + " not equal to expected: "
                                                        + C[i].longValue());
                                    }
                                    if (D[i].longValue() != contracts[i].D().longValue()) {
                                        flag.set(true);
                                        System.out.println(
                                                "Check failed! Time "
                                                        + finalCnt
                                                        + " for state_D: "
                                                        + contracts[i].D().longValue()
                                                        + " not equal to expected: "
                                                        + D[i].longValue());
                                    }
                                } catch (ContractException e) {
                                    e.printStackTrace();
                                }
                            });
            if (flag.get()) {
                System.out.println("\n\n\nCheck " + cnt + " times but failed, maybe bugs occur");
                flag.set(false);
                cnt++;
                Thread.sleep(60000);
            } else {
                break;
            }
        }
        System.out.println("check ClassParaTestOne finish!!!!!");
    }
}

//TODO:write an interface with exec and mutate, which all special txFuncs implement it
//TODO:every contract's func has its own class, which has para as its var and can be passed.


public class TpsGuidedFuzzing {

    private static Client client;
    private static ShardingService shardingService;

    private static String GROUP_NAME = "group0";
    private static int QPS = 1;
    private static int TX_COUNT = 5000;
    private static int SHARD_NUM = 61;
    public static Random random = new Random(System.currentTimeMillis());
    public static ClassParaTestOne classParaTestOne;
//    public static ClassParaTestTwo classParaTestTwo;
    public static ContractTransaction[] contractTransactions;
    public static double min_tps = Double.MAX_VALUE;

    public static ArrayList<Integer> QPS_LIST = new ArrayList<Integer>() {{
        add(1000);
        add(2000);
        add(3000);
        add(4000);
        add(5000);
        add(6000);
        add(7000);
        add(8000);
        add(9000);
        add(10000);
        add(11000);
        add(12000);
        add(13000);
        add(14000);
        add(15000);
        add(16000);
        add(17000);
        add(18000);
        add(19000);
        add(20000);
    }};

    public static int testTime = 10;

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = ParallelOkPerf.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }

            String groupId = GROUP_NAME;
            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            shardingService =
                    new ShardingService(client, client.getCryptoSuite().getCryptoKeyPair());
            ShardingUtil.shardingService = shardingService;
            ShardingUtil.shardNum = SHARD_NUM;
            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "ExecutorDagContractClient",
                            Runtime.getRuntime().availableProcessors());

            init();
            for (Integer QPS : QPS_LIST) {
                for (int i = 0; i < testTime; i++) {
                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " start....");
                    fuzz(QPS);
                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " end....");
                }
            }
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public static ContractTransaction getTransactions() {
//        double p = random.nextDouble();
//        if (p < 0.3) {
//            return new TransactionParaTestOne(classParaTestOne);
//        }
//        return new TransactionParaTestTwo(classParaTestTwo);
        return new TransactionParaTestOne(classParaTestOne);
    }

    public static void init() throws ContractException {
        System.out.println("init for fuzzing.......");
        classParaTestOne = new ClassParaTestOne(1, client);
//        classParaTestTwo = new ClassParaTestTwo(100, client);
        contractTransactions = new ContractTransaction[TX_COUNT];
        IntStream.range(0, TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            contractTransactions[i] = getTransactions();
                        }
                );
        System.out.println("init finish !!!!!!!!!!!");
    }

    public static void fuzz(int QPS) throws InterruptedException {
        System.out.println("start fuzzing at " + System.currentTimeMillis() + " .......");
        RateLimiter limiter = RateLimiter.create(QPS);
        UtilItems utilItems = UtilItems.generate(TX_COUNT);
        classParaTestOne.utilItems = utilItems;
//        classParaTestTwo.utilItems = utilItems;

        IntStream.range(0, TX_COUNT)
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
                            contractTransactions[i].exec();

                            utilItems.sendedBar.step();
                        });
        utilItems.transactionLatch.await();
        utilItems.collector.setTotalTime();
        utilItems.processEnd();
        double tps = utilItems.collector.getTPS();
        AtomicLong totalTxTime = new AtomicLong();
        IntStream.range(0, TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            totalTxTime.addAndGet(contractTransactions[i].executeTime);
                        }
                );
        double latency = totalTxTime.doubleValue() / (double) TX_COUNT;
        Util.appendToFile("reNoConflict_tps.log", String.format("QPS:%d time:%d  tps:%f  tpsNoError:%f latency:%f\n",
                QPS,
                System.currentTimeMillis(),
                tps,
                utilItems.collector.getTpsWithoutError(),
                latency)
        );
        //TODO: now just mutate, not save the seeds
//        if (true) {
//            min_tps = tps;
//            IntStream.range(0, TX_COUNT)
//                    .parallel()
//                    .forEach(
//                            i -> {
//                                contractTransactions[i].mutate();
//                            });
//        }
        classParaTestOne.check();
//        classParaTestTwo.check();
        System.out.println("fuzzing round finish!!!!!!!!!!!!!");
    }
}
//tps guided
//mutate
