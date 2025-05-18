package io.github.irfnhanif.rifasims.validation;

import java.util.Map;
import java.util.HashMap;

public class ThreadLocalTypeInfo {
    private static final ThreadLocal<Map<String, Boolean>> STRING_TYPE_MAP = ThreadLocal.withInitial(HashMap::new);

    public static void setIsString(String fieldPath, boolean isString) {
        STRING_TYPE_MAP.get().put(fieldPath, isString);
    }


    public static boolean isString(String fieldValue) {
        return STRING_TYPE_MAP.get().getOrDefault(fieldValue, true);
    }

    public static void clear() {
        STRING_TYPE_MAP.get().clear();
    }
}
