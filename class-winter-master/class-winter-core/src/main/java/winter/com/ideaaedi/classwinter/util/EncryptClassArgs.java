package winter.com.ideaaedi.classwinter.util;

import java.io.File;
import java.util.Objects;

/**
 * 加密类相关参数
 *
 * @author <font size = "20" color = "#3CAA3C"><a href="https://gitee.com/JustryDeng">JustryDeng</a></font> <img
 * src="https://gitee.com/JustryDeng/shared-files/raw/master/JustryDeng/avatar.jpg" />
 * @since 2.8.1
 */
public class EncryptClassArgs {
    
    /**
     * 待加密的class文件
     */
    private File encryptClassFile;
    
    /**
     * 是否清除类上的注解
     */
    private boolean cleanAnnotationOverClazz = false;
    
    /**
     * 是否清除方法的注解
     */
    private boolean cleanAnnotationOverMethod = false;
    
    /**
     * 是否清除字段的注解
     */
    private boolean cleanAnnotationOverField = false;

    /**
     * 加密的类需要清除的注解全类名前缀(也可以精确匹配)，不指定则为默认规则全部注解清除. 分隔符为|符号，例如：com.xx.verify|com.xx.sun
     */
    private String toCleanAnnotationPrefix = "";
    
    /**
     * 是否保留原方法的参数名
     */
    private boolean keepOriginArgsName = true;
    
    /**
     * fast create
     */
    public static EncryptClassArgs create(File encryptClassFile, boolean cleanAnnotationOverClazz,
                                          boolean cleanAnnotationOverMethod, boolean cleanAnnotationOverField,
                                          String toCleanAnnotationPrefix, boolean keepOriginArgsName) {
        Objects.requireNonNull(encryptClassFile, "encryptClassFile cannot be null.");
        EncryptClassArgs encryptClassArgs = new EncryptClassArgs();
        encryptClassArgs.setEncryptClassFile(encryptClassFile);
        encryptClassArgs.setCleanAnnotationOverClazz(cleanAnnotationOverClazz);
        encryptClassArgs.setCleanAnnotationOverMethod(cleanAnnotationOverMethod);
        encryptClassArgs.setCleanAnnotationOverField(cleanAnnotationOverField);
        encryptClassArgs.setToCleanAnnotationPrefix(toCleanAnnotationPrefix);
        encryptClassArgs.setKeepOriginArgsName(keepOriginArgsName);
        return encryptClassArgs;
    }
    
    public File getEncryptClassFile() {
        return encryptClassFile;
    }
    
    public void setEncryptClassFile(File encryptClassFile) {
        this.encryptClassFile = encryptClassFile;
    }
    
    public boolean isCleanAnnotationOverClazz() {
        return cleanAnnotationOverClazz;
    }
    
    public void setCleanAnnotationOverClazz(boolean cleanAnnotationOverClazz) {
        this.cleanAnnotationOverClazz = cleanAnnotationOverClazz;
    }
    
    public boolean isCleanAnnotationOverMethod() {
        return cleanAnnotationOverMethod;
    }
    
    public void setCleanAnnotationOverMethod(boolean cleanAnnotationOverMethod) {
        this.cleanAnnotationOverMethod = cleanAnnotationOverMethod;
    }
    
    public boolean isCleanAnnotationOverField() {
        return cleanAnnotationOverField;
    }
    
    public void setCleanAnnotationOverField(boolean cleanAnnotationOverField) {
        this.cleanAnnotationOverField = cleanAnnotationOverField;
    }

    public String getToCleanAnnotationPrefix() {
        return toCleanAnnotationPrefix;
    }

    public void setToCleanAnnotationPrefix(String toCleanAnnotationPrefix) {
        this.toCleanAnnotationPrefix = toCleanAnnotationPrefix;
    }
    
    public boolean isKeepOriginArgsName() {
        return keepOriginArgsName;
    }
    
    public void setKeepOriginArgsName(boolean keepOriginArgsName) {
        this.keepOriginArgsName = keepOriginArgsName;
    }
    
    @Override
    public String toString() {
        return "EncryptClassArgs{" +
                "encryptClassFile=" + encryptClassFile +
                ", cleanAnnotationOverClazz=" + cleanAnnotationOverClazz +
                ", cleanAnnotationOverMethod=" + cleanAnnotationOverMethod +
                ", cleanAnnotationOverField=" + cleanAnnotationOverField +
                ", toCleanAnnotationPrefix=" + toCleanAnnotationPrefix +
                ", keepOriginArgsName=" + keepOriginArgsName +
                '}';
    }
}
