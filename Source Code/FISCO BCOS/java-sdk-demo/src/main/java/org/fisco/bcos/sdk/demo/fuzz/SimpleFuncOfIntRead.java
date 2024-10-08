package org.fisco.bcos.sdk.demo.fuzz;


import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Objects;

public class SimpleFuncOfIntRead extends SimpleFuncOfRead{
    public transient Method getVal;
    public transient Method convertVal;

    public SimpleFuncOfIntRead(IntegerVar integerVar, ContractModel contractModel) {
        this.name = Util.getRandomString();
        this.stateVar = integerVar;
        this.contractModel = contractModel;
    }

    public String init() {
        return String.format("    function %s() public returns(int) {\n" +
                             "        return %s;\n" +
                             "    }\n\n",
                this.name,
                stateVar.name
        );
    }
    @Override
    public void initMethod() {
        try {
            getVal = contractModel.loadedContractClass.getMethod(name);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        String newName = "get" + Character.toUpperCase(name.charAt(0)) +
                name.substring(1) + "Output";
        try {
            convertVal = contractModel.loadedContractClass.getMethod(newName, TransactionReceipt.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Long getVal() {
        TransactionReceipt receipt;
        try {
            receipt = (TransactionReceipt) getVal.invoke(contractModel.contract);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        BigInteger bigInteger;
        try {
            Tuple1<BigInteger> tuple1 = (Tuple1<BigInteger>) convertVal.invoke(contractModel.contract, receipt);
            bigInteger = tuple1.getValue1();
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return bigInteger.longValue();
//        String cmd = String.format("cast call --rpc-url http://localhost:8545 " +
//                "--private-key %s " +
//                "%s " +
//                "\"%s()\"", Fuzzer.getRandomPrivateKey(), contractModel.address, this.name);
//        String res;
//        String result = "";
//        while (true) {
//
//            try {
//                result = Util.executeCommand(cmd);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            if (!Objects.equals(result, "")) {
//                res = result.substring(2);
//                break;
//            }
//        }
//        BigInteger bigInteger = new BigInteger(res, 16);
//        return bigInteger.longValue();
    }

}
