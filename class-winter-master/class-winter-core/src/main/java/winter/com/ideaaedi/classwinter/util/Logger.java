package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 日志
 *
 * @author {@link JustryDeng}
 * @since 2021/4/26 23:06:15
 */
public final class Logger {
    
    private Logger() {
    }
    
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /** debug模式 */
    public static final AtomicBoolean ENABLE_DEBUG = new AtomicBoolean(false);
    
    /**
     * 输出debug信息
     *
     * @param msg
     *            信息
     */
    public static void debug(String msg) {
        if (ENABLE_DEBUG.get()) {
            String log = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " [DEBUG] " + msg;
            System.out.println(log);
        }
    }
    
    /**
     * 输出debug信息
     *
     * @param clazz
     *            记录日志操作所在的类
     * @param msg
     *            信息
     */
    public static void debug(Class<?> clazz, String msg) {
        if (ENABLE_DEBUG.get()) {
            String log = DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " [DEBUG] " + clazz.getName() + ": " + msg;
            System.out.println(log);
        }
    }
    
    /**
     * 输出信息
     *
     * @param msg
     *            信息
     */
    public static void simpleInfo(String msg) {
        System.out.println("[INFO] " + msg);
    }
    
    /**
     * 输出信息
     *
     * @param msg
     *            信息
     */
    public static void info(String msg) {
        System.out.println(DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " [ INFO] " + msg);
    }
    
    /**
     * 输出信息
     *
     * @param clazz
     *            记录日志操作所在的类
     * @param msg
     *            信息
     */
    public static void info(Class<?> clazz, String msg) {
        System.out.println(DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " [ INFO] " + clazz.getName() + ": " + msg);
    }
    
    /**
     * 输出信息
     *
     * @param msg
     *            信息
     */
    public static void warn(String msg) {
        System.out.println(DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " [ WARN] " + msg);
    }
    
    /**
     * 输出信息
     *
     * @param clazz
     *            记录日志操作所在的类
     * @param msg
     *            信息
     */
    public static void warn(Class<?> clazz, String msg) {
        System.out.println(DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " [ WARN] " + clazz.getName() + ": " + msg);
    }
    
    /**
     * 输出信息
     *
     * @param msg
     *            信息
     */
    public static void error(String msg) {
        System.out.println(DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " [ERROR] " + msg);
    }
    
    /**
     * 输出信息
     *
     * @param clazz
     *            记录日志操作所在的类
     * @param msg
     *            信息
     */
    public static void error(Class<?> clazz, String msg) {
        System.out.println(DATE_TIME_FORMATTER.format(LocalDateTime.now()) + " [ERROR] " + clazz.getName() + ": " + msg);
    }
}
