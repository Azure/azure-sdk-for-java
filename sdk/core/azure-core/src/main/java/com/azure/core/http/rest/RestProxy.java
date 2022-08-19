// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.implementation.http.rest.AsyncRestProxy;
import com.azure.core.implementation.http.rest.ErrorOptions;
import com.azure.core.implementation.http.rest.RestProxyBase;
import com.azure.core.implementation.http.rest.RestProxyUtils;
import com.azure.core.implementation.http.rest.SwaggerInterfaceParser;
import com.azure.core.implementation.http.rest.SwaggerMethodParser;
import com.azure.core.implementation.http.rest.SyncRestProxy;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.function.Consumer;


/**
 * Type to create a proxy implementation for an interface describing REST API methods.
 * <p>
 * RestProxy can create proxy implementations for interfaces with methods that return deserialized Java objects as well
 * as asynchronous Single objects that resolve to a deserialized Java object.
 */
public final class RestProxy implements InvocationHandler {
    private static final ClientLogger LOGGER = new ClientLogger(RestProxy.class);

    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    private static final boolean GLOBAL_SYNC_PROXY_ENABLE = Configuration.getGlobalConfiguration()
        .get("AZURE_HTTP_REST_PROXY_SYNC_PROXY_ENABLED", false);

    private static final String HTTP_REST_EXPERIMENTAL_PROXY_ENABLE
        = "com.azure.core.http.restproxy.experimental.enable";
    private static final boolean GLOBAL_EXPERIMENTAL_PROXY_ENABLE = Configuration.getGlobalConfiguration()
        .get("AZURE_HTTP_REST_PROXY_EXPERIMENTAL_ENABLED", false);

    private static final AsyncExperimentalConstructor ASYNC_EXPERIMENTAL_CREATOR;
    private static final SyncExperimentalConstructor SYNC_EXPERIMENTAL_CREATOR;
    private static final ParserExperimentalConstructor PARSER_EXPERIMENTAL_CONSTRUCTOR;
    private static final boolean CAN_USE_EXPERIMENTAL;

    static {
        AsyncExperimentalConstructor asyncExperimentalCreator = null;
        try {
            Class<?> asyncExperimentalClass = Class.forName(
                "com.azure.core.experimental.implementation.rest.AsyncRestProxyExperimental");
            MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(asyncExperimentalClass);
            MethodHandle unreflectedConstructor = lookup.unreflectConstructor(asyncExperimentalClass
                .getDeclaredConstructor(HttpPipeline.class, SerializerAdapter.class, SwaggerInterfaceParser.class));
            asyncExperimentalCreator = (AsyncExperimentalConstructor) LambdaMetafactory.metafactory(
                    MethodHandles.lookup(), "apply", AsyncExperimentalConstructor.METHOD_TYPE,
                    AsyncExperimentalConstructor.SIGNATURE, unreflectedConstructor,
                    AsyncExperimentalConstructor.SIGNATURE)
                .getTarget()
                .invokeWithArguments();
        } catch (Exception | LinkageError e) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Unable to reflective locate or access "
                + "'com.azure.core.experimental.implementation.rest.AsyncRestProxyExperimental'. Enablement of "
                + "'AZURE_HTTP_REST_PROXY_EXPERIMENTAL_ENABLED' or 'com.azure.core.http.restproxy.experimental.enable' "
                + "will be ignored and the base 'AsyncRestProxy' will be used instead.", e);
        } catch (Throwable e) {
            // At this point the only type left should be Error, rethrow it as an Error.
            throw (Error) e;
        }
        ASYNC_EXPERIMENTAL_CREATOR = asyncExperimentalCreator;

        SyncExperimentalConstructor syncExperimentalCreator = null;
        try {
            Class<?> syncExperimentalClass = Class.forName(
                "com.azure.core.experimental.implementation.rest.SyncRestProxyExperimental");
            MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(syncExperimentalClass);
            MethodHandle unreflectedConstructor = lookup.unreflectConstructor(syncExperimentalClass
                .getDeclaredConstructor(HttpPipeline.class, SerializerAdapter.class, SwaggerInterfaceParser.class));
            syncExperimentalCreator = (SyncExperimentalConstructor) LambdaMetafactory.metafactory(
                    MethodHandles.lookup(), "apply", SyncExperimentalConstructor.METHOD_TYPE,
                    SyncExperimentalConstructor.SIGNATURE, unreflectedConstructor,
                    SyncExperimentalConstructor.SIGNATURE)
                .getTarget()
                .invokeWithArguments();
        } catch (Exception | LinkageError e) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Unable to reflective locate or access "
                + "'com.azure.core.experimental.implementation.rest.SyncRestProxyExperimental'. Enablement of "
                + "'AZURE_HTTP_REST_PROXY_EXPERIMENTAL_ENABLED' or 'com.azure.core.http.restproxy.experimental.enable' "
                + "will be ignored and the base 'SyncRestProxy' will be used instead.", e);
        } catch (Throwable e) {
            // At this point the only type left should be Error, rethrow it as an Error.
            throw (Error) e;
        }
        SYNC_EXPERIMENTAL_CREATOR = syncExperimentalCreator;

        ParserExperimentalConstructor parserExperimentalConstructor = null;
        try {
            Class<?> swaggerExperimentalClass = Class.forName(
                "com.azure.core.experimental.implementation.rest.SwaggerInterfaceParserExperimental");
            MethodHandles.Lookup lookup = ReflectionUtils.getLookupToUse(swaggerExperimentalClass);
            MethodHandle unreflectedFactory = lookup.unreflect(swaggerExperimentalClass.getDeclaredMethod(
                "getInstance", Class.class));
            parserExperimentalConstructor = (ParserExperimentalConstructor) LambdaMetafactory.metafactory(
                    MethodHandles.lookup(), "apply", ParserExperimentalConstructor.METHOD_TYPE,
                    ParserExperimentalConstructor.SIGNATURE, unreflectedFactory,
                    ParserExperimentalConstructor.SIGNATURE)
                .getTarget()
                .invokeWithArguments();
        } catch (Exception | LinkageError e) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Unable to reflective locate or access "
                + "'com.azure.core.experimental.implementation.rest.SwaggerInterfaceParser'. Enablement of "
                + "'AZURE_HTTP_REST_PROXY_EXPERIMENTAL_ENABLED' or 'com.azure.core.http.restproxy.experimental.enable' "
                + "will be ignored and the base 'SwaggerInterfaceParser' will be used instead.", e);
        } catch (Throwable e) {
            // At this point the only type left should be Error, rethrow it as an Error.
            throw (Error) e;
        }
        PARSER_EXPERIMENTAL_CONSTRUCTOR = parserExperimentalConstructor;
        CAN_USE_EXPERIMENTAL = ASYNC_EXPERIMENTAL_CREATOR != null && SYNC_EXPERIMENTAL_CREATOR != null
            && PARSER_EXPERIMENTAL_CONSTRUCTOR != null;
    }

    private final Class<?> swaggerInterface;
    private final SwaggerInterfaceParser interfaceParser;
    private final SwaggerInterfaceParser interfaceParserExperimental;
    private final AsyncRestProxy asyncRestProxy;
    private final HttpPipeline httpPipeline;
    private final SyncRestProxy syncRestProxy;
    private final AsyncRestProxy asyncRestProxyExperimental;
    private final SyncRestProxy syncRestProxyExperimental;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer the serializer that will be used to convert response bodies to POJOs.
     * @param swaggerInterface The class that represents a Swagger interfaces that will be turned into a proxy.
     */
    private RestProxy(HttpPipeline httpPipeline, SerializerAdapter serializer, Class<?> swaggerInterface) {
        this.swaggerInterface = swaggerInterface;
        this.interfaceParser = SwaggerInterfaceParser.getInstance(swaggerInterface);
        this.interfaceParserExperimental = CAN_USE_EXPERIMENTAL
            ? PARSER_EXPERIMENTAL_CONSTRUCTOR.apply(swaggerInterface)
            : null;
        this.asyncRestProxy = new AsyncRestProxy(httpPipeline, serializer, interfaceParser);
        this.syncRestProxy = new SyncRestProxy(httpPipeline, serializer, interfaceParser);
        this.asyncRestProxyExperimental = CAN_USE_EXPERIMENTAL
            ? ASYNC_EXPERIMENTAL_CREATOR.apply(httpPipeline, serializer, interfaceParser)
            : null;
        this.syncRestProxyExperimental = CAN_USE_EXPERIMENTAL
            ? SYNC_EXPERIMENTAL_CREATOR.apply(httpPipeline, serializer, interfaceParser)
            : null;
        this.httpPipeline = httpPipeline;
    }

    /**
     * Get the SwaggerMethodParser for the provided method. The Method must exist on the Swagger interface that this
     * RestProxy was created to "implement".
     *
     * @param method the method to get a SwaggerMethodParser for
     * @return the SwaggerMethodParser for the provided method
     */
    private SwaggerMethodParser getMethodParser(Method method) {
        return interfaceParser.getMethodParser(method);
    }

    /**
     * Send the provided request asynchronously, applying any request policies provided to the HttpClient instance.
     *
     * @param request the HTTP request to send
     * @param contextData the context
     * @return a {@link Mono} that emits HttpResponse asynchronously
     */
    public Mono<HttpResponse> send(HttpRequest request, Context contextData) {
        return httpPipeline.send(request, contextData);
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) {
        RestProxyUtils.validateResumeOperationIsNotPresent(method);

        // Note: request options need to be evaluated here, as it is a public class with package private methods.
        // Evaluating here allows the package private methods to be invoked here for downstream use.
        SwaggerMethodParser methodParser = getMethodParser(method);
        RequestOptions options = methodParser.setRequestOptions(args);
        Context context = methodParser.setContext(args);
        boolean isReactive = methodParser.isReactive();
        boolean syncRestProxyEnabled = (boolean) context.getData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE)
            .orElse(GLOBAL_SYNC_PROXY_ENABLE);
        boolean experimentalRestProxyEnabled = CAN_USE_EXPERIMENTAL
            && (boolean) context.getData(HTTP_REST_EXPERIMENTAL_PROXY_ENABLE).orElse(GLOBAL_EXPERIMENTAL_PROXY_ENABLE);

        if (experimentalRestProxyEnabled) {
            methodParser = interfaceParserExperimental.getMethodParser(method);
        }

        EnumSet<ErrorOptions> errorOptions = options != null ? options.getErrorOptions() : null;
        Consumer<HttpRequest> requestCallback = options != null ? options.getRequestCallback() : null;

        RestProxyBase restProxy;
        if (isReactive || !syncRestProxyEnabled) {
            restProxy = experimentalRestProxyEnabled ? asyncRestProxyExperimental : asyncRestProxy;
        } else {
            restProxy = experimentalRestProxyEnabled ? syncRestProxyExperimental : syncRestProxy;
        }

        return restProxy.invoke(proxy, method, options, errorOptions, requestCallback, methodParser, isReactive, args);
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface The class that represents a Swagger interfaces that will be turned into a proxy.
     * @param <A> the type of the Swagger interface
     * @return a proxy implementation of the provided Swagger interface
     */
    public static <A> A create(Class<A> swaggerInterface) {
        return create(swaggerInterface, RestProxyUtils.createDefaultPipeline(),
            RestProxyUtils.createDefaultSerializer());
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface The class that represents a Swagger interfaces that will be turned into a proxy.
     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipeline that will be used to send Http requests
     * @param <A> the type of the Swagger interface
     * @return a proxy implementation of the provided Swagger interface
     */
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline) {
        return create(swaggerInterface, httpPipeline, RestProxyUtils.createDefaultSerializer());
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface The class that represents a Swagger interfaces that will be turned into a proxy.
     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipeline that will be used to send Http requests
     * @param serializer the serializer that will be used to convert POJOs to and from request and response bodies
     * @param <A> the type of the Swagger interface.
     * @return a proxy implementation of the provided Swagger interface
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, SerializerAdapter serializer) {
        final RestProxy restProxy = new RestProxy(httpPipeline, serializer, swaggerInterface);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class<?>[]{swaggerInterface},
            restProxy);
    }

    @FunctionalInterface
    private interface AsyncExperimentalConstructor {
        MethodType SIGNATURE = MethodType.methodType(AsyncRestProxy.class, HttpPipeline.class, SerializerAdapter.class,
            SwaggerInterfaceParser.class);
        MethodType METHOD_TYPE = MethodType.methodType(AsyncExperimentalConstructor.class);

        AsyncRestProxy apply(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter,
            SwaggerInterfaceParser interfaceParser);
    }

    @FunctionalInterface
    private interface SyncExperimentalConstructor {
        MethodType SIGNATURE = MethodType.methodType(SyncRestProxy.class, HttpPipeline.class, SerializerAdapter.class,
            SwaggerInterfaceParser.class);
        MethodType METHOD_TYPE = MethodType.methodType(SyncExperimentalConstructor.class);

        SyncRestProxy apply(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter,
            SwaggerInterfaceParser interfaceParser);
    }

    @FunctionalInterface
    private interface ParserExperimentalConstructor {
        MethodType SIGNATURE = MethodType.methodType(SwaggerInterfaceParser.class, Class.class);
        MethodType METHOD_TYPE = MethodType.methodType(ParserExperimentalConstructor.class);

        SwaggerInterfaceParser apply(Class<?> clazz);
    }
}
