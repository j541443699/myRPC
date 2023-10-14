package annotation;

import java.lang.annotation.*;

/**
 * 消费服务
 * 
 * @author jyq
 * @createTime 2023/10/3 19:05
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface RemoteInvoke {
    
}
