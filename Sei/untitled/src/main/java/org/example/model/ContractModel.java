package org.example.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Collector;
import org.example.Constant;
import org.example.Fuzzer;
import org.example.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

//one model ---> multi instance
public class ContractModel implements Serializable {

    public StateVar[] stateVars;
    public ArrayList<SimpleFunc> simpleFuncs;
    public ArrayList<ComplexFunc> complexFuncs;
    public Transaction[] transactions;
    public String address;
    public String name;
    public String rawContract;
    public String contractPath;
    public Collector collector;
    public double tps;
    public transient Bar bar;

    public ContractModel() {
        this.name = Util.getRandomString();
        this.contractPath = Constant.CONTRACT_STORAGE_PATH + "/" + this.name + ".sol";
        this.stateVars = new StateVar[Constant.STATE_VAR_NUMS];
        this.transactions = new Transaction[Constant.TX_COUNT];
        this.collector = new Collector();
        initStateVar();
        simpleFuncs = new ArrayList<>();
        initSimpleFunc();
        complexFuncs = new ArrayList<>();
        initComplexFunc();
        generateContract();
        generateTransactions();
    }


    public void printContract() {
        System.out.println(this.rawContract);
    }

    public void check() {
        System.out.println("start check contract.......");
        int cnt = 0;
        AtomicBoolean flag = new AtomicBoolean();
        while (cnt < 5) {
            IntStream.range(0, Constant.STATE_VAR_NUMS)
                    .parallel()
                    .forEach(
                            i -> {
                                if (!stateVars[i].check()) {
                                    flag.set(true);
                                }
                            });
            if (flag.get()) {
                Fuzzer.logger.info("\n\n\nCheck " + cnt + " times but failed, maybe bugs occur");
                flag.set(false);
                cnt++;
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                break;
            }
        }
        System.out.println("check finish!!!!!!!!!!!!!!!!");
    }

    public void clear() {
        this.name = Util.getRandomString();
        this.contractPath = Constant.CONTRACT_STORAGE_PATH + "/" + this.name + ".sol";
        IntStream.range(0, Constant.STATE_VAR_NUMS)
                .parallel()
                .forEach(
                        i -> {
                            stateVars[i].clear();
                        }
                );
    }

    public void mutate() {
        for (ComplexFunc complexFunc : complexFuncs) {
            complexFunc.mutate();
        }
    }

    public void generateTransactions() {
        IntStream.range(0, Constant.TX_COUNT)
                .parallel()
                .forEach(
                        i -> transactions[i] = new Transaction(complexFuncs.get(
                                ThreadLocalRandom.current().nextInt(complexFuncs.size())), collector)
                );
    }



    public void generateContract() {
        StringBuilder sb = new StringBuilder();
        sb.append("pragma solidity ^0.8.0;\n\n");
        sb.append(String.format("contract %s {\n", this.name));
        sb.append("    //state var declare...\n\n");
        for (StateVar stateVar : this.stateVars) {
            sb.append(stateVar.init());
        }
        //get random number on chain
        sb.append("    function getRandomOnchain() public view returns(uint256){\n" +
                  "        bytes32 randomBytes = keccak256(abi.encodePacked(block.number, msg.sender, blockhash(block.timestamp-1)));\n" +
                  "        \n" +
                  "        return uint256(randomBytes);\n" +
                  "    }");

        sb.append("\n    //simple func declare...\n\n\n");
        for (SimpleFunc simpleFunc : simpleFuncs) {
            sb.append(simpleFunc.init());
        }
        sb.append("\n    //complex func declare...\n\n\n");
        for (ComplexFunc complexFunc : complexFuncs) {
            sb.append(complexFunc.init());
        }
        sb.append("\n}");
        this.rawContract = sb.toString();
    }

    public void writeToLocal() {
        File directory = new File(Constant.CONTRACT_STORAGE_PATH);
        if (!directory.exists()) {
            boolean res = directory.mkdirs(); // 创建多层目录
            if (!res) {
                System.out.println("create " + Constant.CONTRACT_STORAGE_PATH + " fail");
            }
        }
        try {
            FileWriter writer = new FileWriter(contractPath);
            writer.write(this.rawContract);
            writer.close();
        } catch (IOException e) {
            System.out.println("写入文件时出现错误：" + e.getMessage());
        }
    }

    public void deploy(String deployerPrivateKey) throws IOException {
        String cmd = String.format("forge create " +
                "--rpc-url http://localhost:8545 " +
                "--private-key %s " +
                "%s --json ", deployerPrivateKey, this.contractPath + ":" + this.name);
        String res = Util.executeCommand(cmd, Constant.CONTRACT_WORK_PATH);
        while (res.isEmpty()) {
            res = Util.executeCommand(cmd, Constant.CONTRACT_WORK_PATH);
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(res);
            this.address = jsonNode.get("deployedTo").asText();
        } catch (Exception e) {
            System.out.println("error cmd:   !!!!!");
            System.out.println(cmd);
            System.out.println("!!!!!!!!!!!!!!!");
            System.out.println(res);
            System.out.println("!!!!!!!!!!!!!!!");
        }

    }

    public void initComplexFunc() {
        IntStream.range(0, Constant.COMPLEX_FUNC_NUMS)
                .forEach(
                        i -> {
                            ComplexFunc complexFunc = new ComplexFunc(this);
                            complexFunc.initSimpleFunc(simpleFuncs);
                            complexFuncs.add(complexFunc);
                        }
                );
    }

    public void initSimpleFunc() {
        IntStream.range(0, Constant.STATE_VAR_NUMS)
                .forEach(
                        i -> {
                            if (stateVars[i] instanceof IntegerVar) {
                                simpleFuncs.add(
                                        new SimpleFuncOfIntWrite((IntegerVar) stateVars[i], this)
                                );
                                SimpleFuncOfIntRead func = new SimpleFuncOfIntRead((IntegerVar) stateVars[i], this);
                                simpleFuncs.add(func);
                                IntegerVar var = (IntegerVar) stateVars[i];
                                var.setSimpleFuncOfIntRead(func);
                            } else if (stateVars[i] instanceof UnsignedIntegerVar) {
                                UnsignedIntegerVar var = (UnsignedIntegerVar) stateVars[i];
                                simpleFuncs.add(
                                        new SimpleFuncOfUIntWrite((UnsignedIntegerVar) stateVars[i], this)
                                );
                                SimpleFuncOfUIntRead func = new SimpleFuncOfUIntRead(var, this);
                                simpleFuncs.add(
                                        func
                                );
                                var.setSimpleFuncOfUIntRead(func);
                            } else if (stateVars[i] instanceof MapOfIntToInt) {
                                MapOfIntToInt mapOfIntToInt = (MapOfIntToInt) stateVars[i];
                                for (Long key : mapOfIntToInt.keys) {
                                    SimpleFuncOfMapIntToIntWrite func =
                                            new SimpleFuncOfMapIntToIntWrite((MapOfIntToInt) stateVars[i], key, this);
                                    simpleFuncs.add(
                                            func
                                    );
                                    SimpleFuncOfMapIntToIntRead funcRead = new SimpleFuncOfMapIntToIntRead((MapOfIntToInt) stateVars[i], func.key, this);
                                    simpleFuncs.add(
                                            funcRead
                                    );
                                    mapOfIntToInt.addReadFunc(funcRead);
                                }

                            } else {
                                System.out.println("error of unknown state value type......");
                            }
                        }
                );
    }

    public void initStateVar() {
        IntStream.range(0, Constant.STATE_VAR_NUMS)
                .parallel()
                .forEach(
                        i -> {
                            switch (ThreadLocalRandom.current().nextInt(3)) {
                                case 0:
                                    stateVars[i] = new IntegerVar();
                                    break;
                                case 1:
                                    stateVars[i] = new UnsignedIntegerVar();
                                    break;
                                case 2:
                                    stateVars[i] = new MapOfIntToInt();
                                    break;
                            }
                        }
                );
    }



    public SimpleFunc getOneSimpleFunc() {
        if (simpleFuncs == null || simpleFuncs.isEmpty()) {
            return null;
        }
        return simpleFuncs.get(Util.random.nextInt(simpleFuncs.size()));
    }


}
