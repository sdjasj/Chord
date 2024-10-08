#!/bin/bash

# 设置要添加到PATH的路径
aptos_path="/root/blockchains/aptos/aptos-core/target/debug"

# 检查路径是否存在
if [ -d "$aptos_path" ]; then
    # 将路径添加到PATH中
    export PATH="$aptos_path:$PATH"
    echo "success"
else
    echo "failed"
fi