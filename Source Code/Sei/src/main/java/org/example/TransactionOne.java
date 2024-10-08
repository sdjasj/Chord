package org.example;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;

public class TransactionOne extends Transaction implements Cloneable {
    ContractOne contractOne;
    Collector collector;
    public int typeOfCall;
    public int index;
    public int val;

    public TransactionOne(ContractOne contractOne,
                          Collector collector) {
        this.contractOne = contractOne;
        this.collector = collector;
        this.typeOfCall = Util.random.nextInt(4);
        this.index = Util.random.nextInt(contractOne.contractNum);
        this.val = Util.random.nextInt(1000);
    }

    public TransactionOne(TransactionOne transactionOne) {
        this.contractOne = transactionOne.contractOne;
        this.collector = transactionOne.collector;
        this.typeOfCall = transactionOne.typeOfCall;
        this.index = transactionOne.index;
        this.val = transactionOne.val;
    }

    @Override
    void exec() {
        boolean status = false;
        long now = System.currentTimeMillis();
        switch (typeOfCall) {
            case 0:
                try {
                    status = contractOne.addA(index, val);
                    if (status) {
                        contractOne.A[index].addAndGet(val);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case 1:
                try {
                    status = contractOne.addB(index, val);
                    if (status) {
                        contractOne.B[index].addAndGet(val);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                break;
            case 2:
                try {
                    status = contractOne.addC(index, val);
                    if (status) {
                        contractOne.C[index].addAndGet(val);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                break;
            case 3:
                try {
                    status = contractOne.addD(index, val);
                    if (status) {
                        contractOne.D[index].addAndGet(val);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                break;
            case 4:
                try {
                    status = contractOne.addAB(index, val, val);
                    if (status) {
                        contractOne.A[index].addAndGet(val);
                        contractOne.B[index].addAndGet(val);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
        collector.onMessage(status, System.currentTimeMillis() - now);
        executeTime = System.currentTimeMillis() - now;
    }

    @Override
    void mutate() {
        this.typeOfCall = Util.random.nextInt(4);
        this.index = Util.random.nextInt(contractOne.contractNum);
        this.val = Util.random.nextInt(2000);
    }


}
