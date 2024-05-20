package org.example.model;

import org.example.Constant;
import org.example.Main;
import org.example.Util;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

public class SimpleFuncOfUIntRead extends SimpleFuncOfRead {
    public ContractModel contractModel;

    public SimpleFuncOfUIntRead(UnsignedIntegerVar unsignedIntegerVar, ContractModel contractModel) {
        this.contractModel = contractModel;
        this.name = Util.getRandomString();
        this.stateVar = unsignedIntegerVar;
    }

    @Override
    public String init() {
        if (!Constant.CONFLICT) {
            return String.format("    function %s() public returns(uint) {\n" +
                            "        return %s;\n" +
                            "    }\n\n",
                    this.name,
                    stateVar.name
            );
        }
        return String.format("    function %s() public returns(uint) {\n" +
                             "        if (getRandomOnchain() %% %d == 0) {\n" +
                             "            revert(\"revert inject\");\n" +
                             "        }" +
                             "        return %s;\n" +
                             "    }\n\n",
                this.name,
                Constant.FUNC_REVERT,
                stateVar.name
        );
    }

    public Long getVal() {
        String privateKey;
        if (Main.crossFuzzFlag) {
            privateKey = CrossFuzzer.getRandomPrivateKey();
        } else {
            privateKey = Fuzzer.getRandomPrivateKey();
        }
        String cmd = String.format("cast call --rpc-url http://localhost:8545 " +
                "--private-key %s " +
                "%s " +
                "\"%s()\"", privateKey, contractModel.address, this.name);
        String res;
        String result = "";
        while (true) {
            try {
                result = Util.executeCommand(cmd);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (!Objects.equals(result, "")) {
                res = result.substring(2);
                break;
            }
        }
        BigInteger bigInteger = new BigInteger(res, 16);
        return bigInteger.longValue();
    }
}
