package org.example.model;

import org.example.Constant;
import org.example.Util;

public class SimpleFuncOfMapIntToIntWrite extends SimpleFuncOfWrite {
    public long key;
    public long delta;
    public ContractModel contractModel;

    public SimpleFuncOfMapIntToIntWrite(MapOfIntToInt mapOfIntToInt, long key, ContractModel contractModel) {
        this.contractModel = contractModel;
        this.name = Util.getRandomString();
        this.stateVar = mapOfIntToInt;
        this.key = key;
        this.delta = Util.random.nextInt(1000);
    }


    @Override
    public String init() {
        if (!Constant.CONFLICT) {
            return String.format("    function %s() public {\n" +
                            "        %s[%d] += %d;\n" +
                            "    }\n\n",
                    this.name,
                    stateVar.name,
                    this.key,
                    this.delta
            );
        }
        return String.format("    function %s() public {\n" +
                             "        %s[%d] += %d;\n" +
                             "        if (getRandomOnchain() %% %d == 0) {\n" +
                             "            revert(\"revert inject\");\n" +
                             "        }" +
                             "    }\n\n",
                this.name,
                stateVar.name,
                this.key,
                this.delta,
                Constant.FUNC_REVERT
        );
    }


    @Override
    public void modify() {
        MapOfIntToInt map = (MapOfIntToInt) this.stateVar;
        map.modify(key, delta);
    }

}
