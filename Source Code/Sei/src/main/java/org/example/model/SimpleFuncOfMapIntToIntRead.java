package org.example.model;

import org.example.Constant;
import org.example.Main;
import org.example.Util;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Objects;

public class SimpleFuncOfMapIntToIntRead extends SimpleFuncOfRead {
    public long key;
    public ContractModel contractModel;

    public SimpleFuncOfMapIntToIntRead(MapOfIntToInt mapOfIntToInt, long key, ContractModel contractModel) {
        this.contractModel = contractModel;
        this.name = Util.getRandomString();
        this.stateVar = mapOfIntToInt;
        this.key = key;
    }

    @Override
    public String init() {
        if (!Constant.CONFLICT) {
            return String.format("    function %s() public returns(int) {\n" +
                            "        return %s[%d];\n" +
                            "    }\n\n",
                    this.name,
                    stateVar.name,
                    this.key
            );
        }
        return String.format("    function %s() public returns(int) {\n" +
                             "        if (getRandomOnchain() %% %d == 0) {\n" +
                             "            revert(\"revert inject\");\n" +
                             "        }" +
                             "        return %s[%d];\n" +
                             "    }\n\n",
                this.name,
                Constant.FUNC_REVERT,
                stateVar.name,
                this.key
        );
    }

    public Long getVal() throws IOException {
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
            result = Util.executeCommand(cmd);
            if (!Objects.equals(result, "")) {
                res = result.substring(2);
                break;
            }
        }
        BigInteger bigInteger = new BigInteger(res, 16);
        return bigInteger.longValue();
    }
}
