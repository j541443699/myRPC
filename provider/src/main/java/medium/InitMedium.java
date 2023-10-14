package medium;

import annotation.Remote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import registry.ServiceRegistry;
import registry.zk.ZkServiceRegistry;
import server.NettyServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * 服务端通过注解注册服务并写入serviceMap
 * 
 * @author jyq
 * @createTime 2023/10/3 20:00
 */
@Slf4j
@Component
public class InitMedium implements BeanPostProcessor {

    /**
     * 
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        log.info("InitMedium postProcessAfterInitialization");
        try {
            if (bean.getClass().isAnnotationPresent(Remote.class)) {
                
                // Method[] methods = bean.getClass().getDeclaredMethods();//没考虑到重载方法
                ServiceRegistry serviceRegistry = new ZkServiceRegistry();
                String host = InetAddress.getLocalHost().getHostAddress();
                String rpcServiceName = bean.getClass().getInterfaces()[0].getCanonicalName();
                log.info("InitMedium rpcServiceName: " + rpcServiceName);
                //1注册服务
                serviceRegistry.registerService(rpcServiceName, new InetSocketAddress(host, NettyServer.PORT));
                //2注入serviceMap
                // for (Method m : methods) {
                    // Medium.serviceProvider.addService(rpcServiceName, bean, m);
                    // Medium.mediaMap.put(m.getName(), new BeanMethod(bean, m));
                // }
                Medium.mediaMap.put(bean.getClass().getInterfaces()[0].getCanonicalName(), bean);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return bean;
    }
}
