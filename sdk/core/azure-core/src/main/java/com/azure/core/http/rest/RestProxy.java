// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.http.rest.AsyncRestProxy;
import com.azure.core.implementation.http.rest.RestProxyUtils;
import com.azure.core.implementation.http.rest.SwaggerInterfaceParser;
import com.azure.core.implementation.http.rest.SwaggerMethodParser;
import com.azure.core.implementation.http.rest.SyncRestProxy;
import com.azure.core.util.Context;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * Type to create a proxy implementation for an interface describing REST API methods.
 *
 * RestProxy can create proxy implementations for interfaces with methods that return deserialized Java objects as well
 * as asynchronous Single objects that resolve to a deserialized Java object.
 */
public final class RestProxy implements InvocationHandler {
    private final SwaggerInterfaceParser interfaceParser;
    private final AsyncRestProxy asyncRestProxy;
    private final HttpPipeline httpPipeline;
    private final SyncRestProxy syncRestProxy;
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";


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
        boolean isReactive = methodParser.isReactive();
        boolean syncRestProxyEnabled = (boolean) context.getData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE).orElse(false);


        if (isReactive || !syncRestProxyEnabled) {
            return asyncRestProxy.invoke(proxy, method, options, options != null ? options.getErrorOptions() : null,
                options != null ? options.getRequestCallback() : null, methodParser, isReactive, args);
        } else {
            return syncRestProxy.invoke(proxy, method, options, options != null ? options.getErrorOptions() : null,
                options != null ? options.getRequestCallback() : null, methodParser, isReactive, args);
        }
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     * @param <A> the type of the Swagger interface
     * @return a proxy implementation of the provided Swagger interface
     */
    public static <A> A create(Class<A> swaggerInterface) {
        return create(swaggerInterface, RestProxyUtils.createDefaultPipeline(), RestProxyUtils.createDefaultSerializer());
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
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface, serializer);
        final RestProxy restProxy = new RestProxy(httpPipeline, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class<?>[]{swaggerInterface},
            restProxy);
    }
}
