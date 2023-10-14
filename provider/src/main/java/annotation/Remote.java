package annotation;

import java.lang.annotation.*;

/**
 * 注册服务
 * 
 * @author jyq
 * @createTime 2023/10/3 19:14
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Remote {
}
