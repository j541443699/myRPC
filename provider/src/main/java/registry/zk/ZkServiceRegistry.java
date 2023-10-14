package registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import registry.ServiceRegistry;
import registry.zk.utils.CuratorUtils;

import java.net.InetSocketAddress;

/**
 * 服务注册（基于zookeeper实现）
 * 
 * @author jyq
 * @createTime 2023/10/3 13:07
 */
@Slf4j
public class ZkServiceRegistry implements ServiceRegistry {
    
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTRY_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient, servicePath);
    }
}
