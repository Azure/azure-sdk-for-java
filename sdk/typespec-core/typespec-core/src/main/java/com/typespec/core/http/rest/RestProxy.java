// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.rest;

import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.implementation.http.rest.AsyncRestProxy;
import com.typespec.core.implementation.http.rest.RestProxyUtils;
import com.typespec.core.implementation.http.rest.SwaggerInterfaceParser;
import com.typespec.core.implementation.http.rest.SwaggerMethodParser;
import com.typespec.core.implementation.http.rest.SyncRestProxy;
import com.typespec.core.util.Configuration;
import com.typespec.core.util.Context;
import com.typespec.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * Type to create a proxy implementation for an interface describing REST API methods.
 * <p>
 * RestProxy can create proxy implementations for interfaces with methods that return deserialized Java objects as well
 * as asynchronous Single objects that resolve to a deserialized Java object.
 */
public final class RestProxy implements InvocationHandler {
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLED = "com.azure.core.http.restproxy.syncproxy.enable";
    private static final boolean GLOBAL_SYNC_PROXY_ENABLED = Configuration.getGlobalConfiguration()
        .get("AZURE_HTTP_REST_PROXY_SYNC_PROXY_ENABLED", true);

    private final SwaggerInterfaceParser interfaceParser;
    private final AsyncRestProxy asyncRestProxy;
    private final HttpPipeline httpPipeline;
    private final SyncRestProxy syncRestProxy;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     * this RestProxy "implements".
     */
    private RestProxy(HttpPipeline httpPipeline, SerializerAdapter serializer, SwaggerInterfaceParser interfaceParser) {
        this.interfaceParser = interfaceParser;
        this.asyncRestProxy = new AsyncRestProxy(httpPipeline, serializer, interfaceParser);
        this.syncRestProxy = new SyncRestProxy(httpPipeline, serializer, interfaceParser);
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
        final SwaggerMethodParser methodParser = getMethodParser(method);
        RequestOptions options = methodParser.setRequestOptions(args);
        Context context = methodParser.setContext(args);

        if (methodParser.isReactive() || isSyncDisabled(context)) {
            return asyncRestProxy.invoke(proxy, method, options, options != null ? options.getErrorOptions() : null,
                options != null ? options.getRequestCallback() : null, methodParser, methodParser.isReactive(), args);
        } else {
            return syncRestProxy.invoke(proxy, method, options, options != null ? options.getErrorOptions() : null,
                options != null ? options.getRequestCallback() : null, methodParser, false, args);
        }
    }

    private static boolean isSyncDisabled(Context context) {
        return !(boolean) context
            .getData(HTTP_REST_PROXY_SYNC_PROXY_ENABLED)
            .orElse(GLOBAL_SYNC_PROXY_ENABLED);
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
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
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
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
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipline that will be used to send Http requests
     * @param serializer the serializer that will be used to convert POJOs to and from request and response bodies
     * @param <A> the type of the Swagger interface.
     * @return a proxy implementation of the provided Swagger interface
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, SerializerAdapter serializer) {
        final SwaggerInterfaceParser interfaceParser = SwaggerInterfaceParser.getInstance(swaggerInterface);
        final RestProxy restProxy = new RestProxy(httpPipeline, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class<?>[]{swaggerInterface},
            restProxy);
    }
}
