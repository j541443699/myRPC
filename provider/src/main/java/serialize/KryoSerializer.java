package serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import entity.ClientRequest;
import entity.ServerResponse;
import exception.SerializeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Date:2023/10/2 21:47
 * Author:jyq
 * Description:
 */
public class KryoSerializer implements Serializer {

    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(ClientRequest.class);
        kryo.register(ServerResponse.class);
        // kryo.register(RpcMessage.class)
        //如果为 true，则遇到未注册的类时会引发异常。默认为 false。如果为 false，则当遇到未注册的类时，将序列化其完全限定类名，并使用该类的默认序列化程序来序列化该对象。
        //同一对象图中该类的后续出现将被序列化为 int id。
        // kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();//为啥要remove
            return output.toBytes();
        } catch (IOException e) {
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream);) {
            Kryo kryo = kryoThreadLocal.get();
            Object o = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return clazz.cast(o);
        } catch (IOException e) {
            throw new SerializeException("反序列化失败");
        }
    }
}
