// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Map;
import java.util.Objects;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelAttributeKey.castAttributeValue;
import static io.clientcore.core.implementation.instrumentation.otel.OTelAttributeKey.getKey;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.ATTRIBUTES_BUILDER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.ATTRIBUTES_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;

/**
 * A class that wraps the OpenTelemetry attributes builder.
 */
public class OTelAttributes implements InstrumentationAttributes {
    private static final ClientLogger LOGGER = new ClientLogger(OTelAttributes.class);
    private static final FallbackInvoker ATTRIBUTES_BUILDER_INVOKER;
    private static final FallbackInvoker PUT_INVOKER;
    private static final FallbackInvoker BUILD_INVOKER;

    static {
        ReflectiveInvoker attributesBuilderInvoker = null;
        ReflectiveInvoker putInvoker = null;
        ReflectiveInvoker buildInvoker = null;

        try {
            attributesBuilderInvoker = getMethodInvoker(ATTRIBUTES_CLASS, ATTRIBUTES_CLASS.getMethod("builder"));
            putInvoker = getMethodInvoker(ATTRIBUTES_BUILDER_CLASS,
                ATTRIBUTES_BUILDER_CLASS.getMethod("put", ATTRIBUTE_KEY_CLASS, Object.class));
            buildInvoker = getMethodInvoker(ATTRIBUTES_BUILDER_CLASS, ATTRIBUTES_BUILDER_CLASS.getMethod("build"));

        } catch (Throwable t) {
            OTelInitializer.initError(LOGGER, t);
        }

        ATTRIBUTES_BUILDER_INVOKER = new FallbackInvoker(attributesBuilderInvoker, LOGGER);
        PUT_INVOKER = new FallbackInvoker(putInvoker, LOGGER);
        BUILD_INVOKER = new FallbackInvoker(buildInvoker, LOGGER);
    }

    private final Object attributesBuilder;

    /**
     * Creates a new instance of OTelAttributes.
     *
     * @param attributes The attributes to initialize the builder with.
     */
    public OTelAttributes(Map<String, Object> attributes) {
        this.attributesBuilder = ATTRIBUTES_BUILDER_INVOKER.invoke();
        if (attributesBuilder != null && attributes != null) {
            for (Map.Entry<String, Object> kvp : attributes.entrySet()) {
                Objects.requireNonNull(kvp.getKey(), "attribute key cannot be null.");
                Objects.requireNonNull(kvp.getValue(), "attribute value cannot be null.");
                Object otelKey = getKey(kvp.getKey(), kvp.getValue());
                PUT_INVOKER.invoke(attributesBuilder, otelKey, castAttributeValue(kvp.getValue()));
            }
        }
    }

    /**
     * Builds new instance of OpenTelemetry attributes.
     *
     * @return The OpenTelemetry attributes.
     */
    public Object buildOTelAttributes() {
        return isEnable() ? BUILD_INVOKER.invoke(attributesBuilder) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InstrumentationAttributes put(String key, Object value) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        Objects.requireNonNull(value, "'value' cannot be null.");
        if (isEnable()) {
            Object otelKey = getKey(key, value);
            if (otelKey != null) {
                // TODO (limolkova) update
                PUT_INVOKER.invoke(attributesBuilder, otelKey, castAttributeValue(value));
            }
        }
        return this;
    }

    private boolean isEnable() {
        return attributesBuilder != null && OTelInitializer.isInitialized();
    }
}
