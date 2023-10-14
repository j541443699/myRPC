package entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author jyq
 * @createTime 2023/10/4 15:58
 */
@Getter
@Setter
@ToString
public class ClientRequest {
    private Long requestId;//为什么用Long，而不是long
    private String interfaceName;
    private String methodName;//调用方法
    private Object[] parameters;//方法参数
    private Class<?>[] paramTypes;//参数类型
    private static AtomicLong readID = new AtomicLong(0);

    public ClientRequest() {
        requestId = readID.incrementAndGet();
    }
    
    public ClientRequest(Long id) {//用于心跳包
        requestId = id;
    }

    public String getRpcServiceName() {
        return this.getInterfaceName();
    }
}
