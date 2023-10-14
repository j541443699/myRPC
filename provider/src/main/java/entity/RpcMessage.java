package entity;

import lombok.*;

/**
 * @author jyq
 * @createTime 2023/10/6 16:42
 */

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RpcMessage {
    /**
     * rpc message type
     */
    private byte messageType;

    /**
     * serialization type
     */
    private byte codec;
    /**
     * request id
     */
    private int requestId;
    /**
     * request data
     */
    private Object data;
    
}
