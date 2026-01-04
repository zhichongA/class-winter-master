package winter.com.ideaaedi.classwinter.executor;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultText;
import org.xml.sax.SAXException;
import winter.com.ideaaedi.classwinter.Reverses;
import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.exception.ClassWinterException;
import winter.com.ideaaedi.classwinter.exception.JVMException;
import winter.com.ideaaedi.classwinter.util.Cache;
import winter.com.ideaaedi.classwinter.util.Constant;
import winter.com.ideaaedi.classwinter.util.EncryptClassArgs;
import winter.com.ideaaedi.classwinter.util.EncryptUtil;
import winter.com.ideaaedi.classwinter.util.ExceptionUtil;
import winter.com.ideaaedi.classwinter.util.FileOrderSupport;
import winter.com.ideaaedi.classwinter.util.IOUtil;
import winter.com.ideaaedi.classwinter.util.JVMUtil;
import winter.com.ideaaedi.classwinter.util.JarUtil;
import winter.com.ideaaedi.classwinter.util.JavaagentCmdArgs;
import winter.com.ideaaedi.classwinter.util.JavassistUtil;
import winter.com.ideaaedi.classwinter.util.Logger;
import winter.com.ideaaedi.classwinter.util.Pair;
import winter.com.ideaaedi.classwinter.util.PathUtil;
import winter.com.ideaaedi.classwinter.util.SimpleFileOrderSupport;
import winter.com.ideaaedi.classwinter.util.StrUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * java class加密
 *
 * @author {@link JustryDeng}
 * @since 2021/4/28 22:07:51
 */
public class EncryptExecutor {
    
    /**
     * 调用EncryptExecutor进行加密的“人”是否是maven插件
     */
    public boolean invokerIsPlugin = false;
    
    /** 本项目(class-winter)需要打包的代码 */
    public final Set<String> CLASS_WINTER_FILES = new HashSet<>();
    
    {
        CLASS_WINTER_FILES.add(Cache.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(JustryDeng.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(ClassWinterException.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(DecryptExecutor.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(Constant.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(EncryptUtil.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(ExceptionUtil.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(FileOrderSupport.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(IOUtil.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(JarUtil.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(JavaagentCmdArgs.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(Logger.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(Pair.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(PathUtil.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(SimpleFileOrderSupport.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(StrUtil.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(EncryptClassArgs.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(Reverses.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(Reverses.class.getName().replace(".", "/").replace("Reverses", "Reverses$1") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(JVMException.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
        CLASS_WINTER_FILES.add(JVMUtil.class.getName().replace(".", "/") + Constant.CLASS_SUFFIX);
    }
    
    /** 待加密的的jar/war包路径(如" /tmp/abc.jar) */
    private final String originJarOrWar;
    
    /** true-originJarOrWar是jar包；false-originJarOrWar是war包 */
    private final boolean originIsJar;
    
    /** 加密后生成的jar(or war)包的名称(如:abc-encrypted) */
    private final String finalName;
    
    /** 解压jar/war的根目录*/
    private final String targetRootDir;
    
    /** 解压的jar/war包里，lib的根目录*/
    private final String targetLibDir;
    
    /** 解压的jar/war包里，class文件的根目录*/
    private final String targetClassesDir;
    
    /**
     * 加密密码(若为空，则系统会自动生成)
     */
    private final String password;
    
    /**
     * 要加密的xml文件的zipEntry名前缀（如：）
     */
    private final Set<String> includeXmlPrefixSet;
    
    /**
     * 要排除加密的xml文件的zipEntry名前缀
     */
    private final Set<String> excludeXmlPrefixSet;
    
    /**
     * xml中要清除的（根节点的）子节点名称
     */
    private final Set<String> toCleanXmlChildElementNameSet;
    
    /**
     * 要加密的类的全类名前缀(也可以精确匹配)
     * <br />
     * 注：可携带参数， 写法同url后面待参数 url?key1=value1&key2=value2
     */
    private final LinkedHashSet<String> includePrefixSet;
    
    /**
     * 要加密的lib包(若不指定，则加密筛选范围默认仅从项目代码本身进行筛选加密)。
     * <pre>
     *  提示: 不论项目里面的依赖的传递关系是怎样的，这里只需要指定需要加密的jar包全称即可。
     *  说明:假设我们项目的pom依赖关系为:  myProject -> a -> b  -> c
     *      即: 我们的项目myProject直接依赖的是a,我们在pom中主动指定依赖的也是a,没有主动指定需要依赖b和c,但是由于maven依赖传递的原因，
     *          我们的项目实际上也依赖了b和c, 当我们把项目myProject打成jar包时，也会把a、b、c都打进jar包里。
     *           并且，将 a -> b -> c这样的层级关系平铺开了， 为
     *           │
     *           ├─BOOT-INF
     *           │  │
     *           │  └─lib
     *           │     └─a.jar
     *           │     └─b.jar
     *           │     └─c.jar
     *           这里，如果需要对a、b、c进行加密的话，只需要使includeLibSet值为["a.jar","b.jar","c.jar"]即可
     * </pre>
     */
    private final Set<String> includeLibSet;

    /**
     * 如果这次需要加密的lib本身就已经是被加密了的，那么这次不再对其进行加密
     */
    private final Set<String> protectedLibSet;
    
    /**
     * 不需要加密的类的全类名前缀(也可以精确匹配)
     * <pre>
     * includePrefixSet匹配的类中再排除excludePrefixSet匹配的类，即为最终会被加密的类
     * </pre>
     */
    private final Set<String> excludePrefixSet;
    
    /**
     * 已加密lib包所在根目录(，可为空，为空时自动根据当前是jar还是war，去包内对应找lib)
     * <br />
     * 特别注意：当指定此参数时，也会优先去jar/war内部找对应的lib包，找不到时，才会去此参数指定的根目录下找lib包
     * <pre>
     *  在一些外置lib的项目中，可能需要用到此参数；如果是内置lib，忽略此参数即可
     * </pre>
     */
    private final String alreadyProtectedRootDir;
    
    /**
     * 已经被class-winter加密过了的lib的信息
     * <pre>
     *    每个pair对象的属性介绍:
     *        pair-左: lib包名，如: abc.jar
     *        pair-右: 加密该lib时的密码
     *  想一想这种case：
     *      第三方给你提供了一个被class-winter加密过的jar包，你把这个jar包依赖进你的项目your-project中，
     *      你想正常的运行起来项目，那么你就需要用到这个属性了。
     * </pre>
     */
    private final Set<Pair<String, String>> alreadyProtectedLibSet;
    
    /**
     * 加密时，额外往加密上下文中添加的jar文件(或者jar文件所在的目录)。
     * <p>
     * 在进行加密时，某些类可能会因为缺失必要的依赖类而导致加密失败。就可以通过指定此字段的值(值应为某个jar或者某个目录的全路径)，
     * 这样一来，class-winter混淆目标jar时，除了加载要加载的jar/war内部的所有jar外，还会额外加载这里指定的jar(或者这里指定的目录下的所有子孙jar)，
     * 进而保证在加密时，不会因为确实依赖类而加密失败。
     * <p>
     * 注: 某些项目在打jar包时，是没有把其依赖的lib打进jar包中的，所以会出现[某些类可能会因为缺失必要的依赖类而导致加密失败]的情况。
     * <p>
     * 示例说明:
     * 假设加密时日志报warn说
     *    2021-06-11 20:24:31 [ WARN] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Ignore clear-method-body for  className [com.aspire.ssm.handler.impl.DefaultAesPreHandlerImpl], Cannot find 'org.apache.ibatis.executor.parameter.ParameterHandler'
     *    2021-06-11 20:24:31 [ WARN] winter.com.ideaaedi.classwinter.executor.EncryptExecutor: Ignore clear-method-body for className [com.aspire.ssm.interceptor.CustomizeMybatisPlugin], Cannot find 'org.apache.ibatis.plugin.Invocation'
     *  这说明了
     *      DefaultAesPreHandlerImpl类中使用到了org.apache.ibatis.executor.parameter.ParameterHandler类，
     *      DCustomizeMybatisPlugin类中使用到了org.apache.ibatis.plugin.Invocation类，
     *   进一步说明了
     *       要加密的jar包没有把mybatis的lib包打进去。
     *   这时，我们就可以通过指定此字段的值为mybatisjar包的全路径名（如/tmp/lib/mybatis-3.5.1.jar)来解决问题。
     */
    private final String supportFile;
    
    /**
     * 在启动jar包时，需要进行的jvm输入参数校验
     */
    private final Set<String> jvmArgCheckSet;
    
    /**
     * 要加密的lib jar文件的绝对路径  &  其对应的加密临时目录
     * 如:  /xyz/abc.jar     与 /xyz/abc__temp__
     */
    private final Map<String, String> libJarAndTmpDirMap = new HashMap<>(8);
    
    private EncryptExecutor(String originJarOrWar, boolean originIsJar, String finalName, String targetRootDir,
                            String targetLibDir, String targetClassesDir, String password,
                            Set<String> includeXmlPrefixSet, Set<String> excludeXmlPrefixSet, Set<String> toCleanXmlChildElementNameSet,
                            LinkedHashSet<String> includePrefixSet, Set<String> excludePrefixSet, Set<String> includeLibSet, Set<String> protectedLibSet,
                            String alreadyProtectedRootDir, Set<Pair<String, String>> alreadyProtectedLibSet,
                            String supportFile, Set<String> jvmArgCheckSet) {
        this.originJarOrWar = originJarOrWar;
        this.originIsJar = originIsJar;
        this.finalName = finalName;
        this.targetRootDir = targetRootDir;
        this.targetLibDir = targetLibDir;
        this.targetClassesDir = targetClassesDir;
        this.password = password;
        this.includeXmlPrefixSet = includeXmlPrefixSet;
        this.excludeXmlPrefixSet = excludeXmlPrefixSet;
        this.toCleanXmlChildElementNameSet = toCleanXmlChildElementNameSet;
        this.includePrefixSet = includePrefixSet;
        this.excludePrefixSet = excludePrefixSet;
        this.includeLibSet = includeLibSet;
        this.protectedLibSet = protectedLibSet;
        this.alreadyProtectedRootDir = alreadyProtectedRootDir;
        this.alreadyProtectedLibSet = alreadyProtectedLibSet;
        this.supportFile = supportFile;
        this.jvmArgCheckSet = jvmArgCheckSet;
    }
    
    /**
     * do
     *
     * @return  加密处理后生成的jar/war文件的位置
     */
    public String process() {
        // pre-step
        Logger.debug(EncryptExecutor.class, "Generate seal -> " + Constant.SEAL);
        
        // step0. 解压jar到jarRootDir下
        String lastStep = "step13";
        List<String> filePathList = JarUtil.unJarWar(originJarOrWar, targetRootDir);
        showProcess("step00", lastStep, "un-jar-war");
    
        // step1. 混淆xml
        File targetRootDirFile = new File(targetRootDir);
        if (includeXmlPrefixSet != null && includeXmlPrefixSet.size() > 0) {
            cleanXmlFiles(filePathList, targetRootDirFile);
        }
        showProcess("step01", lastStep, "clean-xml-files");
    
        // step2. jar中的某些lib可能也需要加密，这里先(将那些需要加密的lib)进行解压，（解压到对应的临时目录，以便后面进行加密处理）
        unLibJar();
        showProcess("step02", lastStep, "un-lib-jar");
        
        // step3. 找到需要加密混淆的class文件
        List<File> allFiles = IOUtil.listSubFile(targetRootDirFile, 1);
        List<EncryptClassArgs> allNeedEncryptedClassInfoList = filterClasses(allFiles);
        showProcess("step03", lastStep, "filter-classes");
        
        // step4.1. 根据原.class文件,生成加密后的.class文件,并存至Constant.ENCRYPTED_CLASSES_SAVE_DIR
        List<String> allAlreadyEncryptedClassFileList = encryptClasses(allNeedEncryptedClassInfoList,
                new File(targetRootDir, Constant.DEFAULT_ENCRYPTED_CLASSES_SAVE_DIR));
        // step4.2. 生成记录已加密class的全类名的清单文件
        generateChecklistFile(allAlreadyEncryptedClassFileList);
        showProcess("step04", lastStep, "encrypt-classes");
        
        // step5. 记录印章(在解密时，可通过 checklist + 印章 判断一个class是否是被class-winter加密过)
        generateSealFile();
        showProcess("step05", lastStep, "record-seal");

        // step6. 设置启动jar包时需要检查的jvm参数项
        settingJvmArgCheckItems();
        showProcess("step06", lastStep, "setting-jvm-arg-check-items");

        // step7. 混淆原.class文件(即:清空方法体)
        clearClassMethod(allNeedEncryptedClassInfoList);
        showProcess("step07", lastStep, "clear-class-method");
    
        // step8. 清除META-INF/maven下pom.xml中关于class-winter-plugin的相关信息
        clearWinterPluginInfo();
        showProcess("step08", lastStep, "clear-winter-plugin-info");
        
        // step9. 添加class-winter的用于javaagent解密的代码
        addClassWinterAgent();
        showProcess("step09", lastStep, "add-class-winter-agent");
        
        // step10. 汇总那些在本次加密前本身就已经是被class-winter混淆的lib的相关信息到当前项目中
        collectAlreadyProtectedLibInfo();
        showProcess("step10", lastStep, "collect-already-protected-lib-info");
        
        // step11. 与step2项对应，将临时目录还原为原来的lib(此时得到的lib是加密后的)
        doLibJar();
        showProcess("step11", lastStep, "do-lib-jar");
        
        // step12. 压缩targetRootDir目录为绝对文件路径名为generatedJarWar的jar(or war)包
        String generatedJarWar = generateJarWarPath(this.originIsJar);
        JarUtil.doJarWar(targetRootDir, generatedJarWar, new SimpleFileOrderSupport(filePathList));
        showProcess("step12", lastStep, "generate-jar-war-path");
        
        // step13. 删除临时目录
        IOUtil.delete(targetRootDirFile);
        showProcess(lastStep, lastStep, "delete-tmp-dir");
        return generatedJarWar;
    }
    
    /**
     * 从给定的文件中，找到要混淆的xml文件，并进行混淆
     *
     * @param filePathList
     *            文件路径集合
     * @param targetRootDirFile
     *            解压jar/war的根目录
     */
    private void cleanXmlFiles(List<String> filePathList, File targetRootDirFile) {
        Set<String> encryptNonClassFileSet = new HashSet<>();
        String targetRootBaseDir = targetRootDirFile.getAbsolutePath();
        // 筛选出需要加密的non-class文件
        Set<String> allNeedEncryptedNonClassFileSet = filePathList.stream()
                .filter(x -> x.endsWith(Constant.XML_SUFFIX))
                .map(filepath -> {
                    // zipEntryName
                    String zipEntryName = filepath.replace(targetRootBaseDir, "");
                    zipEntryName = zipEntryName.replace('\\', '/');
                    zipEntryName = zipEntryName.startsWith("/") ? zipEntryName.substring(1) : zipEntryName;
                    return zipEntryName;
                })
                .filter(zipEntryName -> {
                    if (excludeXmlPrefixSet != null) {
                        boolean shouldExclude = excludeXmlPrefixSet.stream().anyMatch(s -> StrUtil.startsWithOrRegMatched(s, zipEntryName));
                        if (shouldExclude) {
                            return false;
                        }
                    }
                    return includeXmlPrefixSet.stream().anyMatch(s -> StrUtil.startsWithOrRegMatched(s, zipEntryName));
                })
                .collect(Collectors.toSet());
        
        // 加密non-class文件
        for (String zipEntryName : allNeedEncryptedNonClassFileSet) {
            // 获取源数据
            byte[] bytes = IOUtil.readFileFromWorkbenchRoot(targetRootDirFile, zipEntryName);
            Logger.debug(EncryptExecutor.class, "Encrypt non-class[" + zipEntryName + "] start.");
            // 加密
            final byte[] encryptedBytes = EncryptUtil.encrypt(bytes, obtainPassword());
            // 将加密后的数据文件存储到指定位置
            IOUtil.toFile(encryptedBytes,
                    new File(this.targetRootDir + File.separator + Constant.DEFAULT_ENCRYPTED_NON_CLASSES_SAVE_DIR, zipEntryName),
                    true);
            Logger.debug(EncryptExecutor.class, "Encrypt non-class[" + zipEntryName + "] end.");
            encryptNonClassFileSet.add(zipEntryName);
        }
        
        // 生成清单文件
        String nonClassChecklist = String.join(Constant.COMMA, encryptNonClassFileSet);
        IOUtil.writeContentToFile(nonClassChecklist,
                new File(this.targetRootDir, Constant.ALREADY_ENCRYPTED_NON_CLASS_FILE_CHECKLIST_SAVE_FILE));
        
        // 清空原xml文件的关键节点
        for (String zipEntryName : encryptNonClassFileSet) {
            byte[] bytes = IOUtil.readFileFromWorkbenchRoot(targetRootDirFile, zipEntryName);
            if (bytes == null) {
                continue;
            }
            String cleanedXml = clearXml(new String(bytes, StandardCharsets.UTF_8), "\n\t\t\t" + Constant.TIPS + "\n\t\t\t" + Constant.SEAL, toCleanXmlChildElementNameSet);
            IOUtil.writeContentToFile(cleanedXml, new File(targetRootDir, zipEntryName));
        }
    }
    
    /**
     * showProcess
     *
     * @param completedStep
     *            已完成步骤
     * @param lastStep
     *            最后步骤
     * @param stepDesc
     *            步骤描述
     */
    private void showProcess(String completedStep, String lastStep, String stepDesc) {
        if (invokerIsPlugin) {
            Logger.simpleInfo(completedStep + "/" + lastStep + "\t" + stepDesc  + " completed.");
        } else {
            Logger.info(completedStep + "/" + lastStep + "\t" + stepDesc  + " completed.");
        }
    }
    
    /**
     * 清除META-INF/maven下pom.xml中关于class-winter-plugin的相关信息
     *
     * <pre>
     * {@code
     * <?xml version="1.0" encoding="UTF-8"?>
     * <project ...>
     *     ......
     *     <build>
     *         <plugins>
     *         	// 要把这部分清除 start
     *             <plugin >
     *                 <groupId>com.idea-aedi</groupId>
     *                 <artifactId>class-winter-maven-plugin</artifactId>
     *                 ...
     *             </plugin>
     *             // 要把这部分清除 end
     *         </plugins>
     *     </build>
     *
     * </project>
     * }
     * </pre>
     */
    private void clearWinterPluginInfo() {
        // 只有调用者是插件时，才去清除pom.xml中的class_winter-plugin插件信息
        if (!invokerIsPlugin) {
            return;
        }
        // 清除META-INF/maven下pom.xml中关于class-winter-plugin的相关信息
        File metaInfMavenFile = new File(this.targetRootDir, Constant.POM_XML_ROOT);
        if (!metaInfMavenFile.exists()) {
            Logger.warn(metaInfMavenFile.getAbsolutePath() + "non-exist. skip clearWinterPluginInfo.");
            return;
        }
        IOUtil.listFileOnly(metaInfMavenFile, "pom.xml").stream()
                .filter(x -> "pom.xml".equals(x.getName())).forEach(pomFile -> {
            SAXReader saxReader = new SAXReader();
            FileOutputStream fileOutputStream = null;
            try {
                Document document = saxReader.read(pomFile);
                // rootElement就是上面的<project></project>
                Element projectElement = document.getRootElement();
                if (projectElement == null) {
                    return;
                }
                Element buildElement = projectElement.element("build");
                if (buildElement == null) {
                    return;
                }
                Element pluginsElement = buildElement.element("plugins");
                if (pluginsElement == null) {
                    return;
                }
                @SuppressWarnings("unchecked")
                Iterator<Element> iterator = (Iterator<Element>)pluginsElement.elementIterator();
                while (iterator.hasNext()) {
                    Element pluginElement = iterator.next();
                    Element groupIdElement = pluginElement.element("groupId");
                    if (groupIdElement == null || groupIdElement.getStringValue() == null
                            || !Constant.GROUP_ID.equals(groupIdElement.getStringValue().trim())) {
                        continue;
                    }
                    Element artifactIdElement = pluginElement.element("artifactId");
                    if (artifactIdElement == null || artifactIdElement.getStringValue() == null
                            || !Constant.ARTIFACT_ID.equals(artifactIdElement.getStringValue().trim())) {
                        continue;
                    }
                    pluginsElement.remove(pluginElement);
                }
                
                // 删除原文件
                IOUtil.delete(pomFile);
                // 再生成
                fileOutputStream = new FileOutputStream(pomFile);
                XMLWriter xmlwriter = new XMLWriter(fileOutputStream);
                xmlwriter.write(document);
                xmlwriter.flush();
            } catch (Exception e) {
                throw new ClassWinterException("Delete class-winter-plugin info at pom.xml fail.", e);
            } finally {
                IOUtil.close(fileOutputStream);
            }
        });
    }
    
    /**
     * 采集本来就已经被class-winter混淆了的lib的信息
     */
    private void collectAlreadyProtectedLibInfo() {
        if (alreadyProtectedLibSet == null ||alreadyProtectedLibSet.size() == 0) {
            return;
        }
    
        /*
         * k - 创建出来的jar包对应的文件夹 如:  META-INF/winter/abc-1.0.0_jar/
         * v - jar包的checklist信息, 如: abc-1.0.0.jar对应的libChecklist值
         */
        Map<String, String> allChecklistInfoMap = new HashMap<>(8);
        alreadyProtectedLibSet.forEach(pair -> {
            String libJarFilepath = pair.getLeft();
            String password = pair.getRight();
            // 定位lib
            File lib = new File(targetLibDir, libJarFilepath);
            if (!lib.exists()) {
                if (StrUtil.isBlank(alreadyProtectedRootDir)) {
                    Logger.warn("do locate lib. " + libJarFilepath+ " non-exist. Under targetLibDir " + targetLibDir);
                } else {
                    lib = new File(alreadyProtectedRootDir, libJarFilepath);
                }
                if (lib.exists()) {
                    Logger.debug("do locate lib. " + libJarFilepath+ " exist. Under alreadyProtectedRootDir " + alreadyProtectedRootDir);
                } else {
                    Logger.warn("do locate lib. " + libJarFilepath+ " non-exist. Under alreadyProtectedRootDir " + alreadyProtectedRootDir);
                    return;
                }
            }
            // 形如: META-INF/winter/abc-1.0.0_jar/
            String libName = lib.getName();
            String nonSuffixLibName = libName.substring(0, libName.length() - 4);
            String dirRelativePathForLib = Constant.DEFAULT_ENCRYPTED_CLASSES_SAVE_DIR + nonSuffixLibName + "_jar" + Constant.LINUX_FILE_SEPARATOR;
            String dirAbsolutePathForLib = this.targetRootDir + File.separator + Constant.DEFAULT_ENCRYPTED_CLASSES_SAVE_DIR + nonSuffixLibName + "_jar" + File.separator;
    
            /*
             * my-project依赖的lib包本身可以是被class-winter混淆后的，但是这个lib包依赖的lib包不能是被class-winter混淆后
             * <p>
             * 注: 也就是说对lib的处理，只往下一层，不继续往下了。
             * 注: 所以如果符合预期的话，lib中的对应位置是没有Constant.CHECKLIST_OF_ALL_LIBS文件的，这时
             *     IOUtil.readFileFromWorkbenchRoot(lib, Constant.CHECKLIST_OF_ALL_LIBS)会返回null
             */
            byte[] checklistOfAllLibsByte = IOUtil.readFileFromWorkbenchRoot(lib, Constant.CHECKLIST_OF_ALL_LIBS);
            if (checklistOfAllLibsByte != null) {
                throw new ClassWinterException(libJarFilepath+"'s lib cannot be protected by class-winter.");
            }
            
            // 清单文件
            byte[] checklistByte = IOUtil.readFileFromWorkbenchRoot(lib, Constant.ALREADY_ENCRYPTED_CLASS_CHECKLIST_CLASSES_SAVE_FILE);
            if (checklistByte == null) {
                // 清单文件不存在，说明这个lib中没有任何类被混淆
                return;
            } else {
                String libChecklist = new String(checklistByte, StandardCharsets.UTF_8);
                IOUtil.writeContentToFile(libChecklist, new File(dirAbsolutePathForLib, Constant.CHECKLIST_CLASS_FILE_SIMPLE_NAME));
    
                // 所有lib的checklist汇总至checklistOfAllLibs
                allChecklistInfoMap.put(dirRelativePathForLib, libChecklist);
                
                // 将清单文件指向的加密后的class文件，从lib中挪动至当前项目的dirForLib下
                String[] libClassesArr = libChecklist.split(",");
                byte[] libClazzByte;
                for (String nonSuffixClassLongName : libClassesArr) {
                    libClazzByte = IOUtil.readFileFromWorkbenchRoot(lib, Constant.DEFAULT_ENCRYPTED_CLASSES_SAVE_DIR + nonSuffixClassLongName);
                    IOUtil.toFile(libClazzByte, new File(dirAbsolutePathForLib, nonSuffixClassLongName), true);
                }
            }
    
            // 印章
            byte[] libSealByte = IOUtil.readFileFromWorkbenchRoot(lib, Constant.SEAL_FILE);
            if (libSealByte == null) {
                // lib中没有印章文件
                throw new ClassWinterException("Cannot find seal in lib [" + libJarFilepath + "]. This lib is not "
                        + "protected by class-winter.");
            } else {
                IOUtil.writeContentToFile(new String(libSealByte, StandardCharsets.UTF_8), new File(dirAbsolutePathForLib,
                        Constant.SEAL_FILE_SIMPLE_NAME));
            }
            
            // 密码
            // libUserIfInputPwd代表的意思是: 对lib进行混淆的时候，用户是否主动指定了密码
            boolean libUserIfInputPwd = Boolean.parseBoolean(new String(IOUtil.readFileFromWorkbenchRoot(lib, Constant.USER_IF_INPUT_PWD)));
            Logger.debug(EncryptExecutor.class, "libUserIfInputPwd info of lib [" + libJarFilepath + "] -> " + libUserIfInputPwd);
            if (libUserIfInputPwd && StrUtil.isBlank(password)) {
                // 加密lib时，用户主动指定了密码，那么这里password就不应该为空
                throw new ClassWinterException("Password is required to decrypt lib [" + libJarFilepath + "]");
            }
            if (StrUtil.isBlank(password)) {
                byte[] libEncryptedPwdByte = IOUtil.readFileFromWorkbenchRoot(lib, Constant.PWD_WINTER);
                if (libEncryptedPwdByte == null) {
                    // lib中没有密码文件，请指定此lib的密码
                    throw new ClassWinterException("Cannot find pwd from lib [" + libJarFilepath + "]");
                } else {
                    // 因为写之前是加密写进去的，那么这里解密一下得到明文
                    // 存时，是加密存进去的； 这里读取时，(用lib的印章)解密一下
                    password = EncryptUtil.decrypt(new String(libEncryptedPwdByte, StandardCharsets.UTF_8),
                            new String(libSealByte, StandardCharsets.UTF_8).toCharArray());
                }
            }
            // 存时，加密存进去
            String encryptedPassword =EncryptUtil.encrypt(password, Constant.SEAL.toCharArray());
            IOUtil.writeContentToFile(encryptedPassword, new File(dirAbsolutePathForLib, Constant.PWD_WINTER_SIMPLE_NAME));
            // 由于这里已经处理了，将所有lib的密码(不论当初加密lib时密码是用户主动输入的还是系统自动产生的)，都当做系统自动产生的进行录入，所以这里写死为false
            IOUtil.writeContentToFile("false", new File(dirAbsolutePathForLib, Constant.USER_IF_INPUT_PWD_SIMPLE_NAME));
        });
        
        // 校验 - 当多个lib之间，发生加密类冲突时，快速失败
        Map<String, String> tmpMap = new HashMap<>(128);
        StringBuilder checklistOfAllLibs = new StringBuilder(64);
        allChecklistInfoMap.forEach((k, v) -> {
            if (StrUtil.isBlank(v)) {
                return;
            }
            // 当多个lib之间，发生加密类冲突时，快速失败
            Arrays.stream(v.split(",")).filter(item -> !StrUtil.isBlank(item)).forEach(nonSuffixClassLongName -> {
                String libDir = tmpMap.get(nonSuffixClassLongName);
                if (!StrUtil.isBlank(libDir)) {
                    // 当多个lib之间，发生加密类冲突时，快速失败
                    throw new ClassWinterException(String.format("protected class[%s] conflict. one lib is [%s], another lib is [%s]",
                            nonSuffixClassLongName, libDir, k));
                }
                tmpMap.put(nonSuffixClassLongName, k);
            });
            checklistOfAllLibs.append(k).append("=").append(v).append(Constant.LINE_SEPARATOR);
        });
        if (checklistOfAllLibs.length() > 0) {
            IOUtil.writeContentToFile(checklistOfAllLibs.toString(), new File(this.targetRootDir, Constant.CHECKLIST_OF_ALL_LIBS));
        }
    }
    
    /**
     * 解压jar包中需要加密的lib包
     */
    private void unLibJar() {
        if (includeLibSet == null || includeLibSet.size() == 0) {
            return;
        }
        // 罗列出targetRootDir下的所有.jar文件
        List<File> allJarFiles = IOUtil.listFileOnly(new File(targetRootDir), Constant.JAR_SUFFIX);
        // 筛选出需要加密的lib包
        List<File> neededEncryptedLibs = allJarFiles.stream().filter(jarFile -> !protectedLibSet.contains(jarFile.getName()) &&
                includeLibSet.stream().anyMatch(s -> StrUtil.startsWithOrRegMatched(s, jarFile.getName()))).collect(Collectors.toList());
        // 依次解密
        neededEncryptedLibs.forEach(jar -> {
            // 假设: jarAbsolutePath为  /xyz/abc.jar
            String jarAbsolutePath = jar.getAbsolutePath();
            // 那么: temDir就为  /xyz/abc__temp__
            String temDir = jarAbsolutePath.substring(0, jarAbsolutePath.length() - Constant.JAR_SUFFIX.length()) + Constant.TMP_DIR_SUFFIX;
            JarUtil.unJarWar(jarAbsolutePath, temDir);
            libJarAndTmpDirMap.put(jarAbsolutePath, temDir);
            // 删除原来的lib包
            IOUtil.delete(new File(jarAbsolutePath));
        });
    }
    
    /**
     * 将unLibJar()中解压出来的临时目录打包成jar包
     */
    private void doLibJar() {
        if (libJarAndTmpDirMap.isEmpty()) {
            return;
        }
        libJarAndTmpDirMap.forEach((jarFilePath, tmpDirPath) -> {
            // lib下的jar都不是spring-boot jar,都是些普通jar,不需要排序，即：不需要使用三个参数的方法
            JarUtil.doJarWar(tmpDirPath, jarFilePath);
            // 删除临时目录
            IOUtil.delete(new File(tmpDirPath));
        });
    }
    
    /**
     * 生成记录已加密class的全类名的清单文件
     *
     * @param classLongNameList
     *            已加密class的全类名集合
     */
    private void generateChecklistFile(List<String> classLongNameList) {
        if (classLongNameList == null || classLongNameList.isEmpty()) {
            return;
        }
        String content = String.join(Constant.COMMA, classLongNameList);
        // 将加密后的内容存储到指定位置下
        IOUtil.writeContentToFile(content, new File(targetRootDir, Constant.ALREADY_ENCRYPTED_CLASS_CHECKLIST_CLASSES_SAVE_FILE));
    }
    
    /**
     * 生成记录本次加密印章的文件
     */
    private void generateSealFile() {
        // 将加密后的内容存储到指定位置下
        IOUtil.writeContentToFile(Constant.SEAL, new File(targetRootDir, Constant.SEAL_FILE));
    }
    
    /**
     * 设置启动jar包时需要检查的jvm参数项
     */
    private void settingJvmArgCheckItems() {
        String content;
        if (jvmArgCheckSet == null || jvmArgCheckSet.size() == 0) {
            content = Constant.JVM_ARG_CHECK_NO_ITEM_CONTENT;
        } else {
            StringBuilder sb = new StringBuilder(64);
            for (String checkItem : jvmArgCheckSet) {
                sb.append(checkItem).append(Constant.WHITE_SPACE);
            }
            content = sb.toString();
        }
        Logger.debug(EncryptExecutor.class, "Setting jvmArgCheckItems -> " + content);
        byte[] encryptedBytes = EncryptUtil.encrypt(content.getBytes(StandardCharsets.UTF_8), obtainPassword());
        IOUtil.toFile(Base64.getEncoder().encode(encryptedBytes), new File(targetRootDir, Constant.JVM_ARG_CHECK_FILE), true);
    }
    
    /**
     * 添加class-winter的用于javaagent解密的代码
     */
    private void addClassWinterAgent() {
        String classWinterProjectRootDir = PathUtil.getProjectRootDir(this.getClass());
        int classWinterProjectRootDirLength = classWinterProjectRootDir.length();
        // 当class-winter未打成jar包时
        if (classWinterProjectRootDir.endsWith(Constant.CLASSES_DIR)) {
            List<File> allFileList = IOUtil.listSubFile(new File(classWinterProjectRootDir), 0);
            allFileList.forEach(file -> {
                String classLongNamePath = file.getAbsolutePath().substring(classWinterProjectRootDirLength);
                classLongNamePath = classLongNamePath.replace("\\", "/");
                File destFile = new File(this.targetRootDir, classLongNamePath);
                if (file.isFile() && CLASS_WINTER_FILES.stream().anyMatch(classLongNamePath::startsWith)) {
                    byte[] bytes = IOUtil.toBytes(file);
                    IOUtil.toFile(bytes, destFile, true);
                }
            });
        } else {
            // 当class-winter打包成jar包时
            if (classWinterProjectRootDir.endsWith(Constant.JAR_SUFFIX)) {
                    JarUtil.unJarWar(classWinterProjectRootDir, targetRootDir, false, CLASS_WINTER_FILES);
            } else {
                throw new ClassWinterException("Execute method addClassWinterAgent() fail. Cannot parse classWinterProjectRootDir [" + classWinterProjectRootDir + "].");
            }
        }
        
        // 把javaagent信息加入到MANIFEST.MF
        File manifest = new File(this.targetRootDir, "META-INF/MANIFEST.MF");
        String preMain = Constant.PREMAIN_CLASS + Reverses.class.getName();
        String[] origin = {};
        if (manifest.exists()) {
            String originContent = IOUtil.readContentFromFile(manifest);
            if (!StrUtil.isBlank(originContent) && originContent.contains(Constant.PREMAIN_CLASS)) {
                throw new ClassWinterException(this.originJarOrWar + " already exist Premain-Class at META-INF/MANIFEST.MF");
            }
            origin = originContent.split(System.lineSeparator());
        }
        // 在原来的启动函数行后面，插入pre-main指令
        String str = StrUtil.insertStrAfterLine(origin, preMain, "Main-Class:");
        str = str + System.lineSeparator();
        IOUtil.writeContentToFile(str, manifest);
    }
    
    /**
     * 获取生成的jar(or war)包的全路径名(如:/tmp/abc.jar)
     *
     * @param originIsJar
     *            true-生成jar路径; false-生成war路径
     *
     * @return  生成的jar(or war)包的全路径名(如:/tmp/abc.jar)
     */
    private String generateJarWarPath(boolean originIsJar) {
        int endIndex = originJarOrWar.lastIndexOf(Constant.LINUX_FILE_SEPARATOR);
        String jarRootParentDir;
        if (endIndex > 0) {
            jarRootParentDir = originJarOrWar.substring(0, endIndex + Constant.LINUX_FILE_SEPARATOR.length());
        } else {
            jarRootParentDir = Constant.LINUX_FILE_SEPARATOR;
        }
        return jarRootParentDir + finalName + (originIsJar ? Constant.JAR_SUFFIX : Constant.WAR_SUFFIX);
    }
    
    /**
     * 清空class文件的方法体，并保留参数信息
     *
     * @param allNeedEncryptedClassInfoList 需要加密的class相关参数集合
     */
    private void clearClassMethod(List<EncryptClassArgs> allNeedEncryptedClassInfoList) {
        // step0. 初始化javassist
        ClassPool pool = new ClassPool(true);
        // step1. 把所有可能涉及到的jar、.class添加进加载路径
        try {
            JavassistUtil.loadJar(pool, this.targetRootDir);
            JavassistUtil.loadClass(pool, this.targetRootDir);
            if (!StrUtil.isBlank(this.supportFile)) {
                JavassistUtil.loadJar(pool, this.supportFile);
            }
        } catch (NotFoundException e) {
            throw new ClassWinterException(e);
        }
        // step2. 修改class方法体,并保存文件
        allNeedEncryptedClassInfoList.forEach(encryptClassArgs -> {
            File classFile = encryptClassArgs.getEncryptClassFile();
            String className = JavassistUtil.resolveClassName(classFile.getAbsolutePath(), true);
            try {
                byte[] bts = JavassistUtil.clearMethodBody(pool, className, encryptClassArgs);
                IOUtil.toFile(bts, classFile, true);
            } catch (NotFoundException e) {
                // ignore
                Logger.warn(EncryptExecutor.class, "Ignore clear-method-body for className [" + className + "], Cannot find '" + e.getMessage() + "'");
            } catch (CannotCompileException e) {
                NotFoundException notFoundException = existNotFoundException(e, 5);
                if (notFoundException != null) {
                    // ignore
                    Logger.warn(EncryptExecutor.class, "Ignore clear-method-body for className [" + className + "], Cannot find '" + notFoundException.getMessage() + "'");
                } else {
                    throw new ClassWinterException(e);
                }
            }
        });
        pool.clearImportedPackages();
    }
    
    /**
     * 从异常链中寻找NotFoundException异常
     *
     * @param e 暴露异常
     * @param depth 向下寻找深度
     *
     * @return NotFoundException异常(null-则表示未找到)
     */
    private NotFoundException existNotFoundException(CannotCompileException e, int depth) {
        if (e == null) {
            return null;
        }
        if (depth <= 0) {
            return null;
        }
        
        // 从异常链中直接寻找NotFoundException
        Throwable cause = e.getCause();
        NotFoundException notFoundException = null;
        for (int i = 0; i < depth; i++) {
            if (cause instanceof NotFoundException) {
                notFoundException = (NotFoundException)cause;
                break;
            }
            if (cause == null) {
                break;
            }
            cause = cause.getCause();
        }
        if (notFoundException != null) {
            return notFoundException;
        }
        
        // 从异常信息中寻找NotFoundException
        cause = e;
        depth = depth + 1;
        for (int i = 0; i < depth; i++) {
            if (cause == null) {
                break;
            }
            notFoundException = findFromMessage(cause.getMessage());
            if (notFoundException != null) {
                break;
            }
            cause = cause.getCause();
        }
        return notFoundException;
    }
    
    /**
     * 从异常信息中寻找NotFoundException
     *
     * @param message 异常信息
     *
     * @return NotFoundException异常(null-则表示未找到)
     */
    private static NotFoundException findFromMessage(String message) {
        if (message == null) {
            return null;
        }
        if (!message.contains(Constant.NOT_FOUND_EXCEPTION_FLAG)) {
            return null;
        }
        String targetClassName = message.substring(message.indexOf(Constant.NOT_FOUND_EXCEPTION_FLAG) + (Constant.NOT_FOUND_EXCEPTION_FLAG).length()).trim();
        return new NotFoundException(targetClassName);
    }
    
    /**
     * 加密class文件，并将加密后的class文件放在savaDir里
     *
     * @param encryptClassInfoList 需要加密的class相关参数集合
     * @param savaDir 加密后的class文件的存储目录
     * @return  已经加密了的类的全类名(如: com.aaa.bbb.Abc)
     */
    private List<String> encryptClasses(List<EncryptClassArgs> encryptClassInfoList, File savaDir) {
        List<File> classFiles = encryptClassInfoList.stream().map(EncryptClassArgs::getEncryptClassFile).collect(Collectors.toList());
        // 保证目录存在
        JarUtil.guarantyDirExist(savaDir);
        classFiles.stream().map(File::getName).forEach(name -> {
            if (!name.endsWith(Constant.CLASS_SUFFIX)) {
                throw new ClassWinterException("classFiles must all be class file. file [" + name + "] is illegal.");
            }
        });
        // 加密后存储的位置
        List<String> encryptClasses = new ArrayList<>();
        // 加密，并将得到的加密后的class文件另存到savaDir目录中
        classFiles.forEach(classFile -> {
            String className = JavassistUtil.resolveClassName(classFile.getAbsolutePath(), true);
            byte[] bytes = IOUtil.toBytes(classFile);
            Logger.debug(EncryptExecutor.class, "Encrypt class[" + className + "] start.");
            bytes = EncryptUtil.encrypt(bytes, obtainPassword());
            // 将加密后的内容存储到指定位置下
            IOUtil.toFile(bytes, new File(savaDir, className), true);
            Logger.debug(EncryptExecutor.class, "Encrypt class[" + className + "] end.");
            encryptClasses.add(className);
        });
        return encryptClasses;
    }
    
    /**
     * 获取密码
     *
     * @return  密码
     */
    private char[] obtainPassword() {
        if (Cache.passwordCacheForEncrypt.containsKey(this.originJarOrWar)) {
            return Cache.passwordCacheForEncrypt.get(this.originJarOrWar);
        }
        boolean passwordIsBlank = StrUtil.isBlank(this.password);
        if (passwordIsBlank) {
            char[] generatedPwd = EncryptUtil.generateCharArr(new SecureRandom().nextInt(500) + 100);
            // 将自动生成的密码记录到文件中
            // 存时，加密存进去
            String encryptedGeneratedPwd = EncryptUtil.encrypt(new String(generatedPwd), Constant.SEAL.toCharArray());
            IOUtil.writeContentToFile(encryptedGeneratedPwd, new File(targetRootDir, Constant.PWD_WINTER));
            // 放入缓存
            Cache.passwordCacheForEncrypt.put(this.originJarOrWar, generatedPwd);
        } else {
            Cache.passwordCacheForEncrypt.put(this.originJarOrWar, this.password.toCharArray());
        }
        // 记录加密时，用户是否输入了密码
        IOUtil.writeContentToFile(passwordIsBlank ? "false" : "true", new File(targetRootDir, Constant.USER_IF_INPUT_PWD));
        return Cache.passwordCacheForEncrypt.get(this.originJarOrWar);
    }
    
    /**
     * 找出所有需要加密的class文件
     *
     * @param allFileList 所有文件
     * @return  需要加密的class信息
     */
    private List<EncryptClassArgs> filterClasses(List<File> allFileList) {
        return allFileList.stream()
                .filter(file -> file.getName().endsWith(Constant.CLASS_SUFFIX))
                .map(file -> {
                    // 全类名
                    String classLongName = JavassistUtil.resolveClassName(file.getAbsolutePath(), true);
                    if (excludePrefixSet != null) {
                        for (String excludePrefix : excludePrefixSet) {
                            if (StrUtil.startsWithOrRegMatched(excludePrefix, classLongName)) {
                                // 排除
                                return null;
                            }
                        }
        
                    }
                    if (includePrefixSet != null) {
                        for (String includePrefix : includePrefixSet) {
                            Pair<String, Map<String, String>> pair = extraPrefixAndParam(includePrefix);
                            String pureIncludePrefix = pair.getLeft();
                            Map<String, String> params = pair.getRight();

                            if (StrUtil.startsWithOrRegMatched(pureIncludePrefix, classLongName)) {
                                // 包含
                                return EncryptClassArgs.create(file,
                                        StrUtil.isTrueDefault(params.get(Constant.CLEAN_CLASS_ANNOTATION_PARAM_NAME), false),
                                        StrUtil.isTrueDefault(params.get(Constant.CLEAN_METHOD_ANNOTATION_PARAM_NAME), false),
                                        StrUtil.isTrueDefault(params.get(Constant.CLEAN_FIELD_ANNOTATION_PARAM_NAME), false),
                                        params.get(Constant.TO_CLEAN_ANNOTATION_PREFIX),
                                        StrUtil.isTrueDefault(params.get(Constant.KEEP_ORIGIN_ARGS_NAME), true)
                                );
                            }
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 从includePrefix中抽取纯净的前缀及相关参数
     * <br />
     * 注：参考从url中获取参数
     *
     * @param includePrefix 要加密的类前缀
     *                      <p>
     *                      可携带参数，如：com.ideaaedi.demo.service?key1=value1&key2=value2
     *
     * @return <ul>
     *     <li>左-要加密的类前缀</li>
     *     <li>右-相关加密参数信息</li>
     * </ul>
     */
    private Pair<String, Map<String, String>> extraPrefixAndParam(String includePrefix) {
        Map<String, String> param = new HashMap<>(8);
        int index = includePrefix.indexOf("?");
        if (index < 0) {
           return Pair.of(includePrefix, param);
        }
        String pureIncludePrefix = includePrefix.substring(0, index);
        String paramStr = includePrefix.substring(index + 1);
        for (String keyValue : paramStr.split("&")) {
            int equalIndex = keyValue.indexOf("=");
            if (equalIndex < 0) {
                continue;
            }
            String key = keyValue.substring(0, equalIndex).trim();
            String value = keyValue.substring(equalIndex + 1).trim();
            if (StrUtil.isBlank(key) || StrUtil.isBlank(value)) {
                continue;
            }
            param.put(key, value);
        }
        return Pair.of(pureIncludePrefix, param);
    }
    
    /**
     * 清空xml中指定的一级子节点内容，并以指定的内容进行注释填充
     *
     * @param xmlContent
     *            原xml内容
     * @param paddingComment
     *            填充注释内容
     * @param toClearFirstNodeNameSet
     *            一级子节点名称
     * @return  清空后的xml内容
     */
    private String clearXml(String xmlContent, String paddingComment, Set<String> toClearFirstNodeNameSet) {
        if (toClearFirstNodeNameSet == null || toClearFirstNodeNameSet.size() == 0) {
            return xmlContent;
        }
        if (xmlContent == null || xmlContent.trim().length() == 0) {
            return xmlContent;
        }
        ByteArrayOutputStream os = null;
        try {
            SAXReader reader = new SAXReader();
            // 不校验xml头部
            reader.setFeature(Constant.XERCES_FEATURE_PREFIX + Constant.LOAD_EXTERNAL_DTD_FEATURE, false);
            Document document = reader.read(new StringReader(xmlContent));
            Element rootElement = document.getRootElement();
            if (rootElement == null) {
                return xmlContent;
            }
            //noinspection rawtypes
            Iterator iterator = rootElement.elementIterator();
            if (iterator == null) {
                return null;
            }
            while (iterator.hasNext()) {
                Element element = (Element) iterator.next();
                if (element == null) {
                    continue;
                }
                if (!toClearFirstNodeNameSet.contains(element.getName())) {
                    continue;
                }
                element.clearContent();
                element.add(new DefaultText("\n\t\t"));
                element.addComment(paddingComment + "\n\t\t");
                element.add(new DefaultText("\n\t"));
            }
            os = new ByteArrayOutputStream();
            XMLWriter xmlWriter = new XMLWriter(os);
            xmlWriter.write(document);
            xmlWriter.flush();
            xmlWriter.close();
            return new String(os.toByteArray(), StandardCharsets.UTF_8);
        } catch (SAXException | DocumentException |IOException e) {
            throw new UndeclaredThrowableException(e,
                    String.format("clear xmlContent exception. \nxmlContent -> %s, \npaddingComment -> %s, \ntoClearFirstNodeNameSet -> %s",
                            xmlContent, paddingComment, toClearFirstNodeNameSet));
        } finally {
            IOUtil.close(os);
        }
    }
    
    
    
    @Override
    public String toString() {
        return "EncryptExecutor{" +
                "originJarOrWar='" + originJarOrWar + '\'' +
                ", originIsJar=" + originIsJar +
                ", finalName='" + finalName + '\'' +
                ", targetRootDir='" + targetRootDir + '\'' +
                ", targetLibDir='" + targetLibDir + '\'' +
                ", targetClassesDir='" + targetClassesDir + '\'' +
                // 密码脱敏
                ", password='" + (StrUtil.isBlank(password) ? null : "******") + '\'' +
                ", supportFile='" + supportFile + '\'' +
                ", jvmArgCheckSet='" + jvmArgCheckSet + '\'' +
                ", includePrefixSet=" + includePrefixSet +
                ", excludePrefixSet=" + excludePrefixSet +
                ", includeXmlPrefixSet=" + includeXmlPrefixSet +
                ", excludeXmlPrefixSet=" + excludeXmlPrefixSet +
                ", toCleanChildElementNameSet=" + toCleanXmlChildElementNameSet +
                ", includeLibSet=" + includeLibSet +
                ", protectedLibSet=" + protectedLibSet +
                ", alreadyProtectedLibSet=" + alreadyProtectedLibSet +
                '}';
    }
    
    /**
     * builder for EncryptExecutor
     */
    public static EncryptExecutor.Builder builder() {
        return new EncryptExecutor.Builder();
    }
    
    public static class Builder {
        
        private String originJarOrWar;
        
        private String finalName;
        
        private String password;
        
        private String includePrefix;
        
        private String excludePrefix;
        
        private String includeXmlPrefix;
        
        private String excludeXmlPrefix;
        
        private String toCleanXmlChildElementName;
        
        private String includeLibs;
    
        private String alreadyProtectedRootDir;
        
        private String alreadyProtectedLibs;
        
        private String supportFile;
        
        private String jvmArgCheck;
        
        private Boolean debug = false;
        
        private String tips;
    
        public Builder originJarOrWar(String originJarOrWar) {
            this.originJarOrWar = originJarOrWar;
            return this;
        }
    
        public Builder finalName(String finalName) {
            this.finalName = finalName;
            return this;
        }
        
        public Builder password(String password) {
            this.password = password;
            return this;
        }
        
        public Builder includePrefix(String includePrefix) {
            this.includePrefix = includePrefix;
            return this;
        }
        
        public Builder excludePrefix(String excludePrefix) {
            this.excludePrefix = excludePrefix;
            return this;
        }
    
        public Builder includeXmlPrefix(String includeXmlPrefix) {
            this.includeXmlPrefix = includeXmlPrefix;
            return this;
        }
        
        public Builder excludeXmlPrefix(String excludeXmlPrefix) {
            this.excludeXmlPrefix = excludeXmlPrefix;
            return this;
        }
        
        public Builder toCleanXmlChildElementName(String toCleanXmlChildElementName) {
            this.toCleanXmlChildElementName = toCleanXmlChildElementName;
            return this;
        }
        
        public Builder includeLibs(String includeLibs) {
            this.includeLibs = includeLibs;
            return this;
        }
        
        public Builder alreadyProtectedLibs(String alreadyProtectedLibs) {
            this.alreadyProtectedLibs = alreadyProtectedLibs;
            return this;
        }
        
        public Builder alreadyProtectedRootDir(String alreadyProtectedRootDir) {
            this.alreadyProtectedRootDir = alreadyProtectedRootDir;
            return this;
        }
        
        public Builder supportFile(String supportFile) {
            this.supportFile = supportFile;
            return this;
        }
        
        public Builder jvmArgCheck(String jvmArgCheck) {
            this.jvmArgCheck = jvmArgCheck;
            return this;
        }
        
        public Builder debug(Boolean debug) {
            this.debug = debug;
            return this;
        }
        
        public Builder tips(String tips) {
            this.tips = tips;
            return this;
        }
        
        public EncryptExecutor build() {
            // ---- 其他配置
            // 1. debug模式
            Logger.ENABLE_DEBUG.set(this.debug != null && this.debug);
            // 2. 错误启动jar时，System.out输出的提示信息
            if (!StrUtil.isEmpty(tips)) {
                Constant.TIPS.setLength(0);
                Constant.TIPS.append(tips);
            }
            
            // ---- 参数校验
            // 1. originJarOrWar不能为空
            if (StrUtil.isEmpty(originJarOrWar)) {
                throw new ClassWinterException("originJarOrWar cannot be empty.");
            }
            // 2. originJarOrWar必须是jar文件或者是war文件
            if (!originJarOrWar.endsWith(Constant.JAR_SUFFIX) && !originJarOrWar.endsWith(Constant.WAR_SUFFIX)) {
                throw new ClassWinterException("originJarOrWar must be Jar or War file.");
            }
            // 3. includePrefix不能为空
            if (StrUtil.isEmpty(includePrefix)) {
                throw new ClassWinterException("includePrefix cannot be empty.");
            }
            // 4. supportFile如果不为空， 那么必须存在且只能为件文夹或者.jar文件
            if (!StrUtil.isBlank(supportFile)) {
                File tmpFile = new File(supportFile);
                if (!tmpFile.exists()) {
                    throw new ClassWinterException("supportFile ["+supportFile+"] non-exist.");
                }
                // 文件的话，必须是.jar文件
                if (tmpFile.isFile() && !supportFile.endsWith(Constant.JAR_SUFFIX)) {
                    throw new ClassWinterException("supportFile must be dir or a jar file.");
                }
                
            }
            // 5. 密码不能包含空格或者逗号
            if (!StrUtil.isBlank(password)) {
                this.password = this.password.trim();
                if (password.contains(Constant.WHITE_SPACE) || password.contains(",")) {
                    throw new ClassWinterException("password cannot contain whitespace or comma.");
                }
            }
            
            // ---- 构建EncryptExecutor对象
            File originFile = new File(originJarOrWar);
            if (!originFile.exists()) {
                throw new ClassWinterException("cannot find file [" + originJarOrWar + "].");
            }
            // 文件路径分隔符统一为 /
            originJarOrWar = originFile.getAbsolutePath().replace(File.separator, Constant.LINUX_FILE_SEPARATOR);
    
            // 判断originJarOrWar代表的文件是jar还是war
            boolean originIsJar = JarUtil.isJarOrWar(originJarOrWar);
            
            if (StrUtil.isEmpty(this.finalName)) {
                String originFileName = originFile.getName();
                int idx = originFileName.lastIndexOf(".");
                this.finalName(originFileName.substring(0, idx) + "-encrypted");
            }
            // 无论是.war还是.jar，长度都是4
            String targetRootDir = originJarOrWar.substring(0, originJarOrWar.length() - 4) + Constant.TMP_DIR_SUFFIX;
            String targetLibDir = String.join(File.separator, targetRootDir, originIsJar ? Constant.BOOT_INF :
                    Constant.WEB_INF, Constant.LIB);
            String targetClassesDir = String.join(File.separator, targetRootDir, originIsJar ? Constant.BOOT_INF :
                    Constant.WEB_INF, Constant.CLASSES);

            Set<String> includePrefixSet = StrUtil.strToSet(includePrefix);
            LinkedHashSet<String> includePrefixSortSet = sortByPurePrefixLenthDesc(includePrefixSet);
    
            Set<String> excludePrefixSet = StrUtil.strToSet(excludePrefix);
            Set<String> includeLibSet = StrUtil.strToSet(includeLibs);
            Set<Pair<String, String>> alreadyProtectedLibSet = parseAlreadyProtectedLibs();

            Set<String> matchAlreadyProtectedLibSet = new HashSet<>();

            // 如果这次需要加密的lib本身就已经是被加密了的，那么这次不再对其进行加密
            alreadyProtectedLibSet.stream().map(Pair::getLeft).forEach(lib -> {
                if (includeLibSet.stream().anyMatch(s -> StrUtil.startsWithOrRegMatched(s, lib))) {
                    Logger.warn(EncryptExecutor.class, "Ignore includeLibs item [" + lib + "], because this item already be protected by class-winter.");
                    matchAlreadyProtectedLibSet.add(lib);
                }
            });
            
            for (String jarFile : includeLibSet) {
                // jarFile 形如 abc-1.0.0.jar
                if (jarFile.endsWith(Constant.JAR_SUFFIX)) {
                    continue;
                }
                throw new ClassWinterException("includeLibs format must be shaped like xxx1.jar[,xxx2.jar,xxx3.jar]");
            }
            
            // xml相关参数解析
            Set<String> includeXmlPrefixSet = StrUtil.strToSet(includeXmlPrefix);
            Set<String> jvmArgCheckSet = StrUtil.strToSet(jvmArgCheck);
            Set<String> excludeXmlPrefixSet = StrUtil.strToSet(excludeXmlPrefix);
            toCleanXmlChildElementName = StrUtil.isBlank(toCleanXmlChildElementName) ? Constant.DEFAULT_XML_NODE_NAMES : toCleanXmlChildElementName;
            Set<String> toCleanXmlChildElementNameSet = StrUtil.strToSet(toCleanXmlChildElementName);
            
            return new EncryptExecutor(this.originJarOrWar, originIsJar, this.finalName, targetRootDir, targetLibDir, targetClassesDir,
                    password, includeXmlPrefixSet, excludeXmlPrefixSet, toCleanXmlChildElementNameSet, includePrefixSortSet,
                    excludePrefixSet, includeLibSet,matchAlreadyProtectedLibSet, alreadyProtectedRootDir, alreadyProtectedLibSet, this.supportFile, jvmArgCheckSet);
        }
    
        /**
         * 按照(不带参数的)前缀的长度倒序
         *
         * @param includePrefixSet 前缀集合
         *
         * @return  倒序的前缀集合
         */
        private static LinkedHashSet<String> sortByPurePrefixLenthDesc(Set<String> includePrefixSet) {
            List<String> includePrefixSortList = new ArrayList<>(includePrefixSet);
            includePrefixSortList.sort(Comparator.comparingInt(x -> {
                int idx = x.indexOf("?");
                if (idx < 0) {
                    return x.length();
                }
                return x.substring(0, idx).length();
            }));
            Collections.reverse(includePrefixSortList);
            return new LinkedHashSet<>(includePrefixSortList);
        }
    
        /**
         * 解析alreadyProtectedLibs信息为Set<Pair<String, String>>
         */
        private Set<Pair<String, String>> parseAlreadyProtectedLibs() {
            Set<Pair<String, String>> alreadyProtectedLibSet = new HashSet<>();
            Set<String> alreadyProtectedLibInfoSet = StrUtil.strToSet(alreadyProtectedLibs);
            for (String info : alreadyProtectedLibInfoSet) {
                int idx = info.indexOf(":");
                if (idx >= 0) {
                    alreadyProtectedLibSet.add(Pair.of(info.substring(0, idx), info.substring(idx + 1)));
                } else {
                    alreadyProtectedLibSet.add(Pair.of(info, null));
                }
            }
            return alreadyProtectedLibSet;
        }
    }
}
