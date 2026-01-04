package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.PathUtil;

/**
 * 测试password
 * <p>
 *     password: 非必填
 * </p>
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
public class Password_Test {
    
    public static void main(String[] args) {
        passwordTest();
        // passwordFromFileTest();
        
        // 需要在linux上进一步验证
        // passwordFromShellTest();
    }
    
    /**
     * 测试password
     */
    public static void passwordTest() {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
        String password = "pwd123";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " password=" + password
                + " debug=" + true
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
    
        String encryptedJar = originJarOrWar.replace(".jar", "-encrypted.jar");
    
        // 解密
        // String javaagentArgs = "";
        // String javaagentArgs = "=debug=true";
        String javaagentArgs = "=debug=true,password=" + password;
        BashUtil.runBashAndPrint(String.format("java -javaagent:%s%s -jar %s", encryptedJar, javaagentArgs, encryptedJar));
    }
    
    /**
     * 测试 passwordFromFile
     */
    public static void passwordFromFileTest() {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
        String password = "pwd123";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " password=" + password
                + " debug=" + true
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
    
        String encryptedJar = originJarOrWar.replace(".jar", "-encrypted.jar");
    
        // 解密
        // String javaagentArgs = "";
        // String javaagentArgs = "=debug=true";
        String filepath = projectRootDir + "password.txt";
        String javaagentArgs = "=debug=true,passwordFromFile=" + filepath;
        BashUtil.runBashAndPrint(String.format("java -javaagent:%s%s -jar %s", encryptedJar, javaagentArgs, encryptedJar));
    }
    
    /**
     * 测试passwordFromShell (在linux上解密执行)
     */
    public static void passwordFromShellTest() {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
        String password = "pwd123";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " password=" + password
                + " debug=" + true
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
        // 解密
        /// String javaagentArgs = "";
        /// String javaagentArgs = "=debug=true";
        /*
         * shell脚本内容简单示例：
         *
         * #!/bin/bash
         * echo "pwd123"
         */
        String shellFilePath = "/pwd.shell";
        String javaagentArgs = "=debug=true,passwordFromShell=" + shellFilePath;
        /*
         * 验证下面的启动指令前，需要将对应的pwd.shell、boot-jar-encrypted.jar文件放进linux的/目录下
         */
        System.err.println("请在linux上执行并进一步验证：" + String.format("java -javaagent:%s%s -jar %s", "boot-jar-encrypted.jar", javaagentArgs, "boot-jar-encrypted.jar"));
    }
}
