package service;

import annotation.Remote;
import entity.Test;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author jyq
 * @createTime 2023/10/3 16:52
 */
@Slf4j
@Remote
@Component
public class TestServiceImpl2 implements TestService {

    static {
        System.out.println("TestServiceImpl2被创建");
    }
    
    @Override
    public String test(Test test) {
        log.info("TestServiceImpl2.test被调用");
        log.info("Test message is: {}", test.getMessage());
        log.info("Test description is: {}", test.getDescription());
        String result = "来自服务器的问候——你好2";
        log.info("TestServiceImpl2返回: {}.", result);
        return result;
    }
}
