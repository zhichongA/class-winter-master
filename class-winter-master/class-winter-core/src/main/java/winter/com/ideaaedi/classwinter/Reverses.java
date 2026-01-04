package winter.com.ideaaedi.classwinter;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.exception.ClassWinterException;
import winter.com.ideaaedi.classwinter.executor.DecryptExecutor;
import winter.com.ideaaedi.classwinter.util.Cache;
import winter.com.ideaaedi.classwinter.util.Constant;
import winter.com.ideaaedi.classwinter.util.ExceptionUtil;
import winter.com.ideaaedi.classwinter.util.IOUtil;
import winter.com.ideaaedi.classwinter.util.JVMUtil;
import winter.com.ideaaedi.classwinter.util.JarUtil;
import winter.com.ideaaedi.classwinter.util.JavaagentCmdArgs;
import winter.com.ideaaedi.classwinter.util.Logger;
import winter.com.ideaaedi.classwinter.util.Pair;
import winter.com.ideaaedi.classwinter.util.StrUtil;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipFile;

/**
 * 反向解密(Agent类)
 *
 * @author {@link JustryDeng}
 * @since 2021/5/6 22:38:36
 */
public class Reverses {
    
    private static JavaagentCmdArgs javaagentCmdArgs = null;
    
    /**
     * pre-main 入口函数
     *
     * @param args
     *         参数
     * @param instrumentation
     *         This class provides services needed to instrumentation Java programming language code
     */
    public static void premain(String args, Instrumentation instrumentation) {
        // javaagent指定的参数
        if (javaagentCmdArgs == null) {
            try {
                javaagentCmdArgs = JavaagentCmdArgs.parseJavaagentCmdArgs(args);
                Logger.debug(Reverses.class,
                        "Parse raw javaagent args [" + args + "] to javaagentCmdArgs -> " + javaagentCmdArgs);
            } catch (Exception e) {
                Logger.error(Reverses.class, ExceptionUtil.getStackTraceMessage(e));
                exit(null);
            }
            // 是否启动debug，同步给Logger
            Logger.ENABLE_DEBUG.set(javaagentCmdArgs.isDebug());
        }
        // 禁用gHotSpotVMStructs函数，避免使用sa-jdi HSDB 来dump class，提高代码安全性
        try {
            long structs = JVMUtil.getSymbol("gHotSpotVMStructs");
            JVMUtil.putAddress(structs, 0);
            Logger.debug(Reverses.class, "Disabled VM Structs");
        } catch (Throwable e) {
            // 暂不支持在mac os下禁用sa-jdi HSDB
            Logger.warn(Reverses.class, "Disabled VM Structs fail. " + e.getMessage());
            if (Logger.ENABLE_DEBUG.get()) {
                Logger.debug(Reverses.class, ExceptionUtil.getStackTraceMessage(e));
            }
        }
        // 要忽略的解密处理逻辑的项目路径（ProtectionDomain.getCodeSource().getLocation().getPath()）
        Set<String> skipProjectPathPrefixSet = new HashSet<>();
        String skipProjectPathPrefix = javaagentCmdArgs.getSkipProjectPathPrefix();
        if (!StrUtil.isBlank(skipProjectPathPrefix)) {
            String[] projectPathItem = skipProjectPathPrefix.split("___");
            for (String projectPathPrefix : projectPathItem) {
                if (!StrUtil.isBlank(projectPathPrefix)) {
                    skipProjectPathPrefixSet.add(projectPathPrefix.trim().replace("\\","/"));
                }
            }
        }
        Logger.debug(Reverses.class, "skipProjectPathPrefixSet -> " + skipProjectPathPrefixSet);
        // 要解密处理逻辑的项目路径（ProtectionDomain.getCodeSource().getLocation().getPath()）
        Set<String> decryptProjectPathPrefixSet = new HashSet<>();
        String decryptProjectPathPrefix = javaagentCmdArgs.getDecryptProjectPathPrefix();
        if (!StrUtil.isBlank(decryptProjectPathPrefix)) {
            String[] projectPathItem = decryptProjectPathPrefix.split("___");
            for (String projectPathPrefix : projectPathItem) {
                if (!StrUtil.isBlank(projectPathPrefix)) {
                    decryptProjectPathPrefixSet.add(projectPathPrefix.trim().replace("\\","/"));
                }
            }
        }
        Logger.debug(Reverses.class, "decryptProjectPathPrefixSet -> " + decryptProjectPathPrefixSet);
        
        final AtomicBoolean firstExec = new AtomicBoolean(false);
        final Set<String> projectPathSet = new CopyOnWriteArraySet<>();
        
        // 在JVM加载class字节码之前，通过ClassFileTransformer修改字节码
        if (instrumentation != null) {
            /*
             * 特别注意: 并不是说jar包中的所有class都会走到下面的逻辑中。
             *          只有jar包中被用到的class才会走到下面的逻辑中，不被使用的class是不会走到下面的逻辑的。
             *          也就是说: 如果加密时，加密了一个根本没有使用的class,那么该javaagent加载时，该class根本不会走到下面的逻辑中，进而不会走解密逻辑。
             */
            instrumentation.addTransformer(new ClassFileTransformer() {
                @Override
                public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                        ProtectionDomain protectionDomain, byte[] classfileBuffer) {
                    if (className == null || protectionDomain == null || loader == null) {
                        return classfileBuffer;
                    }
                    // 获取类所在的项目运行路径
                    String projectPath = protectionDomain.getCodeSource().getLocation().getPath();
                    try {
                        projectPath = URLDecoder.decode(projectPath, StandardCharsets.UTF_8.name());
                    } catch (UnsupportedEncodingException ex) {
                        // ignore
                    }
    
                    try {
                        projectPath = JarUtil.getRootPath(projectPath);
                    } catch (ClassWinterException e) {
                        // 如果不需要试着解密该projectPath，那么返回
                        if (notPointToDecrypt(projectPath, decryptProjectPathPrefixSet)) {
                            return classfileBuffer;
                        }
                    } catch (Exception e) {
                        Logger.warn(Reverses.class, "JarUtil.getRootPath occur exception."
                                + " projectPath -> " + projectPath + ", e.getMessage() -> " + e.getMessage());
                        return classfileBuffer;
                    }
                    // 自动去掉嵌套路径前'nested:/'，后'/'
                    if (projectPath != null && projectPath.startsWith(Constant.NESTED_PREFIX)) {
                        projectPath = projectPath.substring(Constant.NESTED_PREFIX.length());
                        if (projectPath.endsWith(Constant.LINUX_FILE_SEPARATOR) && projectPath.length() > 1) {
                            projectPath = projectPath.substring(0, projectPath.length() - 1);
                        }
                    }
                    // 确保非windows环境下，获取到的projectPath为绝对路径
                    if (projectPath != null && !projectPath.startsWith(Constant.LINUX_FILE_SEPARATOR) && !isWindows()) {
                        projectPath = Constant.LINUX_FILE_SEPARATOR + projectPath;
                    }
                    if (!projectPathSet.contains(projectPath)) {
                        Logger.debug(Reverses.class, "Exist projectPath -> " +  projectPath);
                        projectPathSet.add(projectPath);
                    }
                    if (StrUtil.isEmpty(projectPath)) {
                        return classfileBuffer;
                    }
                    for (String skipProjectPathPrefix : skipProjectPathPrefixSet) {
                        if (projectPath.startsWith(skipProjectPathPrefix)) {
                            return classfileBuffer;
                        }
                    }
                    boolean settingDecryptProjectPath = decryptProjectPathPrefixSet.size() > 0;
                    if (settingDecryptProjectPath) {
                        if (notPointToDecrypt(projectPath, decryptProjectPathPrefixSet)) {
                            return classfileBuffer;
                        }
                    }
                    className = className.replace("/", ".").replace("\\", ".");
                    // 抽取印章
                    extractSeal(projectPath);
    
                    /// ============================================ 1.校验启动时是否输入了加密时指定的jvm参数   2.处理non-class文件
                    final String inputPwd = javaagentCmdArgs.getPassword();
                    if (firstExec.compareAndSet(false, true)) {
                        //  1.校验启动时是否输入了加密时指定的jvm参数
                        try {
                            byte[] jvmArgCheckBytes = IOUtil.readFileFromWorkbenchRoot(new File(projectPath),
                                    Constant.JVM_ARG_CHECK_FILE);
                            if (jvmArgCheckBytes == null) {
                                throw new IllegalStateException("jvmArgCheckBytes should not be null.");
                            }
                            jvmArgCheckBytes = DecryptExecutor.decrypt(projectPath, null,
                                    Base64.getDecoder().decode(jvmArgCheckBytes), inputPwd == null ? null :
                                            inputPwd.toCharArray());
                            String jvmArgCheck = new String(jvmArgCheckBytes, StandardCharsets.UTF_8);
                            Logger.debug(Reverses.class, "jvmArgCheck is -> " + jvmArgCheck);
                            if (StrUtil.isBlank(jvmArgCheck)) {
                                throw new IllegalStateException("jvmArgCheck should not be blank.");
                            }
                            if (!Constant.JVM_ARG_CHECK_NO_ITEM_CONTENT.equals(jvmArgCheck)) {
                                List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
                                Logger.debug(Reverses.class, "Parse jvm args -> " + inputArguments);
                                Arrays.stream(jvmArgCheck.split(Constant.WHITE_SPACE))
                                        .filter(str -> !StrUtil.isBlank(str))
                                        .forEach(jvmItem -> {
                                            boolean containTargetArg = false;
                                            for (String inputArgument : inputArguments) {
                                                if (jvmItem.equalsIgnoreCase(inputArgument)) {
                                                    containTargetArg = true;
                                                    break;
                                                }
                                            }
                                            if (!containTargetArg) {
                                                throw new IllegalArgumentException("Miss jvm arg -> " + jvmItem);
                                            }
                                        });
                            }
                        } catch (Exception e) {
                            Logger.error(Reverses.class, "jvm-arg-check fail.");
                            Logger.error(Reverses.class, ExceptionUtil.getStackTraceMessage(e));
                            exit(projectPath);
                            // 上一步System.exit(-1)就退出程序了，照理说是不会走到下面这里的(，不过为了以防万一，这里打出提醒, class-winter失效)
                            for (int i = 0; i < Constant.TEN; i++) {
                                Logger.error(Reverses.class, "!!!!!!!!!!! class-winter Invalidation. !!!!!!!!!!!");
                            }
                            return null;
                        }
                        
                        //  2.处理non-class文件
                        try {
                            // 解混淆 除了class文件外的其它文件
                            Map<String, Pair<byte[], byte[]>> resultMap =
                                    DecryptExecutor.unMaskNonClassFiles(projectPath, inputPwd == null ? null :
                                            inputPwd.toCharArray());
                            Map<String, byte[]> tmpMap = new HashMap<>(16);
                            resultMap.forEach((k, v) -> tmpMap.put(k, v.getLeft()));
                            String finalProjectPath = projectPath;
                            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                                try {
                                    Logger.debug(Reverses.class, "mask non-class files start.");
                                    JarUtil.rewriteZipEntry(new ZipFile(finalProjectPath), tmpMap);
                                    Logger.debug(Reverses.class, "mask non-class files end.");
                                } catch (IOException e) {
                                    // ignore
                                }
                            }));
                        } catch (Exception e) {
                            Logger.error(Reverses.class, ExceptionUtil.getStackTraceMessage(e));
                            Logger.error(Reverses.class, "Decrypt non-classes fail. ");
                            exit(projectPath);
                            // 上一步System.exit(-1)就退出程序了，照理说是不会走到下面这里的(，不过为了以防万一，这里打出提醒, class-winter失效)
                            for (int i = 0; i < Constant.TEN; i++) {
                                Logger.error(Reverses.class, "!!!!!!!!!!! class-winter Invalidation. !!!!!!!!!!!");
                            }
                            return null;
                        }
                    }

                    /// ============================================ 处理class文件
                    // 判断是否应该解密
                    if (DecryptExecutor.checklistContain(projectPath, className) && DecryptExecutor.verifySeal(projectPath, classfileBuffer)) {
                        //noinspection DuplicatedCode
                        try {
                            Logger.debug(Reverses.class, "Decrypt class[" + className + "] start.");
                            classfileBuffer = DecryptExecutor.process(projectPath, null, className,
                                    inputPwd == null ? null :
                                            inputPwd.toCharArray());
                            Logger.debug(Reverses.class, "Decrypt class[" + className + "] end.");
                            return classfileBuffer;
                        } catch (Exception e) {
                            Logger.error(Reverses.class, ExceptionUtil.getStackTraceMessage(e));
                            Logger.error(Reverses.class, "Decrypt class[" + className + "] fail. "
                                    + (StrUtil.isEmpty(inputPwd) ? e.getMessage() : "Please ensure your password is "
                                    + "correct."));
                            exit(projectPath);
                            // 上一步System.exit(-1)就退出程序了，照理说是不会走到下面这里的(，不过为了以防万一，这里打出提醒, class-winter失效)
                            for (int i = 0; i < Constant.TEN; i++) {
                                Logger.error(Reverses.class, "!!!!!!!!!!! class-winter Invalidation. !!!!!!!!!!!");
                            }
                            return null;
                        }
                    } else if (DecryptExecutor.checklistOfAllLibsContain(projectPath, className)
                            && DecryptExecutor.verifyLibSeal(projectPath, className, classfileBuffer)) {
                        String classWinterInfoDir = DecryptExecutor.getLibDirRelativePath(className);
                        //noinspection DuplicatedCode
                        try {
                            Logger.debug(Reverses.class, "Decrypt class[" + className + "] start.");
                            // lib中的密码在加密时，都统一处理好了，这里直接传null
                            classfileBuffer = DecryptExecutor.process(projectPath, classWinterInfoDir, className, null);
                            Logger.debug(Reverses.class, "Decrypt class[" + className + "] end.");
                            return classfileBuffer;
                        } catch (Exception e) {
                            Logger.error(Reverses.class, ExceptionUtil.getStackTraceMessage(e));
                            String lib = DecryptExecutor.parseLib(classWinterInfoDir);
                            Logger.error(Reverses.class, "Decrypt class[" + className + "] fail. \nPlease check:\n"
                                    + "\t1. Ensure 'Your lib " + lib + " need a input password ?'\n"
                                    + "\t2. Ensure 'Your lib " + lib + "'s password is correct ?'");
                            exit(projectPath);
                            // 上一步System.exit(-1)就退出程序了，照理说是不会走到下面这里的(，不过为了以防万一，这里打出提醒, class-winter失效)
                            for (int i = 0; i < Constant.TEN; i++) {
                                Logger.error(Reverses.class, "!!!!!!!!!!! class-winter Invalidation. !!!!!!!!!!!");
                            }
                            return null;
                        }
                    } else {
                        return classfileBuffer;
                    }
                }
            });
        }
    }
    
    /**
     * 抽取项目印章
     */
    private static void extractSeal(String projectPath) {
        if (Cache.sealCache.containsKey(projectPath)) {
            return;
        }
        try {
            byte[] sealByte = IOUtil.readFileFromWorkbenchRoot(new File(projectPath), Constant.SEAL_FILE);
            String sealContent = null;
            if (sealByte == null) {
                // 在没有指定解密路径的情况下，获取第一个印章作为本projectPath的印章
                if (Cache.firstSealCache != null) {
                    sealContent = Cache.firstSealCache;
                    Logger.debug(Reverses.class, "Use first-seal as curr project seal.");
                }
            } else {
                sealContent = new String(sealByte, StandardCharsets.UTF_8);
                Logger.debug(Reverses.class, "direct-seal of the project is -> " + sealContent);
            }
            if (sealContent == null) {
                Logger.error(Reverses.class, "Obtain project seal fail.");
                // 结束程序
                exit(projectPath);
            }
            Cache.sealCache.put(projectPath, sealContent);
            if (Cache.firstSealCache == null) {
                Cache.firstSealCache = sealContent;
                Cache.firstSealProjectPath = projectPath;
                Logger.debug(Reverses.class, "first-seal found. projectPath -> " + Cache.firstSealProjectPath);
                Logger.debug(Reverses.class, "first-seal found. sealContent -> " + Cache.firstSealCache);
            }
        } catch (Exception e) {
            Logger.error(Reverses.class, "Obtain project seal fail.");
            Logger.error(Reverses.class, ExceptionUtil.getStackTraceMessage(e));
            exit(projectPath);
            for (int i = 0; i < Constant.TEN; i++) {
                Logger.error(Reverses.class, "!!!!!!!!!!! class-winter Invalidation. !!!!!!!!!!!");
            }
        }
    }
    
    /**
     * 是否不需要试着解密projectPath指向的代码
     */
    private static boolean notPointToDecrypt(String projectPath, Set<String> decryptProjectPathPrefixSet) {
        boolean notNeedTryDecrypt = true;
        for (String decryptProjectPathPrefix : decryptProjectPathPrefixSet) {
            if (projectPath != null && projectPath.startsWith(decryptProjectPathPrefix)) {
                notNeedTryDecrypt = false;
                break;
            }
        }
        return notNeedTryDecrypt;
    }
    
    /**
     * 退出程序前停几秒，防止以tomcat启动时，太快关闭界面，观察不到日志
     */
    private static void exit(String projectPath) {
        try {
            if (projectPath != null) {
                Logger.error(Reverses.class, "Curr projectPath is -> " + projectPath);
            }
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ignore) {
        }
        System.exit(-1);
    }
    
    /**
     * 当前系统是否是windows
     *
     * @return 当前系统是否是windows
     */
    public static boolean isWindows() {
        return Optional.ofNullable(System.getProperty("os.name"))
                .map(String::toLowerCase)
                .map(x -> x.contains("windows"))
                .orElse(false);
    }
}
