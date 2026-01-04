package winter.com.ideaaedi.classwinter.util;

import java.io.File;
import java.util.Objects;

/**
 * 文件排序支持
 *
 * @author JustryDeng
 * @since 2021/11/7 17:15:23
 */
public interface FileOrderSupport {
    
    /** 默认的排序序号 */
    int DEFAULT_FILE_ORDER = Integer.MAX_VALUE;
    
    /**
     * 获取文件的排序序号
     *
     * @param file
     *            文件
     * @return  文件的排序序号
     */
    default int obtainFileOrder(File file) {
        Objects.requireNonNull(file, "file cannot be null.");
        Integer order = fileOrder(file);
        return order == null ? DEFAULT_FILE_ORDER : order;
    }
    
    /**
     * 获取文件的排序序号
     *
     * @param file
     *            文件
     * @return  文件的排序序号
     */
    Integer fileOrder(File file);
}
