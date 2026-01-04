package winter.com.ideaaedi.classwinter.util;

import sun.misc.Unsafe;
import winter.com.ideaaedi.classwinter.exception.JVMException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

/**
 * JVM 工具类，用于处理 JVM 相关操作。
 *
 * @author wang
 */
public class JVMUtil {

    /**
     * 用于查找本地库中的符号的反射方法。
     */
    private static final Method findNative;

    /**
     * 用于加载本地库的类加载器。
     */
    private static final ClassLoader classLoader;

    /**
     * Unsafe 类的实例，用于执行底层的内存操作。
     */
    public static final Unsafe unsafe = getUnsafe();

    static {
        // 检测操作系统类型
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            // 在 Windows 操作系统上加载相应的 jvm.dll
            String vmName = System.getProperty("java.vm.name");
            String dll = vmName.contains("Client VM") ? "/bin/client/jvm.dll" : "/bin/server/jvm.dll";
            try {
                System.load(System.getProperty("java.home") + dll);
            } catch (UnsatisfiedLinkError e) {
                throw new JVMException("Cannot find jvm.dll. Unsupported JVM?");
            }
            classLoader = JVMUtil.class.getClassLoader();
        } else {
            classLoader = null;
        }
        try {
            // 获取 ClassLoader 类的 findNative 方法并设置访问权限
            findNative = ClassLoader.class.getDeclaredMethod("findNative", ClassLoader.class, String.class);
            findNative.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new JVMException("Method ClassLoader.findNative not found");
        }
    }

    /**
     * 获取指定符号的地址。
     *
     * @param name 符号名称
     * @return 符号的地址
     * @throws NoSuchElementException 如果找不到指定符号
     */
    public static long getSymbol(String name) {
        long address = JVMUtil.lookup(name);
        if (address == 0) {
            throw new NoSuchElementException("No such symbol: " + name);
        }
        return getLong(address);
    }

    /**
     * 从指定地址读取 long 类型的值。
     *
     * @param addr 地址
     * @return 读取的 long 值
     */
    public static long getLong(long addr) {
        return unsafe.getLong(addr);
    }

    /**
     * 将指定值写入指定地址。
     *
     * @param addr 地址
     * @param val  要写入的值
     */
    public static void putAddress(long addr, long val) {
        unsafe.putAddress(addr, val);
    }

    /**
     * 查找本地库中指定名称的符号的地址。
     *
     * @param name 符号名称
     * @return 符号的地址
     */
    public static long lookup(String name) {
        try {
            return (Long) findNative.invoke(null, classLoader, name);
        } catch (InvocationTargetException e) {
            throw new JVMException(e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new JVMException(e);
        }
    }

    /**
     * 获取 Unsafe 类的实例。
     *
     * @return Unsafe 类的实例
     */
    private static Unsafe getUnsafe() {
        try {
            java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new JVMException("Unable to get Unsafe", e);
        }
    }
}
