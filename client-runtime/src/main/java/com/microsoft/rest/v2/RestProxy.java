/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.RestClient;
import com.microsoft.rest.v2.annotations.*;
import okhttp3.*;
import okhttp3.Request.Builder;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.util.Map;

/**
 * This class can be used to create a proxy implementation for a provided Swagger generated
 * interface.
 */
public final class RestProxy implements InvocationHandler {
    private final String host;
    private final RestClient restClient;
    private final SwaggerInterfaceProxyDetails interfaceDetails;

    private RestProxy(String host, RestClient restClient, SwaggerInterfaceProxyDetails interfaceDetails) {
        this.host = host;
        this.restClient = restClient;
        this.interfaceDetails = interfaceDetails;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final SwaggerMethodProxyDetails methodDetails = interfaceDetails.methodDetails.get(method.getName());

        RequestBody requestBody = null;
        if (methodDetails.bodyArg != null) {
            if (args[methodDetails.bodyArg] != null) {
                // TODO: what's the actual media type?
                requestBody = RequestBody.create(MediaType.parse("application/json"), restClient.serializerAdapter().serialize(args[methodDetails.bodyArg]));
            }
        }
        String actualHost = host;
        String actualPath = methodDetails.relativePath;
        for (Map.Entry<String, Integer> hostArg : methodDetails.hostArgs.entrySet()) {
            String hostValue;
            if (hostArg.getValue() < 0) {
                hostValue = encode(String.valueOf(args[-hostArg.getValue()]));
            } else {
                hostValue = String.valueOf(args[hostArg.getValue()]);
            }
            actualHost = actualHost.replace("{" + hostArg.getKey() + "}", hostValue);
        }
        for (Map.Entry<String, Integer> pathArg : methodDetails.pathArgs.entrySet()) {
            String pathValue;
            if (pathArg.getValue() < 0) {
                pathValue = encode(String.valueOf(args[-pathArg.getValue()]));
            } else {
                pathValue = String.valueOf(args[pathArg.getValue()]);
            }
            actualPath = actualPath.replace("{" + pathArg.getKey() + "}", pathValue);
        }

        String[] parts = actualHost.split("://");
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(parts[0])
                .host(parts[1])
                .addEncodedPathSegments(actualPath);

        for (Map.Entry<String, Integer> queryArg : methodDetails.queryArgs.entrySet()) {
            String queryValue;
            if (queryArg.getValue() < 0) {
                queryValue = encode(String.valueOf(args[-queryArg.getValue()]));
            } else {
                queryValue = String.valueOf(args[queryArg.getValue()]);
            }
            urlBuilder.addEncodedQueryParameter(queryArg.getKey(), queryValue);
        }

        Request.Builder requestBuilder = new Builder().method(methodDetails.method, requestBody)
                .url(urlBuilder.build());

        for (Map.Entry<String, Integer> headerArg : methodDetails.headerArgs.entrySet()) {
            String headerValue;
            if (headerArg.getValue() < 0) {
                headerValue = encode(String.valueOf(args[-headerArg.getValue()]));
            } else {
                headerValue = String.valueOf(args[headerArg.getValue()]);
            }
            requestBuilder.header(headerArg.getKey(), headerValue);
        }

        Response response = restClient.httpClient().newCall(requestBuilder.build()).execute();
        if (method.getReturnType().equals(Void.TYPE) || response.body() == null) {
            return null;
        } else if (method.getReturnType().isAssignableFrom(InputStream.class)) {
            return response.body().byteStream();
        } else if (method.getReturnType().isAssignableFrom(byte[].class)) {
            return response.body().bytes();
        } else {
            return restClient.serializerAdapter().deserialize(response.body().string(), method.getReturnType());
        }
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param restClient The internal HTTP client that will be used to make REST calls.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, RestClient restClient) {
        String host = restClient.retrofit().baseUrl().host();
        String protocol = restClient.retrofit().baseUrl().scheme();
        if (swaggerInterface.isAnnotationPresent(Host.class)) {
            host = swaggerInterface.getAnnotation(Host.class).value();
            if (!host.contains("://")) {
                host = protocol + "://" + host;
            }
        }

        final SwaggerInterfaceProxyDetails interfaceProxyDetails = new SwaggerInterfaceProxyDetails();
        for (Method method : swaggerInterface.getDeclaredMethods()) {
            final SwaggerMethodProxyDetails methodProxyDetails = interfaceProxyDetails.createMethodDetails(method.getName());

            final Annotation[][] allParametersAnnotations = method.getParameterAnnotations();
            for (int parameterIndex = 0; parameterIndex < allParametersAnnotations.length; ++parameterIndex) {
                final Annotation[] parameterAnnotations = method.getParameterAnnotations()[parameterIndex];
                for (final Annotation annotation : parameterAnnotations) {
                    final Class<? extends Annotation> annotationType = annotation.annotationType();
                    if (annotationType.equals(HostParam.class)) {
                        methodProxyDetails.addHostParamDetails((HostParam)annotation, parameterIndex);
                    }
                    else if (annotationType.equals(PathParam.class)) {
                        methodProxyDetails.addPathParamDetails((PathParam)annotation, parameterIndex);
                    }
                    else if (annotationType.equals(QueryParam.class)) {
                        methodProxyDetails.addQueryParamDetails((QueryParam)annotation, parameterIndex);
                    }
                    else if (annotationType.equals(HeaderParam.class)) {
                        methodProxyDetails.addHeaderParamDetails((HeaderParam)annotation, parameterIndex);
                    }
                    else if (annotationType.equals(BodyParam.class)) {
                        methodProxyDetails.addBodyParamDetails((BodyParam)annotation, parameterIndex);
                    }
                }
            }

            if (method.isAnnotationPresent(GET.class)) {
                methodProxyDetails.method = "GET";
                methodProxyDetails.relativePath = method.getAnnotation(GET.class).value();
            }
            if (method.isAnnotationPresent(PUT.class)) {
                methodProxyDetails.method = "PUT";
                methodProxyDetails.relativePath = method.getAnnotation(PUT.class).value();
            }
            if (method.isAnnotationPresent(HEAD.class)) {
                methodProxyDetails.method = "HEAD";
                methodProxyDetails.relativePath = method.getAnnotation(HEAD.class).value();
            }
            if (method.isAnnotationPresent(DELETE.class)) {
                methodProxyDetails.method = "DELETE";
                methodProxyDetails.relativePath = method.getAnnotation(DELETE.class).value();
            }
            if (method.isAnnotationPresent(POST.class)) {
                methodProxyDetails.method = "POST";
                methodProxyDetails.relativePath = method.getAnnotation(POST.class).value();
            }
            if (method.isAnnotationPresent(PATCH.class)) {
                methodProxyDetails.method = "PATCH";
                methodProxyDetails.relativePath = method.getAnnotation(PATCH.class).value();
            }
        }
        RestProxy restProxy = new RestProxy(host, restClient, interfaceProxyDetails);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[] { swaggerInterface }, restProxy);
    }

    private static String encode(String segment) {
        try {
            return URLEncoder.encode(segment, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return segment;
        }
    }
}