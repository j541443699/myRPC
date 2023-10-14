package enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author jyq
 * @createTime 2023/10/6 17:14
 */
@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {
    KRYO((byte) 1, "kryo");
    private final byte code;
    private final String serializerName;
    public static String getName(byte code) {
        for (SerializationTypeEnum e : SerializationTypeEnum.values()) {
            if (e.getCode() == code) {
                return e.serializerName;
            }
        }
        return null;
    }
}
