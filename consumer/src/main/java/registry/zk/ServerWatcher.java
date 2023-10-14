package registry.zk;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;
import registry.zk.utils.CuratorUtils;

import java.util.List;

/**
 * @author jyq
 * @createTime 2023/10/5 16:00
 */
@Slf4j
public class ServerWatcher implements CuratorWatcher {
    @Override
    public void process(WatchedEvent event) throws Exception {
        log.info("server nodes has changed!");
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        String servicePath = event.getPath();
        zkClient.getChildren().usingWatcher(this).forPath(servicePath);
        String rpcServiceName = servicePath.split("/")[1];
        List<String> serviceAddresses = zkClient.getChildren().forPath(servicePath);
        CuratorUtils.updateChildrenNodes(rpcServiceName, serviceAddresses);
        log.info("finish server nodes update!");
    }
}
