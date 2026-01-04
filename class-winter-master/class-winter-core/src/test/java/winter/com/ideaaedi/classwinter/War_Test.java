package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.PathUtil;

/**
 * 这里只讲war包进行混淆， 需要执行使用web容器(如tomcat)启动war包
 *
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
public class War_Test {
    
    public static void main(String[] args) {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        test0();
//        test1();
//        test2();
//        test3();
//        test4();
    }
    
    /**
     * 第一个war
     *
     * 输出:
     * 请使用web容器(如tomcat)启动war包：E:/Git/Repository/class-winter/class-winter-core/target/test-classes/war-project-encrypted.war
     * 其中参数附加 -javaagent:class-winter-core.jar
     */
    public static void test0() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "war-project.war";
        String includePrefix = "com";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
        
        String encryptedWar = originJarOrWar.replace(".war", "-encrypted.war");
        System.out.println();
        System.out.println("[加密已完成] 请使用web容器(如tomcat)启动war包：" + encryptedWar);
        System.out.println("其中参数附加 -javaagent:class-winter-core.jar");
        // System.out.println("其中参数附加 -javaagent:class-winter-core.jar=debug=true,password=pwd123");
        System.out.println();
    }
    
    /**
     * 第二个war
     *
     * 输出:
     * 请使用web容器(如tomcat)启动war包：E:/Git/Repository/class-winter/class-winter-core/target/test-classes/my-project-with-normal-lib-encrypted.war
     * 其中参数附加 -javaagent:class-winter-core.jar=password=123456
     */
    public static void test1() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "my-project-with-normal-lib.war";
        String includePrefix = "com";
        String password = "123456";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " password=" + password
                + " debug=" + true
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
        String encryptedWar = originJarOrWar.replace(".war", "-encrypted.war");
        System.out.println();
        System.out.println("[加密已完成] 请使用web容器(如tomcat)启动war包：" + encryptedWar);
        System.out.println("其中参数附加 -javaagent:class-winter-core.jar=password=" + password);
        // System.out.println("其中参数附加 -javaagent:class-winter-core.jar=debug=true,password=pwd123");
        System.out.println();
    }
    
    /**
     * 第三个war
     *
     * 输出:
     * 请使用web容器(如tomcat)启动war包：E:/Git/Repository/class-winter/class-winter-core/target/test-classes/my-project-with-encrypted-lib-no-pwd-encrypted.war
     * 其中参数附加 -javaagent:class-winter-core.jar=password=oop857857
     */
    public static void test2() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "my-project-with-encrypted-lib-no-pwd.war";
        String includePrefix = "com";
        String alreadyProtectedLibs = "encrypted-lib-no-pwd.jar";
        String password = "oop857857";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " alreadyProtectedLibs=" + alreadyProtectedLibs
                + " password=" + password
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
        String encryptedWar = originJarOrWar.replace(".war", "-encrypted.war");
        System.out.println();
        System.out.println("[加密已完成] 请使用web容器(如tomcat)启动war包：" + encryptedWar);
        System.out.println("其中参数附加 -javaagent:class-winter-core.jar=password=" + password);
        // System.out.println("其中参数附加 -javaagent:class-winter-core.jar=debug=true,password=pwd123");
        System.out.println();
    }
    
    /**
     * 第四个war
     *
     * 输出:
     * 请使用web容器(如tomcat)启动war包：E:/Git/Repository/class-winter/class-winter-core/target/test-classes/my-project-with-encrypted-lib-have-pwd-encrypted.war
     * 其中参数附加 -javaagent:class-winter-core.jar=password=yiyakaka
     */
    public static void test3() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "my-project-with-encrypted-lib-have-pwd.war";
        String includePrefix = "com";
        String alreadyProtectedLibs = "encrypted-lib-have-pwd.jar:qwer123~";
        String password = "yiyakaka";
        
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " alreadyProtectedLibs=" + alreadyProtectedLibs
                + " password=" + password
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
        String encryptedWar = originJarOrWar.replace(".war", "-encrypted.war");
        System.out.println();
        System.out.println("[加密已完成] 请使用web容器(如tomcat)启动war包：" + encryptedWar);
        System.out.println("其中参数附加 -javaagent:class-winter-core.jar=password=" + password);
        // System.out.println("其中参数附加 -javaagent:class-winter-core.jar=debug=true,password=pwd123");
        System.out.println();
    }
    
    /**
     * 第五个war
     *
     * 输出:
     * 请使用web容器(如tomcat)启动war包：E:/Git/Repository/class-winter/class-winter-core/target/test-classes/jd.war
     * 其中参数附加 -javaagent:class-winter-core.jar=password=pwd12345
     */
    public static void test4() {
        String projectRootDir = PathUtil.getProjectRootDir(AllTest.class);
        String originJarOrWar = projectRootDir + "my-project-with-encrypted-lib-have-pwd.war";
        String includePrefix = "com.szlaozicl,com.aspire.ssm,org.springframework.aop.target.dynamic,com.fasterxml.jackson";
        String excludePrefix = "com.aspire.ssm.config,com.aspire.ssm.util.Person";
        String finalName = "jd";
        String password = "pwd12345";
        String includeLibs = "spring-aop-5.2.7.RELEASE.jar,jackson-core-2.10.4.jar";
        String tips = "方法已经被class-winter加密保护了，请不要直接使用java -jar xxx.jar启动项目，请使用java -javaagent:xxx.jar -jar xxx.jar启动项目.";
        String alreadyProtectedLibs = "encrypted-lib-have-pwd.jar:qwer123~";
        String debug = "true";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " excludePrefix=" + excludePrefix
                + " finalName=" + finalName
                + " password=" + password
                + " includeLibs=" + includeLibs
                + " tips=" + (BashUtil.IS_WINDOWS ? "\"".concat(tips).concat("\"") : "'".concat(tips).concat("'"))
                + " alreadyProtectedLibs=" + alreadyProtectedLibs
                + " debug=" + debug
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
    
        String encryptedJar = projectRootDir + finalName + ".war";
        System.out.println();
        System.out.println("[加密已完成] 请使用web容器(如tomcat)启动war包：" + encryptedJar);
        System.out.println("其中参数附加 -javaagent:class-winter-core.jar=password=pwd12345");
        // System.out.println("其中参数附加 -javaagent:class-winter-core.jar=debug=true,password=pwd123");
        System.out.println();
    }
}
