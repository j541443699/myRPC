package service;

import annotation.RemoteInvoke;
import constants.RpcConstants;
import entity.Test;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 客户端可调用的方法
 * 
 * @author jyq
 * @createTime 2023/10/3 18:51
 */
@Component
@Slf4j
public class TestController {
    
    @RemoteInvoke
    private TestService testService;
    
    public void test111() {
        // String test = testService.test(new Test("你好", "问候语"));
        // System.out.println(test);
        
        ExecutorService executor = Executors.newFixedThreadPool(RpcConstants.CHANNEL_SIZE);

        // 性能测试
        int times = 100000;
        CountDownLatch countDownLatch = new CountDownLatch(times);
        Long start = System.currentTimeMillis();
        for(int i=1;i<=times;i++){
            int j = i;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    testService.test(new Test("你好", "问候语"));
                    // log.info("finish -----------------------------------------------------------------" + j);
                    // System.out.println("finish -----------------------------------------------------------------" + i);
                    // System.out.print(j + "--" + (System.currentTimeMillis() - start)/1000 + "秒|");
                    countDownLatch.countDown();
                }
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Long end = System.currentTimeMillis();
        Long count = end-start;
        System.out.println(times + "次总计时:"+count/1000+"秒");
    }
    
}
