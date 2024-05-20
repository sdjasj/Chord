package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

public class ContractSharedTwo extends Contract implements Serializable {

    public ContractSharedTwo(String filePath) {
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

    public boolean setOne(String address, long val) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"setOne(address, uint)\" %s %d", Fuzzer.getRandomPrivateKey(), this.address, address, val);
        return Util.execTransaction(cmd);
    }

    public boolean getThree(String address) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"getThree(address)(uint)\" %s", Fuzzer.getRandomPrivateKey(), this.address, address);
        return Util.execTransaction(cmd);
    }

    public boolean getOne(String address) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--private-key %s " +
                "%s \"getOne(address)(uint)\" %s", Fuzzer.getRandomPrivateKey(), this.address, address);
        return Util.execTransaction(cmd);
    }
}
