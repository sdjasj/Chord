from enum import Enum
node0 = "http://127.0.0.1:38817"
node1 = "http://127.0.0.1:42071"
node2 = "http://127.0.0.1:33351"
node3 = "http://127.0.0.1:41951"
nodes = [node0, node1, node2, node3]
modules_account_files = "/root/blockchains/aptos/sdk/modules.json"
senders_account_files = "/root/blockchains/aptos/sdk/senders.json"

class Functions(Enum):
    INITIALIZATION = "enter_initialize"
    APPENDVECTOR = "add_vector"
    POPVECTOR = "pop_back_vector"

class Modules(Enum):
    BASE = "test_parallel"
    OUTSIDER = "test_share"