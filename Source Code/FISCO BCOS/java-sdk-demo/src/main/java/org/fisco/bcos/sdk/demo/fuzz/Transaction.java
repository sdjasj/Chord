package org.fisco.bcos.sdk.demo.fuzz;


import org.fisco.bcos.sdk.demo.perf.Collector;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Transaction implements Serializable {
    public ComplexFunc complexFunc;
    public Collector collector;
    public long executeTime;


    public Transaction(ComplexFunc complexFunc, Collector collector) {
        this.complexFunc = complexFunc;
        this.collector = collector;
    }

    public void exec() throws IOException {
        long now = System.currentTimeMillis();
        Object contract = complexFunc.contractModel.contract;
        Method method;
        try {
            method = complexFunc.contractModel.loadedContractClass.getMethod(complexFunc.name,  TransactionCallback.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            method.invoke(contract, new TransactionCallback() {
                @Override
                public void onResponse(TransactionReceipt receipt) {
                    long cost = System.currentTimeMillis() - now;
                    executeTime = cost;
                    complexFunc.contractModel.collector.onMessage(receipt, cost);

                    complexFunc.contractModel.bar.receivedBar.step();
                    if (!receipt.isStatusOK()) {
                        complexFunc.contractModel.bar.errorBar.step();
                        // System.out.println(receipt.getStatus());
                    } else {
                        complexFunc.modify();
                    }
                    complexFunc.contractModel.bar.transactionLatch.countDown();
                }
            });
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        executeTime = System.currentTimeMillis() - now;
    }
}
