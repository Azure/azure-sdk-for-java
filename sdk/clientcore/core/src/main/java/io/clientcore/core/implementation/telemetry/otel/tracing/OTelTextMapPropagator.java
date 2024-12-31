// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.telemetry.FallbackInvoker;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.tracing.TextMapGetter;
import io.clientcore.core.telemetry.tracing.TextMapPropagator;
import io.clientcore.core.telemetry.tracing.TextMapSetter;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.function.Consumer;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TEXT_MAP_GETTER_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TEXT_MAP_PROPAGATOR_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TEXT_MAP_SETTER_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.tracing.OTelUtils.getOTelContext;
import static io.clientcore.core.telemetry.TelemetryProvider.TRACE_CONTEXT_KEY;

public class OTelTextMapPropagator implements TextMapPropagator {
    public static final TextMapPropagator NOOP = new OTelTextMapPropagator(null);

    private static final ClientLogger LOGGER = new ClientLogger(OTelTextMapPropagator.class);
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

        Consumer<Throwable> onError = t -> OTelInitializer.runtimeError(LOGGER, t);
        INJECT_INVOKER = new FallbackInvoker(injectInvoker, null, onError);
        EXTRACT_INVOKER = new FallbackInvoker(extractInvoker, null, onError);
    }

    private final Object otelPropagator;

    public OTelTextMapPropagator(Object otelPropagator) {
        this.otelPropagator = otelPropagator;
    }

    @Override
    public <C> void inject(Context context, C carrier, TextMapSetter<C> setter) {
        if (isInitialized()) {
            INJECT_INVOKER.invoke(otelPropagator, getOTelContext(context), carrier, Setter.toOTelSetter(setter));
        }
    }

    @Override
    public <C> Context extract(Context context, C carrier, TextMapGetter<C> getter) {
        if (isInitialized()) {
            Object updatedContext
                = EXTRACT_INVOKER.invoke(otelPropagator, getOTelContext(context), carrier, Getter.toOTelGetter(getter));
            if (updatedContext != null) {
                return context.put(TRACE_CONTEXT_KEY, updatedContext);
            }
        }
        return context;
    }

    private boolean isInitialized() {
        return OTelInitializer.isInitialized() && otelPropagator != null;
    }

    private static final class Setter<C> implements InvocationHandler {
        private static final Class<?>[] INTERFACES = new Class<?>[] { TEXT_MAP_SETTER_CLASS };
        private static final Map<TextMapSetter<?>, Object> PROXIES = new java.util.concurrent.ConcurrentHashMap<>();
        private final TextMapSetter<C> setter;

        static Object toOTelSetter(TextMapSetter<?> setter) {
            return PROXIES.computeIfAbsent(setter,
                s -> Proxy.newProxyInstance(TEXT_MAP_SETTER_CLASS.getClassLoader(), INTERFACES, new Setter<>(s)));
        }

        private Setter(TextMapSetter<C> setter) {
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
        private static final Map<TextMapGetter<?>, Object> PROXIES = new java.util.concurrent.ConcurrentHashMap<>();
        private final TextMapGetter<C> getter;

        static Object toOTelGetter(TextMapGetter<?> getter) {
            return PROXIES.computeIfAbsent(getter,
                g -> Proxy.newProxyInstance(TEXT_MAP_GETTER_CLASS.getClassLoader(), INTERFACES, new Getter<>(g)));
        }

        private Getter(TextMapGetter<C> getter) {
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
