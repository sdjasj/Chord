package org.example.model;

import java.io.Serializable;

//every var needs init and check
public abstract class StateVar implements Serializable {
    String name;
    public abstract String init();

    public abstract void clear();

    public abstract boolean check();

}
