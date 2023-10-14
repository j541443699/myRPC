package core;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author jyq
 * @createTime 2023/10/4 17:04
 */
@Slf4j
public class ChannelManager {
    public static Map<String, Channel> channelMap = new ConcurrentHashMap<>();
    public static List<Channel> channelList = new ArrayList<>();
    
    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        log.info("inetSocketAddress: " + key);
        // System.out.println("channelMap: " + channelMap);
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        return null;
    }
    
    

    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
        System.out.println("channelMap: " + channelMap);
    }
}
