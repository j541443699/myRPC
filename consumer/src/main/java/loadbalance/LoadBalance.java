package loadbalance;

import java.util.List;

/**
 * @author jyq
 * @createTime 2023/10/3 16:17
 */
public interface LoadBalance {
    String selectServiceAddress(List<String> serviceUrlList);
}
