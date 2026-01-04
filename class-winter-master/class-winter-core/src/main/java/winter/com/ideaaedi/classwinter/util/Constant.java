package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 常量类
 *
 * @author {@link JustryDeng}
 * @since 2021/4/23 0:36:14
 */
public interface Constant {
    
    /**
     * 被混淆了的内容的提示信息
     */
    StringBuffer TIPS = new StringBuffer("ERROR !!!!!!!!!!! Jar(or War) has been protected by class-winter. Please use javaagent re-start project. !!!!!!!!!!!");
    
    /**
     * 印章， 若class中存在此印章内容，也能说明该class被class-winter加密过
     */
    String SEAL = "At " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME).replace("T"," ") + "\t" + "Random-Character " + new String(EncryptUtil.generateCharArr(32));
    
    /**
     * 默认需要加密的xml一级节点名称
     */
    String DEFAULT_XML_NODE_NAMES = "resultMap,sql,insert,update,delete,select";
    
    /**
     * 解压jar/war包时，临时目录后缀名
     */
    String TMP_DIR_SUFFIX = "__temp__";
    
    /**
     * lib
     */
    String LIB = "lib";
    
    /**
     * classes
     */
    String CLASSES = "classes";
    
    /**
     * 默认的class-winter加密存储目录base
     */
    String DEFAULT_ENCRYPTED_BASE_SAVE_DIR = "META-INF/winter/";
    
    /**
     * 默认的class-winter加密classes相关信息的存储目录
     */
    String DEFAULT_ENCRYPTED_CLASSES_SAVE_DIR = "META-INF/winter/classes/";
    
    /**
     * 默认的class-winter加密其它文件(如.yml .yaml .xml .properties)相关信息的存储目录
     */
    String DEFAULT_ENCRYPTED_NON_CLASSES_SAVE_DIR = "META-INF/winter/non-classes/";
    
    /**
     * 记录已加密.class的全类名的清单文件
     */
    String ALREADY_ENCRYPTED_CLASS_CHECKLIST_CLASSES_SAVE_FILE = "META-INF/winter/checklist.classes.winter";
    
    /**
     * 记录已加密的非class文件的条目名清单文件(内容形如: BOOT-INF/classes/application.properties,BOOT-INF/classes/application-dev.properties)
     */
    String ALREADY_ENCRYPTED_NON_CLASS_FILE_CHECKLIST_SAVE_FILE = "META-INF/winter/checklist.non-classes.winter";
    
    /**
     * 记录已加密.class的全类名的清单文件
     */
    String CHECKLIST_CLASS_FILE_SIMPLE_NAME = "checklist.classes.winter";
    
    /**
     * 记录本次加密印章的文件
     */
    String SEAL_FILE = "META-INF/winter/seal.winter";
    
    /**
     * 记录本次加密印章的文件
     */
    String SEAL_FILE_SIMPLE_NAME = "seal.winter";
    
    /**
     * 记录启动jar包时需要检查的输入参数项文件 文件名
     */
    String JVM_ARG_CHECK_FILE_SIMPLE_NAME = "jscf.winter";
    
    /**
     * 记录启动jar包时需要检查的输入参数项文件
     */
    String JVM_ARG_CHECK_FILE = DEFAULT_ENCRYPTED_BASE_SAVE_DIR + JVM_ARG_CHECK_FILE_SIMPLE_NAME;
    
    /**
     * 当JVM_ARG_CHECK_FILE无检查项时，填写的默认值
     */
    String JVM_ARG_CHECK_NO_ITEM_CONTENT = "NON_CHECK_ITEM_AT_CLASS-WINTER";
    
    /**
     * pom.xml文件所在的祖辈目录
     */
    String POM_XML_ROOT = "META-INF/maven";
    
    /**
     * 汇总记录项目中那些本身就已经被class-winter混淆了的lib的checklist
     */
    String CHECKLIST_OF_ALL_LIBS = DEFAULT_ENCRYPTED_CLASSES_SAVE_DIR + "checklist-of-all-libs.winter";
    
    /**
     * 当用户不主动指定密码时，class_winter会自动生成加密密码，并存至此处
     */
    String PWD_WINTER = "META-INF/winter/pwd.winter";
    
    /**
     * 当用户不主动指定密码时，class_winter会自动生成加密密码，并存至此处
     */
    String PWD_WINTER_SIMPLE_NAME = "pwd.winter";
    
    /**
     * 记录加密时，用户是否输入了密码
     */
    String USER_IF_INPUT_PWD = "META-INF/winter/userIfInputPwd.winter";
    
    /**
     * 记录加密时，用户是否输入了密码
     */
    String USER_IF_INPUT_PWD_SIMPLE_NAME = "userIfInputPwd.winter";
    
    /**
     * BOOT-INF
     */
    String BOOT_INF = "BOOT-INF";
    
    /**
     * WEB-INF
     */
    String WEB_INF = "WEB-INF";
    
    /**
     * 为解压的war包，里面的类路径包含.war!/WEB-INF
     * <br />
     * 注：如spring-boot打可执行的war包
     */
    String DOT_WAR_WEB_INF = ".war!/WEB-INF";
    
    /**
     * class文件后缀
     */
    String CLASS_SUFFIX = ".class";
    
    /**
     * jar包后缀
     */
    String JAR_SUFFIX = ".jar";
    
    /**
     * xml文件后缀
     */
    String XML_SUFFIX = ".xml";
    
    /**
     * war包后缀
     */
    String WAR_SUFFIX = ".war";
    
    /**
     * jar协议
     */
    String JAR_PROTOCOL = "jar:";
    
    /**
     * war协议
     */
    String WAR_PROTOCOL = "war:";
    
    /**
     * file协议
     */
    String FILE_PROTOCOL = "file:";
    
    /**
     * 文件路径分隔符
     */
    String LINUX_FILE_SEPARATOR = "/";
    
    /**
     * /classes/
     */
    String CLASSES_DIR = "/classes/";
    
    /**
     * 逗号
     */
    String COMMA = ",";
    
    /**
     * jar包中文件URL有专用格式 jar:!/{jar-entry}
     */
    String JAR_FILE_URL_SPECIAL_SIGN = "!";
    
    /**
     * 换行符
     */
    String LINE_SEPARATOR = "\r\n";
    
    /**
     * 空格
     */
    String WHITE_SPACE = " ";
    
    /**
     * 10
     */
    int TEN = 10;
    
    /**
     * groupId
     */
    String GROUP_ID = "com.idea-aedi";
    
    /**
     * artifactId
     */
    String ARTIFACT_ID = "class-winter-maven-plugin";
    
    /**
     * PREMAIN_CLASS
     */
    String PREMAIN_CLASS = "Premain-Class: ";
    
    /**
     * NotFoundException标识
     */
    String NOT_FOUND_EXCEPTION_FLAG = "javassist.NotFoundException:";
    
    /**
     * 参数名：是否清除类上的注解. 值为boolean类型
     * <p>
     * {@link EncryptClassArgs#isCleanAnnotationOverClazz()}
     */
    String CLEAN_CLASS_ANNOTATION_PARAM_NAME = "cca";
    
    /**
     * 参数名：是否清除方法的注解. 值应为boolean类型
     * <p>
     * {@link EncryptClassArgs#isCleanAnnotationOverMethod()}
     */
    String CLEAN_METHOD_ANNOTATION_PARAM_NAME = "cma";
    
    /**
     * 参数名：是否清除字段的注解. 值应为boolean类型
     * <p>
     * {@link EncryptClassArgs#isCleanAnnotationOverField()}
     */
    String CLEAN_FIELD_ANNOTATION_PARAM_NAME = "cfa";

    /**
     * 参数名：加密的类需要清除的注解全类名前缀(也可以精确匹配)，不指定则为默认规则全部注解清除. 分隔符为|符号，例如：com.kk.verify|com.xx.sun
     * <p>
     * {@link EncryptClassArgs#getToCleanAnnotationPrefix()}
     * <p>
     * 对类上、字段上、方法上的注解清除都有效
     */
    String TO_CLEAN_ANNOTATION_PREFIX = "caPrefix";
    
    /**
     * 参数名：是否保留原始的方法参数名. 值应为boolean类型
     * <p>
     * 为false的话，则加密后方法的原始参数名信息将被擦除（注：不保证一定成功），
     * 反编译后查看到的将是形如var0, var1...之类的参数名（注：不同反编译工具的默认参数名可能不一样）
     */
    String KEEP_ORIGIN_ARGS_NAME = "keepArgName";
    
    /*
     * 部分projectpath识别出来会多一个nested:/前缀
     */
    String NESTED_PREFIX = "nested:/";
    
    /*
     * Load external dtd when nonvalidating feature ("nonvalidating/load-external-dtd").
     */
    String LOAD_EXTERNAL_DTD_FEATURE = "nonvalidating/load-external-dtd";
    
    /*
     * Xerces features prefix ("http://apache.org/xml/features/")
     */
    String XERCES_FEATURE_PREFIX = "http://apache.org/xml/features/";
}
