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
from Chord.config import *
import time


class Transaction:

    def __init__(self, sender: Account, rest_client: RestClient, module_addr: AccountAddress, target_addr: AccountAddress, func: str):
        self.sender = sender
        self.rest_client = rest_client
        self.module_addr = module_addr
        self.target_addr = target_addr
        self.func = func
        self.module = "counter"
        self.tx_hash = ""
        self.set_args()
        self.excuted_time = 0
        self.modifications = {}

    def set_args(self):
        if self.func == "enter_initialize":
            self.args = []
        elif self.func[:3] == "mod":
            self.args = [TransactionArgument(self.target_addr, Serializer.struct), TransactionArgument(random.randint(1, 100), Serializer.u64)]
        else: 
            self.args = []

    

    async def send_transaction_with_payload(self, payload):
        signed_transaction = await self.rest_client.create_bcs_signed_transaction(
            self.sender, TransactionPayload(payload)
        )
        txn_hash = await self.rest_client.submit_bcs_transaction(signed_transaction)
        # print(txn_hash)
        return txn_hash

    async def send_transaction(self, limiter=None):
        async with limiter:
            start = time.time()
            # 构建并发送事务
            transaction_payload = EntryFunction.natural(
                f"0x{self.module_addr}::counter",
                self.func,
                [],
                [TransactionArgument(str(self.target_addr), Serializer.address)]
            )
            txn = await self.rest_client.create_bcs_signed_transaction(
                self.sender, TransactionPayload(transaction_payload)
            )
            txn_hash = await self.rest_client.submit_bcs_transaction(txn)
            await self.rest_client.wait_for_transaction(txn_hash)
            end = time.time()
            self.excuted_time = end - start
            self.modifications = determine_modifications(self.func, delta=1)  # 根据需要调整 delta
            return txn_hash

    def get_modifications(self):
        return self.modifications

def determine_modifications(func: str, delta: int = 1) -> Dict[str, int]:
    modify_mapping = {
        "modify_0": ["s", "k", "p", "r", "z", "b"],
        "modify_1": ["i", "f", "w", "a", "o", "q"],
        "modify_2": ["k", "n", "l", "e", "x", "l"],
        "modify_3": ["c", "h", "w", "c", "x", "u"],
        "modify_4": ["g", "e", "m", "o", "z", "h"],
        "modify_5": ["c", "f", "p", "h", "e", "w"],
        "modify_6": ["q", "j", "d", "t", "l", "y"],
        # 添加更多的 modify_* 映射
    }
    fields = modify_mapping.get(func, [])
    modifications = {field: delta for field in fields}
    return modifications


# class Transaction:

#     def __init__(self, sender: Account, rest_client: RestClient, module_addr: AccountAddress, target_addr: AccountAddress, module: Modules, func: Functions):
#         self.sender = sender
#         self.rest_client = rest_client
#         self.module_addr = module_addr
#         self.target_addr = target_addr
#         self.object = object
#         self.func = func.value
#         self.module = module.value
#         self.set_args()

#     def set_args(self):
#         if self.func == Functions.INITIALIZATION:
#             self.args = []
#         elif self.func == "add_vector":
#             self.args = [TransactionArgument(self.target_addr, Serializer.struct), TransactionArgument(0, Serializer.u64)]
#         elif self.func == "pop_back_vector":
#             self.args = [TransactionArgument(self.target_addr, Serializer.struct)]
#         else: 
#             self.args = []

    

#     async def send_transaction_with_payload(self, payload):
#         signed_transaction = await self.rest_client.create_bcs_signed_transaction(
#             self.sender, TransactionPayload(payload)
#         )
#         txn_hash = await self.rest_client.submit_bcs_transaction(signed_transaction)
#         # print(txn_hash)
#         return txn_hash

#     async def send_transaction(self):
#         payload = EntryFunction.natural(
#             f"{self.module_addr}::{self.module}",
#             self.func,
#             [],
#             self.args,
#         )
#         txn_hash = await self.send_transaction_with_payload(payload)
#         return txn_hash