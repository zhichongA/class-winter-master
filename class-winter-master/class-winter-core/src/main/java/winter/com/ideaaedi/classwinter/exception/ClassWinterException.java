package winter.com.ideaaedi.classwinter.exception;

import winter.com.ideaaedi.classwinter.author.JustryDeng;

/**
 * 代码混淆异常
 *
 * @author {@link JustryDeng}
 * @since 2021/4/23 20:47:24
 */
public class ClassWinterException extends RuntimeException{
    
    public ClassWinterException() {
        super();
    }
    
    public ClassWinterException(String message) {
        super(message);
    }
    
    public ClassWinterException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ClassWinterException(Throwable cause) {
        super(cause);
    }
    
    protected ClassWinterException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
