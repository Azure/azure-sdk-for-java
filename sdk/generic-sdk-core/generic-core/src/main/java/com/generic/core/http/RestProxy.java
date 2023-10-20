// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http;

import com.generic.core.implementation.http.rest.SwaggerInterfaceParser;
import com.generic.core.implementation.http.rest.SwaggerMethodParser;
import com.generic.core.util.serializer.JsonSerializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Type to create a proxy implementation for an interface describing REST API methods.
 * <p>
 * RestProxy can create proxy implementations for interfaces with methods that return deserialized Java objects as well
 * as asynchronous Single objects that resolve to a deserialized Java object.
 */
public final class RestProxy implements InvocationHandler {
    private final SwaggerInterfaceParser interfaceParser;
    private final HttpPipeline httpPipeline;
    private final com.generic.core.implementation.http.rest.RestProxy restProxy;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     * this RestProxy "implements".
     */
    private RestProxy(HttpPipeline httpPipeline, JsonSerializer serializer, SwaggerInterfaceParser interfaceParser) {
        this.interfaceParser = interfaceParser;
        this.restProxy =
            new com.generic.core.implementation.http.rest.RestProxy(httpPipeline, serializer, interfaceParser);
        this.httpPipeline = httpPipeline;
    }

    /**
     * Get the SwaggerMethodParser for the provided method. The Method must exist on the Swagger interface that this
     * RestProxy was created to "implement".
     *
     * @param method the method to get a SwaggerMethodParser for
     *
     * @return the SwaggerMethodParser for the provided method
     */
    private SwaggerMethodParser getMethodParser(Method method) {
        return interfaceParser.getMethodParser(method);
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) {
        //        RestProxyUtils.validateResumeOperationIsNotPresent(method);

        // Note: request options need to be evaluated here, as it is a public class with package private methods.
        // Evaluating here allows the package private methods to be invoked here for downstream use.
        //     final SwaggerMethodParser methodParser = getMethodParser(method);
        //     HttpRequestOptions options = methodParser.setRequestOptions(args);
        //     Context context = methodParser.setContext(args);
        //
        //     return syncRestProxy.invoke(proxy, method, options, options != null ? options.getErrorOptions() : null,
        //         options != null ? options.getRequestCallback() : null, methodParser, false, args);
        // }
        return null;
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     * @param <A> the type of the Swagger interface
     *
     * @return a proxy implementation of the provided Swagger interface
     */
    public static <A> A create(Class<A> swaggerInterface) {
        return null;
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipeline that will be used to send Http requests
     * @param <A> the type of the Swagger interface
     *
     * @return a proxy implementation of the provided Swagger interface
     */
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline) {
        return null;
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipline that will be used to send Http requests
     * @param serializer the serializer that will be used to convert POJOs to and from request and response bodies
     * @param <A> the type of the Swagger interface.
     *
     * @return a proxy implementation of the provided Swagger interface
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, JsonSerializer serializer) {
        final SwaggerInterfaceParser interfaceParser = SwaggerInterfaceParser.getInstance(swaggerInterface);
        // final RestProxy restProxy = new RestProxy(httpPipeline, serializer, interfaceParser);
        final RestProxy restProxy = null;
        return null;
    }
}
