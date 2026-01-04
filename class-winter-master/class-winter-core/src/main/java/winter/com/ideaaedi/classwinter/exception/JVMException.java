package winter.com.ideaaedi.classwinter.exception;


/**
 * JVM异常
 *
 * @author wang
 */
public class JVMException extends ClassWinterException {

    public JVMException() {
    }

    public JVMException(String message) {
        super(message);
    }

    public JVMException(String message, Throwable cause) {
        super(message, cause);
    }

    public JVMException(Throwable cause) {
        super(cause);
    }
}