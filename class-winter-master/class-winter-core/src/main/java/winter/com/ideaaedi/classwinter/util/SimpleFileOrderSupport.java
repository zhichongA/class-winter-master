package winter.com.ideaaedi.classwinter.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单的文件排序提供者
 *
 * @author JustryDeng
 * @since 2021/11/7 17:31:27
 */
public class SimpleFileOrderSupport implements FileOrderSupport {
    
    private final Map<File, Integer> fileOrderInfo = new HashMap<>(64);
    
    public SimpleFileOrderSupport(List<String> filePathList) {
        if (filePathList == null || filePathList.size() == 0) {
            return;
        }
        for (int i = 0; i < filePathList.size(); i++) {
            fileOrderInfo.put(new File(filePathList.get(i)), i);
        }
    }
    
    @Override
    public Integer fileOrder(File file) {
        return fileOrderInfo.get(file);
    }
}
