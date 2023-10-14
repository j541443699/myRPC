package serialize;

/**
 * Date:2023/10/2 21:31
 * Author:jyq
 * Description:
 */
public interface Serializer {

    /**
     * 序列化
     * @param obj
     * @return
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化
     * @param bytes
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);

}
