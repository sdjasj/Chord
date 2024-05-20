package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
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
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.*;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class ParaTestTwo {
    private static Client client;
    private static ShardingService shardingService;
  private static Random random = new Random();
    private static int CHECK_TIME = 5;
    private static long SLEEP_TIME = 60000;

    public static void usage() {
        System.out.println(" Usage:");
        System.out.println("===== ParaTestTwo test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParaTestTwo [groupId] [userCount] [count] [qps].");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParaTestTwo [groupId] [shardNum] [userCount] [count] [qps].");
    }

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = ParallelOkPerf.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }

            if (args.length < 4) {
                usage();
                return;
            }
            String groupId = null;
            int userCount = 0;
            Integer count = 0;
            Integer qps = 0;
            if (args.length == 4){
                groupId = args[0];
                userCount = Integer.valueOf(args[1]).intValue();
                count = Integer.valueOf(args[2]).intValue();
                qps = Integer.valueOf(args[3]).intValue();
            }
            int shardNum = 0;
            if (args.length == 5){
                groupId = args[0];
                shardNum = Integer.valueOf(args[1]).intValue();
                userCount = Integer.valueOf(args[2]).intValue();
                count = Integer.valueOf(args[3]).intValue();
                qps = Integer.valueOf(args[4]).intValue();
            }

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            if (args.length == 5){
                shardingService =
                        new ShardingService(client, client.getCryptoSuite().getCryptoKeyPair());
            }
            ThreadPoolService threadPoolService =
                    new ThreadPoolService("ParaTestTwo", Runtime.getRuntime().availableProcessors());
            if (args.length == 4){
                start(groupId, userCount, count, qps, threadPoolService);
            } else if (args.length == 5){
                shardTestStart(groupId, shardNum,  userCount, count, qps, threadPoolService);
            }


            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static TransactionCallback callBack(
            long now,
            ProgressBar receivedBar,
            ProgressBar errorBar,
            Collector collector,
            CountDownLatch transactionLatch,
            AtomicLong totalCost,
            AtomicLong[] summary,
            int idx,
            long val
    ) {
        return new TransactionCallback() {
              @Override
              public void onResponse(TransactionReceipt receipt) {
                    long cost = System.currentTimeMillis() - now;
                    collector.onMessage(receipt, cost);

                    receivedBar.step();
                    if (!receipt.isStatusOK()) {
                      errorBar.step();
                      // System.out.println(receipt.getStatus());
                    } else {
                      summary[idx].addAndGet(val);
                    }
                    transactionLatch.countDown();
                    totalCost.addAndGet(System.currentTimeMillis() - now);
              }
            };
    }


    public static TransactionCallback callBack(
            long now,
            ProgressBar receivedBar,
            ProgressBar errorBar,
            Collector collector,
            CountDownLatch transactionLatch,
            AtomicLong totalCost
    ) {
        return new TransactionCallback() {
            @Override
            public void onResponse(TransactionReceipt receipt) {
                long cost = System.currentTimeMillis() - now;
                collector.onMessage(receipt, cost);

                receivedBar.step();
                if (!receipt.isStatusOK()) {
                    errorBar.step();
                    // System.out.println(receipt.getStatus());
                }
                transactionLatch.countDown();
                totalCost.addAndGet(System.currentTimeMillis() - now);
            }
        };
    }

    public static void start(
            String groupId,
            int userCount,
            int count,
            Integer qps,
            ThreadPoolService threadPoolService)
            throws IOException, InterruptedException, ContractException {
        System.out.println(
                "====== Start test, user count: "
                        + userCount
                        + ", count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId);

        RateLimiter limiter = RateLimiter.create(qps.intValue());

        ContractSharedOne[] contractSharedOnes = new ContractSharedOne[userCount];
        ContractSharedTwo[] contractSharedTwos = new ContractSharedTwo[userCount];
        ContractSharedThree[] contractSharedThrees = new ContractSharedThree[userCount];

        AtomicLong[] summaryOne = new AtomicLong[userCount];
        AtomicLong[] summaryTwo = new AtomicLong[userCount];
        AtomicLong[] summaryThree = new AtomicLong[userCount];

        random.setSeed(System.currentTimeMillis());

        System.out.println("Create account of sharedOne...");

        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ContractSharedOne contractSharedOne;
                            try {
                                limiter.acquire();
                                contractSharedOne =
                                        ContractSharedOne.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());
                                contractSharedOne.setEnableDAG(true);
                                summaryOne[i] = new AtomicLong();
                                contractSharedOnes[i] = contractSharedOne;
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });
        System.out.println("Create account of sharedOne finished!");

        System.out.println("Create account of sharedTwo...");

        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ContractSharedTwo contractSharedTwo;
                            try {
                                limiter.acquire();
                                contractSharedTwo =
                                        ContractSharedTwo.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());
                                contractSharedTwo.setEnableDAG(true);
                                contractSharedTwos[i] = contractSharedTwo;
                                summaryTwo[i] = new AtomicLong();
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });
        System.out.println("Create account of sharedTwo finished!");


        System.out.println("Create account of sharedOne...");

        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ContractSharedThree contractSharedThree;
                            try {
                                limiter.acquire();
                                contractSharedThree =
                                        ContractSharedThree.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());
                                contractSharedThree.setEnableDAG(true);
                                contractSharedThrees[i] = contractSharedThree;
                                summaryThree[i] = new AtomicLong();
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });
        System.out.println("Create account of sharedThrees finished!");

        System.out.println("Sending transactions...");
        ProgressBar sendedBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar errorBar =
                new ProgressBarBuilder()
                        .setTaskName("Errors :")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch transactionLatch = new CountDownLatch(count);
        AtomicLong totalCost = new AtomicLong(0);

        Collector collector = new Collector();
        collector.setTotal(count);

        IntStream.range(0, count)
                .parallel()
                .forEach(
                        i -> {
                            limiter.acquire();

                            final int indexA = random.nextInt(Integer.MAX_VALUE) % userCount;
                            final int indexB = random.nextInt(Integer.MAX_VALUE) % userCount;
                            long now = System.currentTimeMillis();

                            final long value = Math.abs(random.nextLong() % 1000);

                            switch (random.nextInt(2)) {
                                case 0:
                                    switch (random.nextInt(6)) {
                                        case 0:
                                            contractSharedOnes[indexA].setTwo(
                                                    contractSharedTwos[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryTwo, indexB, value));
                                            break;
                                        case 1:
                                            contractSharedOnes[indexA].setThree(
                                                    contractSharedThrees[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryThree, indexB, value));
                                            break;
                                        case 2:
                                            contractSharedTwos[indexA].setOne(
                                                    contractSharedOnes[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryOne, indexB, value));
                                            break;
                                        case 3:
                                            contractSharedTwos[indexA].setThree(
                                                    contractSharedThrees[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryThree, indexB, value));
                                            break;
                                        case 4:
                                            contractSharedThrees[indexA].setOne(
                                                    contractSharedOnes[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryOne, indexB, value));
                                            break;
                                        case 5:
                                            contractSharedThrees[indexA].setTwo(
                                                    contractSharedTwos[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryTwo, indexB, value));
                                            break;
                                    }
                                    break;
                                case 1:
                                    switch (random.nextInt(6)) {
                                        case 0:
                                            contractSharedOnes[indexA].getTwo(
                                                    contractSharedTwos[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                        case 1:
                                            contractSharedOnes[indexA].getThree(
                                                    contractSharedThrees[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                        case 2:
                                            contractSharedTwos[indexA].getOne(
                                                    contractSharedOnes[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                        case 3:
                                            contractSharedTwos[indexA].getThree(
                                                    contractSharedThrees[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                        case 4:
                                            contractSharedThrees[indexA].getOne(
                                                    contractSharedOnes[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                        case 5:
                                            contractSharedThrees[indexA].getTwo(
                                                    contractSharedTwos[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                    }
                                    break;
                            }

                            sendedBar.step();
                        });
        transactionLatch.await();

        sendedBar.close();
        receivedBar.close();
        errorBar.close();
        collector.report();

        System.out.println("Sending transactions finished!");

        check(userCount, limiter, contractSharedOnes, contractSharedTwos, contractSharedThrees, summaryOne, summaryTwo, summaryThree);

        System.out.println("Checking finished!");
    }

    private static void check(int userCount, RateLimiter limiter, ContractSharedOne[] contractSharedOnes, ContractSharedTwo[] contractSharedTwos, ContractSharedThree[] contractSharedThrees, AtomicLong[] summaryOne, AtomicLong[] summaryTwo, AtomicLong[] summaryThree) throws InterruptedException {
        System.out.println("Checking result...");
        int cnt = 0;
        AtomicBoolean flag = new AtomicBoolean();
        while (cnt < CHECK_TIME) {
            IntStream.range(0, userCount)
                    .parallel()
                    .forEach(
                            i -> {
                                limiter.acquire();
                                try {
                                    limiter.acquire();
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
                System.out.println("\n\n\nCheck " + cnt + " times but failed, maybe bugs occur");
                flag.set(false);
                cnt++;
                Thread.sleep(SLEEP_TIME);
            } else {
                break;
            }
        }


        System.out.println("Checking finished!");
    }

    public static void linkShard(String address, int shardNum) throws ContractException {
        int idx = random.nextInt(shardNum);
        String shardName = "testShard" + idx;
        shardingService.linkShard(shardName, address);
//        System.out.println(
//                "====== ShardingOk ParaTestTwo, deploy success to shard: "
//                        + shardName
//                        + ", address: "
//                        + address);
    }

    public static void shardTestStart(
            String groupId,
            int shardNum,
            int userCount,
            int count,
            Integer qps,
            ThreadPoolService threadPoolService
    ) throws InterruptedException {
        System.out.println(
                "====== Start shardTest, shardNum: "
                        + shardNum
                        + ", user count: "
                        + userCount
                        + ", count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId);
        RateLimiter limiter = RateLimiter.create(qps.intValue());

        ContractSharedOne[] contractSharedOnes = new ContractSharedOne[userCount];
        ContractSharedTwo[] contractSharedTwos = new ContractSharedTwo[userCount];
        ContractSharedThree[] contractSharedThrees = new ContractSharedThree[userCount];

        AtomicLong[] summaryOne = new AtomicLong[userCount];
        AtomicLong[] summaryTwo = new AtomicLong[userCount];
        AtomicLong[] summaryThree = new AtomicLong[userCount];

        random.setSeed(System.currentTimeMillis());

        System.out.println("Create account of sharedOne...");

        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ContractSharedOne contractSharedOne;
                            try {
                                limiter.acquire();
                                contractSharedOne =
                                        ContractSharedOne.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());

                                contractSharedOnes[i] = contractSharedOne;
                                contractSharedOne.setEnableDAG(true);
                                summaryOne[i] = new AtomicLong();
                                linkShard(contractSharedOne.getContractAddress(), shardNum);
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });
        System.out.println("Create account of sharedOne finished!");

        System.out.println("Create account of sharedTwo...");

        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ContractSharedTwo contractSharedTwo;
                            try {
                                limiter.acquire();
                                contractSharedTwo =
                                        ContractSharedTwo.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());

                                contractSharedTwos[i] = contractSharedTwo;
                                contractSharedTwo.setEnableDAG(true);
                                summaryTwo[i] = new AtomicLong();
                                linkShard(contractSharedTwo.getContractAddress(), shardNum);
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });
        System.out.println("Create account of sharedTwo finished!");


        System.out.println("Create account of sharedThree...");

        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ContractSharedThree contractSharedThree;
                            try {
                                limiter.acquire();
                                contractSharedThree =
                                        ContractSharedThree.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());

                                contractSharedThrees[i] = contractSharedThree;
                                contractSharedThree.setEnableDAG(true);
                                summaryThree[i] = new AtomicLong();
                                linkShard(contractSharedThree.getContractAddress(), shardNum);
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });
        System.out.println("Create account of sharedThrees finished!");

        System.out.println("Sending transactions...");
        ProgressBar sendedBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar errorBar =
                new ProgressBarBuilder()
                        .setTaskName("Errors :")
                        .setInitialMax(count)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();

        CountDownLatch transactionLatch = new CountDownLatch(count);
        AtomicLong totalCost = new AtomicLong(0);

        Collector collector = new Collector();
        collector.setTotal(count);

        IntStream.range(0, count)
                .parallel()
                .forEach(
                        i -> {
                            limiter.acquire();

                            final int indexA = random.nextInt(Integer.MAX_VALUE) % userCount;
                            final int indexB = random.nextInt(Integer.MAX_VALUE) % userCount;
                            long now = System.currentTimeMillis();

                            final long value = Math.abs(random.nextLong() % 1000);

                            switch (random.nextInt(2)) {
                                case 0:
                                    switch (random.nextInt(6)) {
                                        case 0:
                                            contractSharedOnes[indexA].setTwo(
                                                    contractSharedTwos[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryTwo, indexB, value));
                                            break;
                                        case 1:
                                            contractSharedOnes[indexA].setThree(
                                                    contractSharedThrees[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryThree, indexB, value));
                                            break;
                                        case 2:
                                            contractSharedTwos[indexA].setOne(
                                                    contractSharedOnes[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryOne, indexB, value));
                                            break;
                                        case 3:
                                            contractSharedTwos[indexA].setThree(
                                                    contractSharedThrees[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryThree, indexB, value));
                                            break;
                                        case 4:
                                            contractSharedThrees[indexA].setOne(
                                                    contractSharedOnes[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryOne, indexB, value));
                                            break;
                                        case 5:
                                            contractSharedThrees[indexA].setTwo(
                                                    contractSharedTwos[indexB].getContractAddress(),
                                                    BigInteger.valueOf(value),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost, summaryTwo, indexB, value));
                                            break;
                                    }
                                    break;
                                case 1:
                                    switch (random.nextInt(6)) {
                                        case 0:
                                            contractSharedOnes[indexA].getTwo(
                                                    contractSharedTwos[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                        case 1:
                                            contractSharedOnes[indexA].getThree(
                                                    contractSharedThrees[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                        case 2:
                                            contractSharedTwos[indexA].getOne(
                                                    contractSharedOnes[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                        case 3:
                                            contractSharedTwos[indexA].getThree(
                                                    contractSharedThrees[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                        case 4:
                                            contractSharedThrees[indexA].getOne(
                                                    contractSharedOnes[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                        case 5:
                                            contractSharedThrees[indexA].getTwo(
                                                    contractSharedTwos[indexB].getContractAddress(),
                                                    callBack(
                                                            now, receivedBar, errorBar, collector, transactionLatch, totalCost));
                                            break;
                                    }
                                    break;
                            }

                            sendedBar.step();
                        });
        transactionLatch.await();

        sendedBar.close();
        receivedBar.close();
        errorBar.close();
        collector.report();

        System.out.println("Sending transactions in sharding finished!");


        check(userCount, limiter, contractSharedOnes, contractSharedTwos, contractSharedThrees, summaryOne, summaryTwo, summaryThree);
    }

}
