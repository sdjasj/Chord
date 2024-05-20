import aptos_sdk
from aptos_sdk.account import Account
from aptos_sdk.async_client import FaucetClient, RestClient
import asyncio
import os
import sys
from typing import Any, Dict, Optional
import aptos_sdk.ed25519 as ed25519
import random
import json
from aptos_sdk.account import Account
from aptos_sdk.account_address import AccountAddress
from aptos_sdk.aptos_cli_wrapper import AptosCLIWrapper
from aptos_sdk.async_client import FaucetClient, ResourceNotFound, RestClient
from aptos_sdk.bcs import Serializer
from aptos_sdk.transactions import (
    EntryFunction,
    TransactionArgument,
    TransactionPayload,
)
from aptos_sdk.package_publisher import PackagePublisher
from Transaction import Transaction
import subprocess
import multiprocessing
from typing import List
from config import *
from storage import *
import time

rest_clients = []
transactions = []
fund = []
local_storage = {}
# INITIALIZATION = "enter_initialize"

FAUCET_URL = "http://127.0.0.1:8081"
NODE_URL = nodes[0] + "/v1"
rest_client_top = RestClient(NODE_URL)
faucet_client = FaucetClient(FAUCET_URL, rest_client_top)  # <:!:section_1

package_path = '/root/blockchains/aptos/aptos-core/aptos-move/move-examples'

async def deploy_package(acc: Account, package_name: str, rest_client: RestClient, args: str):
    print("Compiling package")
    addr = acc.account_address
    os.chdir(os.path.join(package_path, package_name))
    command = "aptos move compile " + args + " --save-metadata"
    result = subprocess.run(command, shell=True, capture_output=True, text=True)
    if result.returncode == 0: 
        print("Command output:")
        print(result.stdout)
    else:
        print("Command failed.")
        print(result.stdout)
        os.abort()
    print("Finish compiling the package with addr: {}".format(addr))

    module_path = os.path.join(
        package_path, package_name, "build", package_name, "bytecode_modules", package_name + ".mv"
    )
    with open(module_path, "rb") as f:
        module = f.read()

    metadata_path = os.path.join(
        package_path, package_name, "build", package_name, "package-metadata.bcs"
    )
    with open(metadata_path, "rb") as f:
        metadata = f.read()

    print("\nPublishing {} package.".format(package_name))
    package_publisher = PackagePublisher(rest_client)
    txn_hash = await package_publisher.publish_package(acc, metadata, [module])
    await rest_client.wait_for_transaction(txn_hash)
    print("Successfully deploy move module {} with transaction {}".format(package_name, txn_hash))


async def init_test_environment():
        rest_client = RestClient(NODE_URL)
        faucet_client = FaucetClient(FAUCET_URL, rest_client)

        print("Creating Account")
        new_acc = Account.generate()
        print("Funding Account")
        await faucet_client.fund_account(new_acc.address(), 100_000_000_000)
        print("Funding successfully")
        addr = str(new_acc.account_address)
        await deploy_package(new_acc, 'test_parallel', rest_client, "--named-addresses test_parallel=" + addr)
        await deploy_package(new_acc, 'test_share', rest_client, "--named-addresses test_parallel=" + addr + ",test_share=" + addr)
        new_acc.store(modules_account_files)





async def create_accounts(num, filename):
    accounts = []
    fund = []
    for i in range(0, num):
        new_acc = Account.generate()
        new_acc.store(filename)
        new_fund = faucet_client.fund_account(new_acc.address(), 100_000_000)
        accounts.append(new_acc)
        fund.append(new_fund)
    await asyncio.gather(*fund)
    return accounts

def load_accounts(filename):
    accounts = []
    with open(filename, 'r') as file:
        for line in file:
            # 解析 JSON 数据
            data = json.loads(line)
            new_acc = Account(
                AccountAddress.from_str(data["account_address"]),
                ed25519.PrivateKey.from_str(data["private_key"]),
            )
            accounts.append(new_acc)
            # if len(accounts) == num:
            #     break
    return accounts


async def get_onchain_counter(target_address: AccountAddress, object_address: AccountAddress, module: str):
    ret = await rest_client_top.account_resource(target_address, f"{object_address}::{module}::TargetObject")
    return int(ret['data']['counter'])

async def get_onchain_vec(target_address: AccountAddress, object_address: AccountAddress, module: str):
    ret = await rest_client_top.account_resource(target_address, f"{object_address}::{module}::Vec")
    return ret['data']['vec']


async def check_deposit_oracle(target_address: AccountAddress, name: str):
    ret = await rest_client_top.account_resource(target_address, name)
    print(ret['data']['counter'])

operations = ['push', 'pop', 'reverse']
async def parallel_vector_add(target_address: AccountAddress, accounts):
    origin_vec = await get_vector(target_address)
    print("The length of the original vector is: {}".format(len(origin_vec)))

    # init multiple clients
    for node in nodes:
        rest_clients.append(RestClient(node + "/v1"))

    print("Start generating transactions")
    print("Init Vector")
    for account in accounts:
        # for i in range(0, op_num):
        #     print(i)
            # account = random.choice(accounts)
        client = random.choice(rest_clients)
        payload = EntryFunction.natural(
                f"{target_address}::test_object_conflict",
                "add_vector",
                [],
                [TransactionArgument(target_address, Serializer.struct), TransactionArgument(0, Serializer.u64)],
            )
        try:
            signed_transaction = await client.create_bcs_signed_transaction(
                account, TransactionPayload(payload)
            )
            txn_hash = await client.submit_bcs_transaction(signed_transaction)
            transactions.append(txn_hash)
            print("Transaction {} Submitted".format(txn_hash))
        except Exception as e:
            print("Send Transaction Exception: " + str(e))
            continue
    # await asyncio.gather(*transactions)

    print("All {} Transactions Submitted".format(len(transactions)))
    print("All transaction hashes: ")
    # print(transactions)
    for tx in transactions:
        await random.choice(rest_clients).wait_for_transaction(tx)
    print("All Transactions Finished")

    after_vec = await get_vector(target_address)
    print("The length of the original vector is: {}".format(len(after_vec)))
    if len(after_vec) - len(origin_vec) != len(transactions):
        print("The length of vector is not as expected!")
    else:
        print("Add Vector Check Pass!")


async def parallel_vector_add_and_sub(target_address: AccountAddress, accounts):
    origin_vec = await get_vector(target_address)
    print("The length of the original vector is: {}".format(len(origin_vec)))
    origin_len = len(origin_vec)
    # init multiple clients
    for node in nodes:
        rest_clients.append(RestClient(node + "/v1"))

    print("Start generating transactions")
    print("Init Vector")
    for account in accounts:
        # for i in range(0, op_num):
        #     print(i)
            # account = random.choice(accounts)
        client = random.choice(rest_clients)
        func = random.choice(["add_vector", "pop_back_vector"])
        if func == "add_vector":
            payload = EntryFunction.natural(
                    f"{target_address}::test_object_conflict",
                    "add_vector",
                    [],
                    [TransactionArgument(target_address, Serializer.struct), TransactionArgument(i, Serializer.u64)],
                )
            origin_len += 1
        else:
            payload = EntryFunction.natural(
                    f"{target_address}::test_object_conflict",
                    "pop_back_vector",
                    [],
                    [TransactionArgument(target_address, Serializer.struct)],
                )
            origin_len -= 1
        try:
            signed_transaction = await client.create_bcs_signed_transaction(
                account, TransactionPayload(payload)
            )
            txn_hash = await client.submit_bcs_transaction(signed_transaction)
            transactions.append(txn_hash)
            print("Transaction {} Submitted".format(txn_hash))
        except Exception as e:
            print("Send Transaction Exception: " + str(e))
            continue
    # await asyncio.gather(*transactions)

    print("All {} Transactions Submitted".format(len(transactions)))
    print("All transaction hashes: ")
    # print(transactions)
    for tx in transactions:
        await random.choice(rest_clients).wait_for_transaction(tx)
    print("All Transactions Finished")

    after_vec = await get_vector(target_address)
    print("The length of the original vector is: {}".format(len(after_vec)))
    if len(after_vec) != origin_len:
        print("The length of vector is not as expected! The expected length is {}".format(origin_len))
    else:
        print("Add Vector Check Pass!")


async def parallel_deposit(target_address: AccountAddress, accounts):
    counter_before = await get_deposit_count(target_address)
    counter_after = counter_before
    # init all accounts
    # print("Start creating accounts")
    # accounts = await create_accounts(account_num)
    # print("Account create finished!")

    # load n accounts
    # accounts = load_accounts(account_num)

    # init multiple clients
    for node in nodes:
        rest_clients.append(RestClient(node + "/v1"))
    
    # send multiple txs from different account
    print("Start sending transactions")
    for account in accounts:
        rest_client = random.choice(rest_clients)
        num = random.randint(0, 1000)
        counter_after += num
        # init transaction payload
        payload = None
        if num % 2:
            payload = EntryFunction.natural(
                f"{target_address}::ModuleOne",
                "deposit",
                [],
                [TransactionArgument(target_address, Serializer.struct), TransactionArgument(num, Serializer.u64)],
            )
        else:
            payload = EntryFunction.natural(
                f"{target_address}::test_object_conflict",
                "deposit",
                [],
                [TransactionArgument(target_address, Serializer.struct), TransactionArgument(num, Serializer.u64)],
            )
        signed_transaction = await rest_client.create_bcs_signed_transaction(
            account, TransactionPayload(payload)
        )
        txn_hash = await rest_client.submit_bcs_transaction(signed_transaction)
        transactions.append(txn_hash)
    
    # parsing all transactions
    for tx in transactions:
        await rest_clients[0].wait_for_transaction(tx)
    
    print("All transactions finished!")
    # close clients
    for client in rest_clients:
        await client.close()
    
    # check oracle
    counter_now = await get_deposit_count(target_address)
    if counter_now != counter_after:
        print("Assertion Error!!!!. Original Counter is {}. Expected Counter is {}. Real Counter is {}.".format(counter_before, counter_after, counter_now))
    else:
        print("Deposit Oracle Check Passed")

async def init_objects(module_accounts: List[Account], client_accounts: List[Account]):
    print("Creating all the initialization transactions and submit")
    for sender in client_accounts:
        for module in module_accounts:
            module_addr = module.account_address
            transaction = Transaction(sender, random.choice(rest_clients), module_addr, sender.account_address, Modules.BASE, Functions.INITIALIZATION)
            txn_hash = await transaction.send_transaction()
            transactions.append(txn_hash)
    print("Waiting for all initialization to finish")
    for tx in transactions:
        await random.choice(rest_clients).wait_for_transaction(tx)
    print("All initialization transactions finished!")

async def init_local_storage(module_accounts: List[Account], client_accounts: List[Account]):
    for sender in client_accounts:
        storage = Storage(sender, module_accounts)
        local_storage[str(sender.account_address)] = storage
        for module in module_accounts:
            counter_on_chain = await get_onchain_counter(sender.account_address, module.account_address, Modules.BASE.value)
            local_storage[str(sender.account_address)].resources[str(module.account_address)].counter = counter_on_chain
            vec_on_chain = await get_onchain_vec(sender.account_address, module.account_address, Modules.BASE.value)
            local_storage[str(sender.account_address)].resources[str(module.account_address)].vec = vec_on_chain


async def check_oracles(module_accounts: List[Account], client_accounts: List[Account]):
    for client in client_accounts:
        for module in module_accounts:
            counter_on_chain = await get_onchain_counter(client.account_address, module.account_address, Modules.BASE.value)
            counter_of_local = local_storage[str(client.account_address)].resources[str(module.account_address)].counter
            print("counter on chain: {}, counter of local: {}".format(counter_on_chain, counter_of_local))
            assert counter_on_chain == counter_of_local, "The counter on chain does not meet the counter in local storage: {} vs {}".format(counter_on_chain, counter_of_local)
            vec_on_chain = await get_onchain_vec(client.account_address, module.account_address, Modules.BASE.value)
            vec_of_local = local_storage[str(client.account_address)].resources[str(module.account_address)].vec
            print("vec length on chain: {}, vec length of local: {}".format(len(vec_on_chain), len(vec_of_local)))
            assert len(vec_on_chain) == len(vec_of_local), "The vector length on chain does not meet the counter in local storage: {} vs {}".format(vec_on_chain, vec_of_local)
    print("Passed the Oracle Checks")

async def send_transactions(module_accounts: List[Account], client_accounts: List[Account], txns_per_round: int):
    # 每一个account发一个transaction
    txns = []
    tasks = []
    modules_tuple = tuple(module for module in Modules)
    for sender in client_accounts:
        rest_client = random.choice(rest_clients)
        module_addr = random.choice(module_accounts).account_address
        target_addr = random.choice(client_accounts).account_address
        module = random.choice(modules_tuple)
        func = Functions.APPENDVECTOR
        txn = Transaction(sender, rest_client, module_addr, target_addr, module, func)
        local_storage[str(target_addr)].resources[str(module_addr)].add_vector()
        task = txn.send_transaction()
        tasks.append(task)
    txns = await asyncio.gather(*tasks)
    print("All transactions sent!")
    for tx in txns:
        await random.choice(rest_clients).wait_for_transaction(tx)

async def check_client_status(client_accounts: List[Account], timeout=10):
    for client in client_accounts:
        retries = 0
        while True:
            try:
                assert(retries < timeout), f"Max retries ({timeout}) exceeded. Clients Status Check Failed!"
                rest = random.choice(rest_clients)
                result = await rest.account(client.account_address)
                # 处理成功的情况
                break
            except Exception as e:
                print(f"Error: {e}")
                retries += 1
                await asyncio.sleep(1)
        # print(f"Max retries ({timeout}) exceeded. Clients Status Check Failed!")
        # os.abort()

async def start(modules_num: int, account_num: int, test_round: int):
    for node in nodes:
        rest_clients.append(RestClient(node + "/v1"))
    print("Start init accounts and modules")
    tasks = [init_test_environment() for _ in range(modules_num)]
    results = await asyncio.gather(*tasks)  # Concurrently execute tasks and gather results
    print(results)  # Print results

    module_accounts = load_accounts(modules_account_files)
    
    print("Start creating sender accounts")
    client_accounts = await create_accounts(account_num, senders_account_files)
    print("Check sender status")
    await check_client_status(client_accounts, 10)
    print("Sender create finished!")
    # init objects for client accounts
    await init_objects(module_accounts, client_accounts)
    print("Init Local Storage")
    await init_local_storage(module_accounts, client_accounts)
    
    print("Send Transactions")
    for i in range(0, test_round):
        await send_transactions(module_accounts, client_accounts, account_num)
        print("All transactions in round {} finished!".format(i))
        print("Check Oracles")
        await check_oracles(module_accounts, client_accounts)

    # await asyncio.gather(
    #     parallel_vector_add(target_address, accounts),
    #     parallel_deposit(target_address, accounts)
    # )

def run_node_crash():
    subprocess.Popen(["bash", "./node_crash.sh"], stdout=subprocess.PIPE, stderr=subprocess.PIPE, stdin=subprocess.PIPE, shell=True, start_new_session=True)

def reflesh_network():
    subprocess.run(["bash", "./refresh_network.sh"])

if __name__ == "__main__":
    
    # account_address = AccountAddress.from_str(local_addr)
    # reflesh_network()
    # print("Network is refreshed!")
    # run_node_crash()
    # print()
    # asyncio.run(init_test_environment(2))
    
    # process.start()
    asyncio.run(start(1,1,100))
    # asyncio.run(check_deposit_oracle(account_address, "0
    # x359f3a53c280860f2cfc9dafb3e6caf541138ac11e5251a2f8f3017dbfa9467c::test_object_conflict::TargetObject"))