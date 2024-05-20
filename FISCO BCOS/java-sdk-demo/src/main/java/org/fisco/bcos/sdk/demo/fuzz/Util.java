package org.fisco.bcos.sdk.demo.fuzz;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

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

    public static String toChecksumAddress(String address) {
        // Convert the address to lowercase
        address = address.toLowerCase().replace("0x", "");
        address = address.toLowerCase();

        // Calculate the hash of the address
        byte[] addressHash = calculateHash(address.getBytes());

        // Convert the address hash to a hexadecimal string
        String addressHashHex = Hex.toHexString(addressHash);

        // Construct the checksum address
        StringBuilder checksumAddress = new StringBuilder("0x");
        for (int i = 0; i < address.length(); i++) {
            // If the corresponding character in the address is a letter (not a digit)
            if (Character.isLetter(address.charAt(i))) {
                // If the ith character of the addressHash is greater than or equal to '8', the corresponding
                // character in the checksum address should be uppercase.
                if (Character.digit(addressHashHex.charAt(i), 16) >= 8) {
                    checksumAddress.append(Character.toUpperCase(address.charAt(i)));
                } else {
                    checksumAddress.append(address.charAt(i));
                }
            } else {
                checksumAddress.append(address.charAt(i));
            }
        }

        return checksumAddress.toString();
    }

    // Calculate the Keccak-256 hash of the input data
    public static byte[] calculateHash(byte[] data) {
        Keccak.Digest256 digest256 = new Keccak.Digest256();
        digest256.update(data);
        return digest256.digest();
    }


    public static void convertSol2Java() {
        try {
            executeCommand("java -cp \"apps/*:lib/*:conf/\" org.fisco.bcos.sdk.demo.codegen.DemoSolcToJava org.fisco.bcos.sdk.demo.contract"
                    , "/root/java-sdk-demo/dist");
        } catch (IOException e) {
            throw new RuntimeException(e);
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
            String errorLine;
            StringBuilder errorOut = new StringBuilder();
            while ((errorLine = errorReader.readLine()) != null) {
                errorOut.append(errorLine);

            }
            String t = errorOut.toString();
            if (!t.isEmpty()) {
                if (!t.contains("code: -32000, message: not found, data: None") &&
                        !t.contains("tx not found, might have been dropped from mempool") &&
                        !t.contains("revert inject")) {
                    System.out.println(command);
                    System.out.println("Error Output: " + errorOut);
                }
            }

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
        System.out.println("error status:   " + tmp);
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
                if (i == 0) {
                    index = random.nextInt(26);
                }
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

    // 编译源代码并加载生成的类
    public static Class<?> compileAndLoadClass(String className, String sourceCodePath) throws IOException, ClassNotFoundException {
//        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
//        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
//        String fileStored = sourceCodePath + "/" + className + ".java";
//        File sourceFile = new File(fileStored);
//
//        // 设置编译后的.class文件输出位置
//        String outputPath = "../build/classes/java/main/"; // 设置输出路径
//        Iterable<String> options = Arrays.asList("-d", outputPath);
//
//        // 编译Java源文件
//        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFile);
//        compiler.getTask(null, fileManager, null, options, null, compilationUnits).call();
//
//        fileManager.close();
        String fileStored = sourceCodePath + "/" + className + ".java";
        File sourceFile = new File(fileStored);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        // This sets up the class path that the compiler will use.
        // I've added the .jar file that contains the DoStuff interface within in it...
        List<String> optionList = new ArrayList<String>();
        optionList.add("-classpath");
        optionList.add(System.getProperty("java.class.path") + File.pathSeparator + "conf/:"+
                System.getProperty("java.class.path") + File.pathSeparator + "lib/*:"+
                System.getProperty("java.class.path") + File.pathSeparator + "apps/*:");

        Iterable<? extends JavaFileObject> compilationUnit
                = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                optionList,
                null,
                compilationUnit);
        /*** Compilation Requirements **/
        if (task.call()) {
            /** Load and execute **/
            System.out.println("loading " + fileStored);
            // Create a new custom class loader, pointing to the directory that contains the compiled
            // classes, this should point to the top of the package structure!
            URLClassLoader classLoader = new URLClassLoader(new URL[]{new File("/root/java-sdk-demo/dist/dynamic").toURI().toURL()});
            // Load the class from the classloader by name....
            Class<?> loadedClass = classLoader.loadClass("org.fisco.bcos.sdk.demo.contract." + className);
            return loadedClass;
        }
        System.out.println("load error......");
        return null;
    }
}