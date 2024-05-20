package org.example;

import java.io.Serializable;

public abstract class Transaction{
    public long executeTime;
    abstract void exec();

    abstract void mutate();

}