package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.exception.ClassWinterException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.CRC32;

/**
 * IO工具类
 *
 * @author {@link JustryDeng}
 * @since 2021/4/23 20:40:47
 */
public final class IOUtil {
    
    /**
     * 将srcFileBytes写出为destFile文件
     * <p>
     *     注: 若源文件存在，则会覆盖原有的内容。
     * </p>
     *
     * @param srcFileBytes
     *            字节
     * @param destFile
     *            文件
     * @param createIfNecessary
     *            如果需要的话，创建文件
     */
    public static void toFile(byte[] srcFileBytes, File destFile, boolean createIfNecessary) {
        OutputStream os = null;
        try {
            if (destFile.isDirectory()) {
                throw new ClassWinterException("destFile [" + destFile.getAbsolutePath() + "] must be file rather than dir.");
            }
            
            if (createIfNecessary && !destFile.exists()) {
                File parentFile = destFile.getParentFile();
                if (!parentFile.exists() || !parentFile.isDirectory()) {
                    /*
                     * 进入此if，即代表parentFile存在，且为file, 而我们又需要创建一个同名的文件夹。
                     * 如果系统不支持创建与文件同名(大小写不敏感)的文件夹的话，那么创建结果为false
                     */
                    boolean mkdirs = parentFile.mkdirs();
                    if (!mkdirs) {
                        // step0. 将与与文件夹名冲突的文件重命名为：原文件名_时间戳
                        Arrays.stream(Objects.requireNonNull(parentFile.getParentFile().listFiles()))
                                .filter(file -> file.getName().equalsIgnoreCase(parentFile.getName())).findFirst()
                                .ifPresent(conflictFile -> {
                                    String renameFilePath =
                                            conflictFile.getAbsolutePath() + "_" + System.currentTimeMillis();
                                    boolean renameResult = conflictFile.renameTo(new File(renameFilePath));
                                    Logger.warn(IOUtil.class,
                                            "rename file [" + conflictFile.getAbsolutePath() + "] to ["
                                                    + renameFilePath + "] " + (renameResult ? "success" : "fail") + ".");
                                });
                        // step1. 再次创建文件夹
                        mkdirs = parentFile.mkdirs();
                        if (!mkdirs) {
                            Logger.warn(IOUtil.class, "create dir [" + parentFile.getAbsolutePath() + "] fail.");
                        }
                    }
                }
                //noinspection ResultOfMethodCallIgnored
                destFile.createNewFile();
            } else if (!destFile.exists()) {
                throw new IllegalArgumentException("destFile [" + destFile.getAbsolutePath() + "] non exist.");
            }
            os = new FileOutputStream(destFile);
            os.write(srcFileBytes, 0, srcFileBytes.length);
            os.flush();
        } catch (IOException e) {
            throw new ClassWinterException(" toFile [" + destFile.getAbsolutePath() + "] occur exception.", e);
        } finally {
            close(os);
        }
    }
    
    /**
     * 读取文件
     *
     * @param file
     *            文件
     * @return  字节
     */
    public static byte[] toBytes(File file) {
        try {
            FileInputStream inputStream = new FileInputStream(file);
            return toBytes(inputStream);
        } catch (IOException e) {
            throw new ClassWinterException(e);
        }
    }
    
    /**
     * 将inputStream转换为byte[]
     * <p>
     *     注：此方法会释放inputStream
     * </p>
     *
     * @param inputStream
     *            输入流
     * @return  字节
     */
    public static byte[] toBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[4096];
            int n;
            while (-1 != (n = inputStream.read(buffer))) {
                output.write(buffer, 0, n);
            }
            return output.toByteArray();
        } finally {
            close(output, inputStream);
        }
    }
    
    /**
     * 只罗列文件(即：只返回文件)
     * <p>
     *     注：dirOrFile对象本身也会被作为罗列对象。
     * </p>
     *
     * @param dirOrFile
     *            要罗列的文件夹(或者文件)
     * @param suffix
     *            要筛选的文件的后缀(若suffix为null， 则不作筛选)
     *
     * @return  罗列结果
     */
    public static List<File> listFileOnly(File dirOrFile, String... suffix) {
        if (!dirOrFile.exists()) {
            throw new IllegalArgumentException("listFileOnly [" + dirOrFile.getAbsolutePath() + "] non exist.");
        }
        return listFile(dirOrFile, 1).stream()
                .filter(file -> {
                    if (suffix == null) {
                        return true;
                    }
                    String fileName = file.getName();
                    return Arrays.stream(suffix).anyMatch(fileName::endsWith);
                }).collect(Collectors.toList());
    }
    
    /**
     * 罗列所有子孙文件文件夹(不包含dirOrFile对象本身)
     *
     * @param dirOrFile
     *            要罗列的文件夹(或者文件)
     * @param mode
     *            罗列模式(0-罗列文件和文件夹； 1-只罗列文件； 2-只罗列文件夹)
     *
     * @return  罗列结果
     */
    public static List<File> listSubFile(File dirOrFile, int mode) {
        List<File> fileContainer = listFile(dirOrFile, mode);
        String absolutePath = dirOrFile.getAbsolutePath();
        Objects.requireNonNull(absolutePath, "absolutePath cannot be null.");
        fileContainer = fileContainer.stream()
                .filter(file -> !absolutePath.equals(file.getAbsolutePath()))
                .collect(Collectors.toList());
        return fileContainer;
    }
    
    /**
     * 罗列所有文件文件夹
     * <p>
     *     注：dirOrFile对象本身也会被作为罗列对象。
     * </p>
     *
     * @param dirOrFile
     *            要罗列的文件夹(或者文件)
     * @param mode
     *            罗列模式(0-罗列文件和文件夹； 1-只罗列文件； 2-只罗列文件夹)
     *
     * @return  罗列结果
     */
    public static List<File> listFile(File dirOrFile, int mode) {
        List<File> fileContainer = new ArrayList<>(16);
        listFile(dirOrFile, fileContainer, mode);
        return fileContainer;
    }
    
    /**
     * 罗列所有文件文件夹
     * <p>
     *     注：dirOrFile对象本身也会被作为罗列对象。
     * </p>
     *
     * @param dirOrFile
     *            要罗列的文件夹(或者文件)
     * @param fileContainer
     *            罗列结果
     * @param mode
     *            罗列模式(0-罗列文件和文件夹； 1-只罗列文件； 2-只罗列文件夹)
     */
    public static void listFile(File dirOrFile, List<File> fileContainer, int mode) {
        if (!dirOrFile.exists()) {
            return;
        }
        int fileAndDirMode = 0;
        int onlyFileMode = 1;
        int onlyDirMode = 2;
        if (mode != fileAndDirMode && mode != onlyFileMode && mode != onlyDirMode) {
            throw new IllegalArgumentException("mode [" + mode + "] is non-supported. 0,1,2is only support.");
        }
        if (dirOrFile.isDirectory()) {
            File[] files = dirOrFile.listFiles();
            if (files != null) {
                for (File f : files) {
                    listFile(f, fileContainer, mode);
                }
            }
            if (mode == fileAndDirMode || mode == onlyDirMode) {
                fileContainer.add(dirOrFile);
            }
        } else {
            if (mode == fileAndDirMode || mode == onlyFileMode) {
                fileContainer.add(dirOrFile);
            }
        }
    }
    
    /**
     * 删除文件/文件夹
     *
     * @param dirOrFile
     *            要删的除文件/文件夹
     */
    public static void delete(File dirOrFile) {
        if (!dirOrFile.exists()) {
            return;
        }
        if (dirOrFile.isFile()) {
            boolean success = dirOrFile.delete();
            if (!success) {
                Logger.debug(IOUtil.class, "delete file [" + dirOrFile.getAbsolutePath() + "] fail.");
            }
        } else {
            File[] files = dirOrFile.listFiles();
            if (files != null) {
                for (File f : files) {
                    delete(f);
                }
            }
        }
        //noinspection ResultOfMethodCallIgnored
        dirOrFile.delete();
    }
    
    /**
     * 赋值输入流数据至输出流
     * <p>
     *     注：src可以被多次copy。
     *     注：若dest所代表的“文件”已存在，则会覆盖原来的数据。
     * </p>
     *
     * @param src
     *            输入流
     * @param dest
     *            输出流
     * @return  复制了的字节数(字节大小)
     */
    public static int copy(InputStream src, OutputStream dest) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        int n;
        while (-1 != (n = src.read(buffer))) {
            dest.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
    
    /**
     * 计算字节的CRC32
     *
     * @param bytes
     *            字节
     * @return  CRC32值
     */
    public static long computeCrc32(byte[] bytes) {
        CRC32 crc32 = new CRC32();
        crc32.update(bytes);
        return crc32.getValue();
    }
    
    /**
     * 将内容写入到文件
     * <p>
     *     注:若原文件存在，则会覆盖原文件中的内容。
     * </p>
     *
     * @param content
     *            内容
     * @param file
     *            文件
     */
    public static void writeContentToFile(String content, File file) {
        BufferedWriter out = null;
        try {
            // 保证目录存在
            if (!file.getParentFile().exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
            }
            // 保证文件存在
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            out = new BufferedWriter(new FileWriter(file));
            out.write(content);
            out.flush();
        } catch (IOException e) {
            throw new ClassWinterException(e);
        } finally {
            close(out);
        }
    }
    
    /**
     * 读取文件内容
     *
     * @param file
     *            文件
     *
     * @return 内容
     */
    public static String readContentFromFile(File file) {
        StringBuilder content = new StringBuilder();
        FileInputStream fileInputStream = null;
        InputStreamReader read = null;
        BufferedReader bufferedReader = null;
        try {
            fileInputStream = new FileInputStream(file);
            read = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(read);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new ClassWinterException(e);
        } finally {
            close(bufferedReader, read, fileInputStream);
        }
        return content.toString();
    }
    
    /**
     * 关闭流
     *
     * @param ioArr
     *            待关闭的io
     */
    public static void close(Closeable... ioArr) {
        if (ioArr == null) {
            return;
        }
        for (Closeable io : ioArr) {
            if (io == null) {
                continue;
            }
            try {
                io.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
    
    /**
     * 合并byte[]
     *
     * @param byteArr
     *            字节数组
     *
     * @return 合并后的字节
     */
    public static byte[] mergeByte(byte[]... byteArr) {
        int length = 0;
        for (byte[] b : byteArr) {
            length += b.length;
        }
        byte[] bt = new byte[length];
        int lastLength = 0;
        for (byte[] b : byteArr) {
            System.arraycopy(b, 0, bt, lastLength, b.length);
            lastLength += b.length;
        }
        return bt;
    }
    
    
    /**
     * 从jar文件或目录中读取文件字节
     *
     * @param workbenchRoot
     *            zip文件或目录（如: /tmp/, /tmp/abc.jar, /tmp/abc.war等）
     * @param relativeFilePath
     *            要读取的文件的相对路径（如：META-INF/winter/xyz.txt）
     * @return  文件字节数组（若不存在，则返回null）
     */
    public static byte[] readFileFromWorkbenchRoot(File workbenchRoot, String relativeFilePath) {
        byte[] bytes;
        // 文件
        if (workbenchRoot.isFile()) {
            String workDirName = workbenchRoot.getName();
            // 可执行jar or 可执行war
            if (!workDirName.endsWith(Constant.JAR_SUFFIX) && !workDirName.endsWith(Constant.WAR_SUFFIX)) {
                throw new ClassWinterException("workDirName [" + workDirName + "] is not support.");
            }
            bytes = JarUtil.getFileFromZip(workbenchRoot, relativeFilePath);
        } else {
            // 目录
            File file = new File(workbenchRoot, relativeFilePath);
            if (!file.exists()) {
                return null;
            }
            bytes = IOUtil.toBytes(file);
        }
        return bytes;
    }
    
    /**
     * 判断字节流bytes的内容是否是以CAFEBABE打头的
     * 注:
     * 每个Class文件的头4个字节称为魔数(Magic Number),它的唯一作用是确定这个文件是否为一个能被虚拟机接收的Class文件。所有Class文件，魔数均为0xCAFEBABE， 即: 这四个字节分别是 CA FE BA BE
     * 十六进制的ca 等价于 十进制的-54
     * 十六进制的fe 等价于 十进制的-2
     * 十六进制的ba 等价于 十进制的-70
     * 十六进制的be 等价于 十进制的-66
     * 注:一个字节即为8个bit，大小上限为2的8次方， 16的2次方， 所以一个字节，可以通过8位二进制表示，也可以通过2位16进制表示，如上面的 CA FE BA BE就分别表示了4个字节。
     */
    public static boolean startWithCAFEBABE(byte[] bytes) {
        if (bytes == null || bytes.length < 4) {
            return false;
        }
        /*
         * 转换后，空缺的高位会用f填充，而我们只需要比较后面的两位就行
         * Integer.toHexString(-54) 结果为 ffffffca
         * Integer.toHexString(-2) 结果为 fffffffe
         * Integer.toHexString(-70) 结果为 ffffffba
         * Integer.toHexString(-66) 结果为 ffffffbe
         */
        return Integer.toHexString(bytes[0]).toUpperCase(Locale.ENGLISH).endsWith("CA")
               && Integer.toHexString(bytes[0]).toUpperCase(Locale.ENGLISH).endsWith("FE")
                && Integer.toHexString(bytes[0]).toUpperCase(Locale.ENGLISH).endsWith("BA")
                && Integer.toHexString(bytes[0]).toUpperCase(Locale.ENGLISH).endsWith("BE");
    }
}
