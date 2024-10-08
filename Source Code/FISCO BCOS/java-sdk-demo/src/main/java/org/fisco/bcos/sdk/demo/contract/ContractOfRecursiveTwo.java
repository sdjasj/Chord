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
import org.fisco.bcos.sdk.v3.codec.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.v3.contract.Contract;
import org.fisco.bcos.sdk.v3.crypto.CryptoSuite;
import org.fisco.bcos.sdk.v3.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.v3.model.CryptoType;
import org.fisco.bcos.sdk.v3.model.TransactionReceipt;
import org.fisco.bcos.sdk.v3.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class ContractOfRecursiveTwo extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b5061015e806100206000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c80635aa47eea14610030575b600080fd5b61004361003e3660046100cb565b610045565b005b8061004e575050565b6001600160a01b038216635aa47eea30610069600185610103565b6040516001600160e01b031960e085901b1681526001600160a01b0390921660048301526024820152604401600060405180830381600087803b1580156100af57600080fd5b505af11580156100c3573d6000803e3d6000fd5b505050505050565b600080604083850312156100de57600080fd5b82356001600160a01b03811681146100f557600080fd5b946020939093013593505050565b60008282101561012357634e487b7160e01b600052601160045260246000fd5b50039056fea2646970667358221220e9fbb2f697ab695fe67b94162a83b9633c9c7655feee1fad2d66352cdfa161e764736f6c634300080b0033"};

    public static final String BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b5061015e806100206000396000f3fe608060405234801561001057600080fd5b506004361061002b5760003560e01c8063d5d1fe2414610030575b600080fd5b61004361003e3660046100cb565b610045565b005b8061004e575050565b6001600160a01b03821663d5d1fe2430610069600185610103565b6040516001600160e01b031960e085901b1681526001600160a01b0390921660048301526024820152604401600060405180830381600087803b1580156100af57600080fd5b505af11580156100c3573d6000803e3d6000fd5b505050505050565b600080604083850312156100de57600080fd5b82356001600160a01b03811681146100f557600080fd5b946020939093013593505050565b6000828210156101235763b95aa35560e01b600052601160045260246000fd5b50039056fea26469706673582212202dad4b754d7adcd8d6d9329c860d4ee6be59a481c4d69c893deb34c28cd51d5164736f6c634300080b0033"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"inputs\":[{\"internalType\":\"address\",\"name\":\"contractAddress\",\"type\":\"address\"},{\"internalType\":\"uint256\",\"name\":\"count\",\"type\":\"uint256\"}],\"name\":\"recursiveCall\",\"outputs\":[],\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.v3.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_RECURSIVECALL = "recursiveCall";

    protected ContractOfRecursiveTwo(String contractAddress, Client client,
                                     CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public static String getABI() {
        return ABI;
    }

    public TransactionReceipt recursiveCall(String contractAddress, BigInteger count) {
        final Function function = new Function(
                FUNC_RECURSIVECALL,
                Arrays.<Type>asList(new Address(contractAddress),
                        new Uint256(count)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return executeTransaction(function);
    }

    public String recursiveCall(String contractAddress, BigInteger count,
                                TransactionCallback callback) {
        final Function function = new Function(
                FUNC_RECURSIVECALL,
                Arrays.<Type>asList(new Address(contractAddress),
                        new Uint256(count)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForRecursiveCall(String contractAddress, BigInteger count) {
        final Function function = new Function(
                FUNC_RECURSIVECALL,
                Arrays.<Type>asList(new Address(contractAddress),
                        new Uint256(count)),
                Collections.<TypeReference<?>>emptyList(), 0);
        return createSignedTransaction(function);
    }

    public Tuple2<String, BigInteger> getRecursiveCallInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_RECURSIVECALL,
                Arrays.<Type>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = this.functionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<String, BigInteger>(

                (String) results.get(0).getValue(),
                (BigInteger) results.get(1).getValue()
        );
    }

    public static ContractOfRecursiveTwo load(String contractAddress, Client client,
                                              CryptoKeyPair credential) {
        return new ContractOfRecursiveTwo(contractAddress, client, credential);
    }

    public static ContractOfRecursiveTwo deploy(Client client, CryptoKeyPair credential) throws
            ContractException {
        return deploy(ContractOfRecursiveTwo.class, client, credential, getBinary(client.getCryptoSuite()), getABI(), null, null);
    }
}
