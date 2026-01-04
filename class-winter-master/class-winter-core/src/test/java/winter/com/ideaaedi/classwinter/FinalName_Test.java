package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.PathUtil;

/**
 * 测试finalName
 * <p>
 *     finalName: 指定混淆后生成的jar包(，若与原jar/war包同名，则会覆盖原jar/war包)
 * </p>
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
public class FinalName_Test {
    
    public static void main(String[] args) {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
        String finalName = "YiYaKaKa";
    
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " finalName=" + finalName
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
    
    
        String encryptedJar = projectRootDir + finalName + ".jar";
    
        // 解密
        String javaagentArgs = "";
        // String javaagentArgs = "=debug=true";
        // String javaagentArgs = "=debug=true,password=xxx";
        BashUtil.runBashAndPrint(String.format("java -javaagent:%s%s -jar %s", encryptedJar, javaagentArgs, encryptedJar));
    }
}
