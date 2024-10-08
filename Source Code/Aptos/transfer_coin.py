# Copyright © Aptos Foundation
# SPDX-License-Identifier: Apache-2.0
from __future__ import annotations

from aptos_sdk.account import Account
from aptos_sdk.account_address import AccountAddress
from aptos_sdk.bcs import Serializer
from Chord.Transaction import Transaction
from Chord.config import *
from Chord.storage import *
import time
from aptos_sdk.async_client import FaucetClient, RestClient
from aptos_sdk.account_sequence_number import AccountSequenceNumber
from aptos_sdk.transactions import (
    EntryFunction,
    SignedTransaction,
    TransactionArgument,
    TransactionPayload,
)

from aiolimiter import AsyncLimiter

FAUCET_URL = "http://127.0.0.1:8081"
NODE_URL = nodes[0] + "/v1"
# :!:>section_1
rest_client = RestClient(NODE_URL)
faucet_client = FaucetClient(FAUCET_URL, rest_client)  # <:!:section_1

class Accounts:
    source: Account
    senders: List[Account]
    receivers: List[Account]

    def __init__(self, source, senders, receivers):
        self.source = source
        self.senders = senders
        self.receivers = receivers

    @staticmethod
    def generate(path: str, num_accounts: int) -> Accounts:
        source = Account.generate()
        source.store(f"{path}/source.txt")
        senders = []
        receivers = []
        for idx in range(num_accounts):
            senders.append(Account.generate())
            senders[-1].store(f"{path}/sender_{idx}.txt")
            receivers.append(Account.generate())
        return Accounts(source, senders, receivers)

    @staticmethod
    def load(path: str, num_accounts: int) -> Accounts:
        source = Account.load(f"{path}/source.txt")
        senders = []
        receivers = []
        for idx in range(num_accounts):
            receivers.append(Account.generate())
            senders.append(Account.load(f"{path}/sender_{idx}.txt"))
        return Accounts(source, senders, receivers)
# all_accounts = Accounts.load("nodes", 3000)
all_accounts = None
# alice = Account.generate()
# bob = Account.generate()  # <:!:section_2

# print("\n=== Addresses ===")
# print(f"Alice: {alice.address()}")
# print(f"Bob: {bob.address()}")

# tot_time = 0
TX_COUNT = 3000
# account_sequence_number = AccountSequenceNumber(rest_client, alice.address())

async def fund_from_faucet(rest_client: RestClient, source: Account):
    faucet_client = FaucetClient(FAUCET_URL, rest_client)

    fund_txns = []
    fund_txns.append(faucet_client.fund_account(source.address(), 100_000_000_000_000))
    await asyncio.gather(*fund_txns)

async def transfer_transaction(
    client: RestClient,
    sender: Account,
    sequence_number: int,
    recipient: AccountAddress,
    amount: int,
) -> SignedTransaction:
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

async def distribute(
    rest_client: RestClient,
    source: Account,
    senders: List[Account],
    per_node_amount: int,
):
    all_accounts = list(map(lambda account: (account.address(), True), senders))

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
            rest_client, source, sequence_number, account, amount
        )
        txns.append(rest_client.submit_bcs_transaction(txn))

    txn_hashes.extend(await asyncio.gather(*txns))
    for txn_hash in txn_hashes:
        await rest_client.wait_for_transaction(txn_hash)
    await account_sequence_number.synchronize()

class Transaction:
    def __init__(self, limiter, sender, receiver):
        self.limiter = limiter
        self.sender = sender
        self.receiver = receiver
    
    async def send_transaction(self):
        async with self.limiter:
            self.start_time = time.time()
            txn_hash = await rest_client.transfer(self.sender, self.receiver, 10)  # <:!:section_5
            # :!:>section_6
            await rest_client.wait_for_transaction(txn_hash)  # <:!:section_6
            self.end_time = time.time()
        

# async def send_transaction(limiter, sender, receiver):
#     global tot_time
#     async with limiter:
#         start_time = time.time()
#         txn_hash = await rest_client.transfer(sender, receiver, 10)  # <:!:section_5
#         # :!:>section_6
#         await rest_client.wait_for_transaction(txn_hash)  # <:!:section_6
#         end_time = time.time()
#         tot_time += end_time - start_time

async def send_transactions(QPS: int, index: int):
    # 每一个account发一个transaction
    tasks = []
    limiter = AsyncLimiter(float(QPS), 1.0)
    start_time = time.time()
    txn = []
    for i in range(TX_COUNT):
        tx = Transaction(limiter, all_accounts.senders[i], all_accounts.receivers[i].address())
        txn.append(tx)
        task = tx.send_transaction()
        tasks.append(task)
    await asyncio.gather(*tasks)
    print("All transactions sent!")
    end_time = time.time()
    cost = end_time - start_time
    tps = TX_COUNT / cost
    tot_time = 0
    for tx in txn:
        tot_time += tx.end_time - tx.start_time
    latency = tot_time / TX_COUNT
    with open("re_re_stress_tps{}.log".format(index), 'a', encoding='utf-8') as f:
        f.write("QPS:{} time:{}, tps:{}, latency:{}\n".format(QPS, time.time(), tps, latency))


async def main():
    # :!:>section_3
    # alice_fund = faucet_client.fund_account(alice.address(), 100_000_000_000)
    
    # Have Alice give Bob 1_000 coins
    # :!:>section_5
    # all_accounts = Accounts.load("nodes", account_num)
    global all_accounts
    all_accounts = Accounts.generate("nodes", TX_COUNT)
    accounts = all_accounts.senders
    source = all_accounts.source
    if True:
        await fund_from_faucet(rest_client, source)


        balance = await rest_client.account_balance(source.address())
        amount = int(balance * 0.8 / TX_COUNT)
        print("start distribute")
        await distribute(rest_client, source, accounts, amount)
        print("end distribute")
    QPSList = [10, 20, 30, 40, 50, 60, 70, 80, 90, 100]
    for i in range(7, 100):
        for QPS in QPSList:
            await send_transactions(QPS, i)


    # Have Alice give Bob another 1_000 coins using BCS


    await rest_client.close()


if __name__ == "__main__":
    asyncio.run(main())