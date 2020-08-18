package zy.blue7.json.exceptions;

/**
 * @author blue7
 * @date 2020/8/18 11:26
 **/

/**
 * 空指针异常处理
 */
public class NullException extends Exception{
    public NullException() {
        super();
    }

    public NullException(String message) {
        super(message);
    }

    public NullException(String message, Throwable cause) {
        super(message, cause);
    }

    public NullException(Throwable cause) {
        super(cause);
    }

    protected NullException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
