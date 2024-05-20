package org.example;


import org.example.model.ContractModel;
import org.example.model.CrossFuzzer;
import org.example.model.Fuzzer;
import org.example.model.StressTest;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.stream.IntStream;


public class Main {
    public static boolean testFlag = true;
    public static boolean crossFuzzFlag = true;

    public static ArrayList<Integer> QPS_LIST = new ArrayList<Integer>() {{
        add(20);
        add(40);
        add(60);
        add(80);
        add(100);
        add(120);
        add(140);
        add(160);
        add(180);
        add(200);
    }};

    public static int testTime = 1;

    public static void main(String[] args) throws InterruptedException, IOException {
        if (testFlag) {
//            CrossFuzzer fuzzer = new CrossFuzzer();
//            for (Integer QPS : QPS_LIST) {
//                for (int i = 0; i < testTime; i++) {
//                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " start....");
//                    fuzzer.testFuzz(QPS, "re_tps2.log");
//                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " end....");
//                }
//            }
//            System.out.println("our tool finish");
            StressTest stressTest = new StressTest();
            for (Integer QPS : QPS_LIST) {
                for (int i = 0; i < testTime; i++) {
                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " start....");
                    stressTest.test(QPS);
                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " end....");
                }
            }
            System.out.println("stress finish");
//            Constant.CONFLICT = false;
//            Constant.SIMPLE_IN_COMPLEX_NUM = 1;
//            Constant.COMPLEX_FUNC_NUMS = 5;
//            Constant.CROSS_CONTRACT_NUM = 0;
//            CrossFuzzer fuzzer = new CrossFuzzer();
//            for (Integer QPS : QPS_LIST) {
//                for (int i = 0; i < testTime; i++) {
//                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " start....");
//                    fuzzer.testFuzz(QPS, "re_NoConflict_tps3.log");
//                    System.out.println("QPS " + QPS + " fuzzing round " + (i + 1) + " end....");
//                }
//            }
//            System.out.println("noConflict finish");
        } else if (crossFuzzFlag) {
            CrossFuzzer fuzzer = new CrossFuzzer();
            for (int i = 0; i < 1000000; i++) {
                System.out.println("fuzzing round " + (i + 1) + " start....");
                fuzzer.fuzz();
                System.out.println("fuzzing round " + (i + 1) + " end....");
            }
        } else {
            FuzzerWithNotConflict fuzzer = new FuzzerWithNotConflict();
            for (int i = 0; i < 1000000; i++) {
                System.out.println("fuzzing round " + (i + 1) + " start....");
                fuzzer.fuzz();
                System.out.println("fuzzing round " + (i + 1) + " end....");
            }
        }
    }

//    // Convert an Ethereum address to a checksum address
//    public static String toChecksumAddress(String address) {
//        // Convert the address to lowercase
//        address = address.toLowerCase();
//
//        // Calculate the hash of the address
//        byte[] addressHash = calculateHash(address.getBytes());
//
//        // Convert the address hash to a hexadecimal string
//        String addressHashHex = Hex.toHexString(addressHash);
//
//        // Construct the checksum address
//        StringBuilder checksumAddress = new StringBuilder("0x");
//        for (int i = 0; i < address.length(); i++) {
//            // If the corresponding character in the address is a letter (not a digit)
//            if (Character.isLetter(address.charAt(i))) {
//                // If the ith character of the addressHash is greater than or equal to '8', the corresponding
//                // character in the checksum address should be uppercase.
//                if (Character.digit(addressHashHex.charAt(i), 16) >= 8) {
//                    checksumAddress.append(Character.toUpperCase(address.charAt(i)));
//                } else {
//                    checksumAddress.append(address.charAt(i));
//                }
//            } else {
//                checksumAddress.append(address.charAt(i));
//            }
//        }
//
//        return checksumAddress.toString();
//    }
//
//    // Calculate the Keccak-256 hash of the input data
//    private static byte[] calculateHash(byte[] data) {
//        Keccak.Digest256 digest256 = new Keccak.Digest256();
//        digest256.update(data);
//        return digest256.digest();
//    }


//    public static void test4() {
//        String address = "37a44585bf1e9618fdb4c62c4c96189a07dd4b48";
//        String checksumAddress = toChecksumAddress(address);
//        System.out.println("Checksum address: " + checksumAddress);
//    }

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