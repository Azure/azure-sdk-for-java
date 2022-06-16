// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.implementation.http.rest.*;
import com.azure.core.implementation.http.rest.SyncRestProxy;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import reactor.core.Exceptions;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.azure.core.implementation.http.rest.RestProxyUtils.isReactive;
import static com.azure.core.implementation.serializer.HttpResponseBodyDecoder.shouldEagerlyReadResponse;

/**
 * Type to create a proxy implementation for an interface describing REST API methods.
 *
 * RestProxy can create proxy implementations for interfaces with methods that return deserialized Java objects as well
 * as asynchronous Single objects that resolve to a deserialized Java object.
 */
public final class RestProxy implements InvocationHandler {
    // RestProxy is a commonly used class, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(RestProxy.class);

    private final SwaggerInterfaceParser interfaceParser;
    private AsyncRestProxy asyncRestProxy;
    private SyncRestProxy syncRestProxy;

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

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) {
        RestProxyUtils.validateResumeOperationIsNotPresent(method);

        try {
            final SwaggerMethodParser methodParser = getMethodParser(method);

            HttpRequest request;
            boolean isReactive = isReactive(methodParser.getReturnType());
            if (isReactive) {
                request = asyncRestProxy.createHttpRequest(methodParser, args);
            } else {
                request = syncRestProxy.createHttpRequest(methodParser, args);
            }

            Context context = methodParser.setContext(args);

            RequestOptions options = methodParser.setRequestOptions(args);
            context = RestProxyUtils.mergeRequestOptionsContext(context, options);

            context = context.addData("caller-method", methodParser.getFullyQualifiedMethodName())
                .addData("azure-eagerly-read-response", shouldEagerlyReadResponse(methodParser.getReturnType()));


            if (isReactive) {
                return asyncRestProxy.invoke(proxy, method, options, options != null ? options.getErrorOptions() : null,
                    options != null ? options.getRequestCallback() : null, methodParser, request, context);
            } else {
                return syncRestProxy.invoke(proxy, method, options, options != null ? options.getErrorOptions() : null,
                    options != null ? options.getRequestCallback() : null, methodParser, request, context);
            }

        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
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
