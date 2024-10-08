package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

public class ContractSharedOne extends Contract implements Serializable {
    public ContractSharedOne(String filePath) {
        super(filePath);
    }

    public boolean set(long val) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"set(uint)\" %d", Fuzzer.getRandomPrivateKey(), address, val);
        return Util.execTransaction(cmd);
    }

    public boolean get() throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"get()(uint)\"", Fuzzer.getRandomPrivateKey(), address);
        return Util.execTransaction(cmd);
    }

    public boolean setThree(String address, long val) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"setThree(address, uint)\" %s %d", Fuzzer.getRandomPrivateKey(), this.address, address, val);
        return Util.execTransaction(cmd);
    }

    public boolean setTwo(String address, long val) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"setTwo(address, uint)\" %s %d", Fuzzer.getRandomPrivateKey(), this.address, address, val);
        return Util.execTransaction(cmd);
    }

    public boolean getThree(String address) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"getThree(address)(uint)\" %s", Fuzzer.getRandomPrivateKey(), this.address, address);
        return Util.execTransaction(cmd);
    }

    public boolean getTwo(String address) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"getTwo(address)(uint)\" %s", Fuzzer.getRandomPrivateKey(), this.address, address);
        return Util.execTransaction(cmd);
    }
}
