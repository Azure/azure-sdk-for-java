package io.clientcore.core.implementation.observability.otel;

/*
import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.observability.Attributes;
import io.clientcore.core.observability.AttributesBuilder;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.ATTRIBUTES_BUILDER_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;

/*
public class OTelAttributesBuilder implements AttributesBuilder {
    static final OTelAttributesBuilder NOOP = new OTelAttributesBuilder(null);

    private static final ClientLogger LOGGER = new ClientLogger(AttributesBuilder.class);
    private final static ReflectiveInvoker BUILD_INVOKER;
    private final static ReflectiveInvoker PUT_OBJECT_INVOKER;

    private final Object otelAttributesBuilder;
    static {
        ReflectiveInvoker buildInvoker = null;
        ReflectiveInvoker putObjectInvoker = null;

        try {
            buildInvoker = ReflectionUtils.getMethodInvoker(ATTRIBUTES_BUILDER_CLASS,
                ATTRIBUTES_BUILDER_CLASS.getMethod("build"));

            putObjectInvoker = ReflectionUtils.getMethodInvoker(ATTRIBUTES_BUILDER_CLASS,
                ATTRIBUTES_BUILDER_CLASS.getMethod("put", ATTRIBUTE_KEY_CLASS, Object.class));
        } catch (Throwable t) {
            OTelInitializer.INSTANCE.initError(LOGGER, t);
        }

        BUILD_INVOKER = buildInvoker;
        PUT_OBJECT_INVOKER = putObjectInvoker;
    }

    public OTelAttributesBuilder(Object otelAttributesBuilder) {
        this.otelAttributesBuilder = otelAttributesBuilder;
    }

    @Override
    public Attributes build() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelAttributesBuilder != null) {
            try {
                return new OTelAttributes(BUILD_INVOKER.invokeWithArguments(otelAttributesBuilder));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }
        return OTelAttributes.empty();
    }

    @Override
    public AttributesBuilder put(String key, Object value) {
        if (OTelInitializer.INSTANCE.isInitialized() && otelAttributesBuilder != null) {
            try {
                Object otelKey = OTelAttributeKey.getKey(key, value);
                if (otelKey != null) {
                    PUT_OBJECT_INVOKER.invokeWithArguments(otelAttributesBuilder, otelKey, value);
                }
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return this;
    }
}
*/
