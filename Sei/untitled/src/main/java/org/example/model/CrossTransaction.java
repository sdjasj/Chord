package org.example.model;

import org.example.Collector;
import org.example.Main;
import org.example.Util;

import java.io.IOException;

public class CrossTransaction extends Transaction {
    public String name;
    public String address;
    public CrossCallModel crossCallModel;
    public CrossTransaction(ComplexFunc complexFunc, Collector collector, String name, String address, CrossCallModel crossCallModel) {
        super(complexFunc, collector);
        this.name = name;
        this.address = address;
        this.crossCallModel = crossCallModel;
    }

    @Override
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
                this.address, this.name);
        boolean status = Util.execTransaction(cmd);
        executeTime = System.currentTimeMillis() - now;
        collector.onMessage(status, executeTime);
        crossCallModel.contractModel.bar.receivedBar.step();
        if (status) {
            complexFunc.modify();
            crossCallModel.contractModel.bar.errorBar.step();
        }
        crossCallModel.contractModel.bar.transactionLatch.countDown();
    }
}
