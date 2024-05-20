package org.fisco.bcos.sdk.demo.fuzz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class CrossCallModel {
    public ContractModel contractModel;
    public String rawContract;
    public String name;
    public HashMap<String, ArrayList<String>> complexFuncs;
    public HashMap<String, SimpleFunc> simpleFuncs;
    public ArrayList<String> simpleFuncNames;
    public String address;
    public String contractPath;
    public CrossTransaction[] crossTransactions;
    public transient Class<?>  loadedContractClass;
    public transient Object contract;
    public ArrayList<String> complexFuncNames = new ArrayList<>();

    public CrossCallModel(ContractModel contractModel) {
        this.contractModel = contractModel;
        this.name = Util.getRandomString();
        this.contractPath = Constant.CONTRACT_SOL_STORAGE_PATH + "/" + this.name + ".sol";
        this.simpleFuncNames = new ArrayList<>();
        this.complexFuncs = new HashMap<>();
        this.simpleFuncs = new HashMap<>();
        this.crossTransactions = new CrossTransaction[Constant.CROSS_TX_COUNT];
    }

    public void generateTransactions() {

        IntStream.range(0, Constant.CROSS_TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            String funcName = complexFuncNames.get(
                                    ThreadLocalRandom.current().nextInt(complexFuncNames.size())
                            );
                            crossTransactions[i] = new CrossTransaction(
                                    contractModel.collector, this, funcName
                            );
                        }
                );
    }

    public void printContract() {
        System.out.println(this.rawContract);
    }

    public void init() {
        initFuncs();
        generateContract();
        writeToLocal();
    }

    public void initFuncs() {
        IntStream.range(0, Constant.CROSS_SIMPLE_FUNC_NUMS)
                .forEach(
                        i -> {
                            SimpleFunc simpleFunc = contractModel.simpleFuncs.get(
                                    Util.random.nextInt(contractModel.simpleFuncs.size())
                            );
                            String name = Util.getRandomString();
                            simpleFuncs.put(name, simpleFunc);
                            simpleFuncNames.add(name);
                        }
                );
        IntStream.range(0, Constant.COMPLEX_FUNC_NUMS)
                .forEach(
                        i -> {
                            String complexFuncName = Util.getRandomString();
                            for (int j = 0; j < Constant.SIMPLE_IN_COMPLEX_NUM; j++) {
                                ArrayList<String> simpleFuncs = complexFuncs.getOrDefault(complexFuncName, new ArrayList<>());
                                simpleFuncs.add(this.simpleFuncNames.get(Util.random.nextInt(this.simpleFuncNames.size())));
                                complexFuncs.put(complexFuncName, simpleFuncs);
                            }
                            complexFuncNames.add(complexFuncName);
                        }
                );
    }


    public void generateContract() {
        StringBuilder sb = new StringBuilder();
        sb.append("pragma solidity ^0.8.0;\n\n");
        sb.append(String.format("import \"./%s.sol\";\n", contractModel.name));

        sb.append(String.format("\ncontract %s {\n", this.name));
        sb.append("\n    //simple func declare...\n\n\n");
        simpleFuncs.forEach(
                (k, v) -> {
                    sb.append(String.format("    function %s(address adr) public {\n", k));
                    sb.append("        ")
                            .append(String.format("%s(adr).%s();\n",
                                    v.contractModel.name, v.name)
                            );
                    sb.append("    ").append("}\n\n");
                }
        );
        sb.append("\n    //complex func declare...\n\n\n");
        complexFuncs.forEach(
                (k, v) -> {
                    sb.append(String.format("    function %s(address adr) public {\n", k));
                    for (String simpleFunc : v) {
                        sb.append("        ").append(String.format("%s(adr);\n", simpleFunc));
                    }
                    sb.append("    ").append("}\n\n");
                }
        );
        sb.append("\n}");
        this.rawContract = sb.toString();
    }

    public void writeToLocal() {
        //init solidity
        File directory = new File(Constant.CONTRACT_SOL_STORAGE_PATH);
        if (!directory.exists()) {
            boolean res = directory.mkdirs(); // 创建多层目录
            if (!res) {
                System.out.println("create " + Constant.CONTRACT_SOL_STORAGE_PATH + " fail");
            }
        }
        try {
            FileWriter writer = new FileWriter(contractPath);
            writer.write(this.rawContract);
            writer.close();
        } catch (IOException e) {
            System.out.println("写入文件时出现错误：" + e.getMessage());
        }
        //init java
        Util.convertSol2Java();
    }

    public void deploy() throws IOException {
        Util.executeCommand(String.format("cp %s %s", "/root/java-sdk-demo/dist/contracts/sdk/java/org/fisco/bcos/sdk/demo/contract/" + name + ".java", Constant.CONTRACT_JAVA_STORAGE_PATH + "/" + name + ".java"));
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
            method = loadedContractClass.getMethod("setEnableDAG", boolean.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        try {
            method.invoke(contract, true);
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
}
