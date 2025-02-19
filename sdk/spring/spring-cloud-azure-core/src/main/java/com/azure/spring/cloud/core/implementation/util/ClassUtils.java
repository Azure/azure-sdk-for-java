// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ClassUtils {

    private ClassUtils() {

    }

    private static final Map<Class<?>, Function<Object, Boolean>> PRIMITIVE_DEFAULT_VALUE_CHECKER;
    static {
        PRIMITIVE_DEFAULT_VALUE_CHECKER = new HashMap<>(8);
        PRIMITIVE_DEFAULT_VALUE_CHECKER.put(boolean.class, (v) -> !((Boolean) v));
        PRIMITIVE_DEFAULT_VALUE_CHECKER.put(byte.class, (v) -> (Byte) v == 0);
        PRIMITIVE_DEFAULT_VALUE_CHECKER.put(char.class, (v) -> (Character) v == '\u0000');
        PRIMITIVE_DEFAULT_VALUE_CHECKER.put(short.class, (v) -> (Short) v == 0);
        PRIMITIVE_DEFAULT_VALUE_CHECKER.put(int.class, (v) -> (Integer) v == 0);
        PRIMITIVE_DEFAULT_VALUE_CHECKER.put(long.class, (v) -> (Long) v == 0);
        PRIMITIVE_DEFAULT_VALUE_CHECKER.put(double.class, (v) -> (Double) v == 0d);
        PRIMITIVE_DEFAULT_VALUE_CHECKER.put(float.class, (v) -> (Float) v == 0.0f);
    }

    /**
     * Check if it's a primitive type property with default init value.
     * @param type Type class to be checked.
     * @param value Value to be checked.
     * @return True if it's a primitive type property with default init value.
     */
    public static boolean isPrimitiveDefaultValue(Class<?> type, Object value) {
        return PRIMITIVE_DEFAULT_VALUE_CHECKER.containsKey(type)
            && PRIMITIVE_DEFAULT_VALUE_CHECKER.get(type).apply(value);
    }

    /**
     * Check if it's a primitive type property and has a non default value.
     * @param type Type class to be checked.
     * @param value Value to be checked.
     * @return True if it's a primitive type property and has a non default value.
     */
    public static boolean isPrimitiveNonDefaultValue(Class<?> type, Object value) {
        return PRIMITIVE_DEFAULT_VALUE_CHECKER.containsKey(type)
            && !PRIMITIVE_DEFAULT_VALUE_CHECKER.get(type).apply(value);
    }

    /**
     * Check if it's a primitive type property
     * @param type Type class to be checked.
     * @return True if it's a primitive type property
     */
    public static boolean isPrimitive(Class<?> type) {
        return PRIMITIVE_DEFAULT_VALUE_CHECKER.containsKey(type);
    }

}
