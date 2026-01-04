package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.exception.ClassWinterException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

/**
 * 加解密工具类
 *
 * @author {@link JustryDeng}
 * @since 2021/4/23 20:34:09
 */
public final class EncryptUtil {
    
    /**
     * 盐
     */
    private static final char[] SALT = {'c', 'h', 'i', '@', 'm', 'i', 'a', 'n', '#', 'y', 'a', 'o', '%', 'f', 'a', 'n', 'g', '^', 'y', 'a', 'n'};
    
    /**
     * AES 算法/模式/补码方式
     */
    private static final String AES_MODE = "AES/ECB/PKCS5Padding";
    
    /**
     * 加密
     * <p>
     * 与{@link EncryptUtil#decrypt(byte[], char[])}成对使用
     * </p>
     *
     * @param source 要加密的内容
     * @param password 用户密钥
     *
     * @return 加密后的内容
     */
    public static byte[] encrypt(byte[] source, char[] password) {
        try {
            String pwd = md5(StrUtil.toBytes(StrUtil.mergeChar(password, SALT)), true, true);
            return encryptByAes(source, pwd.toCharArray());
        } catch (NoSuchAlgorithmException e) {
            throw new ClassWinterException(e);
        }
    }
    
    /**
     * 解密
     * <p>
     * 与{@link EncryptUtil#encrypt(byte[], char[])}成对使用
     * </p>
     *
     * @param source 要解密的内容
     * @param password 用户密钥
     *
     * @return 解密后的内容
     */
    public static byte[] decrypt(byte[] source, char[] password) {
        try {
            String pwd = md5(StrUtil.toBytes(StrUtil.mergeChar(password, SALT)), true, true);
            return decryptByAes(source, pwd.toCharArray());
        } catch (NoSuchAlgorithmException e) {
            throw new ClassWinterException(e);
        }
    }
    
    /**
     * 加密
     * <p>
     * 与{@link EncryptUtil#decrypt(String, char[])}成对使用
     * </p>
     *
     * @param source 要加密的内容
     * @param password 用户密钥
     *
     * @return 加密后的内容
     */
    public static String encrypt(String source, char[] password) {
        Objects.requireNonNull(source, "source cannot be null.");
        return Base64.getEncoder().encodeToString(encrypt(source.getBytes(StandardCharsets.UTF_8), password));
    }
    
    /**
     * 解密
     * <p>
     * 与{@link EncryptUtil#encrypt(String, char[])}成对使用
     * </p>
     *
     * @param source 要解密的内容
     * @param password 用户密钥
     *
     * @return 解密后的内容
     */
    public static String decrypt(String source, char[] password) {
        Objects.requireNonNull(source, "source cannot be null.");
        return new String(decrypt(Base64.getDecoder().decode(source), password), StandardCharsets.UTF_8);
    }
    
    /**
     * 生成指定长度的随机字符串
     *
     * @param length 长度
     *
     * @return 字符数组
     */
    public static char[] generateCharArr(int length) {
        char[] result = new char[length];
        Character[] chars = {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '=', '_', '+', '.'
        };
        List<Character> list = Arrays.asList(chars);
        // "洗牌"
        Collections.shuffle(list);
        Random random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            if (i < chars.length) {
                result[i] = list.get(i);
            } else {
                result[i] = list.get(random.nextInt(chars.length));
            }
        }
        return result;
    }
    
    /**
     * 进行MD5加密
     * <p>
     * 摘录自 <a href="https://www.jianshu.com/p/b419163272c1" />
     * </p>
     * 注：MD5加密得到的是长度固定的为126bit的二进制串(一堆0和1，一共128)，为了更友好的表示结果，
     * 一般都将128位的二进制串转换为32个16进制位或16个16进制位。（16位的结果是取32位的结果值的中间部分，即32位中第8~24位的片段）
     *
     * @param source 要加密的内容
     * @param abbreviation true-返回16位结果；false-返回32位结果
     * @param upperCase true-结果大写；false-结果小写
     *
     * @return MD5加密结果
     *
     * @throws NoSuchAlgorithmException 无此算法时抛出
     */
    @SuppressWarnings("SameParameterValue")
    private static String md5(byte[] source, boolean abbreviation, boolean upperCase) throws NoSuchAlgorithmException {
        // 1. 获取MessageDigest对象
        MessageDigest digest = MessageDigest.getInstance("md5");
        // 2. 执行加密操作
        // 在MD5算法这，得到的目标字节数组的特点是：长度固定为16
        byte[] targetBytes = digest.digest(source);
        // 3. 声明字符数组
        char[] characters = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        // 4. 遍历targetBytes
        StringBuilder builder = new StringBuilder();
        for (byte b : targetBytes) {
            // 5. 取出b的高四位的值
            // 先把高四位通过右移操作拽到低四位
            int high = (b >> 4) & 15;
            // 6. 取出b的低四位的值
            int low = b & 15;
            // 7. 以high为下标从characters中取出对应的十六进制字符
            char highChar = characters[high];
            // 8. 以low为下标从characters中取出对应的十六进制字符
            char lowChar = characters[low];
            builder.append(highChar).append(lowChar);
        }
        String result = builder.toString();
        if (abbreviation) {
            result = result.substring(8, 24);
        }
        if (upperCase) {
            result = result.toUpperCase(Locale.ENGLISH);
        }
        return result;
    }
    
    /**
     * AES加密
     *
     * @param source 要加密的内容
     * @param key 密钥
     *
     * @return 加密后的内容
     */
    private static byte[] encryptByAes(byte[] source, char[] key) {
        try {
            byte[] raw = StrUtil.toBytes(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            return cipher.doFinal(source);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new ClassWinterException(e);
        }
    }
    
    /**
     * AES解密
     *
     * @param source 要解密的内容
     * @param key 密钥
     *
     * @return 解密后的内容
     */
    private static byte[] decryptByAes(byte[] source, char[] key) {
        try {
            byte[] raw = StrUtil.toBytes(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(AES_MODE);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            return cipher.doFinal(source);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new ClassWinterException(e);
        }
    }
    
    /**
     * 测试
     */
    public static void main(String[] args) {
        //        // => 测试一
        //        String originStr = "嘿~boy~";
        //        byte[] encryptedContent = encrypt(originStr.getBytes(StandardCharsets.UTF_8), "好想吃烧烤JustryDeng123..
        //        .".toCharArray());
        //        String plaintext = new String(decrypt(encryptedContent, "好想吃烧烤JustryDeng123...".toCharArray()),
        //        StandardCharsets.UTF_8);
        //        System.err.println("原文：" + originStr);
        //        System.err.println("加密后：" + encryptedContent);
        //        ///System.err.println("加密后：" + new String(Base64.getEncoder().encode(encryptedContent)));
        //        System.err.println("解密后：" + plaintext);
        
        // => 测试二
        String originStr = "嘿~boy~";
        String encryptedContent = encrypt(originStr, "好想吃烧烤JustryDeng123...".toCharArray());
        String plaintext = decrypt(encryptedContent, "好想吃烧烤JustryDeng123...".toCharArray());
        System.err.println("原文：" + originStr);
        System.err.println("加密后：" + encryptedContent);
        System.err.println("解密后：" + plaintext);
    }
}
