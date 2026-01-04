package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.PathUtil;

/**
 * 测试excludePrefix
 * <p>
 *     excludePrefix: 对includePrefix定位出来的加密范围，进行排除
 * </p>
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
public class ExcludePrefix_Test {
    
    public static void main(String[] args) {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
//        test0();
        test1();
    }
    
    /**
     * case：
     *     根据前缀匹配，以com.aspire.ssm.util.AesEncryptDecryptUtil和com.aspire.ssm.util.DbSecurityBeanUtil为前缀的类都会被加密
     * 观察输入日志:
     *     2021-06-13 15:54:32 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.AesEncryptDecryptUtil] start.
     *     2021-06-13 15:54:34 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.AesEncryptDecryptUtil] end.
     *     2021-06-13 15:54:34 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.DbSecurityBeanUtil$NotSupportedEncryptDecryptException] start.
     *     2021-06-13 15:54:34 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.DbSecurityBeanUtil$NotSupportedEncryptDecryptException] end.
     *     2021-06-13 15:54:34 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.DbSecurityBeanUtil] start.
     *     2021-06-13 15:54:34 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.DbSecurityBeanUtil] end.
     *     可知确实以com.aspire.ssm.util.AesEncryptDecryptUtil和com.aspire.ssm.util.DbSecurityBeanUtil为前缀的类都被加密了
     */
    public static void test0() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com.aspire.ssm.util.AesEncryptDecryptUtil,com.aspire.ssm.util.DbSecurityBeanUtil";
        
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
    
    /**
     * case：
     *     根据前缀匹配，本来以com.aspire.ssm.util.AesEncryptDecryptUtil和com.aspire.ssm.util.DbSecurityBeanUtil为前缀的类都会被加密的，
     *      但是excludePrefix对以com.aspire.ssm.util.DbSecurityBeanUtil为前缀的类进行了排除，所以最
     *      终只会有以com.aspire.ssm.util.AesEncryptDecryptUtil为前缀的类会被加密
     * 观察输入日志:
     *     2021-06-13 15:51:57 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.AesEncryptDecryptUtil] start.
     *     2021-06-13 15:52:01 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.AesEncryptDecryptUtil] end.
     *     可知确实只有AesEncryptDecryptUtil被加密了，DbSecurityBeanUtil没有被加密
     */
    public static void test1() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com.aspire.ssm.util.AesEncryptDecryptUtil,com.aspire.ssm.util.DbSecurityBeanUtil";
        String excludePrefix = "com.aspire.*.DbSecurityBeanUtil";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " excludePrefix=" + excludePrefix
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
