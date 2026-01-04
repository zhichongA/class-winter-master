package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.exception.ClassWinterException;

import java.io.File;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 在使用class-winter进行解密时，如果不需要输入参数，那么可以直接java -javaagent:xxx.jar -jar xxx.jar
 * 如果需要传参，下面两种方式都可以
 *     java -javaagent:xxx.jar="k1=v1,k2=v2" -jar xxx.jar
 *     java -javaagent:xxx.jar=k1=v1,k2=v2 -jar xxx.jar
 *
 * @author {@link JustryDeng}
 * @since 2021/6/1 22:15:28
 */
public class JavaagentCmdArgs {
    
    private String password;
    
    /** javaagent处理premain里的解密时，当识别到项目路径符合指定路径前缀时， 不尝试解密 */
    private String skipProjectPathPrefix;
    
    /**
     * javaagent处理premain里的解密时，当识别到项目路径符合指定路径前缀时， 尝试解密
     * <br/>
     * 注：当此参数不为空时，匹配到此参数前缀（当不匹配skipProjectPathPrefix）的ProjectPath将尝试解密，否则不尝试解密
     */
    private String decryptProjectPathPrefix;
    
    private boolean debug;
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean isDebug() {
        return debug;
    }
    
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    @Override
    public String toString() {
        return "JavaagentCmdArgs{" +
                "password='" + password + '\'' +
                "skipProjectPathPrefix='" + skipProjectPathPrefix + '\'' +
                "decryptProjectPathPrefix='" + decryptProjectPathPrefix + '\'' +
                ", debug=" + debug +
                '}';
    }
    
    /**
     * 解析java -javaagent:xxx.jar="k1=v1,k2=v2" -jar xxx.jar中的k1=v1,k2=v2字符串成JavaagentCmdArgs对象
     *
     * <p>
     *  注：根据本方法的逻辑，如果key重复，那么后面的value值会覆盖前面的。
     *  注：根据本方法的逻辑，如果debug的值不为true,那么其值就为false。
     *
     * @param args
     *            使用javaagent代理时，输入的参数， 如: k1=v1,k2=v2
     * @return 解析出来的对象（这个对象本身一定不为null）
     */
    public static JavaagentCmdArgs parseJavaagentCmdArgs(String args) {
        if (args == null) {
            return new JavaagentCmdArgs();
        }
        if (args.contains(Constant.WHITE_SPACE)) {
            throw new ClassWinterException("-javaagent args [" + args + "] cannot contain whitespace.");
        }
        // args形如 k1=v1,k2=v2
        String[] keyValueArr = args.split(",");
        JavaagentCmdArgs instance = new JavaagentCmdArgs();
        for (String keyValue : keyValueArr) {
            keyValue = keyValue.trim();
            // 读取指定文件中的内容作为password
            if (keyValue.startsWith("passwordFromFile=")) {
                String filepath = keyValue.substring("passwordFromFile=".length());
                Logger.debug(JavaagentCmdArgs.class, "passwordFromFile -> " + filepath);
                instance.setPassword(IOUtil.readContentFromFile(new File(filepath)).trim());
                continue;
            }
            // 调用shell获取返回值作为password
            if (keyValue.startsWith("passwordFromShell=")) {
                String filepath = keyValue.substring("passwordFromShell=".length());
                String shellContent = IOUtil.readContentFromFile(new File(filepath)).trim();
                Logger.debug(JavaagentCmdArgs.class, "passwordFromShell -> " + shellContent);
                List<String> shellResultList = runShell(shellContent);
                String password;
                if (shellResultList.size() == 0) {
                    throw new IllegalArgumentException("There is not any result returned from shell. \n" + shellContent);
                }
                password = shellResultList.get(0).trim();
                if (shellResultList.size() > 1) {
                    Logger.debug(JavaagentCmdArgs.class, String.format("There is multi result returned from shell. \n shellResultList is %s. Use the first result '%s' as password.",
                            shellResultList, password));
                }
                instance.setPassword(password);
                continue;
            }
            // 直接获取password
            if (keyValue.startsWith("password=")) {
                instance.setPassword(keyValue.substring("password=".length()));
                continue;
            }
            // 解析skipProjectPathPrefix
            if (keyValue.startsWith("skipProjectPathPrefix=")) {
                instance.setSkipProjectPathPrefix(keyValue.substring("skipProjectPathPrefix=".length()));
                continue;
            }
            // 解析decryptProjectPathPrefix
            if (keyValue.startsWith("decryptProjectPathPrefix=")) {
                instance.setDecryptProjectPathPrefix(keyValue.substring("decryptProjectPathPrefix=".length()));
                continue;
            }
            if (keyValue.startsWith("debug=")) {
                instance.setDebug(Boolean.parseBoolean(keyValue.substring("debug=".length())));
            }
        }
        return instance;
    }
    
    /**
     * 运行shell并获得结果
     * <br />
     * 注：如果sh中含有awk,一定要按new String[]{"/bin/sh","-c",shStr}写,才可以获得流
     *
     * @param shStr
     *            需要执行的shell
     * @return 结果
     */
    public static List<String> runShell(String shStr) {
        List<String> strList = new ArrayList<>(4);
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", shStr}, null, null);
            InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
            LineNumberReader lineNumberReader = new LineNumberReader(inputStreamReader);
            String line;
            process.waitFor();
            while ((line = lineNumberReader.readLine()) != null) {
                strList.add(line);
            }
        } catch (Exception e) {
            throw new RuntimeException("Get result from shell error. \n" + shStr, e);
        }
        return strList;
    }
    
    public String getSkipProjectPathPrefix() {
        return skipProjectPathPrefix;
    }
    
    public void setSkipProjectPathPrefix(String skipProjectPathPrefix) {
        this.skipProjectPathPrefix = skipProjectPathPrefix;
    }
    
    public String getDecryptProjectPathPrefix() {
        return decryptProjectPathPrefix;
    }
    
    public void setDecryptProjectPathPrefix(String decryptProjectPathPrefix) {
        this.decryptProjectPathPrefix = decryptProjectPathPrefix;
    }
}
