package org.example.model;

import com.google.common.util.concurrent.RateLimiter;
import org.example.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class StressTest {
    public static ArrayList<String> privateKeys = new ArrayList<>();

    public static Random random = new Random(System.currentTimeMillis());

    public Integer[] transactions;
    public Contract contract;
    public Collector collector;
    public Bar bar;

    public StressTest() throws IOException {
        ReadPrivateKeys();
        associateKeys();
        contract = new Contract("src/SimpleStorage.sol:SimpleStorage");
        contract.deploy("/root/test/ParallelTest1/", getRandomPrivateKey());
    }

    public boolean set(long val) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"set(uint256)\" %d", getRandomPrivateKey(), contract.address, val);
        return Util.execTransaction(cmd);
    }

    public boolean get() throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"get()\"", getRandomPrivateKey(), contract.address);
        return Util.execTransaction(cmd);
    }

    public boolean bad() throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"bad()\"", getRandomPrivateKey(), contract.address);
        return Util.execTransaction(cmd);
    }

    public void test(int QPS) throws InterruptedException {
        transactions = new Integer[Math.min(Constant.TX_COUNT, 10 * QPS)];
        IntStream.range(0, Math.min(Constant.TX_COUNT, 10*QPS))
                .parallel()
                .forEach(
                        i -> {
                            transactions[i] = ThreadLocalRandom.current().nextInt(3);
                        });

        RateLimiter limiter = RateLimiter.create(QPS);
        AtomicLong totalCostTime = new AtomicLong();
        collector = new Collector();
        bar = Bar.generate(Math.min(Constant.TX_COUNT, 10*QPS), collector);
        IntStream.range(0, Math.min(Constant.TX_COUNT, 10*QPS))
                .parallel()
                .forEach(
                        i -> {
                            long now = System.currentTimeMillis();
                            //not fixed qps now, just as fast as possible
                            limiter.acquire();
                            bar.sendedBar.step();
                            try {
                                boolean flag;
                                switch (transactions[i]) {
                                    case 0:
                                        flag = set(1);
                                        if (!flag) {
                                            bar.errorBar.step();
                                        }
                                        bar.receivedBar.step();
                                        break;
                                    case 1:
                                        flag = get();
                                        if (!flag) {
                                            bar.errorBar.step();
                                        }
                                        bar.receivedBar.step();
                                        break;
                                    case 2:
                                        flag = bad();
                                        if (!flag) {
                                            bar.errorBar.step();
                                        }
                                        bar.receivedBar.step();
                                        break;
                                }
                                totalCostTime.addAndGet(System.currentTimeMillis() - now);
                                bar.transactionLatch.countDown();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
//        double tps = utilItems.getTPS();
        bar.transactionLatch.await();
        collector.setTotalTime();
        bar.processEnd();
        double tps = collector.getTPS();
        double averageLatency = totalCostTime.doubleValue() / (double) (Math.min(Constant.TX_COUNT, 10*QPS) +  Math.min(Constant.CROSS_TX_COUNT, QPS) * Constant.CROSS_CONTRACT_NUM);

        Util.appendToFile("re_stress_tps3.log", String.format("QPS:%d time:%d  tps:%f  tpsNoError:%f latency:%f\n",
                QPS,
                System.currentTimeMillis(),
                tps,
                collector.getTpsWithoutError(),
                averageLatency)
        );
        System.out.println("fuzzing round finish!!!!!!!!!!!!!");
        collector.report();
    }

    public static String getRandomPrivateKey() {
        return privateKeys.get(random.nextInt(privateKeys.size()));
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
