package org.example.model;

import org.example.Constant;
import org.example.Util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ComplexFunc implements Serializable {
    public String name;
    public ArrayList<SimpleFunc> simpleFuncs;

    public ContractModel contractModel;

    public ComplexFunc(ContractModel contractModel) {
        this.name = Util.getRandomString();
        this.simpleFuncs = new ArrayList<>();
        this.contractModel = contractModel;
    }

    public void modify() {
        for (SimpleFunc simpleFunc : simpleFuncs) {
            if (simpleFunc instanceof SimpleFuncOfWrite) {
                SimpleFuncOfWrite func = (SimpleFuncOfWrite) simpleFunc;
                func.modify();
            }
        }
    }


    public void addSimpleFunc(SimpleFunc simpleFunc) {
        simpleFuncs.add(simpleFunc);
    }

    public void initSimpleFunc(ArrayList<SimpleFunc> simpleFuncs) {
        for (int i = 0; i < Math.min(Constant.SIMPLE_IN_COMPLEX_NUM , simpleFuncs.size()); i++) {
            this.simpleFuncs.add(simpleFuncs.get(Util.random.nextInt(simpleFuncs.size())));
        }
    }

    public void deleteSimpleFunc() {
        if (simpleFuncs.size() == 1) {
            insertSimpleFunc();
            return;
        }
        simpleFuncs.remove(Util.random.nextInt(simpleFuncs.size()));
    }

    public void insertSimpleFunc() {
        SimpleFunc simpleFunc = contractModel.getOneSimpleFunc();
        simpleFuncs.add(Util.random.nextInt(simpleFuncs.size() + 1), simpleFunc);
    }

    public void swapSimpleFunc() {
        if (simpleFuncs.size() < 2) {
            insertSimpleFunc();
            return;
        }
        int idx1 = Util.random.nextInt(simpleFuncs.size());
        int idx2 = Util.random.nextInt(simpleFuncs.size());
        Collections.swap(simpleFuncs, idx1, idx2);
    }

    public void repeatSimpleFunc() {
        if (simpleFuncs.isEmpty()) {
            insertSimpleFunc();
            return;
        }
        int idx = Util.random.nextInt(simpleFuncs.size());
        SimpleFunc simpleFunc = simpleFuncs.get(idx);
        simpleFuncs.add(idx + 1, simpleFunc);
    }

    public void mutate() {
        if (simpleFuncs.isEmpty()) return;
        switch (Util.random.nextInt(4)) {
            case 0:
                deleteSimpleFunc();
                break;
            case 1:
                insertSimpleFunc();
                break;
            case 2:
                swapSimpleFunc();
                break;
            case 3:
                repeatSimpleFunc();
                break;
        }
    }

    public String init() {
        StringBuilder sb = new StringBuilder(String.format("    function %s() public {\n", name));
        for (SimpleFunc simpleFunc : simpleFuncs) {
            sb.append("        ").append(simpleFunc.name).append("();\n");
        }
        sb.append("    ").append("}\n\n");
        return sb.toString();
    }

}
