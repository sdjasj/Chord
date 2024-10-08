package org.example.model;

import org.example.Util;

import java.util.concurrent.atomic.AtomicBoolean;

//value is set by order?
public class BoolVar {
    public AtomicBoolean var;
    public String name;

    public BoolVar() {
        this.var = new AtomicBoolean(false);
        this.name = Util.getRandomString();
    }

    public String init() {
        return String.format("bool public %s = false;\n", this.name);
    }

}
