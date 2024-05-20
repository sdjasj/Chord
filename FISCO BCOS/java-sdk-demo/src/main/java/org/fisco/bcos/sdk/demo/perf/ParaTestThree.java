package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.ContractOfRecursiveOne;
import org.fisco.bcos.sdk.demo.contract.ContractOfRecursiveTwo;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class ParaTestThree {
    private static Client client;

    public static void usage() {
        System.out.println(" Usage:");
        System.out.println("===== ParaTestThree test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParaTestThree [groupId] [userCount] [count] [recursiveTime] [tps].");
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

            if (args.length != 5) {
                usage();
                return;
            }
            String groupId = args[0];
            int userCount = Integer.valueOf(args[1]).intValue();
            Integer count = Integer.valueOf(args[2]).intValue();
            Integer recursiveTime = Integer.valueOf(args[3]).intValue();
            Integer qps = Integer.valueOf(args[4]).intValue();


            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            ThreadPoolService threadPoolService =
                    new ThreadPoolService("ParaTestThree", Runtime.getRuntime().availableProcessors());

            start(groupId, userCount, count, recursiveTime, qps, threadPoolService);

            threadPoolService.getThreadPool().awaitTermination(0, TimeUnit.SECONDS);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void start(
            String groupId,
            int userCount,
            int count,
            int recursiveTime,
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
        System.out.println(userCount + " contract A and contract B will call each other until count reaches zero");

        RateLimiter limiter = RateLimiter.create(qps.intValue());

        ContractOfRecursiveOne[] contractAList = new ContractOfRecursiveOne[userCount];
        ContractOfRecursiveTwo[] contractBList = new ContractOfRecursiveTwo[userCount];

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        System.out.println("Create contract of A...");

        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ContractOfRecursiveOne contract;
                            try {
                                limiter.acquire();
                                contract =
                                        ContractOfRecursiveOne.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());

                                contractAList[i] = contract;
                                contract.setEnableDAG(true);
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });


        System.out.println("Create contract A finished!\n");

        System.out.println("Create contract of B...");

        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            ContractOfRecursiveTwo contract;
                            try {
                                limiter.acquire();
                                contract =
                                        ContractOfRecursiveTwo.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());

                                contractBList[i] = contract;
                                contract.setEnableDAG(true);
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });

        System.out.println("Create contract B finished!\n");

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

              ContractOfRecursiveOne contractA = contractAList[indexA];
              ContractOfRecursiveTwo contractB = contractBList[indexB];

              long now = System.currentTimeMillis();

              switch (random.nextInt(2)) {
                  case 0:
                      contractA.recursiveCall(
                              contractB.getContractAddress(),
                              BigInteger.valueOf(recursiveTime),
                              new TransactionCallback() {
                          @Override
                          public void onResponse(TransactionReceipt receipt) {
                              long cost = System.currentTimeMillis() - now;
                              collector.onMessage(receipt, cost);

                              receivedBar.step();
                              transactionLatch.countDown();
                              totalCost.addAndGet(System.currentTimeMillis() - now);
                          }
                      });
                      break;
                  case 1:
                      contractB.recursiveCall(
                              contractA.getContractAddress(),
                              BigInteger.valueOf(recursiveTime),
                              new TransactionCallback() {
                          @Override
                          public void onResponse(TransactionReceipt receipt) {
                              if (!receipt.isStatusOK()) {

                              }
                              long cost = System.currentTimeMillis() - now;
                              collector.onMessage(receipt, cost);

                              receivedBar.step();
                              transactionLatch.countDown();
                              totalCost.addAndGet(System.currentTimeMillis() - now);
                          }
                      });
                      break;
              }

              sendedBar.step();
            });
        transactionLatch.await();

        sendedBar.close();
        receivedBar.close();
        collector.report();
        System.out.println("Sending transactions finished!");
    }
}
