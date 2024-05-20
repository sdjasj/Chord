package org.fisco.bcos.sdk.demo.fuzz;

import org.fisco.bcos.sdk.demo.perf.ParallelOkPerf;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;
import org.fisco.bcos.sdk.v3.model.ConstantConfig;
import org.fisco.bcos.sdk.v3.utils.ThreadPoolService;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.stream.IntStream;

public class Main {
    public static boolean testFlag = false;
    public static boolean evmFlag = false;
    public static Client client;
    public static ShardingService shardingService;
    public static boolean crossFuzzFlag = true;
    private static String GROUP_NAME = "group0";

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

    public static int testTime = 5;

    public static void main(String[] args) throws InterruptedException {
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
        ShardingUtil.shardNum = 61;

        if (testFlag) {
            CrossFuzzer fuzzer = new CrossFuzzer();
            for (Integer QPS : QPS_LIST) {
                for (int i = 0; i < testTime; i++) {
                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " start....");
                    fuzzer.testFuzz(QPS);
                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " end....");
                }
            }
        } else if (crossFuzzFlag) {
            CrossFuzzer fuzzer = new CrossFuzzer();
            for (int i = 0; i < 1000000; i++) {
                System.out.println("fuzzing round " + (i + 1) + " start....");
                fuzzer.fuzz();
                System.out.println("fuzzing round " + (i + 1) + " end....");
            }
        } else if (evmFlag) {
            EvmFuzzer fuzzer = null;
            try {
                fuzzer = new EvmFuzzer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < 1000000; i++) {
                System.out.println("fuzzing round " + (i + 1) + " start....");
                fuzzer.fuzz();
                System.out.println("fuzzing round " + (i + 1) + " end....");
            }
        } else {
            Fuzzer fuzzer = new Fuzzer();
            for (int i = 0; i < 1000000; i++) {
                System.out.println("fuzzing round " + (i + 1) + " start....");
                fuzzer.fuzz();
                System.out.println("fuzzing round " + (i + 1) + " end....");
            }
        }

    }

    public static void test3() {
        ContractModel contractModel = new ContractModel();
        contractModel.printContract();
    }

    public static void test2() {
        System.out.println(Integer.parseInt("0x1", 16));

    }

    public static void test1() {
        IntStream.range(0, 300)
                .parallel()
                .forEach(
                        i -> {
                            String cmd = "ls -al";
                            try {
                                String res = Util.executeCommand(cmd);
                                System.out.println(res);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                );
    }
}
