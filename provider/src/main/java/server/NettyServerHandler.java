package server;

import constants.RpcConstants;
import entity.ClientRequest;
import entity.RpcMessage;
import entity.ServerResponse;
import enums.SerializationTypeEnum;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import medium.Medium;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Date:2023/10/2 16:28
 * Author:jyq
 * Description:
 */
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    // private final Executor exec = Executors.newFixedThreadPool(RpcConstants.EVENTLOOP_EXECUTOR_SIZE);
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("server：连接建立");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // log.info("服务器channelRead：" + msg.toString());
        //无编解码器
        // System.out.println(msg.toString());// 不能这样写，这样会输出PooledUnsafeDirectByteBuf(ridx: 0, widx: 12, cap: 2048)
        // ByteBuf byteBuf = (ByteBuf) msg;
        // System.out.println("客户端发送的消息是:" + byteBuf.toString(CharsetUtil.UTF_8));

        //有编解码器
        // String response = (String) msg;
        // System.out.println("客户端发送的消息是：" + response);
        
        try {
            if (msg instanceof RpcMessage) {
                log.info("server receive message: {}", msg);
                RpcMessage tmp = (RpcMessage) msg;
                byte messageType = tmp.getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.KRYO.getCode());//这里可以更换序列化器
                if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
                    log.info("server receive heart beat request [{}]", tmp.getData());
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                } else {
                    ClientRequest clientRequest = (ClientRequest) tmp.getData();
                    Medium medium = Medium.newInstance();
                    Object result = medium.process(clientRequest);
                    log.info("server get result: {}", result.toString());
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    ServerResponse response = new ServerResponse();
                    response.setRequestId(clientRequest.getRequestId());
                    response.setCode(200);
                    response.setMessage("The remote call is successful");
                    response.setData(result);
                    rpcMessage.setData(response);
                }
                ctx.channel().writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
        
        // exec.execute(new Runnable() {
        //     @Override
        //     public void run() {
        //         try {
        //             if (msg instanceof RpcMessage) {
        //                 log.info("server receive message: {}", msg);
        //                 RpcMessage tmp = (RpcMessage) msg;
        //                 byte messageType = tmp.getMessageType();
        //                 RpcMessage rpcMessage = new RpcMessage();
        //                 rpcMessage.setCodec(SerializationTypeEnum.KRYO.getCode());//这里可以更换序列化器
        //                 if (messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE) {
        //                     log.info("server receive heart beat request [{}]", tmp.getData());
        //                     rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
        //                     rpcMessage.setData(RpcConstants.PONG);
        //                 } else {
        //                     ClientRequest clientRequest = (ClientRequest) tmp.getData();
        //                     Medium medium = Medium.newInstance();
        //                     Object result = medium.process(clientRequest);
        //                     log.info("server get result: {}", result.toString());
        //                     rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
        //                     ServerResponse response = new ServerResponse();
        //                     response.setRequestId(clientRequest.getRequestId());
        //                     response.setCode(200);
        //                     response.setMessage("The remote call is successful");
        //                     response.setData(result);
        //                     rpcMessage.setData(response);
        //                 }
        //                 ctx.channel().writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        //             }
        //         } finally {
        //             ReferenceCountUtil.release(msg);
        //         }
        //     }
        // });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        //无编解码器
        // ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer();
        // byteBuf.writeBytes("你好client".getBytes(CharsetUtil.UTF_8));
        // byteBuf.writeBytes("你好client".getBytes());
        // ctx.writeAndFlush(byteBuf);//有编码器的情况下，使用该写法，不会通过NettyEncoder吗？如果MessageToByteEncoder<T>中T为Object，会进入encode方法，
        // 如果T为String，则不会进入encode方法，而是绕过NettyEncoder直接发出去？然后客户端这边NettyDecoder报错，待解决。

        //有编解码器
        // String content = "你好client";
        // ctx.writeAndFlush(content);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //利用链路空闲机制实现心跳检测，检测链路的可用性
        // log.info("userEventTriggered call");
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state().equals(IdleState.READER_IDLE)) {
                // System.out.println("读空闲");
                log.info("read timeout happen, close the connection");
                ctx.close();
            }
            // if (event.state().equals(IdleState.WRITER_IDLE)) {
            //     System.out.println("写空闲");
            // }
            // if (event.state().equals(IdleState.ALL_IDLE)) {
            //     System.out.println("读写空闲");
            // }
        }
    }
}
