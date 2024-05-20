/**
 * Copyright 2014-2020 [fisco-dev]
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fisco.bcos.sdk.demo.perf;

import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.contract.Account;
import org.fisco.bcos.sdk.demo.fuzz.Util;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

/** @author monan */
public class PerformanceDMC {
    private static Client client;
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
    public static void usage() {
        System.out.println(" Usage:");
        System.out.println("===== PerformanceDMC test===========");
        System.out.println(
                " \t java -cp 'conf/:lib/*:apps/*' org.fisco.bcos.sdk.demo.perf.PerformanceDMC [groupId] [userCount] [count] [qps].");
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

//            if (args.length < 4) {
//                usage();
//                return;
//            }
            String groupId = "group0";
            int userCount = 100;
            int count = 40000;
//            Integer qps = Integer.valueOf(args[3]).intValue();

            String configFile = configUrl.getPath();
            BcosSDK sdk = BcosSDK.build(configFile);
            client = sdk.getClient(groupId);
            for (Integer QPS : QPS_LIST) {
                for (int i = 0; i < 10; i++) {
                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " start....");
                    start(groupId, userCount, Math.min(10 * QPS, count) , QPS);
                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " end....");
                }
            }


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
            Integer qps)
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

        Account[] accounts = new Account[userCount];
        AtomicLong[] summary = new AtomicLong[userCount];

        final Random random = new Random();
        random.setSeed(System.currentTimeMillis());

        System.out.println("Create account...");
        IntStream.range(0, userCount)
                .parallel()
                .forEach(
                        i -> {
                            Account account;
                            try {
                                long initBalance = Math.abs(random.nextLong());

                                limiter.acquire();
                                account =
                                        Account.deploy(
                                                client, client.getCryptoSuite().getCryptoKeyPair());
                                account.addBalance(BigInteger.valueOf(initBalance));

                                accounts[i] = account;
                                summary[i] = new AtomicLong(initBalance);
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });
        System.out.println("Create account finished!");

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

                            final int index = i % accounts.length;
                            Account account = accounts[index];
                            long now = System.currentTimeMillis();

                            final long value = Math.abs(random.nextLong() % 1000);

                            account.addBalance(
                                    BigInteger.valueOf(value),
                                    new TransactionCallback() {
                                        @Override
                                        public void onResponse(TransactionReceipt receipt) {
                                            AtomicLong count = summary[index];
                                            count.addAndGet(value);

                                            long cost = System.currentTimeMillis() - now;
                                            collector.onMessage(receipt, cost);

                                            receivedBar.step();
                                            transactionLatch.countDown();
                                            totalCost.addAndGet(System.currentTimeMillis() - now);
                                        }
                                    });
                            sendedBar.step();
                        });
        transactionLatch.await();
        collector.setTotalTime();
        sendedBar.close();
        receivedBar.close();
        collector.report();
        System.out.println("Sending transactions finished!");
        Util.appendToFile("re_stress_tps.log", String.format("QPS:%d time:%d  tps:%f  tpsNoError:%f latency:%f\n",
                qps,
                System.currentTimeMillis(),
                collector.getTPS(),
                collector.getTpsWithoutError(),
                totalCost.doubleValue() / (double) count)
        );
        System.out.println("Checking result...");
        IntStream.range(0, summary.length)
                .parallel()
                .forEach(
                        i -> {
                            limiter.acquire();
                            final long expectBalance = summary[i].longValue();
                            try {
                                limiter.acquire();
                                BigInteger balance = accounts[i].balance();
                                if (balance.longValue() != expectBalance) {
                                    System.out.println(
                                            "Check failed! Account["
                                                    + i
                                                    + "] balance: "
                                                    + balance
                                                    + " not equal to expected: "
                                                    + expectBalance);
                                }
                            } catch (ContractException e) {
                                e.printStackTrace();
                            }
                        });

        System.out.println("Checking finished!");
    }
}
