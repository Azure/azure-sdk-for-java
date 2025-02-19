// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Utility class for code generation.
 */
public final class CodegenUtil {

    /**
     * Infer the type name from the return type string.
     *
     * @param returnTypeString The return type string.
     * @return The inferred parameterized type.
     * @throws RuntimeException If the class cannot be found.
     */
    public static ParameterizedType inferTypeNameFromReturnType(String returnTypeString) {
        if (returnTypeString == null || returnTypeString.isEmpty()) {
            return null;
        }
        int angleBracketIndex = returnTypeString.indexOf('<');
        if (angleBracketIndex == -1) {
            return null;
        }
        String rawTypeString = returnTypeString.substring(0, angleBracketIndex).trim();
        String typeArgumentsString
            = returnTypeString.substring(angleBracketIndex + 1, returnTypeString.length() - 1).trim();
        Class<?> rawType;
        try {
            rawType = Class.forName(rawTypeString);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String[] typeArgumentNames = typeArgumentsString.split(",");
        Type[] typeArguments = new Type[typeArgumentNames.length];
        for (int i = 0; i < typeArgumentNames.length; i++) {
            try {
                typeArguments[i] = Class.forName(typeArgumentNames[i].trim());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new ParameterizedType() {

            @Override
            public Type[] getActualTypeArguments() {
                return typeArguments;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
    }

    // Private Ctr
    private CodegenUtil() {
    }
}
