package org.fisco.bcos.sdk.demo.fuzz;

public class Constant {
    public static boolean DEBUG = false;
    public static int CONTRACT_NUM = 200;
    public static int TX_COUNT = 50000;
    public static int QPS = 6000;
    public static int SEED_COUNTS = 10;
    public static int SIMPLE_FUNC_NUMS = 100;
    public static int STATE_VAR_NUMS = 60;
    public static int COMPLEX_FUNC_NUMS = 15;
    public static int SIMPLE_IN_COMPLEX_NUM = 6;
    public static String CONTRACT_SOL_STORAGE_PATH = "/root/java-sdk-demo/dist/contracts/solidity";
    public static String CONTRACT_JAVA_STORAGE_PATH = "/root/java-sdk-demo/dist/dynamic/org/fisco/bcos/sdk/demo/contract";
    public static int CROSS_SIMPLE_FUNC_NUMS = 100;
    public static int CROSS_TX_COUNT = 10000;
    public static int CROSS_CONTRACT_NUM = 1;
    public static double REVERT_INJECT_POSSIBILITY = 0.2;
    public static int FUNC_REVERT = 20;
    public static int SHARD_NUM = 61;
    public static int MAX_TPS_DESCEND_TIMES = 5;
    public static double MIN_TPS_TOLERANT = 10.0;
    public static boolean NO_REVERT = false;
}
