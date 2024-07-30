// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http;

import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.http.rest.RestProxyImpl;
import io.clientcore.core.implementation.http.rest.RestProxyUtils;
import io.clientcore.core.implementation.http.rest.SwaggerInterfaceParser;
import io.clientcore.core.implementation.http.rest.SwaggerMethodParser;
import io.clientcore.core.util.serializer.ObjectSerializer;

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
    private final SwaggerInterfaceParser interfaceParser;
    private final RestProxyImpl restProxyImpl;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     * this RestProxy "implements".
     */
    private RestProxy(HttpPipeline httpPipeline, ObjectSerializer serializer, SwaggerInterfaceParser interfaceParser) {
        this.interfaceParser = interfaceParser;
        this.restProxyImpl = new RestProxyImpl(httpPipeline, serializer, interfaceParser);
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
        // Note: RequestOptions need to be evaluated here, as it is a public class with package private methods.
        // Evaluating here allows the package private methods to be invoked here for downstream use.
        final SwaggerMethodParser methodParser = getMethodParser(method);
        RequestOptions options = methodParser.setRequestOptions(args);

        return restProxyImpl.invoke(proxy, options, methodParser, args);
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
     *
     * @return a proxy implementation of the provided Swagger interface
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, ObjectSerializer serializer) {
        final SwaggerInterfaceParser interfaceParser = SwaggerInterfaceParser.getInstance(swaggerInterface);
        final RestProxy restProxy = new RestProxy(httpPipeline, serializer, interfaceParser);

        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class<?>[]{swaggerInterface},
            restProxy);
    }
}
