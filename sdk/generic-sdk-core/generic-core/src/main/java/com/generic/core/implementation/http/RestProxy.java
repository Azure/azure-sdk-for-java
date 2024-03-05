// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.RequestOptions;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.implementation.http.rest.RestProxyImpl;
import com.generic.core.implementation.http.rest.RestProxyUtils;
import com.generic.core.implementation.http.rest.SwaggerInterfaceParser;
import com.generic.core.implementation.http.rest.SwaggerMethodParser;
import com.generic.core.util.serializer.ObjectSerializer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Type to create a {@link Proxy} implementation for an interface describing REST API methods.
 *
 * <p>{@link RestProxy} can create {@link Proxy} implementations for interfaces with methods that return deserialized
 * Java objects as  * well as asynchronous single objects that resolve to a deserialized Java object.</p>
 */
public final class RestProxy implements InvocationHandler {
    private final SwaggerInterfaceParser interfaceParser;
    private final RestProxyImpl restProxyImpl;

    /**
     * Create a {@link RestProxy}.
     *
     * @param httpPipeline The {@link HttpPipeline pipeline} that will be used to send {@link HttpRequest requests}.
     * @param serializer The {@link ObjectSerializer serializer} that will be used to convert POJOs to and from request
     * and response bodies.
     * @param interfaceParser The {@link SwaggerInterfaceParser parser} that contains information about the interface
     * describing REST API methods that this {@link RestProxy} implements.
     */
    private RestProxy(HttpPipeline httpPipeline, ObjectSerializer serializer, SwaggerInterfaceParser interfaceParser) {
        this.interfaceParser = interfaceParser;
        this.restProxyImpl = new RestProxyImpl(httpPipeline, serializer, interfaceParser);
    }

    /**
     * Get the {@link SwaggerMethodParser} for the provided {@link Method}. The {@link Method} must exist on the Swagger
     * interface that this {@link RestProxy} was created to implement.
     *
     * @param method The method to get a {@link SwaggerMethodParser} for.
     *
     * @return The {@link SwaggerMethodParser} for the provided {@link Method}.
     */
    private SwaggerMethodParser getMethodParser(Method method) {
        return interfaceParser.getMethodParser(method);
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) {
        // Note: Request options need to be evaluated here, as it is a public class with package private methods.
        // Evaluating here allows the package private methods to be invoked here for downstream use.
        final SwaggerMethodParser methodParser = getMethodParser(method);
        RequestOptions options = methodParser.setRequestOptions(args);

        return restProxyImpl.invoke(proxy, method, options, options != null ? options.getErrorOptions() : null,
            options != null ? options.getRequestCallback() : null, methodParser, args);
    }

    /**
     * Create a {@link Proxy} implementation of the provided Swagger interface.
     *
     * @param swaggerInterface The Swagger interface to provide a {@link Proxy} implementation for.
     * @param httpPipeline The {@link HttpPipeline pipeline} that will be used to send {@link HttpRequest requests}.
     * @param <A> The type of the Swagger interface.
     *
     * @return A {@link Proxy} implementation of the provided Swagger interface.
     */
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline) {
        return create(swaggerInterface, httpPipeline, RestProxyUtils.createDefaultSerializer());
    }

    /**
     * Create a {@link Proxy} implementation of the provided Swagger interface.
     *
     * @param swaggerInterface The Swagger interface to provide a {@link Proxy} implementation for.
     * @param httpPipeline The {@link HttpPipeline pipeline} that will be used to send {@link HttpRequest requests}.
     * @param serializer The {@link ObjectSerializer serializer} that will be used to convert POJOs to and from request
     * and response bodies.
     * @param <A> The type of the Swagger interface.
     *
     * @return A {@link Proxy} implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, ObjectSerializer serializer) {
        final SwaggerInterfaceParser interfaceParser = SwaggerInterfaceParser.getInstance(swaggerInterface);
        final RestProxy restProxy = new RestProxy(httpPipeline, serializer, interfaceParser);

        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class<?>[]{ swaggerInterface },
            restProxy);
    }
}
