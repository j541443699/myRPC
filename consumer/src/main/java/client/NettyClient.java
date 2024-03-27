package client;

import codec.NettyDecoder;
import codec.NettyEncoder;
import constants.RpcConstants;
import core.ChannelManager;
import core.ResultFuture;
import entity.ClientRequest;
import entity.RpcMessage;
import entity.ServerResponse;
import enums.SerializationTypeEnum;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import registry.ServiceDiscovery;
import registry.zk.ZkServiceDiscovery;
import serialize.KryoSerializer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Date:2023/10/2 16:49
 * Author:jyq
 * Description:
 */
@Slf4j
public class NettyClient {

    // private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private ChannelManager channelManager;
    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;

    public volatile static NettyClient nettyClient;//防止指令重排序，其他线程获取未初始化完成的对象
    public static NettyClient getClient() {
        //双重校验锁
        if (nettyClient == null) {//一重校验
            synchronized (NettyClient.class) {
                if (nettyClient == null) {//二重校验
                    nettyClient = new NettyClient();
                    System.out.println("new NettyClient()");
                }
            }
        }
        return nettyClient;
    }
    
    public NettyClient() {

        // try {
            
            channelManager = new ChannelManager();
        
            // final String host = "127.0.0.1";
            // final int port = 9999;
            // Bootstrap bootstrap = new Bootstrap();
            bootstrap = new Bootstrap();
            // EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            eventLoopGroup = new NioEventLoopGroup();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            //连接不繁忙时，每隔五秒发送一次心跳包，保证连接可用性
                            pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new NettyEncoder(new KryoSerializer(), ClientRequest.class));
                            pipeline.addLast(new NettyDecoder(new KryoSerializer(), ServerResponse.class));
                            pipeline.addLast(new NettyClientHandler(NettyClient.this));
                        }
                    });

            // ChannelFuture f = bootstrap.connect(new InetSocketAddress(host, port)).sync();
            // log.info("send message");
        
            //无编解码器
            // ctx.writeAndFlush("你好server");// 不能直接将String对象写入Channel，这样无法传输，需要先进行序列化处理再写入Channel
            // ByteBuf byteBuf = ByteBufAllocator.DEFAULT.directBuffer();
            // byteBuf.writeBytes("你好server".getBytes(CharsetUtil.UTF_8));
            // f.core().writeAndFlush(byteBuf);

            //有编解码器
            // String content = "你好server";
            // f.core().writeAndFlush(content);
            // f.core().closeFuture().sync();
        // } catch (InterruptedException e) {
        //     log.error("occur exception when start client:" + e);
            // logger.error("occur exception when start client:" + e);
        // }
        
        //创建包含多个连接的连接池，用于单服务器处理多个连接n次调用的测试
        ServiceDiscovery serviceDiscovery = new ZkServiceDiscovery();
        String rpcServiceName = "service.TestService";//测试该服务下某个方法的n次调用
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);
        for (int i = 0; i < RpcConstants.CHANNEL_SIZE; i++) {
            try {
                ChannelFuture channelFuture = bootstrap.connect(inetSocketAddress).sync();
                ChannelManager.channelList.add(channelFuture.channel());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public ServerResponse sendRequest(ClientRequest clientRequest) {
        //服务发现
        ServiceDiscovery serviceDiscovery = new ZkServiceDiscovery();
        String rpcServiceName = clientRequest.getRpcServiceName();
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcServiceName);
        //获取/建立连接
        // Channel channel = getChannel(inetSocketAddress);
        Channel channel = getChannel2(inetSocketAddress);
        // System.out.println("channel: " + channel);
        //发送消息
        if (channel.isActive()) {
            log.info("send request");
            RpcMessage rpcMessage = RpcMessage.builder()
                    .messageType(RpcConstants.REQUEST_TYPE)
                    .codec(SerializationTypeEnum.KRYO.getCode())
                    .data(clientRequest)
                    .build();
            ResultFuture resultFuture = new ResultFuture(clientRequest);
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    log.info("client send message: [{}]", rpcMessage);
                } else {//优化点：保证连接发生异常后及时断开，尤其是resultFuture要调用receive方法，以结束sendRequest方法调用，以便下一次调用 sendRequest 方法时发起连接的重连
                    future.channel().close();
                    ServerResponse response = new ServerResponse();
                    response.setRequestId(clientRequest.getRequestId());
                    response.setData("send failed");
                    resultFuture.receive(response);
                    log.error("Send failed: " + future.cause());
                    System.out.println("Send failed:" + future.cause());
                }
            });
            ServerResponse serverResponse = resultFuture.get();//若连接出现异常，客户端无法发送消息，那么服务端无法接收请求，也就无法发回响应，那么 sendRequest 方法就会阻塞在这里，测试用例的for循环也就卡在这里！！
            return serverResponse;
        } else {
            throw new IllegalStateException("无法发送消息");
        }
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress) {
        Channel channel = channelManager.get(inetSocketAddress);
        if (channel == null) {
            channel = doConnect(inetSocketAddress);
            channelManager.set(inetSocketAddress, channel);
        }
        return channel;
    }
    
    //随机轮询连接池，用于单服务器处理多个连接n次调用的测试
    public Channel getChannel2(InetSocketAddress inetSocketAddress) {
        List<Channel> channelList = ChannelManager.channelList;
        if (channelList.size() == 0) {
            ChannelFuture channelFuture = null;
            // channelFuture = bootstrap.connect(inetSocketAddress).sync();
            // ChannelManager.channelList.add(channelFuture.channel());
            Channel channel = doConnect(inetSocketAddress);
            ChannelManager.channelList.add(channel);
        }
        Random random = new Random();
        int idx = random.nextInt(channelList.size());
        Channel channel = channelList.get(idx);
        //若服务器断开了连接，那么客户端这里从channelList获取的连接就不可用，会执行到这里，那么就重建连接
        if (!channel.isActive()) {
            ChannelManager.channelList.remove(channel);
            channel = doConnect(inetSocketAddress);
            ChannelManager.channelList.add(channel);
        }
        return channel;
    }

    public Channel doConnect(InetSocketAddress inetSocketAddress) {
        ChannelFuture channelFuture = null;
        try {
            log.info("doConnect: " + inetSocketAddress.toString());
            channelFuture = bootstrap.connect(inetSocketAddress).sync();
            //TODO: 连接建立失败后重连
            while (!channelFuture.channel().isActive()) {
                Thread.sleep(3000);
                channelFuture = bootstrap.connect(inetSocketAddress).sync();
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("连接失败");
        }
        return channelFuture.channel();
    }
}
