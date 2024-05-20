package org.example;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
public class Collector implements Serializable {
    private AtomicLong less50 = new AtomicLong(0);
    private AtomicLong less100 = new AtomicLong(0);
    private AtomicLong less200 = new AtomicLong(0);
    private AtomicLong less400 = new AtomicLong(0);
    private AtomicLong less1000 = new AtomicLong(0);
    private AtomicLong less2000 = new AtomicLong(0);
    private AtomicLong timeout2000 = new AtomicLong(0);
    private AtomicLong totalCost = new AtomicLong(0);

    private Integer total = 0;
    private AtomicInteger received = new AtomicInteger(0);

    private AtomicInteger error = new AtomicInteger(0);
    private Long startTimestamp = System.currentTimeMillis();

    private Long totalTime;

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getReceived() {
        return received.get();
    }

    public void setReceived(Integer received) {
        this.received.getAndSet(received);
    }

    public AtomicInteger getError() {
        return error;
    }

    public void onMessage(boolean isStatusOk, Long cost) {
        try {
            boolean errorMessage = false;
            if (!isStatusOk) {
//                Fuzzer.logger.info(
//                        "error receipt, status: failed");
                errorMessage = true;
            }
            stat(errorMessage, cost);
        } catch (Exception e) {
            Fuzzer.logger.info("error: " + e.getMessage());
        }
    }


    public void stat(boolean errorMessage, Long cost) {
        if (errorMessage) {
            error.addAndGet(1);
        }

        if (cost < 50) {
            less50.incrementAndGet();
        } else if (cost < 100) {
            less100.incrementAndGet();
        } else if (cost < 200) {
            less200.incrementAndGet();
        } else if (cost < 400) {
            less400.incrementAndGet();
        } else if (cost < 1000) {
            less1000.incrementAndGet();
        } else if (cost < 2000) {
            less2000.incrementAndGet();
        } else {
            timeout2000.incrementAndGet();
        }

        totalCost.addAndGet(cost);
    }

    public void setTotalTime() {
        this.totalTime = System.currentTimeMillis() - startTimestamp;
    }

    public double getTPS() {
        return total / ((double) totalTime / 1000);
    }

    public double getTpsWithoutError() {
        return (total - error.get()) / ((double) totalTime / 1000);
    }


    public void report() {
        System.out.println("total");

        System.out.println("===================================================================");

        System.out.println("Total transactions:  " + total);
        System.out.println("Total time: " + totalTime + "ms");
        System.out.println("TPS(include error requests): " + total / ((double) totalTime / 1000));
        System.out.println(
                "TPS(exclude error requests): "
                        + (total - error.get()) / ((double) totalTime / 1000));
        System.out.println("Avg time cost: " + totalCost.get() / total + "ms");
        System.out.println("Errors: " + error.get());

        System.out.println("Time group:");
        System.out.println(
                "0    < time <  50ms   : "
                        + less50
                        + "  : "
                        + (double) less50.get() / total * 100
                        + "%");
        System.out.println(
                "50   < time <  100ms  : "
                        + less100
                        + "  : "
                        + (double) less100.get() / total * 100
                        + "%");
        System.out.println(
                "100  < time <  200ms  : "
                        + less200
                        + "  : "
                        + (double) less200.get() / total * 100
                        + "%");
        System.out.println(
                "200  < time <  400ms  : "
                        + less400
                        + "  : "
                        + (double) less400.get() / total * 100
                        + "%");
        System.out.println(
                "400  < time <  1000ms : "
                        + less1000
                        + "  : "
                        + (double) less1000.get() / total * 100
                        + "%");
        System.out.println(
                "1000 < time <  2000ms : "
                        + less2000
                        + "  : "
                        + (double) less2000.get() / total * 100
                        + "%");
        System.out.println(
                "2000 < time           : "
                        + timeout2000
                        + "  : "
                        + (double) timeout2000.get() / total * 100
                        + "%");
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public void flush() {
        this.less50 = new AtomicLong(0);
        this.less100 = new AtomicLong(0);
        this.less200 = new AtomicLong(0);
        this.less400 = new AtomicLong(0);
        this.less1000 = new AtomicLong(0);
        this.less2000 = new AtomicLong(0);
        this.timeout2000 = new AtomicLong(0);
        this.totalCost = new AtomicLong(0);

        this.total = 0;
        this.received = new AtomicInteger(0);

        this.error = new AtomicInteger(0);
        this.startTimestamp = System.currentTimeMillis();

        this.totalTime = 0L;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }
}
