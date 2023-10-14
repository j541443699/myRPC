package exception;

/**
 * @author jyq
 * @createTime 2023/10/3 14:17
 */
public class RpcException extends RuntimeException {
    public RpcException(String message) {
        super(message);
    }
}
