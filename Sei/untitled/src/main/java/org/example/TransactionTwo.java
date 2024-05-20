package org.example;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;

public class TransactionTwo extends Transaction implements Cloneable {
    ContractTwo contractTwo;
    Collector collector;
    public int typeOfRW;
    public int typeOfCall;

    public int indexA;
    public int indexB;
    public int value;

    public TransactionTwo(ContractTwo contractTwo,
                          Collector collector) {
        this.contractTwo = contractTwo;
        this.collector = collector;
        this.typeOfRW = Util.random.nextInt(2);
        this.typeOfCall = Util.random.nextInt(6);
        this.value = Util.random.nextInt(1000);
        this.indexA = Util.random.nextInt(contractTwo.contractNum);
        this.indexB = Util.random.nextInt(contractTwo.contractNum);
    }

    public TransactionTwo(TransactionTwo transactionTwo) {
        this.contractTwo = transactionTwo.contractTwo;
        this.collector = transactionTwo.collector;
        this.typeOfRW = transactionTwo.typeOfRW;
        this.typeOfCall = transactionTwo.typeOfCall;
        this.value = transactionTwo.value;
        this.indexA = transactionTwo.indexA;
        this.indexB = transactionTwo.indexB;
    }

    @Override
    public void exec() {
        boolean status = false;
        long now = System.currentTimeMillis();
        switch (typeOfRW) {
            case 0:
                switch (typeOfCall) {
                    case 0:
                        try {
                            status = contractTwo.contractSharedOnes[indexA].setTwo(
                                    contractTwo.contractSharedTwos[indexB].address, value);
                            if (status) {
                                contractTwo.summaryTwo[indexB].addAndGet(value);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 1:
                        try {
                            status = contractTwo.contractSharedOnes[indexA].setThree(
                                    contractTwo.contractSharedThrees[indexB].address, value);
                            if (status) {
                                contractTwo.summaryThree[indexB].addAndGet(value);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 2:
                        try {
                            status = contractTwo.contractSharedTwos[indexA].setOne(
                                    contractTwo.contractSharedOnes[indexB].address, value);
                            if (status) {
                                contractTwo.summaryOne[indexB].addAndGet(value);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 3:
                        try {
                            status = contractTwo.contractSharedTwos[indexA].setThree(
                                    contractTwo.contractSharedThrees[indexB].address, value);
                            if (status) {
                                contractTwo.summaryThree[indexB].addAndGet(value);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 4:
                        try {
                            status = contractTwo.contractSharedThrees[indexA].setOne(
                                    contractTwo.contractSharedOnes[indexB].address, value);
                            if (status) {
                                contractTwo.summaryOne[indexB].addAndGet(value);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 5:
                        try {
                            status = contractTwo.contractSharedThrees[indexA].setTwo(
                                    contractTwo.contractSharedTwos[indexB].address, value);
                            if (status) {
                                contractTwo.summaryTwo[indexB].addAndGet(value);
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                }
                break;
            case 1:
                switch (typeOfCall) {
                    case 0:
                        try {
                            status = contractTwo.contractSharedOnes[indexA].getTwo(
                                    contractTwo.contractSharedTwos[indexB].address);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 1:
                        try {
                            status = contractTwo.contractSharedOnes[indexA].getThree(
                                    contractTwo.contractSharedThrees[indexB].address);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 2:
                        try {
                            status = contractTwo.contractSharedTwos[indexA].getOne(
                                    contractTwo.contractSharedOnes[indexB].address);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 3:
                        try {
                            status = contractTwo.contractSharedTwos[indexA].getThree(
                                    contractTwo.contractSharedThrees[indexB].address);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 4:
                        try {
                            status = contractTwo.contractSharedThrees[indexA].getOne(
                                    contractTwo.contractSharedOnes[indexB].address);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 5:
                        try {
                            status = contractTwo.contractSharedThrees[indexA].getTwo(
                                    contractTwo.contractSharedTwos[indexB].address);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                }
                break;
        }
        collector.onMessage(status, System.currentTimeMillis() - now);
        executeTime = System.currentTimeMillis() - now;
    }

    @Override
    public void mutate() {
        this.typeOfRW = Util.random.nextInt(2);
        this.typeOfCall = Util.random.nextInt(6);
        this.value += Util.random.nextInt(20000);
        this.indexA = Util.random.nextInt(contractTwo.contractNum);
        this.indexB = Util.random.nextInt(contractTwo.contractNum);
    }

}
