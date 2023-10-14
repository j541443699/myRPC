package service;

import entity.Test;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jyq
 * @createTime 2023/10/3 16:52
 */
@Slf4j
public class TestServiceImpl implements TestService {

    static {
        System.out.println("TestServiceImpl被创建");
    }
    
    @Override
    public String test(Test test) {
        log.info("TestServiceImpl.test被调用");
        log.info("Test message is: {}", test.getMessage());
        log.info("Test description is: {}", test.getDescription());
        String result = "来自服务器的问候——你好";
        log.info("TestServiceImpl返回: {}", result);
        return result;
    }
}
