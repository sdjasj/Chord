import os
import fileinput

def delete_lines_except_last(filename):
    lines = []
    with open(filename, 'r') as file:
        lines = file.readlines()
    with open(filename, 'w') as file:
        file.write(lines[-1])

def delete_lines_except_last_in_directory(directory):
    for root, _, files in os.walk(directory):
        for file in files:
            filepath = os.path.join(root, file)
            delete_lines_except_last(filepath)

# 要遍历的目录
directory_path = './nodes'

# 删除目录下所有文件的除了最后一行之外的内容
delete_lines_except_last_in_directory(directory_path)
