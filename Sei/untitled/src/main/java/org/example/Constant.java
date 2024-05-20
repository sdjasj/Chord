package org.example;

public class Constant {
    public static boolean DEBUG = false;
    public static int CONTRACT_NUM = 1;
    public static int TX_COUNT = 20000;
    public static int QPS = 5000;
    public static int SEED_COUNTS = 10;
    public static String ACCOUNT_FILE = "/root/sei-chain/build/generated/exported_keys";
    public static String CONTRACT_ONE_ADDRESS_FILE = "/sei-tmp/contract_one_address.txt";
    public static String CONTRACT_SHARED_ONE_ADDRESS_FILE = "/sei-tmp/contract_shared_one_address.txt";
    public static String CONTRACT_SHARED_TWO_ADDRESS_FILE = "/sei-tmp/contract_shared_two_address.txt";
    public static String CONTRACT_SHARED_THREE_ADDRESS_FILE = "/sei-tmp/contract_shared_three_address.txt";
    public static String CONTRACT_REC_ONE_ADDRESS_FILE = "/sei-tmp/contract_rec_one_address.txt";
    public static String CONTRACT_REC_TWO_ADDRESS_FILE = "/sei-tmp/contract_rec_two_address.txt";
    public static int SIMPLE_FUNC_NUMS = 100;
    public static int STATE_VAR_NUMS = 60;
    public static int COMPLEX_FUNC_NUMS = 15;
    public static int SIMPLE_IN_COMPLEX_NUM = 6;
    public static String CONTRACT_STORAGE_PATH = "/root/test/contracts/src";
    public static String CONTRACT_WORK_PATH = "/root/test/contracts";
    public static int CROSS_FUNC_NUMS = 100;
    public static int CROSS_TX_COUNT = 10000;
    public static int CROSS_CONTRACT_NUM = 1;
    public static double REVERT_INJECT_POSSIBILITY = 0.2;
    public static int FUNC_REVERT = 20;
    public static int MAX_TPS_DESCEND_TIMES = 5;
    public static double MIN_TPS_TOLERANT = 10.0;
    public static boolean CONFLICT = true;
}
