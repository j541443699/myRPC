package registry.zk.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import registry.zk.ServerWatcher;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date:2023/10/3 13:11
 * Author:jyq
 * Description:
 */
@Slf4j
public class CuratorUtils {

    public static final String ZK_REGISTRY_ROOT_PATH ="/myRPC";
    private static CuratorFramework zkClient;
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    
    public static CuratorFramework getZkClient() {
        if (zkClient == null) {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
            String zookeeperAddress = "localhost:2181";
            zkClient = CuratorFrameworkFactory.builder()
                    .connectString(zookeeperAddress)
                    .retryPolicy(retryPolicy)
                    .build();
            zkClient.start();
            return zkClient;
        }
        return zkClient;
    }

    /**
     * 
     * @param zkClient
     * @param servicePath eg: /myRPC/service.TestService/192.168.253.1:9999
     */
    public static void createPersistentNode(CuratorFramework zkClient, String servicePath) {
        try {
            //zk服务器注册的服务是REGISTERED_PATH_SET的超集
            if (REGISTERED_PATH_SET.contains(servicePath) || zkClient.checkExists().forPath(servicePath) != null) {
                log.info("The node already exists. The node is: [{}]", servicePath);
            } else {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(servicePath);
                log.info("The node was created successfully. The node is [{}]", servicePath);
            }
            REGISTERED_PATH_SET.add(servicePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param zkClient
     * @param rpcServiceName eg: service.TestService
     * @return
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTRY_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(servicePath, result);
            registerWatcher(zkClient, rpcServiceName);
        } catch (Exception e) {
            log.error("get children nodes for path [{}] fail", servicePath);
        }
        return result;
    }

    /**
     * 监听注册中心节点的变更
     * 
     * @param zkClient
     * @param rpcServiceName
     */
    public static void registerWatcher(CuratorFramework zkClient, String rpcServiceName) {
        CuratorWatcher watcher = new ServerWatcher();
        String servicePath = ZK_REGISTRY_ROOT_PATH + "/" + rpcServiceName;
        log.info("register watcher path: " +servicePath);
        try {
            zkClient.getChildren().usingWatcher(watcher).forPath(servicePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void updateChildrenNodes(String rpcServiceName, List<String> serviceAddresses) {
        SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
    }
}
