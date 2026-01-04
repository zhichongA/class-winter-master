package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.PathUtil;

/**
 * 测试includePrefix
 * <p>
 *     includePrefix: 根据前缀匹配，以com.aspire.ssm.util.AesEncryptDecryptUtil和com.aspire.ssm.util.DbSecurityBeanUtil为前缀的类都会被加密
 * </p>
 *
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
public class IncludePrefix_Test {
    
    /**
     * 观察日志，你会发现日志记录:
     *     2021-06-13 16:21:15 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.AesEncryptDecryptUtil] start.
     *     2021-06-13 16:21:18 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.AesEncryptDecryptUtil] end.
     *     2021-06-13 16:21:18 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.ClazzDumpCustomAgent] start.
     *     2021-06-13 16:21:18 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.ClazzDumpCustomAgent] end.
     *     2021-06-13 16:21:18 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.DbSecurityBeanUtil$NotSupportedEncryptDecryptException] start.
     *     2021-06-13 16:21:18 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.DbSecurityBeanUtil$NotSupportedEncryptDecryptException] end.
     *     2021-06-13 16:21:18 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.DbSecurityBeanUtil] start.
     *     2021-06-13 16:21:18 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.DbSecurityBeanUtil] end.
     *     2021-06-13 16:21:18 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.EmployeePO$EmployeePOBuilder] start.
     *     2021-06-13 16:21:18 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.EmployeePO$EmployeePOBuilder] end.
     *     2021-06-13 16:21:18 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.EmployeePO] start.
     *     2021-06-13 16:21:18 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.aspire.ssm.util.EmployeePO] end.
     *
     * 直接解压反编译加密出来的包，观察com.aspire.ssm.util包下的类，就会发现确实是被加密了的。
     */
    public static void main(String[] args) {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com.aspire.*.util";
    
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
