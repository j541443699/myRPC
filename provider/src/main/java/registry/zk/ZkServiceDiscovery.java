package registry.zk;

import exception.RpcException;
import loadbalance.LoadBalance;
import loadbalance.RandomLoadBalance;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import registry.ServiceDiscovery;
import registry.zk.utils.CuratorUtils;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author jyq
 * @createTime 2023/10/3 14:11
 */
@Slf4j
public class ZkServiceDiscovery implements ServiceDiscovery {
    
    private LoadBalance loadBalance;
    
    public ZkServiceDiscovery() {
        this.loadBalance = new RandomLoadBalance();
    }
    
    @Override
    public InetSocketAddress lookupService(String rpcServiceName) {
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if (serviceUrlList.size() == 0) {
            throw new RpcException("没有找到指定的服务");
        }
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList);
        log.info("successfully find the service address: [{}]", targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host, port);
    }
}
