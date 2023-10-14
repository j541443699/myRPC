package codec;

import constants.RpcConstants;
import entity.ClientRequest;
import entity.RpcMessage;
import entity.ServerResponse;
import enums.SerializationTypeEnum;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import serialize.KryoSerializer;
import serialize.Serializer;

/**
 * Date:2023/10/3 0:15
 * Author:jyq
 * Description:
 */
@Slf4j
public class NettyDecoder extends /*ByteToMessageDecoder*/LengthFieldBasedFrameDecoder {

    private final Serializer serializer;
    private final Class<?> genericClass;//接收消息的类型
    
    /**
     * 4B full length（消息长度）  1B messageType（消息类型）  1B codec（序列化类型）  4B requestId（请求的Id）  body（object类型数据）
     *
     */
    public NettyDecoder(Serializer serializer, Class<?> genericClass) {
        super(RpcConstants.MAX_FRAME_LENGTH, 0, 4, -4, 0);
        this.serializer = serializer;
        this.genericClass = genericClass;
    }

    // /**
    //  * 解码ByteBuf对象
    //  *
    //  * @param channelHandlerContext
    //  * @param byteBuf
    //  * @param list
    //  * @throws Exception
    //  */
    // @Override
    // protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
    //
    //     log.info("decode call");
    //
    //     byte[] body = new byte[byteBuf.readableBytes()];
    //     byteBuf.readBytes(body);
    //     Object obj = serializer.deserialize(body, genericClass);
    //     list.add(obj);
    //     log.info("successful decode ByteBuf to Object");
    // }

    /**
     * 字节流分段并转换为RpcMessage对象，并将该对象传给下一个handler
     * 
     * @param ctx
     * @param in 字节流
     * @return
     * @throws Exception
     */
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        log.info("decode call");
        // System.out.print("decode call");
        //获取字节流的分段（依然是ByteBuf对象）
        Object decoded = super.decode(ctx, in);
        if (decoded != null) {
            ByteBuf frame = (ByteBuf) decoded;
            if (frame.readableBytes() >= RpcConstants.HEAD_LENGTH) {
                try {
                    //分段转换为RpcMessage对象，并将该对象传给下一个handler
                    return decodeFrame(frame);
                } catch (Exception e) {
                    log.error("Decode frame error!");
                    throw e;
                } finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    private Object decodeFrame(ByteBuf in) {
        int fullLength = in.readInt();
        byte messageType = in.readByte();
        byte codecType = in.readByte();
        int requestId = in.readInt();
        RpcMessage rpcMessage = RpcMessage.builder()
                .messageType(messageType)
                .codec(codecType)
                .requestId(requestId).build();
        if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        int bodyLength = fullLength - RpcConstants.HEAD_LENGTH;
        if (bodyLength > 0) {
            byte[] bodyBytes = new byte[bodyLength];
            in.readBytes(bodyBytes);
            //解压缩待补充
            String codecName = SerializationTypeEnum.getName(codecType);
            //SPI待补充
            Serializer serializer = new KryoSerializer();
            if (messageType == RpcConstants.REQUEST_TYPE) {
                ClientRequest clientRequest = serializer.deserialize(bodyBytes, ClientRequest.class);
                rpcMessage.setData(clientRequest);
            } else {
                ServerResponse serverResponse = serializer.deserialize(bodyBytes, ServerResponse.class);
                rpcMessage.setData(serverResponse);
            }
        }
        // System.out.print("finish decode call");
        return rpcMessage;
    }
}
