package medium;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Method;

/**
 * @author jyq
 * @createTime 2023/10/3 19:54
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BeanMethod {
    private Object bean;
    private Method method;
}
