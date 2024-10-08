package org.fisco.bcos.sdk.demo.fuzz;


import java.util.concurrent.atomic.AtomicLong;

public class IntegerVar extends StateVar {
    public AtomicLong var;
    public SimpleFuncOfIntRead simpleFuncOfIntRead;

    public IntegerVar() {
        this.var = new AtomicLong();
        this.name = Util.getRandomString();
    }

    public void setSimpleFuncOfIntRead(SimpleFuncOfIntRead simpleFuncOfIntRead) {
        this.simpleFuncOfIntRead = simpleFuncOfIntRead;
    }

    public String init() {
        return String.format("    int public %s;\n", this.name);
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
        long onChained = simpleFuncOfIntRead.getVal();
        boolean status = expected == onChained ;
        if (!status) {
            System.out.println("state value " + name + " is inconsistent, expected " + expected + " but on chain value is " + onChained);
        }
        return status;
    }

}
