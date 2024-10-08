package org.example;

import java.io.IOException;
import java.io.Serializable;

public class TransactionThree extends Transaction implements Cloneable {
    ContractThree contractThree;
    Collector collector;

    public int typeOfCall;
    public int indexA;
    public int indexB;
    public int value;

    public TransactionThree(ContractThree contractThree, Collector collector) {
        this.contractThree = contractThree;
        this.collector = collector;
        this.typeOfCall = Util.random.nextInt(2);
        this.value = Util.random.nextInt(1000);
        this.indexA = Util.random.nextInt(contractThree.contractNum);
        this.indexB = Util.random.nextInt(contractThree.contractNum);
    }

    public TransactionThree(TransactionThree transactionThree) {
        this.contractThree = transactionThree.contractThree;
        this.collector = transactionThree.collector;
        this.typeOfCall = transactionThree.typeOfCall;
        this.value = transactionThree.value;
        this.indexA = transactionThree.indexA;
        this.indexB = transactionThree.indexB;
    }


    @Override
    void exec() {
        boolean status = false;
        long now = System.currentTimeMillis();
        switch (typeOfCall) {
            case 0:
                try {
                    status = contractThree.contractOfRecursiveOnes[indexA].
                            recursiveCall(
                                    contractThree.contractOfRecursiveTwos[indexB].address,
                                    value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case 1:
                try {
                    status = contractThree.contractOfRecursiveTwos[indexA].
                            recursiveCall(
                                    contractThree.contractOfRecursiveOnes[indexB].address,
                                    value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
        }
        collector.onMessage(status, System.currentTimeMillis() - now);
        executeTime = System.currentTimeMillis() - now;
    }

    @Override
    void mutate() {
        this.typeOfCall = Util.random.nextInt(2);
        this.value += Util.random.nextInt(1000);
        this.indexA = Util.random.nextInt(contractThree.contractNum);
        this.indexB = Util.random.nextInt(contractThree.contractNum);
    }


}
