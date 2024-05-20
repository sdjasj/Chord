#!/bin/bash

for nodeid in {0..3}
do
    pid=$(ps aux | grep "${nodeid}/node.yaml" | grep -v grep | awk '{print $2}')
    
    if [ -n "$pid" ]; then
        # 找到进程ID，执行kill操作
        echo "Found process with PID $pid"
        # kill -9 "$pid"
    else
        # 未找到进程ID，执行启动操作
        echo "No existing process found for node $nodeid"
        # 启动新的进程
        echo "Start node $nodeid"
        /root/blockchains/aptos/aptos-core/target/debug/aptos-node -f "/tmp/.tmpweCuRe/${nodeid}/node.yaml" >> "/tmp/.tmpweCuRe/${nodeid}/log" 2>&1 &
    fi
done