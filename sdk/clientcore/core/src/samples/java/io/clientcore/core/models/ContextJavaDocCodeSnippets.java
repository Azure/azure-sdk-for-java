// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.models;

import io.clientcore.core.util.Context;

/**
 * Code snippets for {@link Context}
 */
public class ContextJavaDocCodeSnippets {

    /**
     * Code snippet for {@link Context#of(Object, Object)}
     */
    public void constructContextObject() {
        // BEGIN: io.clientcore.core.util.context#object-object
        // Create an empty context having no data
        Context emptyContext = Context.none();

        // Create a context with one key value pair
        Context contextWithOnePair = Context.of("key", "value");
        // END: io.clientcore.core.util.context#object-object
    }

    /**
     * Code snippet for {@link Context#put(Object, Object)}
     */
    public void putToContext() {
        // BEGIN: io.clientcore.core.util.context.addData#object-object
        // using the io.clientcore.core.util.Context object.

        Context originalContext = Context.none();

        final String hostNameValue = "host-name-value";
        final String entityPathValue = "entity-path-value";

        // Add a new key value pair to the existing context object.
        Context updatedContext = originalContext.put("HOST_NAME_KEY", hostNameValue)
            .put("ENTITY_PATH_KEY", entityPathValue);

        // Both key values found on the same updated context object
        System.out.printf("Hostname value: %s%n", updatedContext.get("HOST_NAME_KEY"));
        System.out.printf("Entity Path value: %s%n", updatedContext.get("ENTITY_PATH_KEY"));
        // END: io.clientcore.core.util.context.addData#object-object
    }

    /**
     * Code snippet for {@link Context#get(Object)}
     */
    public void getDataContext() {
        // BEGIN: io.clientcore.core.util.context.getData#object
        final String key1 = "Key1";
        final String value1 = "first-value";

        // Create a context object with given key and value
        Context context = Context.of(key1, value1);

        // Look for the specified key in the returned context object
        Object optionalObject = context.get(key1);
        if (optionalObject != null) {
            System.out.printf("Key1 value: %s%n", optionalObject);
        } else {
            System.out.println("Key1 does not exist or have data.");
        }
        // END: io.clientcore.core.util.context.getData#object
    }

    static class TraceContext {
        public static TraceContext root() {
            return new TraceContext();
        }
    }
}
