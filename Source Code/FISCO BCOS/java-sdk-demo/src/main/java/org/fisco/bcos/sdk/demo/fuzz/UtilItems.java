package org.fisco.bcos.sdk.demo.fuzz;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarBuilder;
import me.tongfei.progressbar.ProgressBarStyle;
import org.fisco.bcos.sdk.demo.perf.Collector;

import java.util.concurrent.CountDownLatch;

public class UtilItems {
    public Collector collector;
    public ProgressBar sendedBar;
    public ProgressBar receivedBar;
    public ProgressBar errorBar;
    CountDownLatch transactionLatch;

    public UtilItems(Collector collector, ProgressBar sendedBar, ProgressBar receivedBar, ProgressBar errorBar, CountDownLatch transactionLatch) {
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

    public double getTPS() {
        long totalTime = System.currentTimeMillis() - collector.getStartTimestamp();
        return (double) collector.getTotal() / ((double) totalTime / 1000);
    }

    public static UtilItems generate(int txCount) {
        Collector collector = new Collector();
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
        return new UtilItems(collector, sendedBar, receivedBar, errorBar, transactionLatch);
    }
}