package org.fisco.bcos.sdk.demo.contract;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.datatypes.Address;
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
public class ContractSharedOne extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b50610293806100206000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c806301b7417914610067578063387f1cad1461008c57806360fe47b1146100a15780636d4ce63c146100b4578063bd77b41b14610067578063f753e7d81461008c575b600080fd5b61007a6100753660046101b9565b6100bc565b60405190815260200160405180910390f35b61009f61009a3660046101db565b610126565b005b61009f6100af366004610205565b610184565b60005461007a565b6000816001600160a01b0316636d4ce63c6040518163ffffffff1660e01b8152600401602060405180830381865afa1580156100fc573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190610120919061021e565b92915050565b6040516360fe47b160e01b8152600481018290526001600160a01b038316906360fe47b190602401600060405180830381600087803b15801561016857600080fd5b505af115801561017c573d6000803e3d6000fd5b505050505050565b806000808282546101959190610237565b909155505050565b80356001600160a01b03811681146101b457600080fd5b919050565b6000602082840312156101cb57600080fd5b6101d48261019d565b9392505050565b600080604083850312156101ee57600080fd5b6101f78361019d565b946020939093013593505050565b60006020828403121561021757600080fd5b5035919050565b60006020828403121561023057600080fd5b5051919050565b6000821982111561025857634e487b7160e01b600052601160045260246000fd5b50019056fea2646970667358221220a1287f48a077c81b60bf526c7ca73d172a332b9b492e9dc30070629d24fa1c3564736f6c634300080b0033"};

    public static final String BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b5061028f806100206000396000f3fe608060405234801561001057600080fd5b50600436106100625760003560e01c8063299f7f9d1461006757806366f334991461007d5780638311131014610092578063e55fafb9146100a5578063eac8669f146100a5578063ff69d4961461007d575b600080fd5b6000545b60405190815260200160405180910390f35b61009061008b3660046101b5565b6100b8565b005b6100906100a03660046101df565b610116565b61006b6100b33660046101f8565b61012f565b604051630831113160e41b8152600481018290526001600160a01b03831690638311131090602401600060405180830381600087803b1580156100fa57600080fd5b505af115801561010e573d6000803e3d6000fd5b505050505050565b80600080828254610127919061021a565b909155505050565b6000816001600160a01b031663299f7f9d6040518163ffffffff1660e01b8152600401602060405180830381865afa15801561016f573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906101939190610240565b92915050565b80356001600160a01b03811681146101b057600080fd5b919050565b600080604083850312156101c857600080fd5b6101d183610199565b946020939093013593505050565b6000602082840312156101f157600080fd5b5035919050565b60006020828403121561020a57600080fd5b61021382610199565b9392505050565b6000821982111561023b5763b95aa35560e01b600052601160045260246000fd5b500190565b60006020828403121561025257600080fd5b505191905056fea2646970667358221220de643720dbf3865dbff5d488b731c42036fb68c2fa86b158042c61f4c5a629c064736f6c634300080b0033"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"inputs\":[],\"name\":\"get\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"view\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"one\",\"type\":\"address\"}],\"name\":\"getThree\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"one\",\"type\":\"address\"}],\"name\":\"getTwo\",\"outputs\":[{\"internalType\":\"uint256\",\"name\":\"\",\"type\":\"uint256\"}],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"uint256\",\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"set\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"one\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"setThree\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"inputs\":[{\"internalType\":\"address\",\"name\":\"one\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"x\",\"type\":\"uint256\"}],\"name\":\"setTwo\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_GET = "get";

    public static final String FUNC_GETTHREE = "getThree";

    public static final String FUNC_GETTWO = "getTwo";

    public static final String FUNC_SET = "set";

    public static final String FUNC_SETTHREE = "setThree";

    public static final String FUNC_SETTWO = "setTwo";

    protected ContractSharedOne(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public BigInteger get() throws ContractException {
        final Function function = new Function(FUNC_GET,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallWithSingleValueReturn(function, BigInteger.class);
    }

    public TransactionReceipt getThree(String one) {
        final Function function = new Function(
                FUNC_GETTHREE,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String getThree(String one, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_GETTHREE,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForGetThree(String one) {
        final Function function = new Function(
                FUNC_GETTHREE,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple1<String> getGetThreeInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_GETTHREE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
        );
    }

    public Tuple1<BigInteger> getGetThreeOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_GETTHREE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public TransactionReceipt getTwo(String one) {
        final Function function = new Function(
                FUNC_GETTWO,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String getTwo(String one, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_GETTWO,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForGetTwo(String one) {
        final Function function = new Function(
                FUNC_GETTWO,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple1<String> getGetTwoInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_GETTWO,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<String>(

                (String) results.get(0).getValue()
        );
    }

    public Tuple1<BigInteger> getGetTwoOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_GETTWO,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public TransactionReceipt set(BigInteger x) {
        final Function function = new Function(
                FUNC_SET,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String set(BigInteger x, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SET,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSet(BigInteger x) {
        final Function function = new Function(
                FUNC_SET,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple1<BigInteger> getSetInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SET,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
        );
    }

    public TransactionReceipt setThree(String one, BigInteger x) {
        final Function function = new Function(
                FUNC_SETTHREE,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one),
                        new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String setThree(String one, BigInteger x, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SETTHREE,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one),
                        new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSetThree(String one, BigInteger x) {
        final Function function = new Function(
                FUNC_SETTHREE,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one),
                        new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple2<String, BigInteger> getSetThreeInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SETTHREE,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(

                (String) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue()
        );
    }

    public TransactionReceipt setTwo(String one, BigInteger x) {
        final Function function = new Function(
                FUNC_SETTWO,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one),
                        new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String setTwo(String one, BigInteger x, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_SETTWO,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one),
                        new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForSetTwo(String one, BigInteger x) {
        final Function function = new Function(
                FUNC_SETTWO,
                Arrays.<Type>asList(new org.fisco.bcos.sdk.v3.codec.datatypes.Address(one),
                        new org.fisco.bcos.sdk.v3.codec.datatypes.generated.Uint256(x)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple2<String, BigInteger> getSetTwoInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_SETTWO,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(

                (String) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue()
        );
    }

    public static ContractSharedOne load(String contractAddress, Client client,
                                         CryptoKeyPair credential) {
        return new ContractSharedOne(contractAddress, client, credential);
    }

    public static ContractSharedOne deploy(Client client, CryptoKeyPair credential) throws
            ContractException {
        return deploy(ContractSharedOne.class, client, credential, getBinary(client.getCryptoSuite()), getABI(), null, null);
    }
}
