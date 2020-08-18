package zy.blue7.json.Iinterface;

/**
 * @author blue7
 * @date 2020/8/17 9:18
 **/
public interface IHandler<T> {
    <E> E handler(T t, String jsonStr, String path) throws Exception;
}
