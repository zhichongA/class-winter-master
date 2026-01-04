package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.PathUtil;

/**
 * 测试tips
 * <p>
 *     tips：主动指定提示信息。
 *     注: tips默认值为ERROR !!!!!!!!!!! Jar(or War) has been protected by class-winter. Please use javaagent re-start project. !!!!!!!!!!!。
 *     注: 当用户没解密，而直接使用混淆后的代码时，就会报错(给出此tips)，同时退出程序。
 * </p>
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
public class Tips_Test {
    
    public static void main(String[] args) {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
//        test0();
        test1();
    }
    
    /**
     * 默认提示
     */
    public static void test0() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        // 加密(模拟命令行输入对jar包进行加密)
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                ;
        BashUtil.runBashAndPrint(startBat);
    
    
        String encryptedJar = originJarOrWar.replace(".jar", "-encrypted.jar");
    
        // 直接使用混淆后的jar
        BashUtil.runBashAndPrint(String.format("java -jar %s", encryptedJar));
    }
    
    /**
     * 指定提示
     */
    public static void test1() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
        String tips = "[潘嘎之交] 潘: 你别怪潘叔说话比较直白, 网络上的东西都是虚拟的, 你把握不住, 孩子! 因为这里的水很深...";
    
        // 如果tips值由一些特殊字符(如空格等)的话，可以用windows双引号(linux单引号)引起来。
        // java -jar class-winter-core-2.9.7.jar originJarOrWar=/boot-jar.jar includePrefix=com tips="[潘嘎之交] 潘: 你别怪潘叔说话比较直白, 网络上的东西都是虚拟的, 你把握不住, 孩子! 因为这里的水很深..."
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " tips=" + (BashUtil.IS_WINDOWS ? "\"".concat(tips).concat("\"") : "'".concat(tips).concat("'"));
                ;
        BashUtil.runBashAndPrint(startBat);
    
        String encryptedJar = originJarOrWar.replace(".jar", "-encrypted.jar");
    
        // 直接使用混淆后的jar
        BashUtil.runBashAndPrint(String.format("java -jar %s", encryptedJar));
    }
}
