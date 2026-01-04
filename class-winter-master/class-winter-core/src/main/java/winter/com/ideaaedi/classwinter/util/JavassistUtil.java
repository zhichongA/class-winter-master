package winter.com.ideaaedi.classwinter.util;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.annotation.Annotation;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.exception.ClassWinterException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 字节码操作工具类
 *
 * @author {@link JustryDeng}
 * @since 2021/4/25 20:54:30
 */
public final class JavassistUtil {
    
    /**
     * 清空类中的方法体，并简单处理一下main,使提示密码无效
     *
     * @param classPool javassist的classPool
     * @param className 要修改的class类的全类名
     * @param encryptClassArgs 加密相关参数
     *
     * @return  处理后的类的字节
     * @throws NotFoundException 当清空的类中涉及到了某些其他的类，但是根本就没有引入（这些其他的类）相应的依赖时，就会抛出此异常(即:这个类本身就报错来着)
     *                           注：在编写项目A时，如果引入的依赖B的scope范围是provided时，那么当另一个项目X，引入A的依赖时，B是不会被依赖传递到X的，
     *                               就会出现上面的情况。
     *                           注：在某些其它情况下，也会抛出此异常。
     * @throws  CannotCompileException 统一封装异常
     */
    public static byte[] clearMethodBody(ClassPool classPool, String className, EncryptClassArgs encryptClassArgs)
            throws CannotCompileException, NotFoundException {
        CtClass ctClass;
        try {
            ctClass = classPool.getCtClass(className);
            if (ctClass.isFrozen()) {
                Logger.debug(JavassistUtil.class, "defrost class " + className);
                ctClass.defrost();
            }
            // 清空注解
            boolean cleanOverCLass = encryptClassArgs.isCleanAnnotationOverClazz();
            boolean cleanOverMethod = encryptClassArgs.isCleanAnnotationOverMethod();
            boolean cleanOverField = encryptClassArgs.isCleanAnnotationOverField();
            String toCleanAnnotationPrefix = encryptClassArgs.getToCleanAnnotationPrefix();
            
            if (cleanOverCLass || cleanOverMethod || cleanOverField) {
                try {
                    clearAnnotation(ctClass, cleanOverCLass, cleanOverMethod, cleanOverField, toCleanAnnotationPrefix);
                } catch (Exception ignore) {
                    Logger.warn(JavassistUtil.class, "clean className [" + className + "] annotation fail. ignore.");
                    // ignore
                }
            }
            boolean keepOriginArgsName = encryptClassArgs.isKeepOriginArgsName();
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod ctMethod : methods) {
                // 当前类中的方法 && 非构造方法
                if (ctMethod.getLongName().startsWith(className) && !ctMethod.getName().contains("<")) {
                    if (keepOriginArgsName) {
                        CodeAttribute codeAttribute = ctMethod.getMethodInfo().getCodeAttribute();
                        // 如果是接口，那么codeAttribute就是null； 方法体本来就是空的，codeAttribute.getCode()[0]就是-79
                        if (codeAttribute != null && codeAttribute.getCodeLength() != 1 && codeAttribute.getCode()[0] != 79) {
                            clearMethodBodyKeepOriginArgName(ctMethod);
                        }
                    } else {
                        ctMethod.setBody(null);
                    }
                    try {
                        ctMethod.insertBefore(
                                "System.out.println(\"\\n " + Constant.TIPS + " \\n\");"
                                // 也输出印章信息吧
                                + "\nSystem.out.println(\" Located in " + className + " \");"
                                + "\nSystem.out.println(\" " + Constant.SEAL + " \");"
                                + "\nSystem.exit(-1);"
                        );
                    } catch (CannotCompileException e) {
                        if ("no method body".equals(e.getMessage())) {
                            Logger.debug(JavassistUtil.class, "[" + ctMethod.getLongName() + "] no method body. ignore to add tips info.");
                            // ignore
                        } else {
                            throw e;
                        }
                    }
                }
            }
            return ctClass.toBytecode();
        } catch (IOException e) {
            throw new ClassWinterException(e);
        }
    }
    
    /**
     * 清空注解
     *
     * @param ctClass 目标类
     * @param cleanOverCLass 是否清空类上的注解
     * @param cleanOverMethod 是否清空方法上的注解
     * @param cleanOverField 是否清空字段上的注解
     */
    private static void clearAnnotation(CtClass ctClass, boolean cleanOverCLass, boolean cleanOverMethod,
                                        boolean cleanOverField, String toCleanAnnotationPrefix) {
        // 清空类上的注解
        if (cleanOverCLass) {
            removeAnnotationsAttribute(ctClass.getClassFile().getAttributes(), toCleanAnnotationPrefix);
        }
        // 清空方法上的注解
        if (cleanOverMethod) {
            CtMethod[] methods = ctClass.getDeclaredMethods();
            for (CtMethod ctMethod : methods) {
                removeAnnotationsAttribute(ctMethod.getMethodInfo().getAttributes(), toCleanAnnotationPrefix);
            }
        }
        // 清空字段上的注解
        if (cleanOverField) {
            CtField[] fields = ctClass.getDeclaredFields();
            for (CtField declaredField : fields) {
                removeAnnotationsAttribute(declaredField.getFieldInfo().getAttributes(), toCleanAnnotationPrefix);
            }
        }
    }
    
    /**
     * 移除注解属性
     *
     * @param attributeInfoList 属性信息集合
     *
     * @return 操作是否成功
     */
    @SuppressWarnings("UnusedReturnValue")
    private static boolean removeAnnotationsAttribute(List<AttributeInfo> attributeInfoList, String toCleanAnnotationPrefix) {
        if (attributeInfoList == null || attributeInfoList.size() == 0) {
            return true;
        }
        // 匹配注解全部删除
        if (StrUtil.isBlank(toCleanAnnotationPrefix)) {
            return attributeInfoList.removeIf(x -> {
                try {
                    return x instanceof AnnotationsAttribute;
                } catch (Exception e) {
                    return false;
                }
            });
        }
        // 删除指定注解
        StrUtil.strToSet(toCleanAnnotationPrefix, "\\|").forEach(x -> attributeInfoList.forEach(y -> {
            if (y instanceof AnnotationsAttribute) {
                AnnotationsAttribute annotationsAttribute = (AnnotationsAttribute) y;
                Annotation[] annotations = annotationsAttribute.getAnnotations();
                Annotation[] newAnnotations = Arrays.stream(annotations)
                        .filter(annotation -> !annotation.getTypeName().startsWith(x))
                        .toArray(Annotation[]::new);
                annotationsAttribute.setAnnotations(newAnnotations);
            }
        }));
        return true;
    }
    
    /**
     * 加载paths指定的.jar文件(或加载对应子孙目录下的所有jar包)
     *
     * @param classPool javassist的classPool
     * @param paths 要加载的文件夹或jar包
     */
    public static void loadJar(ClassPool classPool, String... paths) throws NotFoundException {
        if (paths == null) {
            return;
        }
        for (String path : paths) {
            loadJar(classPool, new File(path));
        }
    }
    
    /**
     * 加载指定的.class文件(或加载对应子孙目录下的所有.class文件)
     *
     * @param classPool javassist的classPool
     * @param paths 要加载的文件夹或.class文件
     */
    public static void loadClass(ClassPool classPool, String... paths) throws NotFoundException {
        if (paths == null) {
            return;
        }
        for (String path : paths) {
            loadClasses(classPool, new File(path));
        }
    }
    
    /**
     * 判断ctMethod是否代表main方法
     */
    private static boolean isMain(CtMethod ctMethod) throws NotFoundException {
        // 返回值校验
        boolean returnValueValid = "void".equalsIgnoreCase(ctMethod.getReturnType().getName());
        // 方法名、参数类型校验
        boolean nameArgTypeValid = ctMethod.getLongName().endsWith(".main(java.lang.String[])");
        // 访问修饰符校验
        boolean accessFlag = ctMethod.getMethodInfo().getAccessFlags() == 9;
        return returnValueValid && nameArgTypeValid && accessFlag;
    }
    
    
    /**
     * 清空方法体，并且保留原参数名
     *
     * @param ctMethod javassist的方法对象
     *
     * @throws CannotCompileException 编译异常
     * @throws NotFoundException 确实类异常
     */
    private static void clearMethodBodyKeepOriginArgName(CtMethod ctMethod) throws CannotCompileException, NotFoundException {
        CtClass ctClass = ctMethod.getDeclaringClass();
        if (ctClass.isFrozen()) {
            throw new ClassWinterException(ctClass.getName() + " class is frozen.");
        }
        CodeAttribute codeAttribute = ctMethod.getMethodInfo().getCodeAttribute();
        if (codeAttribute == null) {
            throw new ClassWinterException("no method body.");
        } else {
            CodeIterator iterator = codeAttribute.iterator();
            Javac jv = new Javac(ctClass);
            try {
                Bytecode bytecode = jv.compileBody(ctMethod, null);
                int maxStack = bytecode.getMaxStack();
                if (maxStack > codeAttribute.getMaxStack()) {
                    codeAttribute.setMaxStack(maxStack);
                }
                int maxLocals = bytecode.getMaxLocals();
                if (maxLocals > codeAttribute.getMaxLocals()) {
                    codeAttribute.setMaxLocals(maxLocals);
                }
                iterator.insertEx(bytecode.get());
                /*
                 * 移除ExceptionTable中的所有项.
                 *
                 * 注: 因为都把方法体置空了，里面的所有异常(try-catch)处理都没了，那么方法体当然需要要置空。
                 * 注: 一个方法的exception table中的entry数量等于这个方法内部catch了多少次异常,
                 *      如： ...catch (AbcException e)... ，那么entry个数为1
                 *      如： ...catch (AbcException|XyzException e)... ，那么entry个数为2
                 *      如： ...catch (AbcException e)... ，   ...catch (QwerException e)..那么entry个数为2
                 * <p>
                 * exception table 表示异常表，异常表是用于存储代码中涉及到的所有异常，每个类编译后，都会跟随一个异常表，如果发生异常，首先在异常表中查找对应的行（即代码中相应的 try{}catch(){}代码块），如果找到，则跳转到异常处理代码执行，如果没有找到，则返回（执行 finally 之后），并 copy 异常的应用给父调用者，接着查询父调用的异常表，以此类推。
                 */
                ExceptionTable exceptionTable = codeAttribute.getExceptionTable();
                if (exceptionTable != null) {
                    int size = exceptionTable.size();
                    for (int i = size - 1; i >= 0; i--) {
                        exceptionTable.remove(i);
                    }
                }
                ctMethod.getMethodInfo().rebuildStackMapIf6(ctClass.getClassPool(), ctClass.getClassFile2());
            } catch (CompileError compileError) {
                throw new CannotCompileException(compileError);
            } catch (BadBytecode badBytecode) {
                throw new CannotCompileException(badBytecode);
            }
        }
    }
    
    /**
     * 加载指定的jar包(或加载对应子孙目录下的所有jar包)
     *
     * @param pool javassist的ClassPool
     * @param dirOrFile 要加载的文件夹或.jar文件
     */
    private static void loadJar(ClassPool pool, File dirOrFile) throws NotFoundException {
        if (dirOrFile == null || !dirOrFile.exists()) {
            return;
        }
        List<File> jars = IOUtil.listFileOnly(dirOrFile, Constant.JAR_SUFFIX);
        for (File jar : jars) {
            pool.insertClassPath(jar.getAbsolutePath());
        }
    }
    
    /**
     * 加载指定的.class文件(或加载对应子孙目录下的所有.class文件)
     *
     * @param pool javassist的ClassPool
     * @param dirOrFile 要加载的文件夹或.class文件
     */
    private static void loadClasses(ClassPool pool, File dirOrFile) throws NotFoundException {
        if (dirOrFile == null || !dirOrFile.exists()) {
            return;
        }
        /*
         * 获取.class全类名对应的目录的根目录
         * 假设class文件全路径为/tmp/classes/com/niantou/iwork/core/Abc.class
         * 那么这里获取到的就是/tmp/classes
         */
        Set<String> classesRootDirSet = IOUtil.listFileOnly(dirOrFile, Constant.CLASS_SUFFIX)
                .stream().map(x -> resolveClassName(x.getAbsolutePath(), false))
                .collect(Collectors.toSet());
        for (String rootDir : classesRootDirSet) {
            /*
             * pathname – the path name of the directory or jar file. It must not end with a path separator ("/").
             * If the path name ends with "/*", then all the jar files matching the path name are inserted
             */
            pool.insertClassPath(rootDir);
        }
    }
    
    /**
     * 根据class的绝对路径解析出class全类名（或class全类名文件所在的目录路径）
     * <pre>
     * 假设文件的全路径名是这样的/tmp/class-winter-core/src/main/java/com/niantou/iwork/core/Abc.class,
     * 那么, 解析出来的class全类名即为com.niantou.iwork.core.Abc
     *       解析出来的class全类名文件所在的目录路径即为/tmp/class-winter-core/src/main/java
     * </pre>
     *
     * @param fileName class文件的绝对路径
     * @param classOrPath true-解析全类名;false-解析全类名文件所在的目录路径
     *
     * @return class所代表类的全类名(如com.aaa.bbb.Abc) 或者路径(如: /tmp/class-winter-core/src/main/java)
     */
    public static String resolveClassName(String fileName, boolean classOrPath) {
        // 去除后缀名
        String nonSuffixFileName = fileName.substring(0, fileName.length() - Constant.CLASS_SUFFIX.length());
        String classesFlag = File.separator + "classes" + File.separator;
        String libFlag = File.separator + "lib" + File.separator;
        String classPath;
        String className;
        int libFlagIndex = nonSuffixFileName.indexOf(libFlag, nonSuffixFileName.indexOf(Constant.TMP_DIR_SUFFIX));
        int classesFlagIndex = nonSuffixFileName.indexOf(classesFlag, nonSuffixFileName.indexOf(Constant.TMP_DIR_SUFFIX));
        // lib内的jar包
        if (libFlagIndex >= 0) {
            /*
             * 如果是对jar包(如my-project-1.0.0.jar)中的某些lib(如cglib-3.1.jar)还需要进行加密的话，那么根据本项目class-winter的解压逻辑，
             * 解压后的目录就会存在形如下面这样的解压临时路径
             * /tmp/abc/my-project-1.0.0__temp__/BOOT-INF/lib/cglib-3.1__temp__/com/aaa/bbb/Abc.class；
             * 所以要从lib开始找__temp__，
             * 然后再跳过__temp__本身的长度
             */
            className = nonSuffixFileName.substring(nonSuffixFileName.indexOf(Constant.TMP_DIR_SUFFIX, libFlagIndex) + Constant.TMP_DIR_SUFFIX.length() + 1);
        }
        // jar/war包xxx-INF/classes下的class文件
        else if (classesFlagIndex >= 0) {
            /*
             * nonSuffixFileName为/tmp/abc/my-project-1.0.0__temp__/BOOT-INF/classes/com/aspire/ssm/Aaaaaaaaa
             * 获取到的className为
             */
            className = nonSuffixFileName.substring(classesFlagIndex + classesFlag.length());
        }
        // jar包下的class文件
        else {
            className = nonSuffixFileName.substring(nonSuffixFileName.indexOf(Constant.TMP_DIR_SUFFIX) + Constant.TMP_DIR_SUFFIX.length() + 1);
        }
        classPath = nonSuffixFileName.substring(0, nonSuffixFileName.length() - className.length() - 1);
        return classOrPath ? className.replace(File.separator, ".") : classPath;
    }
    
}
