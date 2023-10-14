package constants;

import io.netty.util.NettyRuntime;

/**
 * @author jyq
 * @createTime 2023/10/6 17:08
 */
public class RpcConstants {
    
    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    //ping
    public static final byte HEARTBEAT_REQUEST_TYPE = 3;
    //pong
    public static final byte HEARTBEAT_RESPONSE_TYPE = 4;
    public static final int HEAD_LENGTH = 10;

    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;
    public static final String PING = "ping";
    public static final String PONG = "pong";

    //线程组大小
    // public static final int CHANNEL_SIZE = NettyRuntime.availableProcessors() * 2;//16 * 2
    public static final int CHANNEL_SIZE = 32;
    //EventLoop分配的线程池大小
    public static final int EVENTLOOP_EXECUTOR_SIZE = 10;
}
