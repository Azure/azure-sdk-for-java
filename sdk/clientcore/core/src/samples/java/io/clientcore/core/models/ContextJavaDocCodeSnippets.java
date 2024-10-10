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

        // OpenTelemetry context can be optionally passed using PARENT_TRACE_CONTEXT_KEY
        // when OpenTelemetry context is not provided explicitly, ambient
        // io.opentelemetry.context.Context.current() is used

        // Context contextWithSpan = new Context(PARENT_TRACE_CONTEXT_KEY, openTelemetryContext);
        // END: io.clientcore.core.util.context#object-object
    }

    /**
     * Code snippet for {@link Context#put(Object, Object)}
     */
    public void putToContext() {
        // BEGIN: io.clientcore.core.util.context.addData#object-object
        // Users can pass parent trace context information and additional metadata to attach to spans created by SDKs
        // using the io.clientcore.core.util.Context object.
        final String hostNameValue = "host-name-value";
        final String entityPathValue = "entity-path-value";

        // TraceContext represents a tracing solution context type - io.opentelemetry.context.Context for OpenTelemetry.
        final TraceContext parentContext = TraceContext.root();
        Context parentSpanContext = Context.of("PARENT_TRACE_CONTEXT_KEY", parentContext);

        // Add a new key value pair to the existing context object.
        Context updatedContext = parentSpanContext.put("HOST_NAME_KEY", hostNameValue)
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
