package zy.blue7.json.exceptions;

/**
 * @author blue7
 * @date 2020/8/18 9:53
 **/

/**
 * 这是一个判断 数据 是否符合规范的用法
 */
public class NumberException extends Exception{

    public NumberException() {
        super();
    }

    public NumberException(String message) {
        super(message);
    }

    public NumberException(String message, Throwable cause) {
        super(message, cause);
    }

    public NumberException(Throwable cause) {
        super(cause);
    }

    protected NumberException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
