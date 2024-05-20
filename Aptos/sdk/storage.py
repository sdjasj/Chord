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
from typing import List

class Resource:

    def __init__(self, target_addr: AccountAddress):
        self.target_addr = target_addr
        self.vec = []
        self.counter = 1
    
    def add_counter(self):
        self.counter += 1
    
    def add_vector(self):
        self.vec.append(0)
    
    def pop_back_vector(self):
        self.vec.pop()
    
    def get_counter(self):
        return self.counter

    def get_vector(self):
        return self.vec


class Storage:

    def __init__(self, sender: Account, target_addresses: List[Account]):
        self.sender = sender
        self.resources = {}
        for target in target_addresses:
            res = Resource(target.account_address)
            self.resources[str(target.account_address)] = res