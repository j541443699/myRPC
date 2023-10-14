package codec;

import constants.RpcConstants;
import entity.RpcMessage;
import enums.SerializationTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import serialize.KryoSerializer;
import serialize.Serializer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date:2023/10/2 23:36
 * Author:jyq
 * Description:
 */

/**
 * <p>
 * custom protocol decoder
 * <p>
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId      |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 *
 * @author WangTao
 * @createTime on 2020/10/2
 * @see <a href="https://zhuanlan.zhihu.com/p/95621344">LengthFieldBasedFrameDecoder解码器</a>
 */
@Slf4j
public class NettyEncoder extends MessageToByteEncoder</*Object*/RpcMessage> {
    private final Serializer serializer;
    private final Class<?> genericClass;//
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    
    public NettyEncoder(Serializer serializer, Class<?> genericClass) {
        this.serializer = serializer;
        this.genericClass = genericClass;//能够发送的消息类型
    }

    /**
     * 将对象转换为字节数据然后写入到ByteBuf对象中
     *
     * @param channelHandlerContext
     * @param msg
     * @param out
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, /*Object*/RpcMessage msg, ByteBuf out) {
        // if (msg instanceof genericClass) {//为啥不能用这个

        // }
        log.info("encode call");
        // System.out.print("encode call");
        // Object msg = new String();
        // Class<?> genericClass = String.class;
        // log.info(msg.getClass().toString());
        // log.info(msg.toString());
        // log.info(genericClass.getClass().toString());
        // log.info(genericClass.toString());
        
        /*if (genericClass.isInstance(msg)) {//String.class isInstance(ByteBuf/String)，只能发送pipeline传输过来的genericClass类型数据，其他均被拦截下来不进行发送
            //需要加消息长度避免粘包问题
            byte[] body = serializer.serialize(msg);
            out.writeBytes(body);
            log.info("successful encode Object to ByteBuf");
        }*/

        /**
         * 4B full length（消息长度）  1B messageType（消息类型）  1B codec（序列化类型）  4B  requestId（请求的Id）  body（object类型数据）
         * 
         */
        try {
            //leave a place to write the value of full length
            out.writerIndex(4);
            byte messageType = msg.getMessageType();
            out.writeByte(messageType);
            out.writeByte(msg.getCodec());
            out.writeInt(ATOMIC_INTEGER.getAndIncrement());
            byte[] bodyBytes = null;
            int fullLength = RpcConstants.HEAD_LENGTH;
            if (messageType != RpcConstants.HEARTBEAT_REQUEST_TYPE && messageType != RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                String codecName = SerializationTypeEnum.getName(msg.getCodec());
                //SPI机制待补充
                Serializer serializer = new KryoSerializer();
                bodyBytes = serializer.serialize(msg.getData());
                //压缩机制待补充
                fullLength += bodyBytes.length;
            }

            if (bodyBytes != null) {
                out.writeBytes(bodyBytes);
            }
            
            int writeIndex = out.writerIndex();
            out.writerIndex(writeIndex - fullLength);
            // write the value of full length
            out.writeInt(fullLength);
            out.writerIndex(writeIndex);
            // System.out.print("finish encode call");
        } catch (Exception e) {
            log.error("Encode request error!");
            //百万压测异常重现，待测试
            System.out.println("Encode request error!");
        }
    }
    
}
