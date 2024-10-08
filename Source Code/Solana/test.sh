#!/bin/bash

# 无限循环
while true; do
  # 运行 npm run test 命令
  npm run test

  # 检查命令退出状态码
  if [ $? -ne 0 ]; then
    echo "测试失败，重新运行测试..."
  else
    echo "测试成功，重新运行测试..."
  fi

  # 添加适当的延迟以防止过载，视情况而定，可以调整或移除
  sleep 1
done