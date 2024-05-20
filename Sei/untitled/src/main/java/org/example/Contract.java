package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.IOException;
import java.io.Serializable;

public class Contract implements Serializable {
    public String address;
    public String filePath;

    public Contract(String filePath) {
        this.filePath = filePath;
    }

    public void deploy(String path, String deployerPrivateKey) throws IOException {
        String cmd = String.format("forge create " +
                "--rpc-url http://localhost:8545 " +
                "--private-key %s " +
                "%s --json ", deployerPrivateKey, filePath);
        String res = Util.executeCommand(cmd, path);
        while (res.isEmpty()) {
            res = Util.executeCommand(cmd, path);
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(res);
            this.address = jsonNode.get("deployedTo").asText();
        } catch (Exception e) {
            System.out.println("error cmd:   !!!!!");
            System.out.println(cmd);
            System.out.println("!!!!!!!!!!!!!!!");
            System.out.println(res);
            System.out.println("!!!!!!!!!!!!!!!");
        }

    }
}
