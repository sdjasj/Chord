async def get_deposit_count(target_address: AccountAddress):
    ret = await rest_client_top.account_resource(target_address, "0x359f3a53c280860f2cfc9dafb3e6caf541138ac11e5251a2f8f3017dbfa9467c::test_object_conflict::TargetObject")
    return int(ret['data']['counter'])

async def get_vector(target_address: AccountAddress):
    ret = await rest_client_top.account_resource(target_address, "0x359f3a53c280860f2cfc9dafb3e6caf541138ac11e5251a2f8f3017dbfa9467c::test_object_conflict::Vec")
    print(ret['data']['vec'])
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