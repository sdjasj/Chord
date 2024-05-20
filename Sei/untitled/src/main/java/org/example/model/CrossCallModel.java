package org.example.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Constant;
import org.example.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

public class CrossCallModel {
    ContractModel contractModel;
    public String rawContract;
    public String name;
    public HashMap<String, ComplexFunc> funcs;
    public ArrayList<String> funcNames;
    public String address;
    public String contractPath;
    public CrossTransaction[] crossTransactions;

    public CrossCallModel(ContractModel contractModel) {
        this.contractModel = contractModel;
        this.name = Util.getRandomString();
        this.contractPath = Constant.CONTRACT_STORAGE_PATH + "/" + this.name + ".sol";
        this.funcNames = new ArrayList<>();
        this.funcs = new HashMap<>();
        this.crossTransactions = new CrossTransaction[Constant.CROSS_TX_COUNT];
    }

    public void generateTransactions() {
        IntStream.range(0, Constant.CROSS_TX_COUNT)
                .parallel()
                .forEach(
                        i -> {
                            String funcName = funcNames.get(
                                    ThreadLocalRandom.current().nextInt(funcNames.size())
                            );
                            crossTransactions[i] = new CrossTransaction(
                                    funcs.get(funcName), contractModel.collector, funcName, address, this
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
        IntStream.range(0, Constant.CROSS_FUNC_NUMS)
                .forEach(
                        i -> {
                            ComplexFunc complexFunc = contractModel.complexFuncs.get(
                                    Util.random.nextInt(contractModel.complexFuncs.size())
                            );
                            String name = Util.getRandomString();
                            funcs.put(name, complexFunc);
                            funcNames.add(name);
                        }
                );
    }


    public void generateContract() {
        StringBuilder sb = new StringBuilder();
        sb.append("pragma solidity ^0.8.0;\n\n");
        sb.append(String.format("import \"./%s.sol\";\n", contractModel.name));

        sb.append(String.format("\ncontract %s {\n", this.name));
        sb.append("\n    //complex func declare...\n\n\n");
        funcs.forEach(
                (k, v) -> {
                    sb.append(String.format("    function %s() public {\n", k));
                    sb.append("        ")
                            .append(String.format("%s(%s).%s();\n",
                                    v.contractModel.name, v.contractModel.address, v.name)
                            );
                    sb.append("    ").append("}\n\n");
                }
        );
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
}
