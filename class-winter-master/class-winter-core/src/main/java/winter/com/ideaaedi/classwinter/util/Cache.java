package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.executor.EncryptExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单缓存
 *
 * @author {@link JustryDeng}
 * @since 2021/5/27 23:15:57
 */
public final class Cache {
    
    /**
     * 解密时的密码缓存
     * <ul>
     *     <li>key-projectPath</li>
     *     <li>value-密码</li>
     * </ul>
     */
    public static Map<String, char[]> passwordCacheForDecrypt = new ConcurrentHashMap<>(8);
    
    /**
     * 加密时的密码缓存
     * <ul>
     *     <li>key-{@link EncryptExecutor#originJarOrWar}</li>
     *     <li>value-密码</li>
     * </ul>
     */
    public static Map<String, char[]> passwordCacheForEncrypt = new ConcurrentHashMap<>(8);
    
    /** 本项目加密时的印章 (key-projectPath; value-印章) */
    public static Map<String, String>  sealCache =  new ConcurrentHashMap<>(8);
    
    /** 本项目获取到的第一个印章 */
    public static volatile String  firstSealCache;
    
    /** 本项目获取到的第一个印章的项目路径 */
    public static volatile String  firstSealProjectPath;
    
    /**
     * 以key-value的形式记录lib的印章。(key-存放采集到的类所在lib的信息的文件夹， value-印章字符串)
     *
     * key   - META-INF/winter/abc-1.0.0_jar/
     * value - 印章字符串
     */
    public static Map<String, String> libSealCache;
    
    /**
     * 以key-value的形式记录各个libs密码的印章。(key-lib对应的class-winter信息文件夹， value-密码)
     *
     * key   - 形如: META-INF/winter/abc-1.0.0_jar/
     * value - 密码
     */
    public static Map<String, char[]> libPasswordCache = null;
}
