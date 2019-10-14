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
        // Create an empty context having no data
        Context emptyContext = Context.NONE;

        // Tracing spans created by users can be passed to calling methods in sdk clients using Context object
        final String userParentSpan = "user-parent-span";
        final String openCensusSpanKey = "opencensus-span";

        // Create a context using the provided key and user parent span
        Context keyValueContext = new Context(openCensusSpanKey, userParentSpan);
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

        final String openCensusSpanKey = "opencensus-span";
        final String hostName = "hostName-key";
        final String entityPath = "entity-path-key";
        Context parentSpanContext = new Context(openCensusSpanKey, userParentSpan);

        // Add a new key value pair to the existing context object.
        Context updatedContext = parentSpanContext.addData(hostName, hostNameValue)
            .addData(entityPath, entityPathValue);

        // Both key values found on the same updated context object
        System.out.printf("HOSTNAME value: %s%n", updatedContext.getData(hostName).get());
        System.out.printf("ENTITY_PATH value: %s%n", updatedContext.getData(entityPath).get());
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
}
