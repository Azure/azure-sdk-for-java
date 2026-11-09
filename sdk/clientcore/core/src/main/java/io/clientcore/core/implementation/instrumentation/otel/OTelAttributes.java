// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.implementation.instrumentation.NoopAttributes;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

import java.util.Map;
import java.util.Objects;

import static io.clientcore.core.implementation.instrumentation.otel.OTelAttributeKey.castAttributeValue;
import static io.clientcore.core.implementation.instrumentation.otel.OTelAttributeKey.getKey;

/**
 * A class that wraps the OpenTelemetry attributes builder.
 */
@SuppressWarnings("unchecked")
public final class OTelAttributes implements InstrumentationAttributes {
    private static final ClientLogger LOGGER = new ClientLogger(OTelAttributes.class);
    private static final OTelAttributes EMPTY_INSTANCE = new OTelAttributes(
        OTelInitializer.isInitialized() ? FallbackInvoker.call(LOGGER, null, () -> Attributes.empty()) : null);

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
        if (!OTelInitializer.isInitialized()) {
            return NoopAttributes.INSTANCE;
        }

        AttributesBuilder attributesBuilder = FallbackInvoker.call(LOGGER, null, () -> Attributes.builder());
        if (attributesBuilder == null) {
            return NoopAttributes.INSTANCE;
        }

        for (Map.Entry<String, Object> kvp : attributes.entrySet()) {
            String key = kvp.getKey();
            Object value = kvp.getValue();
            Objects.requireNonNull(key, "attribute key cannot be null.");
            Objects.requireNonNull(value, "attribute value cannot be null.");
            Object otelKey = getKey(key, value);
            FallbackInvoker.run(LOGGER,
                () -> attributesBuilder.put((AttributeKey<Object>) otelKey, castAttributeValue(value)));
        }

        return new OTelAttributes(FallbackInvoker.call(LOGGER, null, () -> attributesBuilder.build()));
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
            AttributesBuilder attributesBuilder
                = FallbackInvoker.call(LOGGER, null, () -> ((Attributes) otelAttributes).toBuilder());
            if (attributesBuilder == null) {
                return NoopAttributes.INSTANCE;
            }

            Object otelKey = getKey(key, value);
            if (otelKey != null) {
                FallbackInvoker.run(LOGGER,
                    () -> attributesBuilder.put((AttributeKey<Object>) otelKey, castAttributeValue(value)));
            }

            return new OTelAttributes(FallbackInvoker.call(LOGGER, null, () -> attributesBuilder.build()));
        }
        return NoopAttributes.INSTANCE;
    }

    private boolean isEnabled() {
        return otelAttributes != null && OTelInitializer.isInitialized();
    }
}
