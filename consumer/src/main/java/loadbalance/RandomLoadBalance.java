package loadbalance;

import java.util.List;
import java.util.Random;

/**
 * @author jyq
 * @createTime 2023/10/3 16:29
 */
public class RandomLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceUrlList) {
        if (serviceUrlList.size() == 0) return null;
        Random random = new Random();
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));
    }
}
