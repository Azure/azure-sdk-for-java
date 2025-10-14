// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.NoopAttributes;
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
public final class OTelAttributes implements InstrumentationAttributes {
    private static final ClientLogger LOGGER = new ClientLogger(OTelAttributes.class);
    private static final FallbackInvoker ATTRIBUTES_BUILDER_INVOKER;
    private static final FallbackInvoker PUT_INVOKER;
    private static final FallbackInvoker BUILD_INVOKER;
    private static final FallbackInvoker TO_BUILD_INVOKER;
    private static final OTelAttributes EMPTY_INSTANCE;

    static {
        ReflectiveInvoker attributesBuilderInvoker = null;
        ReflectiveInvoker putInvoker = null;
        ReflectiveInvoker buildInvoker = null;
        ReflectiveInvoker toBuildInvoker = null;
        Object emptyInstance = null;

        try {
            attributesBuilderInvoker = getMethodInvoker(ATTRIBUTES_CLASS, ATTRIBUTES_CLASS.getMethod("builder"));
            putInvoker = getMethodInvoker(ATTRIBUTES_BUILDER_CLASS,
                ATTRIBUTES_BUILDER_CLASS.getMethod("put", ATTRIBUTE_KEY_CLASS, Object.class));
            buildInvoker = getMethodInvoker(ATTRIBUTES_BUILDER_CLASS, ATTRIBUTES_BUILDER_CLASS.getMethod("build"));

            toBuildInvoker = getMethodInvoker(ATTRIBUTES_CLASS, ATTRIBUTES_CLASS.getMethod("toBuilder"));
            ReflectiveInvoker emptyInvoker = getMethodInvoker(ATTRIBUTES_CLASS, ATTRIBUTES_CLASS.getMethod("empty"));
            emptyInstance = emptyInvoker.invoke();
        } catch (Throwable t) {
            OTelInitializer.initError(LOGGER, t);
        }

        ATTRIBUTES_BUILDER_INVOKER = new FallbackInvoker(attributesBuilderInvoker, LOGGER);
        PUT_INVOKER = new FallbackInvoker(putInvoker, LOGGER);
        BUILD_INVOKER = new FallbackInvoker(buildInvoker, LOGGER);
        TO_BUILD_INVOKER = new FallbackInvoker(toBuildInvoker, LOGGER);
        EMPTY_INSTANCE = new OTelAttributes(emptyInstance);
    }

    private final Object otelAttributes;

    /**
     * Creates a new instance of OpenTelemetry attributes or noops if OpenTelemetry is not initialized.
     *
     * @param attributes The attributes to initialize the builder with.
     * @return The OpenTelemetry attributes.
     */
    public static InstrumentationAttributes create(Map<String, Object> attributes) {
        if (attributes == null) {
            return EMPTY_INSTANCE;
        }

        Object attributesBuilder = ATTRIBUTES_BUILDER_INVOKER.invoke();
        if (attributesBuilder == null) {
            return NoopAttributes.INSTANCE;
        }

        for (Map.Entry<String, Object> kvp : attributes.entrySet()) {
            String key = kvp.getKey();
            Object value = kvp.getValue();
            Objects.requireNonNull(key, "attribute key cannot be null.");
            Objects.requireNonNull(value, "attribute value cannot be null.");
            Object otelKey = getKey(key, value);
            PUT_INVOKER.invoke(attributesBuilder, otelKey, castAttributeValue(value));
        }

        return new OTelAttributes(BUILD_INVOKER.invoke(attributesBuilder));
    }

    private OTelAttributes(Object otelAttributes) {
        this.otelAttributes = otelAttributes;
    }

    /**
     * Builds new instance of OpenTelemetry attributes.
     *
     * @return The OpenTelemetry attributes.
     */
    public Object getOTelAttributes() {
        return otelAttributes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InstrumentationAttributes put(String key, Object value) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        Objects.requireNonNull(value, "'value' cannot be null.");
        if (isEnabled()) {
            Object attributesBuilder = TO_BUILD_INVOKER.invoke(otelAttributes);
            if (attributesBuilder == null) {
                return NoopAttributes.INSTANCE;
            }

            Object otelKey = getKey(key, value);
            if (otelKey != null) {
                PUT_INVOKER.invoke(attributesBuilder, otelKey, castAttributeValue(value));
            }

            return new OTelAttributes(BUILD_INVOKER.invoke(attributesBuilder));
        }
        return NoopAttributes.INSTANCE;
    }

    private boolean isEnabled() {
        return otelAttributes != null && OTelInitializer.isInitialized();
    }
}
