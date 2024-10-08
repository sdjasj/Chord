#!/bin/bash

# 定义测试命令
TEST_COMMAND="npm run test"

# 设置最大尝试次数
MAX_RETRIES=3000
retries=0

# 循环执行测试命令，直到成功或达到最大尝试次数
while [ $retries -lt $MAX_RETRIES ]; do
  # 执行测试命令
  $TEST_COMMAND

  # 检查执行结果
  if [ $? -eq 0 ]; then
    # 如果执行成功，则退出循环
    echo "测试成功"
    exit 0
  else
    # 如果执行失败，则增加重试次数，并等待一段时间后继续重试
    echo "测试失败，正在重新尝试..."
    retries=$((retries + 1))
    sleep 5
  fi
done

# 如果达到最大尝试次数仍然失败，则输出错误信息并退出
echo "测试失败次数达到最大尝试次数"
exit 1
