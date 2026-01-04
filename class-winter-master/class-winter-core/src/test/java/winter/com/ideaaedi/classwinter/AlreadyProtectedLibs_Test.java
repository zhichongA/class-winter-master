package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.Constant;
import winter.com.ideaaedi.classwinter.util.PathUtil;

/**
 * 测试originJarOrWar
 *
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
@SuppressWarnings("unused")
public class AlreadyProtectedLibs_Test {
    
    public static void main(String[] args) {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        
//        test0();
//        test1();
        test2();
    }
    
    /**
     * 场景:
     *   项目my-project-with-encrypted-lib-no-pwd.jar中依赖了encrypted-lib-no-pwd-1.0.0.jar包，
     *   但是这个encrypted-lib-no-pwd-1.0.0.jar包是被class-winter混淆了的，
     *   如过想要正常使用my-project-with-encrypted-lib-no-pwd.jar，那么必须
     *   对my-project-with-encrypted-lib-no-pwd.jar进行class-winter混淆，
     *   且指定alreadyProtectedLibs为encrypted-lib-no-pwd-1.0.0.jar。
     *
     * 测试case:
     *    不对项目my-project-with-encrypted-lib-no-pwd.jar进行class-winter混淆，直接(java -jar)启动jar。
     *
     * 期望结果:
     *    启动后，不能正常使用encrypted-lib-no-pwd-1.0.0.jar包的功能。
     *    根据我提供的jar中的代码逻辑，启动后会报错。
     */
    private static void test0() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "my-project-with-encrypted-lib-no"
                + "-pwd.jar";
        // 启动
        BashUtil.runBashAndPrint(String.format("java -jar %s", originJarOrWar));

    }
    
    /**
     * 场景:
     *   项目my-project-with-encrypted-lib-no-pwd.jar中依赖了encrypted-lib-no-pwd-1.0.0.jar包，
     *   但是这个encrypted-lib-no-pwd-1.0.0.jar包是被class-winter混淆了的，
     *   如过想要正常使用my-project-with-encrypted-lib-no-pwd.jar，那么必须
     *   对my-project-with-encrypted-lib-no-pwd.jar进行class-winter混淆，
     *   且指定alreadyProtectedLibs为encrypted-lib-no-pwd-1.0.0.jar。
     *
     * 测试case:
     *    对项目my-project-with-encrypted-lib-no-pwd.jar进行class-winter混淆，然后使用javaagent启动jar。
     *
     * 期望结果:
     *    项目正常启动，且能正常使用encrypted-lib-no-pwd-1.0.0.jar的功能。
     *    根据我提供的jar中的代码逻辑， 当有<我是业务逻辑的输出>输出时，即表示成功了。
     */
    private static void test1() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + Constant.LINUX_FILE_SEPARATOR + "my-project-with-encrypted-lib-no-pwd.jar";
        String includePrefix = "non-exist";
        String alreadyProtectedLibs = "encrypted-lib-no-pwd-1.0.0.jar";
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " alreadyProtectedLibs=" + alreadyProtectedLibs
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
     * 场景:
     *   项目my-project-with-encrypted-lib-have-pwd.jar中依赖了encrypted-lib-have-pwd-1.0.0.jar包，
     *   但是这个encrypted-lib-have-pwd-1.0.0.jar包是被class-winter混淆了的，
     *   如过想要正常使用my-project-with-encrypted-lib-have-pwd.jar，那么必须
     *   对my-project-with-encrypted-lib-have-pwd.jar进行class-winter混淆，
     *   且在alreadyProtectedLibs中指定encrypted-lib-have-pwd-1.0.0.jar及其密码。
     *
     *   注:对mencrypted-lib-have-pwd-1.0.0.jar加密时，我主动指定了密码为qwer123~。
     *      所以这里在alreadyProtectedLibs中指定encrypted-lib-have-pwd-1.0.0.jar时，需要把密码带上。
     *
     * 测试case:
     *    对项目my-project-with-encrypted-lib-have-pwd.jar进行class-winter混淆，然后使用javaagent启动jar。
     *
     * 期望结果:
     *    项目正常启动，且能正常使用my-project-with-encrypted-lib-have-pwd.jar的功能。
     *    根据我提供的jar中的代码逻辑， 当有<我是业务逻辑的输出>输出时，即表示成功了。
     */
    private static void test2() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        // 加密
        String originJarOrWar = projectRootDir + "my-project-with-encrypted-lib-have-pwd.jar";
        String includePrefix = "non-exist";
        String alreadyProtectedLibs = "encrypted-lib-have-pwd-1.0.0.jar:qwer123~";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " alreadyProtectedLibs=" + alreadyProtectedLibs
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
