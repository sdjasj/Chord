package org.fisco.bcos.sdk.demo.fuzz;


public class SimpleFuncOfUIntWrite extends SimpleFuncOfWrite {
    public long delta;

    public SimpleFuncOfUIntWrite(UnsignedIntegerVar unsignedIntegerVar, ContractModel contractModel) {
        this.contractModel = contractModel;
        this.name = Util.getRandomString();
        this.stateVar = unsignedIntegerVar;
        this.delta = Util.random.nextInt(1000);
    }

    @Override
    public String init() {
        if (Constant.NO_REVERT) {
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
        UnsignedIntegerVar unsignedIntegerVar = (UnsignedIntegerVar) this.stateVar;
        unsignedIntegerVar.modify(delta);
    }


}
