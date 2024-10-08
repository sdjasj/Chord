package org.example.model;

import org.example.Util;

import java.util.concurrent.atomic.AtomicLong;

public class UnsignedIntegerVar extends StateVar {
    public AtomicLong var;
    public SimpleFuncOfUIntRead simpleFuncOfUIntRead;


    public UnsignedIntegerVar() {
        this.var = new AtomicLong();
        this.name = Util.getRandomString();
    }

    public void setSimpleFuncOfUIntRead(SimpleFuncOfUIntRead simpleFuncOfUIntRead) {
        this.simpleFuncOfUIntRead = simpleFuncOfUIntRead;
    }

    public String init() {
        return String.format("    uint public %s;\n", this.name);
    }

    @Override
    public void clear() {
        var = new AtomicLong();
    }

    public void modify(long newVar) {
        var.addAndGet(newVar);
    }

    @Override
    public boolean check() {
        long expected = var.longValue();
        long onChained = simpleFuncOfUIntRead.getVal();
        boolean status = expected == onChained ;
        if (!status) {
            System.out.println("state value " + name + " is inconsistent, expected " + expected + " but on chain value is " + onChained);
        }
        return status;
    }
}
