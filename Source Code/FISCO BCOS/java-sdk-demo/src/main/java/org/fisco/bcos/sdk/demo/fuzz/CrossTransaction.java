package org.fisco.bcos.sdk.demo.fuzz;

import org.fisco.bcos.sdk.demo.perf.Collector;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class CrossTransaction extends Transaction {
    CrossCallModel crossCallModel;
    String complexFuncName;

    public CrossTransaction(Collector collector, CrossCallModel crossCallModel, String complexFuncName) {
        super(null, collector);
        this.crossCallModel = crossCallModel;
        this.complexFuncName = complexFuncName;
    }

    @Override
    public void exec() throws IOException {
        long now = System.currentTimeMillis();
        Object contract = crossCallModel.contract;
        Method method;
        try {
            method = crossCallModel.loadedContractClass.getMethod(complexFuncName, String.class, TransactionCallback.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            method.invoke(contract, crossCallModel.contractModel.address, new TransactionCallback() {
                @Override
                public void onResponse(TransactionReceipt receipt) {
                    long cost = System.currentTimeMillis() - now;
                    executeTime = cost;
                    crossCallModel.contractModel.collector.onMessage(receipt, cost);

                    crossCallModel.contractModel.bar.receivedBar.step();
                    if (!receipt.isStatusOK()) {
                        crossCallModel.contractModel.bar.errorBar.step();
                        // System.out.println(receipt.getStatus());
                    } else {
                        for (String simpleFuncName : crossCallModel.complexFuncs.get(complexFuncName)) {
                            SimpleFunc simpleFunc = crossCallModel.simpleFuncs.get(simpleFuncName);
                            if (simpleFunc instanceof SimpleFuncOfWrite) {
                                ((SimpleFuncOfWrite) simpleFunc).modify();
                            }
                        }
                    }
                    crossCallModel.contractModel.bar.transactionLatch.countDown();
                }
            });

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        executeTime = System.currentTimeMillis() - now;
    }
}