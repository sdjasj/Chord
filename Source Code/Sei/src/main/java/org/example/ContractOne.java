package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

public class ContractOne implements Serializable {
    public Contract[] contracts;
    public int contractNum;

    public AtomicLong[] A;
    public AtomicLong[] B;
    public AtomicLong[] C;
    public AtomicLong[] D;

    public ContractOne(int contractNum) {
        this.contractNum = contractNum;
        this.contracts = new Contract[contractNum];
        this.A = new AtomicLong[contractNum];
        this.B = new AtomicLong[contractNum];
        this.C = new AtomicLong[contractNum];
        this.D = new AtomicLong[contractNum];
    }

    public boolean addA(int idx, long val) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"addA(uint)\" %d", FuzzerWithNotConflict.getRandomPrivateKey(), contracts[idx].address, val);
        return Util.execTransaction(cmd);
    }

    public boolean addB(int idx, long val) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"addB(uint)\" %d", FuzzerWithNotConflict.getRandomPrivateKey(), contracts[idx].address, val);
        return Util.execTransaction(cmd);
    }

    public boolean addC(int idx, long val) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"addC(uint)\" %d", FuzzerWithNotConflict.getRandomPrivateKey(), contracts[idx].address, val);
        return Util.execTransaction(cmd);
    }

    public boolean addD(int idx, long val) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"addD(uint)\" %d", FuzzerWithNotConflict.getRandomPrivateKey(), contracts[idx].address, val);
        return Util.execTransaction(cmd);
    }

    public boolean addAB(int idx, long val1, long val2) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"addAB(uint, uint)\" %d %d", FuzzerWithNotConflict.getRandomPrivateKey(), contracts[idx].address, val1, val2);
        return Util.execTransaction(cmd);
    }

    public void init() {
        System.out.println("deploy contract One.....");

        ArrayList<String> deployedAddress = Util.readContractAddress(Constant.CONTRACT_ONE_ADDRESS_FILE);

        if (deployedAddress.size() < contractNum) {
            IntStream.range(0, deployedAddress.size())
                            .forEach(
                                    i -> {
                                        contracts[i] = new Contract("src/ParallelTest1.sol:ParallelTest1");
                                        contracts[i].address = deployedAddress.get(i);
                                        this.A[i] = new AtomicLong();
                                        this.B[i] = new AtomicLong();
                                        this.C[i] = new AtomicLong();
                                        this.D[i] = new AtomicLong();
                                    }
                            );

            IntStream.range(deployedAddress.size(), contractNum)
                    .forEach(
                            i -> {
                                try {
                                    contracts[i] = new Contract("src/ParallelTest1.sol:ParallelTest1");
                                    contracts[i].deploy("/root/test/ParallelTest1/", FuzzerWithNotConflict.getRandomPrivateKey());
                                    this.A[i] = new AtomicLong();
                                    this.B[i] = new AtomicLong();
                                    this.C[i] = new AtomicLong();
                                    this.D[i] = new AtomicLong();
                                    Util.writeContractAddress(contracts[i].address, Constant.CONTRACT_ONE_ADDRESS_FILE);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
        } else {
            IntStream.range(0, contractNum)
                    .parallel()
                    .forEach(
                            i -> {
                                contracts[i] = new Contract("src/ParallelTest1.sol:ParallelTest1");
                                contracts[i].address = deployedAddress.get(i);
                                this.A[i] = new AtomicLong();
                                this.B[i] = new AtomicLong();
                                this.C[i] = new AtomicLong();
                                this.D[i] = new AtomicLong();
                            }
                    );
        }

        System.out.println("deploy contract One finish....");
    }

    public void initSlow() {

        System.out.println("deploy contract One.....");
        IntStream.range(0, contractNum)
                .forEach(
                        i -> {
                            try {
                                contracts[i] = new Contract("src/ParallelTest1.sol:ParallelTest1");
                                contracts[i].deploy("/root/test/ParallelTest1/", FuzzerWithNotConflict.getRandomPrivateKey());
                                this.A[i] = new AtomicLong();
                                this.B[i] = new AtomicLong();
                                this.C[i] = new AtomicLong();
                                this.D[i] = new AtomicLong();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
        System.out.println("deploy contract One finish....");
    }

    private long getVal(Contract contract, String calledFunc) throws IOException {
        String cmd = String.format("cast call --rpc-url http://localhost:8545 " +
                "--private-key %s " +
                "%s " +
                "\"%s\"", FuzzerWithNotConflict.getRandomPrivateKey(), contract.address, calledFunc);
        String res;
        String result = "";
        while (true) {
            result = Util.executeCommand(cmd);
            if (!Objects.equals(result, "")) {
                res = result.split(" ")[0];
                break;
            }
        }
        return Long.parseLong(res);
    }

    public void check() throws InterruptedException {
        System.out.println("start check ClassParaTestOne.........");
        int cnt = 0;
        AtomicBoolean flag = new AtomicBoolean(false);
        while (cnt < 5) {
            int finalCnt = cnt;
            IntStream.range(0, contractNum)
                    .parallel()
                    .forEach(
                            i -> {
                                try {
                                    long valueA = getVal(contracts[i], "A()(uint)");
                                    long valueB = getVal(contracts[i], "B()(uint)");
                                    long valueC = getVal(contracts[i], "C()(uint)");
                                    long valueD = getVal(contracts[i], "D()(uint)");
                                    if (A[i].longValue() != valueA) {
                                        flag.set(true);
                                        System.out.println(
                                                "Check failed! Time "
                                                        + finalCnt
                                                        + "contract "
                                                        + i
                                                        + " for state_A: "
                                                        + valueA
                                                        + " not equal to expected: "
                                                        + A[i].longValue());
                                    }
                                    if (B[i].longValue() != valueB) {
                                        flag.set(true);
                                        System.out.println(
                                                "Check failed! Time "
                                                        + finalCnt
                                                        + "contract "
                                                        + i
                                                        + " for state_B: "
                                                        + valueB
                                                        + " not equal to expected: "
                                                        + B[i].longValue());
                                    }
                                    if (C[i].longValue() != valueC) {
                                        flag.set(true);
                                        System.out.println(
                                                "Check failed! Time "
                                                        + finalCnt
                                                        + "contract "
                                                        + i
                                                        + " for state_C: "
                                                        + valueC
                                                        + " not equal to expected: "
                                                        + C[i].longValue());
                                    }
                                    if (D[i].longValue() != valueD) {
                                        flag.set(true);
                                        System.out.println(
                                                "Check failed! Time "
                                                        + finalCnt
                                                        + "contract "
                                                        + i
                                                        + " for state_D: "
                                                        + valueD
                                                        + " not equal to expected: "
                                                        + D[i].longValue());
                                    }
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
            if (flag.get()) {
                Fuzzer.logger.info("\n\n\nCheck " + cnt + " times but failed, maybe bugs occur");
                flag.set(false);
                cnt++;
                Thread.sleep(6000);
            } else {
                break;
            }
        }
        System.out.println("check ClassParaTestOne finish!!!!!");
    }
}
