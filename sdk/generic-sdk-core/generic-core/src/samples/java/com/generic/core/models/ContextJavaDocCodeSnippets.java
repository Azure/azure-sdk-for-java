// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.models;

import java.util.Optional;

/**
 * Code snippets for {@link Context}
 */
public class ContextJavaDocCodeSnippets {

    /**
     * Code snippet for {@link Context#Context(Object, Object)}
     */
    public void constructContextObject() {
        // BEGIN: com.generic.core.util.context#object-object
        // Create an empty context having no data
        Context emptyContext = Context.NONE;

        // OpenTelemetry context can be optionally passed using PARENT_TRACE_CONTEXT_KEY
        // when OpenTelemetry context is not provided explicitly, ambient
        // io.opentelemetry.context.Context.current() is used

        // Context contextWithSpan = new Context(PARENT_TRACE_CONTEXT_KEY, openTelemetryContext);
        // END: com.generic.core.util.context#object-object
    }

    /**
     * Code snippet for {@link Context#addData(Object, Object)}
     */
    public void addDataToContext() {
        // BEGIN: com.generic.core.util.context.addData#object-object
        // Users can pass parent trace context information and additional metadata to attach to spans created by SDKs
        // using the com.generic.core.util.Context object.
        final String hostNameValue = "host-name-value";
        final String entityPathValue = "entity-path-value";

        // TraceContext represents a tracing solution context type - io.opentelemetry.context.Context for OpenTelemetry.
        final TraceContext parentContext = TraceContext.root();
        Context parentSpanContext = new Context("PARENT_TRACE_CONTEXT_KEY", parentContext);

        // Add a new key value pair to the existing context object.
        Context updatedContext = parentSpanContext.addData("HOST_NAME_KEY", hostNameValue)
            .addData("ENTITY_PATH_KEY", entityPathValue);

        // Both key values found on the same updated context object
        System.out.printf("Hostname value: %s%n", updatedContext.getData("HOST_NAME_KEY").get());
        System.out.printf("Entity Path value: %s%n", updatedContext.getData("ENTITY_PATH_KEY").get());
        // END: com.generic.core.util.context.addData#object-object
    }

    /**
     * Code snippet for {@link Context#getData(Object)}
     */
    public void getDataContext() {
        // BEGIN: com.generic.core.util.context.getData#object
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
        // END: com.generic.core.util.context.getData#object
    }

    static class TraceContext {
        public static TraceContext root() {
            return new TraceContext();
        }
    }
}
