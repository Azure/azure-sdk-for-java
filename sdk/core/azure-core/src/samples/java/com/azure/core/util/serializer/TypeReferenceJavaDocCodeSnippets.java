// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import java.util.Map;

/**
 * Code snippets for {@link TypeReference}.
 */
public class TypeReferenceJavaDocCodeSnippets {
    public void constructGenericTypeReference() {
        // BEGIN: com.azure.core.util.serializer.constructor
        // Construct a TypeReference<T> for a Java generic type.
        // This pattern should only be used for generic types, for classes use the createInstance factory method.
        TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() { };
        // END: com.azure.core.util.serializer.constructor
    }

    public void useFactoryForClass() {
        // BEGIN: com.azure.core.util.serializer.createInstance#class
        // Construct a TypeReference<T> for a Java class.
        // This pattern should only be used for non-generic classes when possible, use the constructor for generic
        // class when possible.
        TypeReference<Integer> typeReference = TypeReference.createInstance(int.class);
        // END: com.azure.core.util.serializer.createInstance#class
    }
}
