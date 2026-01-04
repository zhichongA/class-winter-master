package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 *
 * @author {@link JustryDeng}
 * @since 2021/4/26 23:26:34
 */
public final class StrUtil {
    
    /**
     * 判断字符串是否为空
     *
     * @param str
     *            字符串
     * @return  是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
    
    /**
     * 判断字符串是否为true
     *
     * @param str 字符串
     * @param defaultValue 字符串为空时的默认值
     * @return  是否为true
     */
    public static boolean isTrueDefault(String str, boolean defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        }
        return str.equalsIgnoreCase("true");
    }
    
    /**
     * <p>Checks if a CharSequence is empty (""), null or whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs  the CharSequence to check, may be null
     * @return {@code true} if the CharSequence is null, empty or whitespace only
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 判断字符串是否为空
     *
     * @param str
     *            字符串
     * @return  是否为空
     */
    public static boolean isEmpty(char[] str) {
        return str == null || str.length == 0;
    }
    
    /**
     * 在字符串的某行后插入字符串
     *
     * <p>
     *     注:若origin中，存在多行的前缀是linePrefix，那么在第一个匹配行后面进行插入。
     *     注:若origin中，不存在任何行的前缀是linePrefix，那么在最后插入。
     * </p>
     *
     * @param origin
     *            原(按行拆分后的)字符串数组
     * @param insertStr
     *            要插入的字符串
     * @param linePrefix
     *            定位insertStrAfterLine中Line的行前缀
     * @return  插入后的字符串
     */
    public static String insertStrAfterLine(String[] origin, String insertStr, String linePrefix) {
        StringBuilder newStr = new StringBuilder();
        boolean alreadyInsert = false;
        for (String str : origin) {
            newStr.append(str).append(System.lineSeparator());
            if (str.startsWith(linePrefix)) {
                newStr.append(insertStr).append(System.lineSeparator());
                alreadyInsert = true;
            }
        }
        // 若origin中，不存在任何行的前缀是linePrefix，那么在最后插入
        if (!alreadyInsert) {
            newStr.append(insertStr).append(System.lineSeparator());
        }
        return newStr.toString();
    }

    /**
     * 将由逗号分割的信息分割到set里面
     *
     * @param str 逗号分割的字符串
     * @return 承载各个前缀信息的集合
     */
    public static Set<String> strToSet(String str) {
        return strToSet(str, ",");
    }
    
    /**
     * 将由逗号分割的信息分割到set里面
     *
     * @param str
     *            逗号分割的字符串
     * @param split
     *            分隔符
     * @return  承载各个前缀信息的集合
     */
    public static Set<String> strToSet(String str, String split) {
        Set<String> set = new HashSet<>();
        if (!StrUtil.isBlank(str)) {
            str = str.trim();
            String[] arr = str.split(split);
            for (String item : arr) {
                if (StrUtil.isEmpty(item)) {
                    continue;
                }
                item = item.trim();
                set.add(item);
            }
        }
        return set;
    }
    
    /**
     * 合并char[]
     *
     * @param charArr
     *            要合并的字符数组们
     * @return  合并后的字符数组
     */
    public static char[] mergeChar(char[]... charArr) {
        int length = 0;
        for (char[] c : charArr) {
            length += c.length;
        }
        char[] chars = new char[length];
        int lastLength = 0;
        for (char[] c : charArr) {
            System.arraycopy(c, 0, chars, lastLength, c.length);
            lastLength += c.length;
        }
        return chars;
    }
    
    /**
     * 字符数组转换成字节数组
     *
     * @param charArr
     *            字符数组
     * @return  字节数组
     */
    public static byte[] toBytes(char[] charArr) {
        char[] chars0 = new char[charArr.length];
        System.arraycopy(charArr, 0, chars0, 0, charArr.length);
        CharBuffer charBuffer = CharBuffer.wrap(chars0);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        // release resources \u0000 即代表一个空格
        Arrays.fill(charBuffer.array(), '\u0000');
        // release resources
        Arrays.fill(byteBuffer.array(), (byte) 0);
        return bytes;
    }

    /**
     * 检查字符串是否以指定的前缀或正则表达式匹配。
     *
     * @param prefixOrRegex
     *              前缀或正则表达式字符串
     * @param text
     *              待匹配字符串
     * @return 如果前缀匹配或正则表达式匹配成功，返回 true；否则返回 false。
     */
    public static boolean startsWithOrRegMatched(String prefixOrRegex, String text) {
        return text.startsWith(prefixOrRegex) || regMatched(prefixOrRegex, text);
    }

    /**
     * 获取正则表达式匹配次数
     *
     * @param patternStr
     *              正则表达式字符串
     * @param text
     *              待匹配字符串
     * @return 匹配次数
     */
    public static boolean regMatched(String patternStr, String text) {
        return regMatched(Pattern.compile(patternStr), text);
    }

    /**
     * 获取正则表达式匹配次数
     *
     * @param pattern
     *            正则表达式匹配器
     * @param text
     *          待匹配字符串
     * @return 匹配次数
     */
    public static boolean regMatched(Pattern pattern, String text) {
        return pattern.matcher(text).find();
    }
}
