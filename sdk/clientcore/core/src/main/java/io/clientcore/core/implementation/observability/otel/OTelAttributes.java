package io.clientcore.core.implementation.observability.otel;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.observability.Attributes;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.ATTRIBUTES_CLASS;

public class OTelAttributes implements Attributes {
    private static final ClientLogger LOGGER = new ClientLogger(OTelAttributes.class);

    private static final OTelAttributes EMPTY;
    private static final ReflectiveInvoker SIZE_INVOKER;
    private static final ReflectiveInvoker IS_EMPTY_INVOKER;
    private static final ReflectiveInvoker BUILDER_INVOKER;
    private final Object otelAttributes;

    static {
        ReflectiveInvoker sizeInvoker = null;
        ReflectiveInvoker isEmptyInvoker = null;
        ReflectiveInvoker builderInvoker = null;
        Object emptyInstance = null;
        try {
            sizeInvoker = ReflectionUtils.getMethodInvoker(ATTRIBUTES_CLASS,
                ATTRIBUTES_CLASS.getMethod("size"));

            isEmptyInvoker = ReflectionUtils.getMethodInvoker(ATTRIBUTES_CLASS,
                ATTRIBUTES_CLASS.getMethod("isEmpty"));

            builderInvoker = ReflectionUtils.getMethodInvoker(ATTRIBUTES_CLASS,
                ATTRIBUTES_CLASS.getMethod("builder"));

            ReflectiveInvoker emptyInvoker = ReflectionUtils.getMethodInvoker(ATTRIBUTES_CLASS,
                ATTRIBUTES_CLASS.getMethod("empty"));

            emptyInstance = emptyInvoker.invokeStatic();
        } catch (Throwable t) {
            OTelInitializer.INSTANCE.initError(LOGGER, t);
        }

        SIZE_INVOKER = sizeInvoker;
        IS_EMPTY_INVOKER = isEmptyInvoker;
        BUILDER_INVOKER = builderInvoker;
        EMPTY = new OTelAttributes(emptyInstance);
    }

    OTelAttributes(Object otelAttributes) {
        this.otelAttributes = otelAttributes;
    }

    @Override
    public int size() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelAttributes != null) {
            try {
                return (int) SIZE_INVOKER.invokeWithArguments(otelAttributes);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return 0;
    }

    @Override
    public boolean isEmpty() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelAttributes != null) {
            try {
                return (boolean) IS_EMPTY_INVOKER.invokeWithArguments(otelAttributes);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return true;
    }

    static Attributes empty() {
        return EMPTY;
    }

    /*
    public static AttributesBuilder builder() {
        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                return new OTelAttributesBuilder(BUILDER_INVOKER.invokeStatic());
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return OTelAttributesBuilder.NOOP;
    }*/
}
