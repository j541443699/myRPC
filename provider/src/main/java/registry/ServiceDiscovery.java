package registry;

import java.net.InetSocketAddress;

/**
 * Date:2023/10/3 13:01
 * Author:jyq
 * Description:
 */

public interface ServiceDiscovery {

    /**
     * 根据rpcServiceName获取远程服务地址
     *
     * @param rpcServiceName
     * @return
     */
    InetSocketAddress lookupService(String rpcServiceName);

}
