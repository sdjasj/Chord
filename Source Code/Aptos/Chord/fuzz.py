from __future__ import annotations
import aptos_sdk
from aptos_sdk.account import Account
from aptos_sdk.async_client import FaucetClient, RestClient
import asyncio
import os
import sys
from typing import Any, Dict, Optional, List
import aptos_sdk.ed25519 as ed25519
import random
import json
from aptos_sdk.account_address import AccountAddress
from aptos_sdk.aptos_cli_wrapper import AptosCLIWrapper
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
from config import *
from storage import *
import time
from aptos_sdk.async_client import ClientConfig
from aptos_sdk.account_sequence_number import AccountSequenceNumber

from aiolimiter import AsyncLimiter
from collections import defaultdict
from dataclasses import dataclass, asdict

# rest_clients = []
transactions = []
# fund = []
# local_storage = {}
# INITIALIZATION = "enter_initialize"
from aiolimiter import AsyncLimiter

FAUCET_URL = "http://127.0.0.1:8081"
NODE_URL = nodes[0] + "/v1"
# rest_client_top = RestClient(NODE_URL)
# faucet_client = FaucetClient(FAUCET_URL, rest_client_top)  # <:!:section_1

package_path = '/root/blockchains/aptos/aptos-core/aptos-move/move-examples'
# funcList = ["add_a", "add_b", "add_c", "add_d", "add_e", "add_f", "add_g", "add_h", "add_i", "add_j", "add_k", "add_l", "add_m", "add_n", "add_o", "add_p", "add_q", "add_r", "add_s", "add_t", "add_u", "add_v", "add_w", "add_x", "add_y", "add_z"]
funcList = ["modify_0", "modify_1", "modify_2", "modify_3", "modify_4", "modify_5", "modify_6"]


# funcList = ["greet"]

@dataclass
class Model:
    a: int = 0
    b: int = 0
    c: int = 0
    d: int = 0
    e: int = 0
    f: int = 0
    g: int = 0
    h: int = 0
    i: int = 0
    j: int = 0
    k: int = 0
    l: int = 0
    m: int = 0
    n: int = 0
    o: int = 0
    p: int = 0
    q: int = 0
    r: int = 0
    s: int = 0
    t: int = 0
    u: int = 0
    v: int = 0
    w: int = 0
    x: int = 0
    y: int = 0
    z: int = 0


# 初始化本地模型数据
local_models: Dict[str, Model] = defaultdict(Model)


class Accounts:
    source: Account
    senders: List[Account]
    receivers: List[Account]

    def __init__(self, source, senders):
        self.source = source
        self.senders = senders

    @staticmethod
    def generate(path: str, num_accounts: int) -> Accounts:
        source = Account.generate()
        source.store(f"{path}/source.txt")
        senders = []
        for idx in range(num_accounts):
            senders.append(Account.generate())
            senders[-1].store(f"{path}/sender_{idx}.txt")
        return Accounts(source, senders)

    @staticmethod
    def load(path: str, num_accounts: int) -> Accounts:
        source = Account.load(f"{path}/source.txt")
        senders = []
        for idx in range(num_accounts):
            senders.append(Account.load(f"{path}/sender_{idx}.txt"))
        return Accounts(source, senders)

async def fetch_model_via_getter(rest_client: RestClient, addr: str) -> Model:
    try:
        # 构建调用get_model函数的交易
        transaction_payload = EntryFunction.natural(
            f"0x{addr}::counter",
            "get_model",
            [],
            [TransactionArgument(str(addr), Serializer.address)]
        )
        txn = await rest_client.create_bcs_signed_transaction(
            sender=Account.generate(),  # 使用任意账户，因为是只读操作
            payload=TransactionPayload(transaction_payload)
        )
        txn_hash = await rest_client.submit_bcs_transaction(txn)
        await rest_client.wait_for_transaction(txn_hash)

        # 获取返回值
        response = await rest_client.view_function(
            f"0x{addr}::counter", "get_model", [addr]
        )

        # 假设返回的是一个字典，解析各字段
        data = response['data']
        model = Model(
            a=data.get('a', 0),
            b=data.get('b', 0),
            c=data.get('c', 0),
            d=data.get('d', 0),
            e=data.get('e', 0),
            f=data.get('f', 0),
            g=data.get('g', 0),
            h=data.get('h', 0),
            i=data.get('i', 0),
            j=data.get('j', 0),
            k=data.get('k', 0),
            l=data.get('l', 0),
            m=data.get('m', 0),
            n=data.get('n', 0),
            o=data.get('o', 0),
            p=data.get('p', 0),
            q=data.get('q', 0),
            r=data.get('r', 0),
            s=data.get('s', 0),
            t=data.get('t', 0),
            u=data.get('u', 0),
            v=data.get('v', 0),
            w=data.get('w', 0),
            x=data.get('x', 0),
            y=data.get('y', 0),
            z=data.get('z', 0),
        )
        return model
    except Exception as e:
        print(f"Error fetching Model via getter for account {addr}: {e}")
        return Model()


async def fetch_and_initialize_local_models(rest_client: RestClient, accounts: List[Account]):
    tasks = []
    for account in accounts:
        addr = account.address().hex()
        tasks.append(fetch_model_via_getter(rest_client, addr))

    models = await asyncio.gather(*tasks)

    for account, model in zip(accounts, models):
        addr = account.address().hex()
        local_models[addr] = model


async def compare_models(rest_client: RestClient, accounts: List[Account]):
    discrepancies = []
    tasks = []
    for account in accounts:
        addr = account.address().hex()
        tasks.append(fetch_model_via_getter(rest_client, addr))

    on_chain_models = await asyncio.gather(*tasks)

    for account, on_chain_model in zip(accounts, on_chain_models):
        addr = account.address().hex()
        local_model = local_models[addr]
        if asdict(on_chain_model) != asdict(local_model):
            discrepancies.append((addr, asdict(on_chain_model), asdict(local_model)))

    if discrepancies:
        print("Discrepancies found:")
        for addr, on_chain, local in discrepancies:
            print(f"Account: {addr}")
            print(f"On-chain: {on_chain}")
            print(f"Local: {local}")
            print("-----")
        with open("discrepancies.log", 'a', encoding='utf-8') as f:
            for addr, on_chain, local in discrepancies:
                f.write(f"Account: {addr}\nOn-chain: {on_chain}\nLocal: {local}\n-----\n")
    else:
        print("All local models match the on-chain data.")


async def transfer_transaction(
        client: RestClient,
        sender: Account,
        sequence_number: int,
        recipient: AccountAddress,
        amount: int,
):
    transaction_arguments = [
        TransactionArgument(recipient, Serializer.struct),
        TransactionArgument(amount, Serializer.u64),
    ]
    payload = EntryFunction.natural(
        "0x1::aptos_account",
        "transfer",
        [],
        transaction_arguments,
    )

    return await client.create_bcs_signed_transaction(
        sender, TransactionPayload(payload), sequence_number
    )


def generate_rest_client() -> RestClient:
    client_config = ClientConfig()
    client_config.http2 = True
    client_config.max_gas_amount = 100_000
    client_config.transaction_wait_in_seconds = 60
    return RestClient(NODE_URL, client_config)


async def fund_from_faucet(rest_client: RestClient, source: Account):
    faucet_client = FaucetClient(FAUCET_URL, rest_client)

    fund_txns = []
    fund_txns.append(faucet_client.fund_account(source.address(), 100_000_000_000_000))
    await asyncio.gather(*fund_txns)


async def distribute(
        rest_client: RestClient,
        source: Account,
        senders: List[Account],
        per_node_amount: int,
):
    all_accounts = list(map(lambda account: (account.address().hex(), True), senders))

    account_sequence_number = AccountSequenceNumber(rest_client, source.address())

    txns: List[Any] = []
    txn_hashes: List[str] = []

    for account, fund in all_accounts:
        sequence_number = await account_sequence_number.next_sequence_number(
            block=False
        )
        if sequence_number is None:
            txn_hashes.extend(await asyncio.gather(*txns))
            txns = []
            sequence_number = await account_sequence_number.next_sequence_number()
        assert sequence_number is not None
        amount = per_node_amount
        txn = await transfer_transaction(
            rest_client, source, sequence_number, AccountAddress.from_hex(account), amount
        )
        txns.append(rest_client.submit_bcs_transaction(txn))

    txn_hashes.extend(await asyncio.gather(*txns))
    for txn_hash in txn_hashes:
        await rest_client.wait_for_transaction(txn_hash)
    await account_sequence_number.synchronize()


async def deploy_package(acc: Account, package_name: str, rest_client: RestClient, args: str):
    print("Compiling package")
    addr = acc.address().hex()
    os.chdir(os.path.join(package_path, package_name))
    command = "aptos move compile " + args + " --save-metadata"
    print(command)
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


async def init_test_environment(rest_client: RestClient, source: Account):
    addr = str(source.address().hex())
    await deploy_package(source, 'counter', rest_client, "--named-addresses counter=" + addr)


async def init_objects(module_account: Account, client_accounts: List[Account], rest_client: RestClient):
    print("Creating all the initialization transactions and submit")
    transactions = []
    for sender in client_accounts:
        module_addr = module_account.address().hex()
        transaction = Transaction(sender, rest_client, module_addr, sender.address().hex(), "enter_initialize")
        txn_hash = await transaction.send_transaction()
        transactions.append(txn_hash)
    print("Waiting for all initialization to finish")
    cnt = 0
    for tx in transactions:
        cnt += 1
        await rest_client.wait_for_transaction(tx)
        print("{} time success".format(cnt))
    print("All initialization transactions finished!")


first_tps = 0


async def send_transactions(module_account: Account, client_accounts: List[Account], rest_client: RestClient, QPS: int,
                            index):
    global first_tps
    txns = []
    tasks = []
    limiter = AsyncLimiter(float(QPS), 1.0)
    start_time = time.time()
    transaction_list = []
    for sender in client_accounts:
        module_addr = module_account.address().hex()
        target_addr = random.choice(client_accounts).address().hex()
        func = random.choice(funcList)
        txn = Transaction(sender, rest_client, module_addr, target_addr, func)
        task = txn.send_transaction(limiter)
        transaction_list.append(txn)
        tasks.append(task)
    txn_hashes = await asyncio.gather(*tasks)
    print("All transactions sent!")
    end_time = time.time()
    cost = end_time - start_time
    tps = len(client_accounts) / cost
    tot_time = 0
    for ele in transaction_list:
        tot_time += ele.excuted_time
        # 确保事务已成功，才更新本地模型
        if ele.modifications:
            addr = ele.target_addr
            for field, delta in ele.modifications.items():
                setattr(local_models[addr], field, getattr(local_models[addr], field) + delta)
    latency = tot_time / len(client_accounts)
    if index == 0:
        first_tps = tps
    else:
        with open("discussion_tps.log", 'a', encoding='utf-8') as f:
            f.write(f"QPS:{QPS} time:{time.time()}, tps:{tps}, first_tps:{first_tps}\n")


async def start(account_num: int):
    rest_client = generate_rest_client()
    print("Starting...")
    print("Create source")
    all_accounts = Accounts.load("nodes", account_num)
    # all_accounts = Accounts.generate("nodes", account_num)
    accounts = all_accounts.senders
    source = all_accounts.source
    # if True:
    #     tasks = [init_test_environment(rest_client, source)]
    #     results = await asyncio.gather(*tasks)  # Concurrently execute tasks and gather results
    #     print(results)  # Print results
    # if True:
    #     await fund_from_faucet(rest_client, source)

    #     tasks = [init_test_environment(rest_client, source)]
    #     results = await asyncio.gather(*tasks)  # Concurrently execute tasks and gather results
    #     print(results)  # Print results

    #     balance = await rest_client.account_balance(source.address())
    #     amount = int(balance * 0.8 / account_num)
    #     print("start distribute")
    #     await distribute(rest_client, source, accounts, amount)
    #     print("end distribute")

    # 初始化对象
    print("init objects for client accounts")
    # init objects for client accounts
    await init_objects(source, accounts, rest_client)

    # QPSList = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
    # for i in range(10, 100):
    #     for QPS in QPSList:
    #         await send_transactions(source,  accounts, rest_client, QPS, i)
    #         print("All transactions in round {} finished!".format(QPS))

    # 初始化本地模型数据
    await fetch_and_initialize_local_models(rest_client, accounts)
    print("Local models initialized.")

    print("Send Transactions")
    for i in range(10000):
        await send_transactions(source, accounts, rest_client, 30, i)
        # 每轮测试后进行差分比较
        await compare_models(rest_client, accounts)
        print(f"Round {i} comparison completed.")

    await rest_client.close()
    # await asyncio.gather(
    #     parallel_vector_add(target_address, accounts),
    #     parallel_deposit(target_address, accounts)
    # )


if __name__ == "__main__":
    # account_address = AccountAddress.from_str(local_addr)
    # reflesh_network()
    # print("Network is refreshed!")
    # run_node_crash()
    # print()
    # asyncio.run(init_test_environment(2))

    # process.start()
    asyncio.run(start(600))
    # asyncio.run(check_deposit_oracle(account_address, "0x359f3a53c280860f2cfc9dafb3e6caf541138ac11e5251a2f8f3017dbfa9467c::test_object_conflict::TargetObject"))
