// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;

/**
 * Code snippets for {@link Context}
 */
public class ContextJavaDocCodeSnippets {

    /**
     * Code snippet for {@link Context#Context(Object, Object)}
     */
    public void constructContextObject() {
        // BEGIN: com.azure.core.util.context#object-object
        // Create an empty context having no data
        Context emptyContext = Context.NONE;

        // Tracing spans created by users can be passed to calling methods in sdk clients using Context object
        final String userParentSpan = "user-parent-span";

        // Create a context using the provided key and user parent span
        Context keyValueContext = new Context(PARENT_SPAN_KEY, userParentSpan);
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
        System.out.printf("Key1 value %s%n", keyValueContext.getData(key1).get());
        // END: com.azure.core.util.context.of#map
    }

    /**
     * Code snippet for {@link Context#addData(Object, Object)}
     */
    public void addDataToContext() {
        // BEGIN: com.azure.core.util.context.addData#object-object
        // Users can send parent span information and pass additional metadata to attach to spans of the calling methods
        // using the Context object
        final String hostNameValue = "host-name-value";
        final String entityPathValue = "entity-path-value";
        final String userParentSpan = "user-parent-span";
        Context parentSpanContext = new Context(PARENT_SPAN_KEY, userParentSpan);

        // Add a new key value pair to the existing context object.
        Context updatedContext = parentSpanContext.addData(HOST_NAME_KEY, hostNameValue)
            .addData(ENTITY_PATH_KEY, entityPathValue);

        // Both key values found on the same updated context object
        System.out.printf("Hostname value: %s%n", updatedContext.getData(HOST_NAME_KEY).get());
        System.out.printf("Entity Path value: %s%n", updatedContext.getData(ENTITY_PATH_KEY).get());
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
        if (optionalObject.isPresent()) {
            System.out.printf("Key1 value: %s%n", optionalObject.get());
        } else {
            System.out.println("Key1 does not exist or have data.");
        }
        // END: com.azure.core.util.context.getData#object
    }

    /**
     * Code snippet for {@link Context#getValues()}
     */
    public void getValues() {
        // BEGIN: com.azure.core.util.Context.getValues
        final String key1 = "Key1";
        final String value1 = "first-value";
        final String key2 = "Key2";
        final String value2 = "second-value";

        Context context = new Context(key1, value1)
            .addData(key2, value2);

        Map<Object, Object> contextValues = context.getValues();
        if (contextValues.containsKey(key1)) {
            System.out.printf("Key1 value: %s%n", contextValues.get(key1));
        } else {
            System.out.println("Key1 does not exist.");
        }

        if (contextValues.containsKey(key2)) {
            System.out.printf("Key2 value: %s%n", contextValues.get(key2));
        } else {
            System.out.println("Key2 does not exist.");
        }
        // END: com.azure.core.util.Context.getValues
    }
}
