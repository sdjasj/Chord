package org.fisco.bcos.sdk.demo.fuzz;

import com.google.common.util.concurrent.RateLimiter;
import org.fisco.bcos.sdk.demo.perf.Collector;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static org.fisco.bcos.sdk.demo.fuzz.Util.executeCommand;

public class EvmFuzzer {
    public transient Object contract;
    public transient Bar bar;
    public String name = "MyContract";
    public transient Class<?> loadedContractClass;
    public String address;
    public Collector collector;

    public EvmFuzzer() throws IOException {
        collector = new Collector();
        executeCommand("java -cp \"apps/*:lib/*:conf/\" org.fisco.bcos.sdk.demo.codegen.DemoSolcToJava org.fisco.bcos.sdk.demo.contract"
                , "/root/java-sdk-demo/dist");
        executeCommand(String.format("cp %s %s", "/root/java-sdk-demo/dist/contracts/sdk/java/org/fisco/bcos/sdk/demo/contract/" + name + ".java", Constant.CONTRACT_JAVA_STORAGE_PATH + "/" + name + ".java"));
        try {
            loadedContractClass = Util.compileAndLoadClass(name, Constant.CONTRACT_JAVA_STORAGE_PATH);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // 调用动态生成的方法
        Method method = null;
        try {
            method = loadedContractClass.getMethod("deploy", Client.class, CryptoKeyPair.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            contract = method.invoke(null, Main.client, Main.client.getCryptoSuite().getCryptoKeyPair());
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        try {
            this.address = (String) loadedContractClass.getMethod("getContractAddress").invoke(contract);
            ShardingUtil.linkShard(address);
        } catch (ContractException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        System.out.println("deploy contract " + name + " successfully!!!!!");
    }

    public void fuzz() throws InterruptedException {
        System.out.println("start fuzzing at " + System.currentTimeMillis() + " .......");
        RateLimiter limiter = RateLimiter.create(Constant.QPS);
//        specifySeed();
//
//        //mutate the seed
//        if (!seedPool.isEmpty()) {
//            mutateSeed();
//            initContract();
//        }




//        getTransactions();
        collector.flush();

        bar = Bar.generate(Constant.TX_COUNT, collector);

        IntStream.range(0, Constant.TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            limiter.acquire();

                            long now = System.currentTimeMillis();
                            Method method;
                            try {
                                method = loadedContractClass.getMethod("multiPath", BigInteger.class, BigInteger.class,
                                        TransactionCallback.class);
                            } catch (NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                            try {
                                method.invoke(contract, new BigInteger(1000, ThreadLocalRandom.current()),
                                        new BigInteger(1000, ThreadLocalRandom.current()), new TransactionCallback() {
                                    @Override
                                    public void onResponse(TransactionReceipt receipt) {
                                        long cost = System.currentTimeMillis() - now;
                                        collector.onMessage(receipt, cost);

                                        bar.receivedBar.step();
                                        if (!receipt.isStatusOK()) {
                                            bar.errorBar.step();
                                            // System.out.println(receipt.getStatus());
                                        }
                                        bar.transactionLatch.countDown();
                                    }
                                });
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                            bar.sendedBar.step();
                        });
//        double tps = utilItems.getTPS();
        bar.transactionLatch.await();
        collector.setTotalTime();
        bar.processEnd();
        double tps = collector.getTPS();
        Util.appendToFile("evm_tps.log", String.format("time:%d  tps:%f  tpsNoError:%f\n",
                System.currentTimeMillis(),
                tps,
                collector.getTpsWithoutError())
        );

//        updateSeed(contractModel, tps);
        System.out.println("fuzzing round finish!!!!!!!!!!!!!");
        System.out.println("now mutate contract for explore more combination");
    }
}
