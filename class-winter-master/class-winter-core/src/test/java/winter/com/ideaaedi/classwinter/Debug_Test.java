package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.PathUtil;

/**
 * 测试debug
 * <p>
 *     debug: 是否开启debug模式
 * </p>
 *
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
public class Debug_Test {
    
    public static void main(String[] args) {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        
//        test0();
        test1();
    
    }
    
    /**
     * 不开启debug(默认即为不开启)
     * <p>
     *     对照{@link Debug_Test#test1()}
     * </p>
     */
    private static void test0() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        // 加密
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
        
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
        
        
        String encryptedJar = originJarOrWar.replace(".jar", "-encrypted.jar");
        
        // 解密
        String javaagentArgs = "";
        // String javaagentArgs = "=debug=true";
        // String javaagentArgs = "=debug=true,password=xxx";
        BashUtil.runBashAndPrint(String.format("java -javaagent:%s%s -jar %s", encryptedJar, javaagentArgs, encryptedJar));
    }
    
    /**
     * 开启debug
     * <p>
     *     对照{@link Debug_Test#test0()}
     * </p>
     */
    private static void test1() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " debug=" + true
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
        
        
        
        String encryptedJar = originJarOrWar.replace(".jar", "-encrypted.jar");
        
        // 解密
        // String javaagentArgs = "";
        String javaagentArgs = "=debug=true";
        // String javaagentArgs = "=debug=true,password=xxx";
        BashUtil.runBashAndPrint(String.format("java -javaagent:%s%s -jar %s", encryptedJar, javaagentArgs, encryptedJar));
    }
}
