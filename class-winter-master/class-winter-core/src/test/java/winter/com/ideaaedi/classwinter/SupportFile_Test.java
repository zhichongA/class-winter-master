package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.PathUtil;

/**
 * 测试supportFile
 * <p>
 *     supportFile：在加密某些类时，由于找不到该类中依赖的其它的类，会导致加密失败。此时，可通过supportFile指定一个jar文件(或者指定一个文件夹)。
 *                  这个jar文件(这个文件夹及其子孙文件夹下面的所有jar文件)都会被作为一个辅助，来进行加密，这样一来，就不会因为找不到类而导致加密失败了。
 *            提示: 可以放心的是，作为supportFile的jar包，是不会被打进加密后的jar/war的。
 * </p>
 * <p>
 *  注: 某些项目在打jar包时，是没有把其依赖的lib打进jar包中的，所以会出现[找不到该类中依赖的其它的类，导致加密失败]的情况。
 * </p>
 *
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
public class SupportFile_Test {
    
    public static void main(String[] args) {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
    
//        test0();
        test1();
    }
    
    /**
     * 会因为找不到类，而导致混淆失败
     *
     * 日志也有相关提示:
     * 2021-06-13 16:39:12 [ WARN] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Ignore clear-method-body for className [com.aspire.ssm.config.ServletInitializer], Cannot find 'org.springframework.boot.builder.SpringApplicationBuilder'
     * 2021-06-13 16:39:12 [ WARN] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Ignore clear-method-body for className [com.aspire.ssm.handler.impl.DefaultAesPreHandlerImpl], Cannot find 'org.apache.ibatis.executor.parameter.ParameterHandler'
     * 2021-06-13 16:39:12 [ WARN] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Ignore clear-method-body for className [com.aspire.ssm.interceptor.CustomizeMybatisPlugin], Cannot find 'org.apache.ibatis.plugin.Invocation'
     * 2021-06-13 16:39:12 [ WARN] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Ignore clear-method-body for className [com.aspire.ssm.SsmApplication], Cannot find 'org.springframework.boot.ApplicationArguments'
     */
    private static void test0() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "normal-jar.jar";
        String includePrefix = "com";
        
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    }
    
    /**
     * 混淆成功
     *
     * 日志没有warn提示说混淆失败，同时反编译混淆后的jar包进行观察也会发现是成功了的
     */
    private static void test1() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        // 加密(模拟命令行输入对jar包进行加密)
        String originJarOrWar = projectRootDir + "normal-jar.jar";
        String includePrefix = "com";
        String supportFile = projectRootDir + "help";
        
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " supportFile=" + supportFile
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    }
}
