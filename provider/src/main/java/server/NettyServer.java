package server;

import codec.NettyDecoder;
import codec.NettyEncoder;
import entity.ClientRequest;
import entity.ServerResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import serialize.KryoSerializer;

import java.util.concurrent.TimeUnit;

/**
 * Date:2023/10/2 16:12
 * Author:jyq
 * Description:
 */
@Slf4j
@Component
public class NettyServer implements ApplicationListener<ContextRefreshedEvent> {

    // private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    public final static int PORT = 9999;
    
    public void start() {
        log.info("netty server start");
        // System.out.println("netty server start");

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.TCP_NODELAY, true)
                // .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        // 10秒之内没有收到客户端的请求，则关闭该连接
                        pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                        pipeline.addLast(new NettyEncoder(new KryoSerializer(), ServerResponse.class));
                        pipeline.addLast(new NettyDecoder(new KryoSerializer(), ClientRequest.class));
                        pipeline.addLast(new NettyServerHandler());
                    }
                });
        try {
            ChannelFuture f = serverBootstrap.bind(PORT).sync();
            // log.info("begin register service");
            
            //手动注册服务
            // TestService testService = new TestServiceImpl();
            // String rpcServiceName = testService.getClass().getInterfaces()[0].getCanonicalName();
            // log.info("rpcServiceName: " + rpcServiceName);//service.TestService
            // ServiceRegistry serviceRegistry = new ZkServiceRegistry();
            // String host = InetAddress.getLocalHost().getHostAddress();
            // serviceRegistry.registerService(rpcServiceName, new InetSocketAddress(host, PORT));
            //注入serviceMap
            // Medium.serviceProvider.addService(rpcServiceName, testService, testService.getClass().getDeclaredMethods()[0]);
            // Method m = testService.getClass().getDeclaredMethods()[0];
            // String methodName = m.getName();
            // log.info("methodName: " + methodName);
            // Medium.mediaMap.put(methodName, new BeanMethod(testService, m));
            
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            // logger.error("occur exception when start server:" + e);
            log.error("occur exception when start server:" + e);
        }  finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.start();
    }
}
