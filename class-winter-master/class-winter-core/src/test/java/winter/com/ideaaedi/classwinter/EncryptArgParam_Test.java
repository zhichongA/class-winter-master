package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.util.BashUtil;
import winter.com.ideaaedi.classwinter.util.Constant;
import winter.com.ideaaedi.classwinter.util.PathUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试加密参数
 * <p>
 *     注：在includePrefix后面允许设置加密参数（方式同在url后加参数一样，只是不需要urlEncoding）
 *
 * @author {@link JustryDeng}
 * @since 2021/6/12 14:34:45
 */
public class EncryptArgParam_Test {
    
    public static void main(String[] args) {
//        // 测试1: 无参数（默认参数）测试
//        Map<String, String> argMap = new HashMap<>();
//        have(argMap);
    
//        // 测试2: 有参数测试
//        Map<String, String> argMap = new HashMap<>();
//        argMap.put(Constant.CLEAN_CLASS_ANNOTATION_PARAM_NAME, "true");  // 默认为false
//        argMap.put(Constant.CLEAN_METHOD_ANNOTATION_PARAM_NAME, "true");  // 默认为false
//        argMap.put(Constant.CLEAN_FIELD_ANNOTATION_PARAM_NAME, "true"); // 默认为false
//        argMap.put(Constant.KEEP_ORIGIN_ARGS_NAME, "false"); // 默认为true
//        have(argMap);
    
//        // 测试3: 有参数测试
//        Map<String, String> argMap = new HashMap<>();
//        argMap.put(Constant.CLEAN_CLASS_ANNOTATION_PARAM_NAME, "false");  // 默认为false
//        argMap.put(Constant.CLEAN_METHOD_ANNOTATION_PARAM_NAME, "true");  // 默认为false
//        argMap.put(Constant.CLEAN_FIELD_ANNOTATION_PARAM_NAME, "false"); // 默认为false
//        argMap.put(Constant.KEEP_ORIGIN_ARGS_NAME, "false"); // 默认为true
//        have(argMap);
    
        // 测试4: 有参数测试
        Map<String, String> argMap = new HashMap<>();
        argMap.put(Constant.CLEAN_CLASS_ANNOTATION_PARAM_NAME, "true");  // 默认为false
        argMap.put(Constant.CLEAN_METHOD_ANNOTATION_PARAM_NAME, "false");  // 默认为false
        argMap.put(Constant.CLEAN_FIELD_ANNOTATION_PARAM_NAME, "true"); // 默认为false
        // 这里仅作测试实际清除spring boot注解会有问题 ，spring ClassReader读的是本地classfile 所以清除的spring 注解读取不到了
        argMap.put(Constant.TO_CLEAN_ANNOTATION_PREFIX, "org.springframework.boot.autoconfigure.SpringBootApplication"); // 默认为''
        argMap.put(Constant.KEEP_ORIGIN_ARGS_NAME, "true"); // 默认为true
        have(argMap);
    }
    
    /**
     * jar测试 - 失败测试
     */
    public static void have(Map<String, String> argMap) {
        // 杀下进程(以保证端口没有被占用)
        BashUtil.killProcessByPorts("8080");
        
        String projectRootDir = PathUtil.getProjectRootDir(AlreadyProtectedLibs_Test.class);
        String originJarOrWar = projectRootDir + "boot-jar.jar";
        String includePrefix = "com";
        
        // 在includePrefix后面组装参数（和在url后面增加参数一样）
        if (argMap != null && argMap.size() > 0) {
            StringBuilder sb = new StringBuilder(includePrefix);
            sb.append("?");
            argMap.forEach((key, value) -> {
                sb.append(key).append("=").append(value).append("&");
            });
            sb.replace(sb.length() - 1, sb.length(), "");
            includePrefix= sb.toString();
        }
        
        String startBat = "java -jar " + projectRootDir + "class-winter-core-2.9.7.jar"
                + " originJarOrWar=" + originJarOrWar
                + " includePrefix=" + includePrefix
                + " debug=true"
                ;
        // 加密
        BashUtil.runBashAndPrint(startBat);
    
    
        String encryptedJar = originJarOrWar.replace(".jar", "-encrypted.jar");
    
        // 解密
        String javaagentArgs = "";
        // String javaagentArgs = "=debug=true";
        // String javaagentArgs = "=debug=true,password=xxx";
        BashUtil.runBashAndPrint(String.format("java -javaagent:%s%s=debug=true -jar %s", encryptedJar, javaagentArgs, encryptedJar));
    }
    
}
