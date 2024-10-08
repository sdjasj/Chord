package org.fisco.bcos.sdk.demo.fuzz;


import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public abstract class SimpleFunc implements Serializable {
    public String name;
    public StateVar stateVar;
    public double revertPossibility = ThreadLocalRandom.current().nextDouble();
    public ContractModel contractModel;

    public abstract String init();
}
