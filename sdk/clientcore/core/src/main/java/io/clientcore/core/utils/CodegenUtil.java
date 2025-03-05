// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.ResponseBodyMode;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for code generation.
 */
public final class CodegenUtil {

    /**
     * Infers the parameterized type from the return type string.
     *
     * @param returnTypeString The return type string.
     * @return The inferred parameterized type.
     * @throws RuntimeException If the class cannot be found.
     */
    public static ParameterizedType inferTypeNameFromReturnType(String returnTypeString) {
        if (returnTypeString == null || returnTypeString.isEmpty()) {
            return null;
        }

        // Extract raw type (before "<") and type arguments (inside "< >")
        int angleBracketIndex = returnTypeString.indexOf('<');
        if (angleBracketIndex == -1) {
            return null; // Not a parameterized type
        }

        String rawTypeString = returnTypeString.substring(0, angleBracketIndex).trim();
        String typeArgumentsString
            = returnTypeString.substring(angleBracketIndex + 1, returnTypeString.lastIndexOf('>')).trim();

        Class<?> rawType;
        try {
            rawType = Class.forName(rawTypeString); // Load raw type (e.g., java.util.List)
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Raw type class not found: " + rawTypeString, e);
        }

        List<Type> typeArguments = parseTypeArguments(typeArgumentsString);

        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return typeArguments.toArray(new Type[0]);
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

    /**
     * Parses the type arguments from a string.
     */
    private static List<Type> parseTypeArguments(String typeArgumentsString) {
        List<Type> typeArguments = new ArrayList<>();
        List<String> extractedTypeNames = extractTypeNames(typeArgumentsString);

        for (String typeName : extractedTypeNames) {
            try {
                // Recursively resolve parameterized types
                if (typeName.contains("<")) {
                    typeArguments.add(inferTypeNameFromReturnType(typeName));
                } else {
                    typeArguments.add(Class.forName(typeName.trim()));
                }
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Type argument class not found: " + typeName, e);
            }
        }

        return typeArguments;
    }

    /**
     * Extracts type names, handling nested generics correctly.
     */
    private static List<String> extractTypeNames(String input) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        StringBuilder current = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c == '<') {
                depth++;
            }
            if (c == '>') {
                depth--;
            }

            if (c == ',' && depth == 0) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString().trim());
        }

        return result;
    }

    /**
     * Retrieve the ResponseBodyMode from RequestOptions or use the default ResponseBodyMode.BUFFER.
     * @param requestOptions the request options set on the HttpRequest
     * @return the ResponseBodyMode from RequestOptions or ResponseBodyMode.BUFFER
     */
    public static ResponseBodyMode getOrDefaultResponseBodyMode(RequestOptions requestOptions) {
        ResponseBodyMode responseBodyMode;
        if (requestOptions != null && requestOptions.getResponseBodyMode() != null) {
            responseBodyMode = requestOptions.getResponseBodyMode();
        } else {
            responseBodyMode = ResponseBodyMode.BUFFER;
        }
        return responseBodyMode;
    }

    // Private Ctr
    private CodegenUtil() {
    }
}
