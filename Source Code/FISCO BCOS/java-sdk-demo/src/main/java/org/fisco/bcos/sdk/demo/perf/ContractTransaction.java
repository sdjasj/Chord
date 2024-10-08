package org.fisco.bcos.sdk.demo.perf;

import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

public abstract class ContractTransaction {
    public int sleep_time;
    public long executeTime;
    abstract void exec();

    abstract void mutate();

    void execAfterSleep() throws InterruptedException {
        Thread.sleep(sleep_time);
        exec();
    }


}
