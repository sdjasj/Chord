package org.example.model;

import org.example.Constant;
import org.example.Util;

public class SimpleFuncOfIntWrite extends SimpleFuncOfWrite {
    public ContractModel contractModel;
    public long delta;

    public SimpleFuncOfIntWrite(IntegerVar integerVar, ContractModel contractModel) {
        this.contractModel = contractModel;
        this.name = Util.getRandomString();
        this.stateVar = integerVar;
        this.delta = Util.random.nextInt(2000) - 1000;
    }

    @Override
    public String init() {
        if (!Constant.CONFLICT) {
            return String.format("    function %s() public {\n" +
                            "        %s += %d;\n" +
                            "    }\n\n",
                    this.name,
                    stateVar.name,
                    this.delta
            );
        }
        return String.format("    function %s() public {\n" +
                             "        %s += %d;\n" +
                             "        if (getRandomOnchain() %% %d == 0) {\n" +
                             "            revert(\"revert inject\");\n" +
                             "        }" +
                             "    }\n\n",
                this.name,
                stateVar.name,
                this.delta,
                Constant.FUNC_REVERT
        );
    }

    @Override
    public void modify() {
        IntegerVar integerVar = (IntegerVar) this.stateVar;
        integerVar.modify(delta);
    }

}
