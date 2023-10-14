package client;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import constants.RpcConstants;
import core.ResultFuture;
import entity.RpcMessage;
import entity.ServerResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Date:2023/10/2 16:53
 * Author:jyq
 * Description:
 */
@Slf4j
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);
    // private final Executor exec = Executors.newFixedThreadPool(RpcConstants.EVENTLOOP_EXECUTOR_SIZE);
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("client：连接建立");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //无编解码器
        // ByteBuf byteBuf = (ByteBuf) msg;
        // System.out.println("服务端发送的消息是:" + byteBuf.toString(CharsetUtil.UTF_8));

        //有编解码器
        // String response = (String) msg;
        // System.out.println("服务端发送的消息是：" + response);
        
        try {
            if (msg instanceof RpcMessage) {
                log.info("client receive message: " + msg);
                RpcMessage tmp = (RpcMessage) msg;
                byte messageType = tmp.getMessageType();
                if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
                    log.info("client receive heart beat response [{}]", tmp.getData());
                } else if (messageType == RpcConstants.RESPONSE_TYPE) {
                    ServerResponse response = (ServerResponse) ((RpcMessage) msg).getData();
                    ResultFuture.receive(response);
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
        
        // exec.execute(new Runnable() {
        //     @Override
        //     public void run() {
        //         try {
        //             if (msg instanceof RpcMessage) {
        //                 log.info("client receive message: " + msg);
        //                 RpcMessage tmp = (RpcMessage) msg;
        //                 byte messageType = tmp.getMessageType();
        //                 if (messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE) {
        //                     log.info("client receive heart beat response [{}]", tmp.getData());
        //                 } else if (messageType == RpcConstants.RESPONSE_TYPE) {
        //                     ServerResponse response = (ServerResponse) ((RpcMessage) msg).getData();
        //                     ResultFuture.receive(response);
        //                 }
        //             }
        //         } finally {
        //             ReferenceCountUtil.release(msg);
        //         }
        //     }
        // });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("client catch exception", cause);
        ctx.close();
    }

    // @Override
    // public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    //     if (evt instanceof IdleStateEvent) {
    //         IdleStateEvent event = (IdleStateEvent) evt;
    //         if (event.state().equals(IdleState.WRITER_IDLE)) {
    //             log.info("write timeout happen, send the heart beat request");
    //             // ClientRequest heartBeatRequest = new ClientRequest(0L);
    //             RpcMessage rpcMessage = new RpcMessage();
    //             rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
    //             rpcMessage.setCodec(SerializationTypeEnum.KRYO.getCode());//可以指定protobuf
    //             rpcMessage.setData(RpcConstants.PING);
    //             //发送消息操作失败则关闭连接
    //             ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    //         }
    //     } else {
    //         ctx.fireUserEventTriggered(evt);
    //     }
    // }
}
