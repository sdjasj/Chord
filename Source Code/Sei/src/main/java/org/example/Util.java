package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Util {
    public static Random random = new Random(System.currentTimeMillis());
    public static HashSet<String> stringHashSet = new HashSet<>();

    public static void appendToFile(String filePath, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String executeCommand(String command, String workingDirectory) throws IOException {

        try {
            // 创建ProcessBuilder对象
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);
            if (workingDirectory != null) {
                processBuilder.directory(new File(workingDirectory));
            }

            // 分别重定向标准输出流和错误输出流
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);

            // 启动进程
            Process process = processBuilder.start();

            // 获取进程的输入流，这里包含了标准输出
            InputStream inputStream = process.getInputStream();
            // 获取进程的错误输入流
            InputStream errorStream = process.getErrorStream();

            // 读取标准输出流
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            StringBuilder output = new StringBuilder();
            while ((inputLine = inputReader.readLine()) != null) {
                output.append(inputLine);
            }

            // 读取错误输出流
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                System.out.println("Error Output: " + errorLine);
            }

            // 等待进程执行完毕
            int exitCode = process.waitFor();

            // 关闭资源
            inputReader.close();
            errorReader.close();
            inputStream.close();
            errorStream.close();

            if (exitCode != 0) {
                Fuzzer.logger.info("Command execution failed with exit code " + exitCode);
            }
            return output.toString();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String executeCommand(String command) throws IOException {
//        System.out.println(command);

        try {
            // 创建ProcessBuilder对象
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);

            // 分别重定向标准输出流和错误输出流
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);

            // 启动进程
            Process process = processBuilder.start();

            // 获取进程的输入流，这里包含了标准输出
            InputStream inputStream = process.getInputStream();
            // 获取进程的错误输入流
            InputStream errorStream = process.getErrorStream();

            // 读取标准输出流
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            StringBuilder output = new StringBuilder();
            while ((inputLine = inputReader.readLine()) != null) {
                output.append(inputLine);
            }

            // 读取错误输出流
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
//            String errorLine;
//            StringBuilder errorOut = new StringBuilder();
//            while ((errorLine = errorReader.readLine()) != null) {
//                errorOut.append(errorLine);
//
//            }
//            String t = errorOut.toString();
//            if (!t.isEmpty()) {
//                if (!t.contains("code: -32000, message: not found, data: None") &&
//                        !t.contains("tx not found, might have been dropped from mempool") &&
//                        !t.contains("revert inject")) {
//                    System.out.println(command);
//                    System.out.println("Error Output: " + errorOut);
//                }
//            }

            // 等待进程执行完毕
            int exitCode = process.waitFor();

            // 关闭资源
            inputReader.close();
            errorReader.close();
            inputStream.close();
            errorStream.close();

            if (exitCode != 0) {
//                Fuzzer.logger.info("Command execution failed with exit code " + exitCode);
//                System.out.println(output);
//                throw new IOException("Command execution failed with exit code " + exitCode);
            }
            return output.toString();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String executeCommandWithoutErrorOutput(String command) throws IOException {
//        System.out.println(command);

        try {
            // 创建ProcessBuilder对象
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", command);

            // 分别重定向标准输出流和错误输出流
            processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
            processBuilder.redirectError(ProcessBuilder.Redirect.PIPE);

            // 启动进程
            Process process = processBuilder.start();

            // 获取进程的输入流，这里包含了标准输出
            InputStream inputStream = process.getInputStream();
            // 获取进程的错误输入流
            InputStream errorStream = process.getErrorStream();

            // 读取标准输出流
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(inputStream));
            String inputLine;
            StringBuilder output = new StringBuilder();
            while ((inputLine = inputReader.readLine()) != null) {
                output.append(inputLine);
            }

            // 读取错误输出流
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));

            // 等待进程执行完毕
            int exitCode = process.waitFor();

            // 关闭资源
            inputReader.close();
            errorReader.close();
            inputStream.close();
            errorStream.close();

            if (exitCode != 0) {
//                Fuzzer.logger.info("Command execution failed with exit code " + exitCode);
//                System.out.println(output);
//                throw new IOException("Command execution failed with exit code " + exitCode);
            }
            return output.toString();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<String> readContractAddress(String path) {
        File file = new File(path);
        File parentDirectory = file.getParentFile();

        if (!parentDirectory.exists()) {
            boolean created = parentDirectory.mkdirs();
            if (created) {
                System.out.println("父目录已创建: " + parentDirectory.getAbsolutePath());
            } else {
                System.out.println("无法创建父目录: " + parentDirectory.getAbsolutePath());
                return null;
            }
        }

        if (!file.exists()) {
            try {
                boolean created = file.createNewFile();
                if (created) {
                    System.out.println("文件已创建: " + file.getAbsolutePath());
                } else {
                    System.out.println("无法创建文件: " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                System.out.println("发生异常: " + e.getMessage());
            }
        }

        ArrayList<String> deployedAddress = new ArrayList<>();

        // 使用try-with-resources语句确保在完成后关闭文件流
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            // 逐行读取文件内容并输出到控制台
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    deployedAddress.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.out.println("读取文件时发生异常: " + e.getMessage());
        }
        return deployedAddress;
    }

    public static void writeContractAddress(String address, String file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            // 写入文件内容
            writer.write(address.trim() + "\n");

        } catch (IOException e) {
            System.out.println("写入文件时发生异常: " + e.getMessage());
        }
    }

    public static boolean execTransaction(String cmd) throws IOException {
        String res = Util.executeCommand(cmd);
        ObjectMapper objectMapper = new ObjectMapper();
        String tmp;
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(res);
            tmp = jsonNode.get("status").asText();
        } catch (NullPointerException e) {
            return false;
        }

        if (tmp.startsWith("0x")) {
            int status = Integer.parseInt(tmp.substring(2), 16);
            if (Constant.DEBUG && status != 1) {
                System.out.println("status is   " + jsonNode.get("status").asText());
                System.out.println(cmd);
            }
            return status == 1;
        }
//        System.out.println("error status:   " + tmp);
        return false;
    }

    public static synchronized String getRandomString() {
        // 定义随机字符串的长度
        int length = 10;

        // 可能包含的字符集合
        String characterSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


        // 创建 StringBuilder 对象用于构建随机字符串
        StringBuilder sb = new StringBuilder();

        // 生成随机字符串
        while (true) {
            for (int i = 0; i < length; i++) {
                // 从字符集合中随机选择一个字符，并追加到 StringBuilder 中
                int index = random.nextInt(characterSet.length());
                sb.append(characterSet.charAt(index));
            }
            if (stringHashSet.contains(sb.toString())) {
                sb = new StringBuilder();
            } else {
                stringHashSet.add(sb.toString());
                break;
            }
        }

        // 打印随机生成的字符串
        return sb.toString();
    }

    public static <T extends Serializable> T deepCopy(T object) {
        try {
            // 将对象序列化为字节数组
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(object);
            out.flush();

            // 从字节数组中反序列化对象
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);
            T copiedObject = (T) in.readObject();

            // 关闭流
            out.close();
            in.close();

            return copiedObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
