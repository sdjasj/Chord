package org.example.model;

import org.example.Collector;
import org.example.Main;
import org.example.Util;

import java.io.IOException;
import java.io.Serializable;

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
        String privateKey;
        if (Main.crossFuzzFlag) {
            privateKey = CrossFuzzer.getRandomPrivateKey();
        } else {
            privateKey = Fuzzer.getRandomPrivateKey();
        }
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"%s()\"" , privateKey,
                this.complexFunc.contractModel.address, this.complexFunc.name);
        boolean status = Util.execTransaction(cmd);
        if (status) {
            complexFunc.modify();
        }
        executeTime = System.currentTimeMillis() - now;
        collector.onMessage(status, executeTime);
        complexFunc.contractModel.bar.receivedBar.step();
        if (status) {
            complexFunc.modify();
            complexFunc.contractModel.bar.errorBar.step();
        }
        complexFunc.contractModel.bar.transactionLatch.countDown();
    }
}
