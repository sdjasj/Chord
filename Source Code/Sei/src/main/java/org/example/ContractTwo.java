package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class ContractTwo implements Serializable {
    public int contractNum;

    public ContractSharedOne[] contractSharedOnes;
    public ContractSharedTwo[] contractSharedTwos;
    public ContractSharedThree[] contractSharedThrees;
    public AtomicLong[] summaryOne;
    public AtomicLong[] summaryTwo;
    public AtomicLong[] summaryThree;

    public ContractTwo(int contractNum) {
        this.contractNum = contractNum;
        this.contractSharedOnes = new ContractSharedOne[contractNum];
        this.contractSharedTwos = new ContractSharedTwo[contractNum];
        this.contractSharedThrees = new ContractSharedThree[contractNum];
        this.summaryOne = new AtomicLong[contractNum];
        this.summaryTwo = new AtomicLong[contractNum];
        this.summaryThree = new AtomicLong[contractNum];
    }

    public void initContractSharedOne() {

        ArrayList<String> deployedAddress = Util.readContractAddress(Constant.CONTRACT_SHARED_ONE_ADDRESS_FILE);


        if (deployedAddress.size() < contractNum) {
            IntStream.range(0, deployedAddress.size())
                    .forEach(
                            i -> {
                                contractSharedOnes[i] = new ContractSharedOne("src/ContractSharedOne.sol:ContractSharedOne");
                                contractSharedOnes[i].address = deployedAddress.get(i);
                                summaryOne[i] = new AtomicLong();
                            }
                    );

            IntStream.range(deployedAddress.size(), contractNum)
                    .forEach(
                            i -> {
                                ContractSharedOne contractSharedOne;
                                try {
                                    contractSharedOne = new ContractSharedOne("src/ContractSharedOne.sol:ContractSharedOne");
                                    contractSharedOne.deploy("/root/test/ParallelTest2/", Fuzzer.getRandomPrivateKey());
                                    summaryOne[i] = new AtomicLong();
                                    contractSharedOnes[i] = contractSharedOne;
                                    Util.writeContractAddress(contractSharedOne.address, Constant.CONTRACT_SHARED_ONE_ADDRESS_FILE);

                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
        } else {
            IntStream.range(0, contractNum)
                    .parallel()
                    .forEach(
                            i -> {
                                contractSharedOnes[i] = new ContractSharedOne("src/ContractSharedOne.sol:ContractSharedOne");
                                contractSharedOnes[i].address = deployedAddress.get(i);
                                summaryOne[i] = new AtomicLong();
                            }
                    );
        }
        System.out.println("init contractSharedOne successfully....., total number is " + contractNum);
    }

    public void initContractSharedTwo() {

        ArrayList<String> deployedAddress =
                Util.readContractAddress(Constant.CONTRACT_SHARED_TWO_ADDRESS_FILE);

        if (deployedAddress.size() < contractNum) {
            IntStream.range(0, deployedAddress.size())
                    .forEach(
                            i -> {
                                contractSharedTwos[i] = new ContractSharedTwo("src/ContractSharedTwo.sol:ContractSharedTwo");
                                contractSharedTwos[i].address = deployedAddress.get(i);
                                summaryTwo[i] = new AtomicLong();
                            }
                    );

            IntStream.range(deployedAddress.size(), contractNum)
                    .forEach(
                            i -> {
                                ContractSharedTwo contractSharedTwo;
                                try {
                                    contractSharedTwo = new ContractSharedTwo("src/ContractSharedTwo.sol:ContractSharedTwo");
                                    contractSharedTwo.deploy("/root/test/ParallelTest2/", Fuzzer.getRandomPrivateKey());
                                    contractSharedTwos[i] = contractSharedTwo;
                                    summaryTwo[i] = new AtomicLong();
                                    Util.writeContractAddress(contractSharedTwo.address, Constant.CONTRACT_SHARED_TWO_ADDRESS_FILE);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
        } else {
            IntStream.range(0, contractNum)
                    .parallel()
                    .forEach(
                            i -> {
                                contractSharedTwos[i] = new ContractSharedTwo("src/ContractSharedTwo.sol:ContractSharedTwo");
                                contractSharedTwos[i].address = deployedAddress.get(i);
                                summaryTwo[i] = new AtomicLong();
                            }
                    );
        }
        System.out.println("init contractSharedTwo successfully....., total number is " + contractNum);

    }

    public void initContractSharedThree() {

        ArrayList<String> deployedAddress =
                Util.readContractAddress(Constant.CONTRACT_SHARED_THREE_ADDRESS_FILE);

        if (deployedAddress.size() < contractNum) {
            IntStream.range(0, deployedAddress.size())
                    .forEach(
                            i -> {
                                contractSharedThrees[i] = new ContractSharedThree("src/ContractSharedThree.sol:ContractSharedThree");
                                contractSharedThrees[i].address = deployedAddress.get(i);
                                summaryThree[i] = new AtomicLong();
                            }
                    );

            IntStream.range(deployedAddress.size(), contractNum)
                    .forEach(
                            i -> {
                                ContractSharedThree contractSharedThree;
                                try {
                                    contractSharedThree = new ContractSharedThree("src/ContractSharedThree.sol:ContractSharedThree");
                                    contractSharedThree.deploy("/root/test/ParallelTest2/", Fuzzer.getRandomPrivateKey());
                                    contractSharedThrees[i] = contractSharedThree;
                                    summaryThree[i] = new AtomicLong();
                                    Util.writeContractAddress(contractSharedThree.address, Constant.CONTRACT_SHARED_THREE_ADDRESS_FILE);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
        } else {
            IntStream.range(0, contractNum)
                    .parallel()
                    .forEach(
                            i -> {
                                contractSharedThrees[i] = new ContractSharedThree("src/ContractSharedThree.sol:ContractSharedThree");
                                contractSharedThrees[i].address = deployedAddress.get(i);
                                summaryThree[i] = new AtomicLong();
                            }
                    );
        }
        System.out.println("init contractSharedThree successfully....., total number is " + contractNum);

    }

    public void init() {

        System.out.println("init ContractTwo, generate contracts......");
        initContractSharedOne();
        initContractSharedTwo();
        initContractSharedThree();
    }

    public void initSlow() {
        System.out.println("init ContractTwo, generate contracts......");
        IntStream.range(0, contractNum)
                .forEach(
                        i -> {
                            ContractSharedOne contractSharedOne;
                            try {
                                contractSharedOne = new ContractSharedOne("src/ContractSharedOne.sol:ContractSharedOne");
                                contractSharedOne.deploy("/root/test/ParallelTest2/", Fuzzer.getRandomPrivateKey());
                                summaryOne[i] = new AtomicLong();
                                contractSharedOnes[i] = contractSharedOne;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
        System.out.println("init contractSharedOne successfully....., total number is " + contractNum);

        IntStream.range(0, contractNum)
                .forEach(
                        i -> {
                            ContractSharedTwo contractSharedTwo;
                            try {
                                contractSharedTwo = new ContractSharedTwo("src/ContractSharedTwo.sol:ContractSharedTwo");
                                contractSharedTwo.deploy("/root/test/ParallelTest2/", Fuzzer.getRandomPrivateKey());
                                contractSharedTwos[i] = contractSharedTwo;
                                summaryTwo[i] = new AtomicLong();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
        System.out.println("init contractSharedTwo successfully....., total number is " + contractNum);

        IntStream.range(0, contractNum)
                .forEach(
                        i -> {
                            ContractSharedThree contractSharedThree;
                            try {
                                contractSharedThree = new ContractSharedThree("src/ContractSharedThree.sol:ContractSharedThree");
                                contractSharedThree.deploy("/root/test/ParallelTest2/", Fuzzer.getRandomPrivateKey());
                                contractSharedThrees[i] = contractSharedThree;
                                summaryThree[i] = new AtomicLong();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
        System.out.println("init contractSharedThree successfully....., total number is " + contractNum);
    }

    private long getVal(Contract contract, String calledFunc) throws IOException {
        String cmd = String.format("cast call --rpc-url http://localhost:8545 " +
                "--private-key %s " +
                "%s " +
                "\"%s\"", Fuzzer.getRandomPrivateKey(), contract.address, calledFunc);
        String res;
        String result = "";
        while (true) {
            result = Util.executeCommand(cmd);
            if (!Objects.equals(result, "")) {
                res = result.split(" ")[0];
                break;
            }
        }
        return Long.parseLong(res);
    }

    public void check() throws InterruptedException {
        System.out.println("start check ParaTestTwo.......");
        int cnt = 0;
        AtomicBoolean flag = new AtomicBoolean();
        while (cnt < 5) {
            IntStream.range(0, contractNum)
                    .parallel()
                    .forEach(
                            i -> {
                                try {
                                    final long expectShareOneVal = getVal(contractSharedOnes[i], "get()(uint)");
                                    final long expectShareTwoVal =  getVal(contractSharedTwos[i], "get()(uint)");
                                    final long expectShareThreeVal = getVal(contractSharedThrees[i], "get()(uint)");
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
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
            if (flag.get()) {
                Fuzzer.logger.info("\n\n\nCheck " + cnt + " times but failed, maybe bugs occur");
                flag.set(false);
                cnt++;
                Thread.sleep(6000);
            } else {
                break;
            }
        }
        System.out.println("check finish!!!!!!!!!!!!!!!!");
    }
}
