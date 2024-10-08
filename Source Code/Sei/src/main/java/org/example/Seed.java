package org.example;

import org.checkerframework.common.value.qual.IntRange;

import java.io.*;
import java.util.stream.IntStream;

public class Seed implements Serializable {
    public Transaction[] transactions;
    public double tps;

    public Seed(Transaction[] transactions, double tps) {
        this.transactions = new Transaction[transactions.length];
        IntStream.range(0, transactions.length)
                .parallel()
                .forEach(
                        i -> {
                            if (transactions[i].getClass().equals(TransactionOne.class)) {
                                this.transactions[i] = new TransactionOne((TransactionOne) transactions[i]);
                            } else if (transactions[i].getClass().equals(TransactionTwo.class)) {
                                this.transactions[i] = new TransactionTwo((TransactionTwo) transactions[i]);
                            } else if (transactions[i].getClass().equals(TransactionThree.class)) {
                                this.transactions[i] = new TransactionThree((TransactionThree) transactions[i]);
                            }
                        }
                );
        this.tps = tps;
    }
}
