package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Function;
import org.fisco.bcos.sdk.v3.codec.datatypes.Type;
import org.fisco.bcos.sdk.v3.codec.datatypes.TypeReference;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class ParallelTest1 extends Contract {
    public static final String[] BINARY_ARRAY = {"60806040526000805560006001556000600255600060035534801561002357600080fd5b50610240806100336000396000f3fe608060405234801561001057600080fd5b50600436106100935760003560e01c80638240e07a116100665780638240e07a146100de57806383dfe5fd146100f157806391574b3c146100f9578063ec968bec1461010c578063f446c1d01461011f57600080fd5b80630f529ba21461009857806319348549146100ae57806332e7c5bf146100c357806338632160146100cb575b600080fd5b6003545b60405190815260200160405180910390f35b6100c16100bc3660046101a9565b610127565b005b60015461009c565b6100c16100d93660046101cb565b61015a565b6100c16100ec3660046101cb565b610174565b60025461009c565b6100c16101073660046101cb565b610186565b6100c161011a3660046101cb565b610197565b60005461009c565b8160008082825461013891906101e4565b92505081905550806001600082825461015191906101e4565b90915550505050565b806003600082825461016c91906101e4565b909155505050565b806001600082825461016c91906101e4565b8060008082825461016c91906101e4565b806002600082825461016c91906101e4565b600080604083850312156101bc57600080fd5b50508035926020909101359150565b6000602082840312156101dd57600080fd5b5035919050565b6000821982111561020557634e487b7160e01b600052601160045260246000fd5b50019056fea2646970667358221220f25b87497236b246af59b9a51e2c9c8da1d4f1c7d5ec0a4becb6ab6b10d85c8064736f6c634300080b0033"};

    public static final String BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"60806040526000805560006001556000600255600060035534801561002357600080fd5b50610240806100336000396000f3fe608060405234801561001057600080fd5b50600436106100935760003560e01c80634f4f7203116100665780634f4f7203146100e957806358b8a106146100fc578063a79e17a514610104578063a8154cc81461010c578063db8838fc1461011457600080fd5b8063013df93114610098578063212d47b8146100ad5780634d3ed88d146100c35780634d96c8c5146100d6575b600080fd5b6100ab6100a63660046101a9565b610127565b005b6000545b60405190815260200160405180910390f35b6100ab6100d13660046101cb565b61015a565b6100ab6100e43660046101cb565b610174565b6100ab6100f73660046101cb565b610185565b6002546100b1565b6001546100b1565b6003546100b1565b6100ab6101223660046101cb565b610197565b8160008082825461013891906101e4565b92505081905550806001600082825461015191906101e4565b90915550505050565b806001600082825461016c91906101e4565b909155505050565b8060008082825461016c91906101e4565b806002600082825461016c91906101e4565b806003600082825461016c91906101e4565b600080604083850312156101bc57600080fd5b50508035926020909101359150565b6000602082840312156101dd57600080fd5b5035919050565b600082198211156102055763b95aa35560e01b600052601160045260246000fd5b50019056fea264697066735822122051ed6d9dcf66f00fc76926e454aceb34014170f3ec63676e03631bc5ac8f866564736f6c634300080b0033"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"inputs\":[],\"name\":\"A\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"B\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"C\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[],\"name\":\"D\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"addA\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"x\",\"type\":\"uint256\"},{\"internalType\":\"uint256\",\"name\":\"y\",\"type\":\"uint256\"}],\"name\":\"addAB\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"addB\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"addC\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"addD\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_A = "A";

    public static final String FUNC_B = "B";

    public static final String FUNC_C = "C";

    public static final String FUNC_D = "D";

    public static final String FUNC_ADDA = "addA";

    public static final String FUNC_ADDAB = "addAB";

    public static final String FUNC_ADDB = "addB";

    public static final String FUNC_ADDC = "addC";

    public static final String FUNC_ADDD = "addD";

    protected ParallelTest1(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public BigInteger A() throws ContractException {
        final Function function = new Function(FUNC_A,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public BigInteger B() throws ContractException {
        final Function function = new Function(FUNC_B,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public BigInteger C() throws ContractException {
        final Function function = new Function(FUNC_C,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public BigInteger D() throws ContractException {
        final Function function = new Function(FUNC_D,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public TransactionReceipt addA(BigInteger x) {
        final Function function = new Function(
                FUNC_ADDA,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String addA(BigInteger x, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_ADDA,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAddA(BigInteger x) {
        final Function function = new Function(
                FUNC_ADDA,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple1<BigInteger> getAddAInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_ADDA,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public TransactionReceipt addAB(BigInteger x, BigInteger y) {
        final Function function = new Function(
                FUNC_ADDAB,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x),
                        new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(y)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String addAB(BigInteger x, BigInteger y, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_ADDAB,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x),
                        new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(y)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAddAB(BigInteger x, BigInteger y) {
        final Function function = new Function(
                FUNC_ADDAB,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x),
                        new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(y)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple2<BigInteger, BigInteger> getAddABInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_ADDAB,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, BigInteger>(

                (BigInteger) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue()
        );
    }

    public TransactionReceipt addB(BigInteger x) {
        final Function function = new Function(
                FUNC_ADDB,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String addB(BigInteger x, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_ADDB,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAddB(BigInteger x) {
        final Function function = new Function(
                FUNC_ADDB,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple1<BigInteger> getAddBInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_ADDB,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public TransactionReceipt addC(BigInteger x) {
        final Function function = new Function(
                FUNC_ADDC,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String addC(BigInteger x, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_ADDC,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAddC(BigInteger x) {
        final Function function = new Function(
                FUNC_ADDC,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple1<BigInteger> getAddCInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_ADDC,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public TransactionReceipt addD(BigInteger x) {
        final Function function = new Function(
                FUNC_ADDD,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String addD(BigInteger x, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_ADDD,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForAddD(BigInteger x) {
        final Function function = new Function(
                FUNC_ADDD,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple1<BigInteger> getAddDInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_ADDD,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public static ParallelTest1 load(String contractAddress, Client client,
                                     CryptoKeyPair credential) {
        return new ParallelTest1(contractAddress, client, credential);
    }

    public static ParallelTest1 deploy(Client client, CryptoKeyPair credential) throws
            ContractException {
        return deploy(ParallelTest1.class, client, credential, getBinary(client.getCryptoSuite()), getABI(), null, null);
    }
}
