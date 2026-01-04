package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.PathUtil;

/**
 * 测试jvmArgCheck
 * <p>
 *     jvmArgCheck: 在启动混淆包时，检查是否输入有(在加密时)指定的jvm参数
 * </p>
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
public class JvmArgCheck_Test {
    
    public static void main(String[] args) {
//        jvmCheckJarTestFail();
//        jvmCheckJarTestSuccess();
        jvmCheckJarOneTest();
    }
    
    /**
     * jar测试 - 失败测试
     */
    public static void jvmCheckJarTestFail() {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
    
        String jvmArg = "-XX:+DisableAttachMechanism,-Xms1024M";
        
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " jvmArgCheck=" + jvmArg
                + " debug=true"
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
    
        String encryptedJar = originJarOrWar.replace(".jar", "-encrypted.jar");
    
        // 解密
        String javaagentArgs = "";
        // String javaagentArgs = "=debug=true";
        // String javaagentArgs = "=debug=true,password=xxx";
        BashUtil.runBashAndPrint(String.format("java -javaagent:%s%s=debug=true -XX:+DisableAttachMechanism -Xms1022M -jar %s", encryptedJar, javaagentArgs, encryptedJar));
    }
    
    /**
     * jar测试 - 成功测试
     */
    public static void jvmCheckJarTestSuccess() {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
    
        String jvmArg = "-XX:+DisableAttachMechanism,-Xms1024M";
        
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " jvmArgCheck=" + jvmArg
                + " debug=true"
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
    
        String encryptedJar = originJarOrWar.replace(".jar", "-encrypted.jar");
    
        // 解密
        String javaagentArgs = "";
        // String javaagentArgs = "=debug=true";
        // String javaagentArgs = "=debug=true,password=xxx";
        BashUtil.runBashAndPrint(String.format("java -javaagent:%s%s=debug=true -XX:+DisableAttachMechanism -Xms1024m -jar %s", encryptedJar, javaagentArgs, encryptedJar));
    }
    
    /**
     * jar测试 - 只指定指定一个
     */
    public static void jvmCheckJarOneTest() {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
    
        String jvmArg = "-XX:+DisableAttachMechanism";
        
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " jvmArgCheck=" + jvmArg
                + " debug=true"
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
    
        String encryptedJar = originJarOrWar.replace(".jar", "-encrypted.jar");
    
        // 解密
        String javaagentArgs = "";
        // String javaagentArgs = "=debug=true";
        // String javaagentArgs = "=debug=true,password=xxx";
        BashUtil.runBashAndPrint(String.format("java -javaagent:%s%s=debug=true -XX:+DisableAttachMechanism -jar %s", encryptedJar, javaagentArgs, encryptedJar));
    }
}
