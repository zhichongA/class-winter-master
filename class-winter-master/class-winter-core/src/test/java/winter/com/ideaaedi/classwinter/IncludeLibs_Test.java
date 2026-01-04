package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.PathUtil;

/**
 * 测试includeLibs
 * <p>
 *     includeLibs: 默认的，我们的加密范畴为当前项目的class, 而includeLibs和excludePrefix也对在加密范畴内的class才会生效。
 *                  在某些场景中，我们不仅需要对当前项目的class进行加密，我们还可能需要对项目里依赖的libs(里的某些class)进行加密。
 *                  那么就可以通过includeLibs来指定libs，将libs也一同拉入加密范畴。
 *            提示: 对于依赖传递的场景，我们在使用includeLibs时，直接平铺开就行。比如说，你的项目的依赖关系是: your-project -> a.jar -> b.jar -> c.jar，
 *                  除了项目本身外，你想要同时对a和c加密的话， 直接这样写includeLibs=a.jar,c.jar即可， 不用考虑依赖传递的问题
 * </p>
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
public class IncludeLibs_Test {
    
    /**
     * 观察日志，你会发现日志记录:
     * 2021-06-13 16:13:57 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.google.common.xml.package-info] start.
     * 2021-06-13 16:13:59 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.google.common.xml.package-info] end.
     * 2021-06-13 16:13:59 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.google.common.xml.XmlEscapers] start.
     * 2021-06-13 16:13:59 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[com.google.common.xml.XmlEscapers] end.
     * 2021-06-13 16:13:59 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[org.springframework.aop.target.dynamic.AbstractRefreshableTargetSource] start.
     * 2021-06-13 16:13:59 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[org.springframework.aop.target.dynamic.AbstractRefreshableTargetSource] end.
     * 2021-06-13 16:13:59 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[org.springframework.aop.target.dynamic.BeanFactoryRefreshableTargetSource] start.
     * 2021-06-13 16:13:59 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[org.springframework.aop.target.dynamic.BeanFactoryRefreshableTargetSource] end.
     * 2021-06-13 16:13:59 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[org.springframework.aop.target.dynamic.Refreshable] start.
     * 2021-06-13 16:13:59 [DEBUG] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Encrypt class[orgspringframework.aop.target.dynamic.Refreshable] end.
     *
     * 直接解压反编译加密出来的包，然后再反编译里面的spring-aop-5.1.8.RELEASE.jar和guava-28.2-jre.jar，观察对应的类，就会发现该类确实是被加密了的。
     */
    public static void main(String[] args) {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
    
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "org.springframework.aop.target.dynamic,com.google.common.xml";
        String includeLibs = "spring-aop-.*.RELEASE.jar,guava-28.2-jre.jar";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " includeLibs=" + includeLibs
                + " debug=" + true
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
}
