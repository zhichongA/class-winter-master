package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.executor.EncryptExecutor;
import winter.com.ideaaedi.classwinter.util.Logger;
import winter.com.ideaaedi.classwinter.util.StrUtil;

import java.util.Objects;

/**
 * 正向加密
 *
 * @author {@link JustryDeng}
 * @since 2021/5/6 23:25:24
 */
public class Forward {
    
    /**
     * 入口
     * <p>
     * 假设是这么java -jar -class-winter-core-1.0.0.jar k1=v1 k2=v2 k3=v3  启动项目的，
     * 那么args的内容形如["k1=v1", "k2=v2", "k3=v3"]
     */
    public static void main(String[] args) {
        /// main test
        ///args =new String[]{
        ///        "originJarOrWar=D:/working/class-winter/class-winter-core/target/test-classes/boot-jar-encrypted.jar",
        ///        "includePrefix=com?cca=true&cma=true&koa=false&cfa=true,com.ideaaedi,com.ideaaedi.demo"
        ///};
        // 解析参数
        String originJarOrWar = parseValueByPrefixFromTail("originJarOrWar=", args);
        String includePrefix = parseValueByPrefixFromTail("includePrefix=", args);
        String excludePrefix = parseValueByPrefixFromTail("excludePrefix=", args);
        String includeXmlPrefix = parseValueByPrefixFromTail("includeXmlPrefix=", args);
        String excludeXmlPrefix = parseValueByPrefixFromTail("excludeXmlPrefix=", args);
        String toCleanXmlChildElementName = parseValueByPrefixFromTail("toCleanXmlChildElementName=", args);
        String finalName = parseValueByPrefixFromTail("finalName=", args);
        String password = parseValueByPrefixFromTail("password=", args);
        String includeLibs = parseValueByPrefixFromTail("includeLibs=", args);
        String alreadyProtectedRootDir = parseValueByPrefixFromTail("alreadyProtectedRootDir=", args);
        String alreadyProtectedLibs = parseValueByPrefixFromTail("alreadyProtectedLibs=", args);
        String tips = parseValueByPrefixFromTail("tips=", args);
        String supportFile = parseValueByPrefixFromTail("supportFile=", args);
        String jvmArgCheck = parseValueByPrefixFromTail("jvmArgCheck=", args);
        String debug = parseValueByPrefixFromTail("debug=", args);
        
        Logger.ENABLE_DEBUG.set(Boolean.parseBoolean(debug));
        Logger.debug(Forward.class, "You input arg originJarOrWar -> " + originJarOrWar);
        Logger.debug(Forward.class, "You input arg includePrefix -> " + includePrefix);
        Logger.debug(Forward.class, "You input arg excludePrefix -> " + excludePrefix);
        Logger.debug(Forward.class, "You input arg includeXmlPrefix -> " + includeXmlPrefix);
        Logger.debug(Forward.class, "You input arg excludeXmlPrefix -> " + excludeXmlPrefix);
        Logger.debug(Forward.class, "You input arg toCleanXmlChildElementName -> " + toCleanXmlChildElementName);
        Logger.debug(Forward.class, "You input arg finalName -> " + finalName);
        Logger.debug(Forward.class, "You input arg password -> " + password);
        Logger.debug(Forward.class, "You input arg includeLibs -> " + includeLibs);
        Logger.debug(Forward.class, "You input arg alreadyProtectedLibs -> " + alreadyProtectedLibs);
        Logger.debug(Forward.class, "You input arg tips -> " + tips);
        Logger.debug(Forward.class, "You input arg supportFile -> " + supportFile);
        Logger.debug(Forward.class, "You input arg jvmArgCheck -> " + jvmArgCheck);
        Logger.debug(Forward.class, "You input arg debug -> " + debug);
        
        // 构造加密执行器
        EncryptExecutor encryptExecutor = EncryptExecutor.builder()
                .originJarOrWar(originJarOrWar)
                .includePrefix(includePrefix)
                .excludePrefix(excludePrefix)
                .includeXmlPrefix(includeXmlPrefix)
                .excludeXmlPrefix(excludeXmlPrefix)
                .toCleanXmlChildElementName(toCleanXmlChildElementName)
                .finalName(finalName)
                .password(password)
                .includeLibs(includeLibs)
                .alreadyProtectedRootDir(alreadyProtectedRootDir)
                .alreadyProtectedLibs(alreadyProtectedLibs)
                .debug(Boolean.parseBoolean(debug))
                .supportFile(supportFile)
                .jvmArgCheck(jvmArgCheck)
                .tips(tips)
                .build();
    
        Logger.debug(Forward.class, "The encrypted executor generated based on your input is -> " + encryptExecutor);
        // 执行加密
        String generatedJar = encryptExecutor.process();
        // 打印加深后生成的jar的全路径
        Logger.info(Forward.class, "The absolute path of the obfuscated jar is [" + generatedJar + "]");
    }
    
    /**
     * 根据前缀解析值
     * <p>
     *     如: 数组中某元素的为 k1=v1, 此方法传入的前缀为k1=， 那么此方法会返回v1
     * </p>
     *
     * @param prefix
     *            前缀
     * @param args
     *            参数数组
     * @return  解析出来的值，若没有则返回null
     */
    private static String parseValueByPrefixFromTail(String prefix, String[] args) {
        Objects.requireNonNull(prefix, "prefix cannot be null.");
        if (args == null) {
            return null;
        }
        // 从后往前找，即: java -jar k1=v1 k2=v2 k3=v3 -xxx.jar中，若k冲突了，那么取后面的k对应的v
        for (int i = args.length - 1; i >= 0; i--) {
            if (args[i] == null || StrUtil.isBlank(args[i])) {
                continue;
            }
            args[i] = args[i].trim();
            if (args[i].startsWith(prefix)) {
                return args[i].substring(prefix.length());
            }
        }
        return null;
    }
}
