package org.fisco.bcos.sdk.demo.fuzz;

import org.fisco.bcos.sdk.v3.contract.precompiled.sharding.ShardingService;
import org.fisco.bcos.sdk.v3.transaction.model.exception.ContractException;

import java.util.Random;

public class ShardingUtil {
    public static ShardingService shardingService;
    public static int shardNum;
    public static Random random = new Random(System.currentTimeMillis());

    public static void linkShard(String address) throws ContractException {
        int idx = random.nextInt(shardNum);
        String shardName = "testShard" + idx;
        shardingService.linkShard(shardName, address);
//        System.out.println(
//                "====== ShardingOk ParaTestTwo, deploy success to shard: "
//                        + shardName
//                        + ", address: "
//                        + address);
    }
}