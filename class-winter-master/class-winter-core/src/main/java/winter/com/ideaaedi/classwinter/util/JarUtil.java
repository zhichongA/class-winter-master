package winter.com.ideaaedi.classwinter.util;

import winter.com.ideaaedi.classwinter.author.JustryDeng;
import winter.com.ideaaedi.classwinter.exception.ClassWinterException;

import java.io.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * jar/war操作工具类
 *
 * @author {@link JustryDeng}
 * @since 2021/4/25 21:58:52
 */
public final class JarUtil {
    
    /** 打包时需要忽略的(可能由操作系统产生的)文件 */
    private static final String[] IGNORE_FILE_SUFFIX = {".DS_Store", "Thumbs.db"};
    
    /**
     * @see JarUtil#doJarWar(String, String, FileOrderSupport)
     */
    public static String doJarWar(String srcDir, String targetJarOrWar) {
        return doJarWar(srcDir, targetJarOrWar, null);
    }
    
    /**
     * 把目录压缩成jar(or war)
     *
     * @param srcDir
     *            需要打包的目录(如 /tmp/demo-1.0.0/)
     * @param targetJarOrWar
     *            打包出的jar/war文件路径(如 /tmp/abc.jar)
     * @param fileOrderSupport
     *            文件排序序号提供者<br>
     *            注：部分jar包会对jar包内的条目有顺序要求（如：spring-boot的jar包的lib目录下，lib包的顺序要和pom.xml中的声明顺序保持一致），
     *                此时就可以使用此字段来实现排序了。
     * @return  打包出的jar/war文件路径
     */
    public static String doJarWar(String srcDir, String targetJarOrWar, FileOrderSupport fileOrderSupport) {
        File jarDirFile = new File(srcDir);
        // 枚举jarDir下的所有文件以及目录
        List<File> files = IOUtil.listSubFile(jarDirFile, 0);
        if (fileOrderSupport != null) {
            files = files.stream().sorted(Comparator.comparing(fileOrderSupport::obtainFileOrder)).collect(Collectors.toList());
        }
        ZipOutputStream zos = null;
        OutputStream out = null;
        try {
            File generatedJar = new File(targetJarOrWar);
            // 如果原来的jar已存在，则先删除原来的jar
            if (generatedJar.exists()) {
                IOUtil.delete(generatedJar);
            }
            // jar包里面的文件的起始"root"位置
            int rootStartIndex = jarDirFile.getAbsolutePath().length() + 1;
            out = new FileOutputStream(generatedJar);
            zos = new ZipOutputStream(out);
            for (File file : files) {
                if (isIgnore(file)) {
                    continue;
                }
                String fileName = file.getAbsolutePath().substring(rootStartIndex);
                fileName = fileName.replace(File.separator, Constant.LINUX_FILE_SEPARATOR);
                // 目录，添加一个目录entry
                if (file.isDirectory()) {
                    ZipEntry zipEntry = new ZipEntry(fileName + Constant.LINUX_FILE_SEPARATOR);
                    zipEntry.setTime(file.lastModified());
                    zipEntry.setLastModifiedTime(FileTime.fromMillis(file.lastModified()));
                    zos.putNextEntry(zipEntry);
                }
                // jar文件， 需要写CRC32信息
                else if (fileName.endsWith(Constant.JAR_SUFFIX)) {
                    byte[] bytes = IOUtil.toBytes(file);
                    ZipEntry ze = new ZipEntry(fileName);
                    ze.setMethod(ZipEntry.STORED);
                    ze.setSize(bytes.length);
                    ze.setCrc(IOUtil.computeCrc32(bytes));
                    ze.setTime(file.lastModified());
                    ze.setLastModifiedTime(FileTime.fromMillis(file.lastModified()));
                    zos.putNextEntry(ze);
                    zos.write(bytes);
                }
                // 其它文件直接写入
                else {
                    ZipEntry zipEntry = new ZipEntry(fileName);
                    zipEntry.setTime(file.lastModified());
                    zipEntry.setLastModifiedTime(FileTime.fromMillis(file.lastModified()));
                    zos.putNextEntry(zipEntry);
                    byte[] bytes = IOUtil.toBytes(file);
                    zos.write(bytes);
                }
                zos.closeEntry();
            }
        } catch (IOException e) {
            throw new ClassWinterException(e);
        } finally {
            IOUtil.close(zos, out);
        }
        return targetJarOrWar;
    }
    
    /**
     * 解压jar(or war)至指定的目录
     *
     * @see JarUtil#unJarWar(String, String, boolean, Collection)
     */
    public static <T extends Collection<String>> List<String> unJarWar(String jarWarPath, String targetDir) {
        return unJarWar(jarWarPath, targetDir, true, null);
    }
    
    /**
     * 解压jar(or war)至指定的目录
     *
     * @param jarWarPath
     *            待解压的jar(or war)文件
     * @param targetDir
     *            解压后文件放置的文件夹
     * @param delOldTargetDirIfAlreadyExist
     *            若targetDir已存在，是否先将原来的targetDir进行删除
     * @param entryNamePrefixes
     *            只有当entryName为指定的前缀时，才对该entry进行解压(若为null或者长度为0， 则解压所有文件)   如: ["BOOT-INF/classes/", "BOOT-INF/classes/com/example/ssm/author/JustryDeng.class"]
     *            <br/>
     *            注:当entry对应jar或者war中的目录时，那么其值形如 BOOT-INF/classes/
     *            <br/>
     *            注:当entry对应jar或者war中的文件时，那么其值形如 BOOT-INF/classes/com/example/ssm/author/JustryDeng.class
     * @return  （按压缩文件中条目的顺序）解压出来的（有序的）文件(包含目录)的完整路径
     */
    public static <T extends Collection<String>> List<String> unJarWar(String jarWarPath, String targetDir,
                                                                       boolean delOldTargetDirIfAlreadyExist,
                                                                       T entryNamePrefixes) {
        Set<String> linkedSet = new LinkedHashSet<>();
        File target = new File(targetDir);
        if (delOldTargetDirIfAlreadyExist) {
            IOUtil.delete(target);
        }
        guarantyDirExist(target);
        
        ZipFile zipFile = null;
        // 此集合中的file会保留上一次修改时间，不会被当前解压操作覆盖
        List<String> storeLastModifiedTimeList = new ArrayList<>();
        Map<String, Long> filePthAndLastModifiedTimeMap = new HashMap<>(64);
        try {
            zipFile = new ZipFile(new File(jarWarPath));
            ZipEntry entry;
            File targetFile;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                entry = entries.nextElement();
                String entryName = entry.getName();
                // 若entryNamePrefixes不为空，则不解压前缀不匹配的文件或文件夹
                if (entryNamePrefixes != null && entryNamePrefixes.size() > 0
                        && entryNamePrefixes.stream().noneMatch(entryName::startsWith)) {
                    continue;
                }
                if (entry.isDirectory()) {
                    targetFile = new File(target, entryName);
                    guarantyDirExist(targetFile);
                    String absolutePath = targetFile.getAbsolutePath();
                    linkedSet.add(absolutePath);
                    storeLastModifiedTimeList.add(absolutePath);
                    filePthAndLastModifiedTimeMap.put(absolutePath, entry.getLastModifiedTime().toMillis());
                } else {
                    // 有时遍历时，文件先于文件夹出来，所以也得保证目录存在
                    int lastSeparatorIndex = entryName.lastIndexOf(Constant.LINUX_FILE_SEPARATOR);
                    if (lastSeparatorIndex > 0) {
                        guarantyDirExist(new File(target, entryName.substring(0, lastSeparatorIndex)));
                    }
                    // 解压文件
                    targetFile = new File(target, entryName);
                    byte[] bytes = IOUtil.toBytes(zipFile.getInputStream(entry));
                    IOUtil.toFile(bytes, targetFile, true);
                    String absolutePath = targetFile.getAbsolutePath();
                    linkedSet.add(absolutePath);
                    storeLastModifiedTimeList.add(absolutePath);
                    filePthAndLastModifiedTimeMap.put(absolutePath, entry.getLastModifiedTime().toMillis());
                }
            }
        } catch (IOException e) {
            throw new ClassWinterException(e);
        } finally {
            IOUtil.close(zipFile);
        }
        for (String filePath : storeLastModifiedTimeList) {
            Long lastModifiedTime = filePthAndLastModifiedTimeMap.get(filePath);
            //noinspection ResultOfMethodCallIgnored
            new File(filePath).setLastModified(lastModifiedTime == null ? System.currentTimeMillis() : lastModifiedTime);
        }
        return new ArrayList<>(linkedSet);
    }
    
    /**
     * 保证目录存在
     *
     * @param dir
     *            目录
     */
    public static void guarantyDirExist(File dir) {
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
    }
    
    /**
     * 从(zip/jar)压缩文件中获取文件
     * <p>
     *     注：jar其实也是zip。
     * </p>
     * <p>
     *     示例
     *     <code>
     *         String path = JarUtil.getFileFromJar("/tmp/demo.jar", "META-INF/services/com.niantou.filter.HttpExtFilter", new File("/root/HttpExtFilter.txt"));
     *     </code>
     * </p>
     *
     * @param zip
     *            压缩文件
     * @param fileName
     *            压缩文件的(相对压缩文件的root的)相对路径文件名
     * @param targetFile
     *            获取出来的目标文件
     * @return  获取出来的目标文件的绝对路径
     */
    public static String getFileFromZip(File zip, String fileName, File targetFile) {
        byte[] bytes = getFileFromZip(zip, fileName);
        IOUtil.toFile(bytes, targetFile, true);
        return targetFile.getAbsolutePath();
    }
    
    /**
     * 修改zip文件（.java、.war文件）中的条目
     *
     * @param zipFile
     *            要修改的zip文件（.java、.war文件）
     * @param replacerMap
     *            替换器(k-ZipFile中，要被替换的ZipEntry的相对路径，如：BOOT-INF/classes/application.yml; V-要替换成的内容)
     * @return  被替换了的ZipEntry的相对路径及重写前后的内容信息<br/>
     *          k - ZipEntry的相对路径，如：BOOT-INF/classes/application.yml<br/>
     *          v - 左：重写前的内容，右：重写后的内容
     */
    public static Map<String, Pair<byte[], byte[]>> rewriteZipEntry(ZipFile zipFile, Map<String, byte[]> replacerMap) throws IOException {
        Map<String, Pair<byte[], byte[]>> map = new HashMap<>(8);
        if (replacerMap == null || replacerMap.size() == 0) {
            return map;
        }
        if (zipFile == null) {
            Logger.warn("zipFile is null.");
            return map;
        }
        String zipFilePath = zipFile.getName();
        if (!new File(zipFilePath).exists()) {
            Logger.warn("zipFile [" + zipFilePath + "] non-exist.");
            return map;
        }
        List<ZipEntry> zipEntryList = new LinkedList<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            zipEntryList.add(zipEntry);
        }
        // zipFile.getName()形如：   /abc/my-project.jar   /abc/my-project.war  /abc/my-projectzip
        try (FileOutputStream fos = new FileOutputStream(zipFilePath, true);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (ZipEntry zipEntry : zipEntryList) {
                String zipEntryName = zipEntry.getName();
                byte[] originBytes = IOUtil.toBytes(zipFile.getInputStream(zipEntry));
                byte[] replaceBytes = replacerMap.get(zipEntryName);
                if (replacerMap.containsKey(zipEntryName) && replaceBytes != null) {
                    // 覆盖
                    ZipEntry ze = new ZipEntry(zipEntryName);
                    ze.setMethod(ZipEntry.STORED);
                    ze.setSize(replaceBytes.length);
                    ze.setCrc(IOUtil.computeCrc32(replaceBytes));
                    ze.setTime(zipEntry.getTime());
                    ze.setLastModifiedTime(zipEntry.getLastModifiedTime());
                    zos.putNextEntry(ze);
                    zos.write(replaceBytes);
                    map.put(zipEntryName, Pair.of(originBytes, replaceBytes));
                } else {
                    // 其它的不动
                    zos.putNextEntry(new ZipEntry(zipEntry));
                    zos.write(originBytes);
                }
            }
        }
        return map;
    }
    
    /**
     * 从(zip/jar/war)压缩文件中获取一个文件的字节
     * <p>
     *     注：jar其实也是zip。 war虽然不是zip,但是也是可以使用压缩/解压zip的方式来进行压缩解压的。
     * </p>
     * <p>
     *     示例
     *     <code>
     *         byte[] bytes = JarUtil.getFileFromJar("/tmp/demo.jar", "META-INF/services/com.niantou.filter.HttpExtFilter");
     *     </code>
     * </p>
     *
     * @param zip
     *            压缩文件
     * @param fileName
     *            压缩文件的(相对压缩文件的root的)相对路径文件名
     * @return  文件字节（可能为null）
     */
    public static byte[] getFileFromZip(File zip, String fileName) {
        ZipFile zipFile = null;
        InputStream is = null;
        try {
            if (!zip.exists()) {
                return null;
            }
            zipFile = new ZipFile(zip);
            ZipEntry zipEntry = zipFile.getEntry(fileName);
            if (zipEntry == null) {
                return null;
            }
            is = zipFile.getInputStream(zipEntry);
            return IOUtil.toBytes(is);
        } catch (IOException e) {
            throw new ClassWinterException(e);
        } finally {
            IOUtil.close(is, zipFile);
        }
    
    }
    
    /**
     * 判断originJarOrWar是jar文件还是war文件
     *
     * @param originJarOrWar
     *            jar或者war的文件名(或全路径文件名)
     * @return  true-jar包; false-war包
     * @throws  IllegalArgumentException 当originJarOrWar既不是jar文件又不是war文件时抛出
     */
    public static boolean isJarOrWar(String originJarOrWar) throws IllegalArgumentException {
        if (originJarOrWar.endsWith(Constant.JAR_SUFFIX)) {
            return true;
        }
        if (originJarOrWar.endsWith(Constant.WAR_SUFFIX)) {
            return false;
        }
        throw new IllegalArgumentException("suffix-file [" + originJarOrWar + "] is not support.");
    }
    
    /**
     * 是否忽略这个文件
     *
     * @param file
     *            文件
     * @return  是否忽略这个文件
     */
    private static boolean isIgnore(File file) {
        for (String suffix : IGNORE_FILE_SUFFIX) {
            if (file.getAbsolutePath().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 获取当前类对应的其所在的classes目录全路径名（或其所在jar包/war包文件全路径名）
     *
     * @param projectPath
     *            当前类对应的项目路径
     * @return  当前类对应的其所在的classes目录全路径名（或其所在jar包/war包文件全路径名）
     */
    public static String getRootPath(String projectPath) {
        Objects.requireNonNull(projectPath, "projectPath cannot be null.");
        if (projectPath.startsWith(Constant.JAR_PROTOCOL) || projectPath.startsWith(Constant.WAR_PROTOCOL)) {
            // jar协议/war协议的协议声明长度为4
            projectPath = projectPath.substring(4);
        }
        if (projectPath.startsWith(Constant.FILE_PROTOCOL)) {
            // file协议的协议声明长度为5
            projectPath = projectPath.substring(5);
        }
        // 没解压的war包
        if (projectPath.contains("*")) {
            return projectPath.substring(0, projectPath.indexOf("*"));
        }
        // 没解压的war包(如：spring-boot使用org.springframework.boot.loader.WarLauncher加载器加载可执行war，直接java -jar xxx.war进行的启动)
        else if (projectPath.contains(Constant.DOT_WAR_WEB_INF)) {
            return projectPath.substring(0, projectPath.indexOf(Constant.DOT_WAR_WEB_INF) + Constant.WAR_SUFFIX.length());
        }
        // 包含印章的projectPath，不作处理直接返回
        else if (IOUtil.readFileFromWorkbenchRoot(new File(projectPath), Constant.SEAL_FILE) != null) {
            return projectPath;
        }
        // war包解压后的WEB-INF目录
        else if (projectPath.contains(Constant.WEB_INF)) {
            return projectPath.substring(0, projectPath.indexOf(Constant.WEB_INF));
        }
        // jar(jar包中文件URL有专用的格式jar:!/{entry}, 所以如果包含!，说明也是jar包)
        else if (projectPath.contains(Constant.JAR_FILE_URL_SPECIAL_SIGN)) {
            return projectPath.substring(0, projectPath.indexOf(Constant.JAR_FILE_URL_SPECIAL_SIGN));
        }
        // 普通jar/war
        else if (projectPath.endsWith(Constant.JAR_SUFFIX) || projectPath.endsWith(Constant.WAR_SUFFIX)) {
            return projectPath;
        }
        // no (项目还未打包时，存放于/classes/下)
        else if (projectPath.contains(Constant.CLASSES_DIR)) {
            return projectPath.substring(0, projectPath.indexOf(Constant.CLASSES_DIR) + Constant.CLASSES_DIR.length());
        }
        throw new ClassWinterException(projectPath);
    }
}
