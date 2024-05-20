#!/bin/bash

sleep 100
while true; do
    sleep $((RANDOM % 100))  # 随机等待时间
    nodeid=$((RANDOM % 4))  # 随机选择nodeid

    # 关闭节点
    ps aux | grep "${nodeid}/node.yaml" | grep -v grep | awk '{print $2}' | xargs kill -9
    echo "Notice!!! Down Node ${nodeid}"

    sleep $((RANDOM % 100))  # 随机等待时间

    echo "Notice!!! Restart Node ${nodeid}"

    # 重新启动节点，将输出追加到日志文件
    /root/blockchains/aptos/aptos-core/target/debug/aptos-node -f "/tmp/.tmpweCuRe/${nodeid}/node.yaml" >> "/tmp/.tmpweCuRe/${nodeid}/log" 2>&1 &

    sleep 2  # 给一些时间确保子进程已经启动
done