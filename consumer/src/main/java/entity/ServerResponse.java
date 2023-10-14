package entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jyq
 * @createTime 2023/10/4 16:15
 */
@Getter
@Setter
@ToString
public class ServerResponse {
    
    private Long requestId;
    private int code;//响应码
    private String message;//响应消息
    private Object data;//响应体
    
    
}
