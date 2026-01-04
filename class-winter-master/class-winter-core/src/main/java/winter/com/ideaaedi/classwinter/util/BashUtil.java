package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.exception.ClassWinterException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

/**
 * bash/sh 工具类
 *
 * @author {@link JustryDeng}
 * @since 2021/6/1 23:41:21
 */
public final class BashUtil {
    
    public static final String PORTS_PLACEHOLDER = "ports_placeholder";
    
    /**
     * 当前操作系统是否是windows
     */
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows");
    
    public static final String ORIGIN_KILL_PROCESS_BY_PORT_BAT = ""
            + "@echo off & setlocal EnableDelayedExpansion\n"
            + "\n"
            + "title kill process by port\n"
            + "\n"
            + "for %%a in (" + PORTS_PLACEHOLDER + ") do (\n"
            + "    set pid=0\n"
            + "    for /f \"tokens=2,5\" %%b in ('netstat -ano ^| findstr \":%%a\"') do (\n"
            + "        set temp=%%b\n"
            + "        for /f \"usebackq delims=: tokens=1,2\" %%i in (`set temp`) do (\n"
            + "            if %%j==%%a (\n"
            + "                taskkill /f /pid %%c\n"
            + "                set pid=%%c\n"
            + "                echo Port [%%a] related process has been killed.\n"
            + "            )\n"
            + "        )\n"
            + "    )\n"
            + "    if !pid!==0 (\n"
            + "       echo Port [%%a] is not occupied.\n"
            + "    )\n"
            + ")\n"
            + "\n"
            + "echo Operation has completed.\n";
    
    /**
     * 运行命令
     *
     * @param cmd
     *            命令
     * @return 结果
     */
    public static String runBash(String cmd) {
        return runBash(cmd, 0);
    }
    
    /**
     * 运行命令
     *
     * @param bash
     *            命令
     * @param line
     *           返回第几行结果，<=0,则返回所有
     * @return 结果
     */
    public static String runBash(String bash, int line) {
        // 直接执行命令行指令，记录warn
        Logger.warn(BashUtil.class, "You are running bash -> " + bash+ ", line -> " + line);
        if (bash == null || bash.length() == 0) {
            throw new ClassWinterException("bash cannot be empty.");
        }
        Process process;
        Scanner sc = null;
        StringBuilder sb = new StringBuilder();
        try {
            process = Runtime.getRuntime().exec(bash);
            process.getOutputStream().close();
            sc = new Scanner(process.getInputStream(), IS_WINDOWS ? "GBK" : StandardCharsets.UTF_8.name());
            int i = 0;
            while (sc.hasNextLine()) {
                i++;
                String str = sc.nextLine();
                if (line <= 0) {
                    sb.append(str).append(System.lineSeparator());
                } else if (i == line) {
                    return str.trim();
                }
            }
            return sb.toString();
        } catch (Exception e) {
            throw new ClassWinterException(e);
        } finally {
            IOUtil.close(sc);
        }
    }
    
    /**
     * 运行命令并输出到控制台
     *
     * @param bash
     *            命令
     */
    public static void runBashAndPrint(String bash) {
        // 代码直接调用执行命令行指令是不被推荐的，记录warn
        Logger.warn(BashUtil.class, "You are running bash -> " + bash);
        if (bash == null || bash.length() == 0) {
            throw new ClassWinterException("bash cannot be empty.");
        }
        Process process;
        Scanner sc = null;
        try {
            process = Runtime.getRuntime().exec(bash);
            process.getOutputStream().close();
            sc = new Scanner(process.getInputStream(), IS_WINDOWS ? "GBK" : StandardCharsets.UTF_8.name());
            while (sc.hasNextLine()) {
                System.out.println(sc.nextLine());
            }
        } catch (Exception e) {
            throw new ClassWinterException(e);
        } finally {
            IOUtil.close(sc);
        }
    }
    
    /**
     * 要执行的bat文件内容
     * <p>
     *     only for windows
     * </p>
     */
    public static String killProcessByPorts(String... port) {
        if (port == null || port.length == 0) {
            return "";
        }
        String batContent = ORIGIN_KILL_PROCESS_BY_PORT_BAT.replace(PORTS_PLACEHOLDER, String.join(",", port));
        Process process = null;
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder sb = new StringBuilder(64);
        try {
            File tmpBatFile = new File("/tmp_" + System.currentTimeMillis() + ".bat");
            IOUtil.writeContentToFile(batContent, tmpBatFile);
            process = Runtime.getRuntime().exec(tmpBatFile.getAbsolutePath());
            inputStream = process.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("Port") && !line.startsWith("Operation")) {
                    continue;
                }
                sb.append(line).append("\t");
            }
            IOUtil.delete(tmpBatFile);
        }catch (IOException e) {
            throw new ClassWinterException(e);
        } finally {
            if (process != null) {
                try {
                    process.waitFor();
                } catch (Exception e) {
                    // ignore
                }
            }
            IOUtil.close(inputStream, bufferedReader);
            if (process != null) {
                try {
                    process.destroy();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
        return sb.toString();
    }
}