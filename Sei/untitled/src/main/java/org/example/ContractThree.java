package org.example;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class ContractThree implements Serializable {
    public int contractNum;

    public ContractOfRecursiveOne[] contractOfRecursiveOnes;
    public ContractOfRecursiveTwo[] contractOfRecursiveTwos;

    public ContractThree(int contractNum) {
        this.contractNum = contractNum;
        contractOfRecursiveOnes = new ContractOfRecursiveOne[contractNum];
        contractOfRecursiveTwos = new ContractOfRecursiveTwo[contractNum];
    }

    public void initContractRecursiveOne() {

        ArrayList<String> deployedAddress = Util.readContractAddress(Constant.CONTRACT_REC_ONE_ADDRESS_FILE);


        if (deployedAddress.size() < contractNum) {
            IntStream.range(0, deployedAddress.size())
                    .forEach(
                            i -> {
                                contractOfRecursiveOnes[i] = new ContractOfRecursiveOne("src/ContractOfRecursiveOne.sol:ContractOfRecursiveOn");
                                contractOfRecursiveOnes[i].address = deployedAddress.get(i);
                            }
                    );

            IntStream.range(deployedAddress.size(), contractNum)
                    .forEach(
                            i -> {
                                ContractOfRecursiveOne contractOfRecursiveOne;
                                try {
                                    contractOfRecursiveOne = new ContractOfRecursiveOne("src/ContractOfRecursiveOne.sol:ContractOfRecursiveOne");
                                    contractOfRecursiveOne.deploy("/root/test/ParallelTest3/", Fuzzer.getRandomPrivateKey());
                                    contractOfRecursiveOnes[i] = contractOfRecursiveOne;
                                    Util.writeContractAddress(contractOfRecursiveOne.address, Constant.CONTRACT_REC_ONE_ADDRESS_FILE);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
        } else {
            IntStream.range(0, contractNum)
                    .parallel()
                    .forEach(
                            i -> {
                                contractOfRecursiveOnes[i] = new ContractOfRecursiveOne("src/ContractOfRecursiveOne.sol:ContractOfRecursiveOne");
                                contractOfRecursiveOnes[i].address = deployedAddress.get(i);
                            }
                    );
        }
        System.out.println("init ContractOfRecursiveOne successfully....., total number is " + contractNum);
    }

    public void initContractRecursiveTwo() {

        ArrayList<String> deployedAddress = Util.readContractAddress(Constant.CONTRACT_REC_TWO_ADDRESS_FILE);


        if (deployedAddress.size() < contractNum) {
            IntStream.range(0, deployedAddress.size())
                    .forEach(
                            i -> {
                                contractOfRecursiveTwos[i] = new ContractOfRecursiveTwo("src/ContractOfRecursiveTwo.sol:ContractOfRecursiveTwo");
                                contractOfRecursiveTwos[i].address = deployedAddress.get(i);
                            }
                    );

            IntStream.range(deployedAddress.size(), contractNum)
                    .forEach(
                            i -> {
                                ContractOfRecursiveTwo contractOfRecursiveTwo;
                                try {
                                    contractOfRecursiveTwo = new ContractOfRecursiveTwo("src/ContractOfRecursiveTwo.sol:ContractOfRecursiveTwo");
                                    contractOfRecursiveTwo.deploy("/root/test/ParallelTest3/", Fuzzer.getRandomPrivateKey());
                                    contractOfRecursiveTwos[i] = contractOfRecursiveTwo;
                                    Util.writeContractAddress(contractOfRecursiveTwo.address, Constant.CONTRACT_REC_TWO_ADDRESS_FILE);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
        } else {
            IntStream.range(0, contractNum)
                    .parallel()
                    .forEach(
                            i -> {
                                contractOfRecursiveTwos[i] = new ContractOfRecursiveTwo("src/ContractOfRecursiveTwo.sol:ContractOfRecursiveTwo");
                                contractOfRecursiveTwos[i].address = deployedAddress.get(i);
                            }
                    );
        }
        System.out.println("init ContractOfRecursiveTwo successfully....., total number is " + contractNum);
    }

    public void init() {
        System.out.println("init ContractThree, generate contracts......");
        initContractRecursiveOne();
        initContractRecursiveTwo();


    }

    public void initSlow() {
        System.out.println("init ContractThree, generate contracts......");
        IntStream.range(0, contractNum)
                .forEach(
                        i -> {
                            ContractOfRecursiveOne contractOfRecursiveOne;
                            try {
                                contractOfRecursiveOne = new ContractOfRecursiveOne("src/ContractOfRecursiveOne.sol:ContractOfRecursiveOne");
                                contractOfRecursiveOne.deploy("/root/test/ParallelTest3/", Fuzzer.getRandomPrivateKey());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
        System.out.println("init ContractOfRecursiveOne successfully....., total number is " + contractNum);

        IntStream.range(0, contractNum)
                .forEach(
                        i -> {
                            ContractOfRecursiveTwo contractOfRecursiveTwo;
                            try {
                                contractOfRecursiveTwo = new ContractOfRecursiveTwo("src/ContractOfRecursiveTwo.sol:ContractOfRecursiveTwo");
                                contractOfRecursiveTwo.deploy("/root/test/ParallelTest3/", Fuzzer.getRandomPrivateKey());
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
        System.out.println("init ContractOfRecursiveTwo successfully....., total number is " + contractNum);
    }

    public boolean check() {
        return true;
    }
}
