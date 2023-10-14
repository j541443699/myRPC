package medium;

import entity.ClientRequest;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author jyq
 * @createTime 2023/10/3 19:37
 */
public class Medium {
    
    // public static final ServiceProvider serviceProvider = new ServiceProviderImpl();
    public static final Map<String, Object> mediaMap = new ConcurrentHashMap<>();//static?
    private static Medium media = null;
    
    public static Medium newInstance() {
        if (media == null) {
            media = new Medium();
        }
        return media;
    }
    
    //处理来自客户端的远程调用请求
    public Object process(ClientRequest request) {
        Object result = null;
        try {
            // String methodName = request.getMethodName();
            // BeanMethod beanMethod = mediaMap.get(methodName);
            // if (beanMethod == null) {
            //     return null;
            // }
            // Object bean = beanMethod.getBean();
            // Method method = beanMethod.getMethod();

            String rpcServiceName = request.getRpcServiceName();
            Object bean = mediaMap.get(rpcServiceName);
            Method method = bean.getClass().getMethod(request.getMethodName(), request.getParamTypes());
            result = method.invoke(bean, request.getParameters());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    
}
