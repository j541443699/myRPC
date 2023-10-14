package client;

import service.TestController;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * @author jyq
 * @createTime 2023/10/3 18:38
 */
@ComponentScan("\\")
public class NettyClientMain {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(NettyClientMain.class);
        TestController testController = (TestController) context.getBean("testController");
        testController.test111();
    }
}
