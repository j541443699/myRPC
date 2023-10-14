package registry;

import java.net.InetSocketAddress;

/**
 * Date:2023/10/3 13:01
 * Author:jyq
 * Description:
 */
public interface ServiceRegistry {

    /**
     * 注册服务到注册中心
     *
     * @param rpcServiceName
     * @param inetSocketAddress
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);

}
