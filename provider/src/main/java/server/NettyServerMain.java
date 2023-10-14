package server;

import ch.qos.logback.classic.util.ContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author jyq
 * @createTime 2023/10/3 17:15
 */
// @SpringBootApplication
@ComponentScan("\\")
public class NettyServerMain {
    public static void main(String[] args) {
        // SpringApplication.run(NettyServerMain.class, args);
        ApplicationContext context = new AnnotationConfigApplicationContext(NettyServerMain.class);
        // System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY,"logback.xml");
    }
}
