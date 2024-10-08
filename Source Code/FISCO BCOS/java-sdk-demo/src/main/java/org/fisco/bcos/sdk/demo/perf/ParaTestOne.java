package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.ParallelTest1;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

public class ParaTestOne {
    private static Client client;
    private static int CHECK_TIME = 5;
    private static long SLEEP_TIME = 60000;

    public static void usage() {
        System.out.println(" Usage:");
        System.out.println("===== ParaTestOne test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.ParaTestOne [groupId] [count] [tps].");
        System.out.println("just test dag in one shard.....");
    }

    public static void main(String[] args)
            throws ContractException, IOException, InterruptedException {
        try {
            String configFileName = ConstantConfig.CONFIG_FILE_NAME;
            URL configUrl = ParaTestOne.class.getClassLoader().getResource(configFileName);
            if (configUrl == null) {
                System.out.println("The configFile " + configFileName + " doesn't exist!");
                return;
            }

            String groupId = args[0];
            Integer count = Integer.valueOf(args[1]);
            Integer qps = Integer.valueOf(args[2]);

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);

            ThreadPoolService threadPoolService =
                    new ThreadPoolService(
                            "ParaTestOne", Runtime.getRuntime().availableProcessors());
            start(groupId, count, qps, threadPoolService);
        } catch (Exception e) {
            System.out.println("ParallelTestOne test failed, error info: " + e.getMessage());
            System.exit(0);
        }
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
        Integer count,
        Integer qps,
        ThreadPoolService threadPoolService
    ) throws ContractException, InterruptedException {
        System.out.println(
                "====== ParallelTestOne trans, count: "
                        + count
                        + ", qps:"
                        + qps
                        + ", groupId: "
                        + groupId);
        RateLimiter limiter = RateLimiter.create(qps.intValue());
        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());
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

        AtomicLong A = new AtomicLong();
        AtomicLong B = new AtomicLong();
        AtomicLong C = new AtomicLong();
        AtomicLong D = new AtomicLong();

        Collector collector = new Collector();
        collector.setTotal(count);
        ParallelTest1 contract = ParallelTest1.deploy(client, client.getCryptoSuite().getCryptoKeyPair());
        contract.setEnableDAG(true);

    IntStream.range(0, count)
        .parallel()
        .forEach(
            index -> {
              limiter.acquire();
              long now = System.currentTimeMillis();
              BigInteger val = BigInteger.valueOf(index % 6);
              switch (random.nextInt(Integer.MAX_VALUE) % 5) {
                case 0:
                  contract.addA(
                      val,
                      new TransactionCallback() {
                        @Override
                        public void onResponse(TransactionReceipt receipt) {
                          long cost = System.currentTimeMillis() - now;
                          collector.onMessage(receipt, cost);

                          receivedBar.step();
                          if (!receipt.isStatusOK()) {
                            errorBar.step();
                            // System.out.println(receipt.getStatus());
                          } else {
                            A.addAndGet(val.longValue());
                          }
                          transactionLatch.countDown();
                          totalCost.addAndGet(System.currentTimeMillis() - now);
                        }
                      });
                  break;
                case 1:
                  contract.addB(
                      val,
                      new TransactionCallback() {
                        @Override
                        public void onResponse(TransactionReceipt receipt) {
                          long cost = System.currentTimeMillis() - now;
                          collector.onMessage(receipt, cost);

                          receivedBar.step();
                          if (!receipt.isStatusOK()) {
                            errorBar.step();
                            // System.out.println(receipt.getStatus());
                          } else {
                            B.addAndGet(val.longValue());
                          }
                          transactionLatch.countDown();
                          totalCost.addAndGet(System.currentTimeMillis() - now);
                        }
                      });

                  break;
                case 2:
                  contract.addC(
                      val,
                      new TransactionCallback() {
                        @Override
                        public void onResponse(TransactionReceipt receipt) {
                          long cost = System.currentTimeMillis() - now;
                          collector.onMessage(receipt, cost);

                          receivedBar.step();
                          if (!receipt.isStatusOK()) {
                            errorBar.step();
                            // System.out.println(receipt.getStatus());
                          } else {
                            C.addAndGet(val.longValue());
                          }
                          transactionLatch.countDown();
                          totalCost.addAndGet(System.currentTimeMillis() - now);
                        }
                      });

                  break;
                case 3:
                  contract.addD(
                      val,
                      new TransactionCallback() {
                        @Override
                        public void onResponse(TransactionReceipt receipt) {
                          long cost = System.currentTimeMillis() - now;
                          collector.onMessage(receipt, cost);

                          receivedBar.step();
                          if (!receipt.isStatusOK()) {
                            errorBar.step();
                            // System.out.println(receipt.getStatus());
                          } else {
                            D.addAndGet(val.longValue());
                          }
                          transactionLatch.countDown();
                          totalCost.addAndGet(System.currentTimeMillis() - now);
                        }
                      });

                  break;
                case 4:
                  contract.addAB(
                      val,
                      val,
                      new TransactionCallback() {
                        @Override
                        public void onResponse(TransactionReceipt receipt) {
                          long cost = System.currentTimeMillis() - now;
                          collector.onMessage(receipt, cost);

                          receivedBar.step();
                          if (!receipt.isStatusOK()) {
                            errorBar.step();
                            // System.out.println(receipt.getStatus());
                          } else {
                            A.addAndGet(val.longValue());
                            B.addAndGet(val.longValue());
                          }
                          transactionLatch.countDown();
                          totalCost.addAndGet(System.currentTimeMillis() - now);
                        }
                      });

                  break;
              }
              sendedBar.step();
            });

        transactionLatch.await();

        System.out.println("Sending transactions finished!");


        sendedBar.close();
        receivedBar.close();
        errorBar.close();
        collector.report();

        System.out.println("Checking result...");
        int i = 0;
        for (; i < CHECK_TIME; i++) {
            if (A.longValue() != contract.A().longValue()) {
                System.out.println(
                    "Check failed! Time "
                        + i
                        + " for state_A: "
                        + contract.A().longValue()
                        + " not equal to expected: "
                        + A.longValue());
                Thread.sleep(SLEEP_TIME);
                continue;
            }
            if (B.longValue() != contract.B().longValue()) {
                System.out.println(
                        "Check failed! Time "
                                + i
                                + " for state_B: "
                                + contract.B().longValue()
                                + " not equal to expected: "
                                + B.longValue());
                Thread.sleep(SLEEP_TIME);
                continue;
            }
            if (C.longValue() != contract.C().longValue()) {
                System.out.println(
                        "Check failed! Time "
                                + i
                                + " for state_C: "
                                + contract.C().longValue()
                                + " not equal to expected: "
                                + C.longValue());
                Thread.sleep(SLEEP_TIME);
                continue;
            }
            if (D.longValue() != contract.D().longValue()) {
                System.out.println(
                        "Check failed! Time "
                                + i
                                + " for state_D: "
                                + contract.D().longValue()
                                + " not equal to expected: "
                                + D.longValue());
                Thread.sleep(SLEEP_TIME);
                continue;
            }
            break;
        }
        if (i > CHECK_TIME) {
            System.out.println("error of check");
        }
        System.out.println("Checking finished!");
    }
}
