package org.fisco.bcos.sdk.demo.fuzz;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.perf.Collector;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

public class Bar implements Serializable {
    public Collector collector;
    public ProgressBar sendedBar;
    public ProgressBar receivedBar;
    public ProgressBar errorBar;
    CountDownLatch transactionLatch;

    public Bar(Collector collector, ProgressBar sendedBar, ProgressBar receivedBar, ProgressBar errorBar, CountDownLatch transactionLatch) {
        this.collector = collector;
        this.sendedBar = sendedBar;
        this.receivedBar = receivedBar;
        this.errorBar = errorBar;
        this.transactionLatch = transactionLatch;
    }

    public void processEnd() {
        sendedBar.close();
        receivedBar.close();
        errorBar.close();
        collector.report();
    }


    public static Bar generate(int txCount, Collector collector) {
        collector.setTotal(txCount);
        ProgressBar sendedBar =
                new ProgressBarBuilder()
                        .setTaskName("Send   :")
                        .setInitialMax(txCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar receivedBar =
                new ProgressBarBuilder()
                        .setTaskName("Receive:")
                        .setInitialMax(txCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        ProgressBar errorBar =
                new ProgressBarBuilder()
                        .setTaskName("Errors :")
                        .setInitialMax(txCount)
                        .setStyle(ProgressBarStyle.UNICODE_BLOCK)
                        .build();
        CountDownLatch transactionLatch = new CountDownLatch(txCount);
        return new Bar(collector, sendedBar, receivedBar, errorBar, transactionLatch);
    }
}
