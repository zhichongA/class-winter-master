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
public class Xml_Test {
    
    public static void main(String[] args) {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        test();
    }
    
    /**
     * 期望结果:
     *    项目正常启动，启动成功后（通过走xml的查询语句）能查出数据
     */
    public static void test() {
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "xml-test-project.jar";
        String includePrefix = "com";
        String includeXmlPrefix = "BOOT-INF/classes/";
        String excludeXmlPrefix = "BOOT-INF/classes/resources/";
        String toCleanXmlChildElementName = "select";
        
        
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " includeXmlPrefix=" + includeXmlPrefix
                + " excludeXmlPrefix=" + excludeXmlPrefix
                + " toCleanXmlChildElementName=" + toCleanXmlChildElementName
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
        
        System.out.println();
        // 解密启动
        String encryptedJar = originJarOrWar.replace(".jar", "-encrypted.jar");
        String javaagentArgs = "=debug=true";
        // String javaagentArgs = "=debug=true,password=xxx";
        BashUtil.runBashAndPrint(String.format("java -javaagent:%s%s -jar %s", encryptedJar, javaagentArgs, encryptedJar));
    }
}
