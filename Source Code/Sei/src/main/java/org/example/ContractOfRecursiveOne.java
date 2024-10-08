package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

public class ContractOfRecursiveOne extends Contract implements Serializable {
    public ContractOfRecursiveOne(String filePath) {
        super(filePath);
    }

    public boolean recursiveCall(String address, long count) throws IOException {
        String cmd = String.format("cast send --rpc-url http://localhost:8545 " +
                "--json " +
                "--gas-limit 1000000000000000 " +
                "--private-key %s " +
                "%s \"recursiveCall(address, uint256)\" %s %d", Fuzzer.getRandomPrivateKey(), this.address, address, count);
        return Util.execTransaction(cmd);
    }
}
