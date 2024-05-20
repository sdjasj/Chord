package org.example.model;


import org.example.Util;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

public abstract class SimpleFunc implements Serializable {
    public String name;
    public StateVar stateVar;
    public double revertPossibility = ThreadLocalRandom.current().nextDouble();

    public abstract String init();
}
