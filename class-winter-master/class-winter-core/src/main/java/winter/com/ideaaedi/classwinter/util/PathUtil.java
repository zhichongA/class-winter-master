package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.exception.ClassWinterException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 路径util
 *
 * @author {@link JustryDeng}
 * @since 2021/4/26 23:11:04
 */
public final class PathUtil {
    
    /**
     * 获取clazz类的全类名对应包的根路径
     *
     * <ul>
     *     <li>如果编译后未打包时直接调用这个方法(即:直接在开发工具里调用此方法)，那么结果形如:   D:/资料整理/demo模板/class-winter/class-winter-core/target/classes/</li>
     *     <li>如果是jar包运行时，(clazz位于jar包中，)调用这个方法，(假设jar包位置是D:/资料整理/demo模板/abc.jar)那么结果形如：   D:/资料整理/demo模板/abc.jar</li>
     * </ul>
     *
     * @return  项目根目录
     */
    public static String getProjectRootDir(Class<?> clazz) {
        URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
        String filePath;
        try {
            filePath = URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        /*
         * C:\Users\JustryDeng\Desktop\file:\E:\Java\demo\Abc_SpringBoot_Demo\target\spring-boot-demo-0.0.1-SNAPSHOT.jar
         * 转换为
         * \E:\Java\demo\Abc_SpringBoot_Demo\target\spring-boot-demo-0.0.1-SNAPSHOT.jar
         */
        if (filePath.contains("file:")) {
            filePath = filePath.substring(filePath.indexOf("file:") + "file:".length());
        }
        File file = new File(filePath);
        filePath = file.getAbsolutePath().replace("\\", "/");
        // 如果file是文件夹，那么保证filePath是以/结尾的
        if (file.isDirectory() && !filePath.endsWith("/")) {
            filePath = filePath + "/";
        }
        // spring-boot jar包里的class， 获取到的形如: your-spring-boot.jar!/BOOT-INF/classes!
        String springBootJarInnerClass = "!/BOOT-INF/classes!";
        if (filePath.endsWith(springBootJarInnerClass)) {
            filePath = filePath.substring(0, filePath.length() - springBootJarInnerClass.length());
        }
        // spring-boot jar包里jar包里的class， 获取到的形如: your-spring-boot.jar!/BOOT-INF/lib/your-lib-jar.jar!
        String springBootJarInnerJar = "!/BOOT-INF/lib";
        int idx = filePath.indexOf(springBootJarInnerJar);
        if (idx >= 0) {
            filePath = filePath.substring(0, idx);
        }
    
        return filePath;
    }
}
