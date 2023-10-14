package proxy;

import annotation.RemoteInvoke;
import client.NettyClient;
import entity.ClientRequest;
import entity.ServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 客户端通过注解增强客户端的方法（cglib）
 * 
 * @author jyq
 * @createTime 2023/10/4 14:46
 */
@Slf4j
@Component
public class InvokeProxy implements BeanPostProcessor {

    public static Enhancer enhancer = new Enhancer();
    
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        log.info("InvokeProxy postProcessBeforeInitialization");
        Field[] fields = bean.getClass().getDeclaredFields();//成员变量
        for (Field field : fields) {
            // System.out.println(field.getDeclaringClass() + " " + field.getType());
            if (field.isAnnotationPresent(RemoteInvoke.class)) {
                field.setAccessible(true);
                enhancer.setInterfaces(new Class[]{field.getType()});
                enhancer.setCallback(new MethodInterceptor() {
                    @Override
                    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
                        log.info("send message");
                        //向服务端发送消息
                        ClientRequest clientRequest = new ClientRequest();
                        clientRequest.setInterfaceName(method.getDeclaringClass().getName());//service.[接口名]
                        clientRequest.setMethodName(method.getName());//[方法名]
                        clientRequest.setParameters(args);
                        clientRequest.setParamTypes(method.getParameterTypes());
                        NettyClient nettyClient = NettyClient.getClient();
                        // System.out.println(nettyClient);
                        ServerResponse serverResponse = nettyClient.sendRequest(clientRequest);
                        log.info("receive response: " + serverResponse);
                        return serverResponse.getData();
                    }
                });
                try {
                    field.set(bean, enhancer.create());
                    log.info("已完成方法增强");
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            
        }
        return bean;
    }
}
