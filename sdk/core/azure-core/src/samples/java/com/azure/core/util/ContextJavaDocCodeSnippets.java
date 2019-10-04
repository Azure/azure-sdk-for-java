// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Code snippets for {@link Context}
 */
public class ContextJavaDocCodeSnippets {

    /**
     * Code snippet for {@link Context#Context(Object, Object)}
     */
    public void constructContextObject() {
        // BEGIN: com.azure.core.util.context#object-object
        final String key1 = "Key1";
        final String value1 = "first-value";

        // Create an empty context having no data
        Context emptyContext = Context.NONE;

        // Create a context using the provided key value pair
        Context keyValueContext = new Context(key1, value1);
        // END: com.azure.core.util.context#object-object
    }

    /**
     * Code snippet for creating Context object using key-value pair map
     */
    public void contextOfObject() {
        // BEGIN: com.azure.core.util.context.of#map
        final String key1 = "Key1";
        final String value1 = "first-value";
        Map<Object, Object> keyValueMap = new HashMap<>();
        keyValueMap.put(key1, value1);

        // Create a context using the provided key value pair map
        Context keyValueContext = Context.of(keyValueMap);
        System.out.printf("Key1 value %s%n", keyValueContext.getData(key1).get().toString());
        // END: com.azure.core.util.context.of#map
    }

    /**
     * Code snippet for {@link Context#addData(Object, Object)}
     */
    public void addDataToContext() {
        // BEGIN: com.azure.core.util.context.addData#object-object
        final String key1 = "Key1";
        final String value1 = "first-value";
        final String key2 = "Key2";
        final String value2 = "second-value";

        Context originalContext = new Context(key1, value1);

        // Add a new key value pair to the existing context object.
        Context updatedContext = originalContext.addData(key2, value2);

        // Both key values found on the same updated context object
        System.out.printf("Key1 value: %s%n", updatedContext.getData(key1).get().toString());
        System.out.printf("Key2 value: %s%n", updatedContext.getData(key2).get().toString());
        // END: com.azure.core.util.context.addData#object-object
    }

    /**
     * Code snippet for {@link Context#getData(Object)}
     */
    public void getDataContext() {
        // BEGIN: com.azure.core.util.context.getData#object
        final String key1 = "Key1";
        final String value1 = "first-value";

        // Create a context object with given key and value
        Context context = new Context(key1, value1);

        // Look for the specified key in the returned context object
        Optional<Object> optionalObject = context.getData(key1);
        System.out.printf("Key1 value : %s%n", optionalObject.get().toString());
        // END: com.azure.core.util.context.getData#object
    }
}
