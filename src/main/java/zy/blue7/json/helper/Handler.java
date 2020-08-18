package zy.blue7.json.helper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import zy.blue7.json.Iinterface.IHandler;
import zy.blue7.json.exceptions.NullException;
import zy.blue7.json.exceptions.NumberException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * @author blue7
 * @date 2020/8/17 9:19
 **/
public class Handler<T> implements IHandler<T> {

    public Environment getEnvironment() {
        return environment;
    }

    public Handler(Environment environment) {
        this.environment = environment;
    }
    public Handler() {
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    Environment environment;


    @Override
    public <E> E handler(T t, String jsonStr, String path) throws Exception {

        List<E> ts = new ArrayList<>();

        Object parse = JSON.parse(jsonStr);
        if (parse instanceof JSONObject) {
            /**
             * 这里是传入的是json对象
             */
            return (E) this.handler((Class) t, (JSONObject) parse, path);
        } else if (parse instanceof JSONArray) {
            /**
             * 这里传入的是json对象数组
             */
            for (Object obj : (JSONArray) parse) {
                /**
                 * 这里暂时只考虑 json数组中的对象只有 json对象，不是其他的
                 */
                if (obj instanceof JSONObject) {
                    ts.add((E) this.handler((Class) t, (JSONObject) obj, path));
                }
            }
            return (E) ts;
        } else {
            /**
             * 暂时不考虑其他情况，只考虑传入的是 对象或者对象数组
             */
            return null;
        }

    }

    /**
     * 这个方法是解析json对象，通过yml文件中的指定值来获取json中的值赋值给Java对象的属性
     *
     * @param clazz      要被赋值的Java对象
     * @param jsonObject 与Java对象对应的json对象
     * @param path       yml中路径，就是key，这里是通过类名加 属性名获取
     * @return
     * @throws Exception
     */
    public Object handler(Class clazz, JSONObject jsonObject, String path) throws Exception {
        Object obj = clazz.newInstance();
        if (obj == null) {
            throw new NullException("空指针异常，传进来的类生成的对象为空。");
        }

        Field[] fields = clazz.getDeclaredFields();

        if (fields.length == 0 || fields == null) {
            throw new NullException(clazz.toString() + " 这个类的对象的属性为空，该类没有属性，空指针异常");
        }
        /**
         * 遍历属性名
         */
        for (Field field : fields) {
            if(environment==null||environment.equals(null)){
                throw new NullException("Environment 环境变量为空，请先设置environment变量的值");
            }

            /**
             * 获取json中对应的key
             */
            String jsonKey = environment.getProperty(path + "." + field.getName() + "." + "value");
            if (jsonKey == null || jsonKey.isEmpty() || jsonKey.equalsIgnoreCase("")) {
                throw new NullException("获取不到该类指定属性名：" + field.getName() + " 对应json字符串中的key ,即获取不到 yml中这个 key: " + path + "." + field.getName() + "." + "value" + "对应的值");
            }
            /**
             * 获取Java对象属性的类型，这里只是为了为Java属性名设置值的时候使用，不能根据这来判断json对象通过key获取的对象是什么类型
             */
            String type = environment.getProperty(path + "." + field.getName() + "." + "type");
            if (type == null || type.isEmpty() || type.equalsIgnoreCase("")) {
                throw new NullException("获取不到该类指定属性名：" + field.getName() + " 的Java类型 ,即获取不到 yml中这个 key: " + path + "." + field.getName() + "." + "type" + "对应的值");
            }
            /**
             * 获取相应的json的object对象
             */
            Object jsonObj = jsonObject.get(jsonKey);
            if (jsonObj == null) {
                throw new NullException("从json中获取不到指定key的object");
            }
            field.setAccessible(true);
            if (jsonObj instanceof String) {
                if (type.equalsIgnoreCase("string")) {
                    /**
                     * 字符串赋值
                     */
                    field.set(obj, jsonObj.toString());
                } else if (type.equalsIgnoreCase("char")) {
                    /**
                     * 这里值是将字符串的第一个字符转换成 字符
                     */
                    field.set(obj, jsonObj.toString().toCharArray()[0]);
                } else if (type.equalsIgnoreCase("date")) {
                    /**
                     * 如果是日期，就根据指定格式转换成 时间
                     */
                    String formatPattern = environment.getProperty(path + "." + field.getName() + "." + "pattern");
                    SimpleDateFormat sdf = new SimpleDateFormat(formatPattern);
                    /**
                     * 格式化时间的时候可能 会出现异常，要格式化的字符串可能不符合要求
                     */
                    try {
                        Date date = sdf.parse(jsonObj.toString());
                        field.set(obj, date);
                    } catch (Exception e) {
                        throw new Exception("要转换成日期的字符串格式不对");
                    } finally {
                        /**
                         * 设置默认时间为当前时间
                         */
//                        field.set(obj,new Date());
                    }
                }
            } else if (jsonObj instanceof JSONObject) {
                /**
                 * 如果是对象，就设置对象属性，并进行其对象属性的相关赋值操作，递归
                 */
                field.set(obj, this.handler(field.getType(), jsonObject.getJSONObject(jsonKey), path + "." + field.getName()));
                /**
                 * 如果是数组，就解析数组
                 */
            } else if (jsonObj instanceof JSONArray) {
                field.set(obj, this.parseArray((JSONArray) jsonObj, field, path + "." + field.getName()));
            } else if (jsonObj instanceof Long) {

                /**
                 *  获取的object对象 只有integer 和long 对象，没得short和 byte
                 * 这里是防止 byte，short，int类型的数据，获取到的类型是long 类型，这是非常有可能的
                 */
                if (type.equalsIgnoreCase("long")) {
                    /**
                     *  long 转换成 long
                     */
                    field.set(obj, ((Long) jsonObj).longValue());
                } else if (type.equalsIgnoreCase("int")) {
                    if ((long) jsonObj > Integer.MAX_VALUE) {
                        throw new NumberException("数值太大不能将 long 转换成 int，int类型的最大值是：" + Integer.MAX_VALUE + "，而获取的值的值是：" + jsonObj.toString() + ", 对应的字段名字是：" + clazz.toString() + "." + field.getName());
                    }
                    field.set(obj, (int) ((Long) jsonObj).longValue());
                } else if (type.equalsIgnoreCase("short")) {
                    if ((long) jsonObj > Short.MAX_VALUE) {
                        throw new NumberException("数值太大不能将 long 转换成 short，short类型的最大值是：" + Short.MAX_VALUE + "，而获取的值的值是：" + jsonObj.toString() + ", 对应的字段名字是：" + clazz.toString() + "." + field.getName());
                    }
                    field.set(obj, (short) ((Long) jsonObj).longValue());
                } else if (type.equalsIgnoreCase("byte")) {
                    if ((long) jsonObj > Byte.MAX_VALUE) {
                        throw new NumberException("数值太大不能将 long 转换成 byte，byte类型的最大值是：" + Byte.MAX_VALUE + "，而获取的值的值是：" + jsonObj.toString() + ", 对应的字段名字是：" + clazz.toString() + "." + field.getName());
                    }
                    field.set(obj, (byte) ((Long) jsonObj).longValue());
                }
            } else if (jsonObj instanceof Integer) {
                if (type.equalsIgnoreCase("int")) {
                    /**
                     *  int 转换成 int
                     */
                    field.set(obj, ((Integer) jsonObj).intValue());
                } else if (type.equalsIgnoreCase("long")) {

                    /**
                     * 可能获取的是 int类型，但是，对象中的属性是long类型
                     *  int 转换成 long 类型
                     */
                    field.set(obj, (long) ((Integer) jsonObj).intValue());
                } else if (type.equalsIgnoreCase("short")) {
                    if ((Integer) jsonObj > Short.MAX_VALUE) {
                        throw new NumberException("数值太大不能将 int 转换成 short，short类型的最大值是：" + Short.MAX_VALUE + "，而获取的值的值是：" + jsonObj.toString() + ", 对应的字段名字是：" + clazz.toString() + "." + field.getName());
                    }
                    /**
                     *  int 转换成 short 类型
                     */
                    field.set(obj, (short) ((Integer) jsonObj).intValue());
                } else if (type.equalsIgnoreCase("byte")) {
                    if ((Integer) jsonObj > Byte.MAX_VALUE) {
                        throw new NumberException("数值太大不能将 int 转换成 byte，byte类型的最大值是：" + Byte.MAX_VALUE + "，而获取的值的值是：" + jsonObj.toString() + ", 对应的字段名字是：" + clazz.toString() + "." + field.getName());
                    }
                    /**
                     *  int 转换成 byte
                     */
                    field.set(obj, (byte) ((Integer) jsonObj).intValue());
                }
            } else if (jsonObj instanceof BigDecimal) {
                if (type.equalsIgnoreCase("BigDecimal")) {
                    field.set(obj, (BigDecimal) jsonObj);
                } else if (type.equalsIgnoreCase("double")) {
                    /**
                     * 如果是大小数，就转换成 double类型
                     */
                    /**
                     * 这里是判断 获取的数值是否大于 double类型的最大值，如果大于，则不能赋值，抛出异常
                     */
                    BigDecimal doubleMaxValue = new BigDecimal(Double.MAX_VALUE);
                    if (doubleMaxValue.compareTo((BigDecimal) jsonObj) == -1) {
                        throw new NumberException("BigDecimal对象的 数值大于 double类型的最大值：" + Double.MAX_VALUE + "。 对应的字段是 " + clazz.toString() + "." + field.getName());
                    }
                    field.set(obj, ((BigDecimal) jsonObj).doubleValue());
                } else if (type.equalsIgnoreCase("float")) {
                    /**
                     * 如果是大小数，就转换成 float类型
                     */
                    /**
                     * 这里是判断 获取的数值是否大于 float类型的最大值，如果大于，则不能赋值，抛出异常
                     */
                    BigDecimal floatMaxValue = new BigDecimal(Float.MAX_VALUE);
                    if (floatMaxValue.compareTo((BigDecimal) jsonObj) == -1) {
                        throw new NumberException("BigDecimal对象的 数值大于 float类型的最大值：" + Float.MAX_VALUE + "。 对应的字段是 " + clazz.toString() + "." + field.getName());
                    }
                    field.set(obj, ((BigDecimal) jsonObj).floatValue());
                }
            } else if (jsonObj instanceof Boolean && type.equalsIgnoreCase("boolean")) {
                field.set(obj, ((Boolean) jsonObj).booleanValue());
            } else {
                field.set(obj, jsonObj);
            }
        }
        return obj;
    }

    private List<Object> parseArray(JSONArray jsonArray, Field field, String path) throws Exception {
        List<Object> objList = new ArrayList<>();

        for (Object obj : jsonArray) {
            if (obj instanceof JSONObject) {
                objList.add(this.handler(this.getListType(field.getGenericType()), (JSONObject) obj, path));
            } else if (obj instanceof JSONArray) {
                objList.add(this.parseArray((JSONArray) obj, field, path));
            } else {
                /**
                 * 如果是其他的基本类型，直接设置属性值
                 */
                objList.add(obj);
            }
        }
        return objList;
    }

    /**
     * 递归获取list集合中的元素类型，返回list集合中最终的元素的类型
     *
     * @param type
     * @return
     */
    private Class getListType(Type type) throws NullException {
        if (type == null) {
            throw new NullException("空指针异常，获取list集合中元素的类型时，类型为空，");
        }
        ParameterizedType parameterizedType = (ParameterizedType) type;
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        /**
         * 如果还有list集合，继续递归，获取list中的最终的元素
         */
        if (actualTypeArgument.getTypeName().startsWith("java.util.List")) {
            return this.getListType(actualTypeArgument);
        }
        Class<?> listType = (Class<?>) actualTypeArgument;
        return listType;
    }
}


