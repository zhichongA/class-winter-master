package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.exception.ClassWinterException;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 异常工具类
 *
 * @author {@link JustryDeng}
 * @since 2021/6/2 0:22:53
 */

public class ExceptionUtil {
    
    /**
     * 将异常堆栈 信息 转换为字符串
     *
     * @param e
     *            异常
     * @return 该异常的错误堆栈信息
     */
    public static String getStackTraceMessage(Throwable e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            // 将异常的的堆栈信息输出到printWriter中
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            return sw.toString();
        } catch (Exception exception) {
            throw new ClassWinterException(exception);
        } finally {
            IOUtil.close(pw, sw);
        }
    }
}