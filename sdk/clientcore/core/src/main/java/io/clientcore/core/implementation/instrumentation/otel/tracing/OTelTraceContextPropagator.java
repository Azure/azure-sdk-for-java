// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelContext;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.tracing.TraceContextGetter;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.TraceContextSetter;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.TEXT_MAP_GETTER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.TEXT_MAP_PROPAGATOR_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.TEXT_MAP_SETTER_CLASS;

/**
 * OpenTelemetry implementation of {@link TraceContextPropagator}.
 */
public class OTelTraceContextPropagator implements TraceContextPropagator {
    public static final TraceContextPropagator NOOP = new OTelTraceContextPropagator(null);

    private static final ClientLogger LOGGER = new ClientLogger(OTelTraceContextPropagator.class);
    private static final FallbackInvoker INJECT_INVOKER;
    private static final FallbackInvoker EXTRACT_INVOKER;

    static {
        ReflectiveInvoker injectInvoker = null;
        ReflectiveInvoker extractInvoker = null;
        if (OTelInitializer.isInitialized()) {
            try {
                injectInvoker = getMethodInvoker(TEXT_MAP_PROPAGATOR_CLASS,
                    TEXT_MAP_PROPAGATOR_CLASS.getMethod("inject", CONTEXT_CLASS, Object.class, TEXT_MAP_SETTER_CLASS));

                extractInvoker = getMethodInvoker(TEXT_MAP_PROPAGATOR_CLASS,
                    TEXT_MAP_PROPAGATOR_CLASS.getMethod("extract", CONTEXT_CLASS, Object.class, TEXT_MAP_GETTER_CLASS));
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        INJECT_INVOKER = new FallbackInvoker(injectInvoker, LOGGER);
        EXTRACT_INVOKER = new FallbackInvoker(extractInvoker, LOGGER);
    }

    private final Object otelPropagator;

    /**
     * Creates a new instance of {@link OTelTraceContextPropagator}.
     *
     * @param otelPropagator the OpenTelemetry propagator
     */
    public OTelTraceContextPropagator(Object otelPropagator) {
        this.otelPropagator = otelPropagator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <C> void inject(InstrumentationContext context, C carrier, TraceContextSetter<C> setter) {
        if (isInitialized()) {
            INJECT_INVOKER.invoke(otelPropagator, OTelContext.fromInstrumentationContext(context), carrier,
                Setter.toOTelSetter(setter));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <C> InstrumentationContext extract(InstrumentationContext context, C carrier, TraceContextGetter<C> getter) {
        if (isInitialized()) {
            Object updatedContext = EXTRACT_INVOKER.invoke(otelPropagator,
                OTelContext.fromInstrumentationContext(context), carrier, Getter.toOTelGetter(getter));
            if (updatedContext != null) {
                return OTelSpanContext.fromOTelContext(updatedContext);
            }
        }
        return context;
    }

    private boolean isInitialized() {
        return otelPropagator != null && OTelInitializer.isInitialized();
    }

    private static final class Setter<C> implements InvocationHandler {
        private static final Class<?>[] INTERFACES = new Class<?>[] { TEXT_MAP_SETTER_CLASS };
        private static final Map<TraceContextSetter<?>, Object> PROXIES
            = new java.util.concurrent.ConcurrentHashMap<>();
        private final TraceContextSetter<C> setter;

        static Object toOTelSetter(TraceContextSetter<?> setter) {
            return PROXIES.computeIfAbsent(setter,
                s -> Proxy.newProxyInstance(TEXT_MAP_SETTER_CLASS.getClassLoader(), INTERFACES, new Setter<>(s)));
        }

        private Setter(TraceContextSetter<C> setter) {
            this.setter = setter;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object invoke(Object proxy, Method method, Object[] args) {
            if ("set".equals(method.getName())) {
                assert args.length == 3;
                setter.set((C) args[0], (String) args[1], (String) args[2]);
            }

            return null;
        }
    }

    private static final class Getter<C> implements InvocationHandler {
        private static final Class<?>[] INTERFACES = new Class<?>[] { TEXT_MAP_GETTER_CLASS };
        private static final Map<TraceContextGetter<?>, Object> PROXIES
            = new java.util.concurrent.ConcurrentHashMap<>();
        private final TraceContextGetter<C> getter;

        static Object toOTelGetter(TraceContextGetter<?> getter) {
            return PROXIES.computeIfAbsent(getter,
                g -> Proxy.newProxyInstance(TEXT_MAP_GETTER_CLASS.getClassLoader(), INTERFACES, new Getter<>(g)));
        }

        private Getter(TraceContextGetter<C> getter) {
            this.getter = getter;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object invoke(Object proxy, Method method, Object[] args) {
            if ("get".equals(method.getName())) {
                assert args.length == 2;
                return getter.get((C) args[0], (String) args[1]);
            }

            if ("keys".equals(method.getName())) {
                assert args.length == 1;
                return getter.keys((C) args[0]);
            }

            return null;
        }
    }
}
