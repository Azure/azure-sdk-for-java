/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.RestClient;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.v2.annotations.BodyParam;
import com.microsoft.rest.v2.annotations.DELETE;
import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.HEAD;
import com.microsoft.rest.v2.annotations.HeaderParam;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.HostParam;
import com.microsoft.rest.v2.annotations.PATCH;
import com.microsoft.rest.v2.annotations.POST;
import com.microsoft.rest.v2.annotations.PUT;
import com.microsoft.rest.v2.annotations.PathParam;
import com.microsoft.rest.v2.annotations.QueryParam;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.OkHttpClient;
import com.microsoft.rest.v2.http.UrlBuilder;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This class can be used to create a proxy implementation for a provided Swagger generated
 * interface.
 * TODO: Convert this to RxNetty and finish.
 */
public final class RestProxy implements InvocationHandler {
    private final String host;
    private final HttpClient httpClient;
    private final SerializerAdapter<?> serializer;
    private final SwaggerInterfaceProxyDetails interfaceDetails;

    private RestProxy(String host, HttpClient httpClient, SerializerAdapter<?> serializer, SwaggerInterfaceProxyDetails interfaceDetails) {
        this.host = host;
        this.httpClient = httpClient;
        this.serializer = serializer;
        this.interfaceDetails = interfaceDetails;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        final SwaggerMethodProxyDetails methodDetails = interfaceDetails.getMethodProxyDetails(methodName);

        final String actualHost = methodDetails.applyHostSubstitutions(host, args);
        final String[] hostParts = actualHost.split("://");
        final String actualPath = methodDetails.getSubstitutedPath(args);

        final UrlBuilder urlBuilder = new UrlBuilder()
                .setScheme(hostParts[0])
                .setHost(hostParts[1])
                .setPath(actualPath);

        for (EncodedParameter queryParameter : methodDetails.getEncodedQueryParameters(args)) {
            urlBuilder.addQueryParameter(queryParameter.getName(), queryParameter.getEncodedValue());
        }

        final String url = urlBuilder.toString();
        final HttpRequest request = new HttpRequest(methodDetails.getMethod(), url);

        for (final EncodedParameter headerParameter : methodDetails.getEncodedHeaderParameters(args)) {
            request.addHeader(headerParameter.getName(), headerParameter.getEncodedValue());
        }

        final Integer bodyContentMethodParameterIndex = methodDetails.getBodyContentMethodParameterIndex();
        if (bodyContentMethodParameterIndex != null) {
            final Object bodyContentObject = args[bodyContentMethodParameterIndex];
            if (bodyContentObject != null) {
                final String bodyContentString = serializer.serialize(bodyContentObject);
                request.setBody(bodyContentString, "application/json");
            }
        }

        final HttpResponse response = httpClient.sendRequest(request);

        final Class<?> returnType = method.getReturnType();
        if (returnType.equals(Void.TYPE) || !response.hasBody()) {
            return null;
        } else if (returnType.isAssignableFrom(InputStream.class)) {
            return response.getBodyAsInputStream();
        } else if (returnType.isAssignableFrom(byte[].class)) {
            return response.getBodyAsByteArray();
        } else {
            final String responseBodyString = response.getBodyAsString();
            return serializer.deserialize(responseBodyString, returnType);
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
        final HttpClient httpClient = new OkHttpClient(restClient.httpClient());
        final SerializerAdapter<?> serializer = restClient.serializerAdapter();
        return create(swaggerInterface, httpClient, serializer);
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param httpClient The internal HTTP client that will be used to make REST calls.
     * @param serializer The serializer that will be used to convert POJOs to and from request and
     *                   response bodies.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpClient httpClient, SerializerAdapter<?> serializer) {
        final SwaggerInterfaceProxyDetails interfaceProxyDetails = new SwaggerInterfaceProxyDetails();

        String host = null;
        final Host hostAnnotation = swaggerInterface.getAnnotation(Host.class);
        if (hostAnnotation != null) {
            host = hostAnnotation.value();
        }

        for (Method method : swaggerInterface.getDeclaredMethods()) {
            final SwaggerMethodProxyDetails methodProxyDetails = interfaceProxyDetails.getMethodProxyDetails(method.getName());

            if (method.isAnnotationPresent(GET.class)) {
                methodProxyDetails.setMethodAndRelativePath("GET", method.getAnnotation(GET.class).value());
            }
            else if (method.isAnnotationPresent(PUT.class)) {
                methodProxyDetails.setMethodAndRelativePath("PUT", method.getAnnotation(PUT.class).value());
            }
            else if (method.isAnnotationPresent(HEAD.class)) {
                methodProxyDetails.setMethodAndRelativePath("HEAD", method.getAnnotation(HEAD.class).value());
            }
            else if (method.isAnnotationPresent(DELETE.class)) {
                methodProxyDetails.setMethodAndRelativePath("DELETE", method.getAnnotation(DELETE.class).value());
            }
            else if (method.isAnnotationPresent(POST.class)) {
                methodProxyDetails.setMethodAndRelativePath("POST", method.getAnnotation(POST.class).value());
            }
            else if (method.isAnnotationPresent(PATCH.class)) {
                methodProxyDetails.setMethodAndRelativePath("PATCH", method.getAnnotation(PATCH.class).value());
            }

            final Annotation[][] allParametersAnnotations = method.getParameterAnnotations();
            for (int parameterIndex = 0; parameterIndex < allParametersAnnotations.length; ++parameterIndex) {
                final Annotation[] parameterAnnotations = method.getParameterAnnotations()[parameterIndex];
                for (final Annotation annotation : parameterAnnotations) {
                    final Class<? extends Annotation> annotationType = annotation.annotationType();
                    if (annotationType.equals(HostParam.class)) {
                        final HostParam hostParamAnnotation = (HostParam) annotation;
                        methodProxyDetails.addHostSubstitution(hostParamAnnotation.value(), parameterIndex, !hostParamAnnotation.encoded());
                    }
                    else if (annotationType.equals(PathParam.class)) {
                        final PathParam pathParamAnnotation = (PathParam) annotation;
                        methodProxyDetails.addPathSubstitution(pathParamAnnotation.value(), parameterIndex, !pathParamAnnotation.encoded());
                    }
                    else if (annotationType.equals(QueryParam.class)) {
                        final QueryParam queryParamAnnotation = (QueryParam) annotation;
                        methodProxyDetails.addQuerySubstitution(queryParamAnnotation.value(), parameterIndex, !queryParamAnnotation.encoded());
                    }
                    else if (annotationType.equals(HeaderParam.class)) {
                        final HeaderParam headerParamAnnotation = (HeaderParam) annotation;
                        methodProxyDetails.addHeaderSubstitution(headerParamAnnotation.value(), parameterIndex);
                    }
                    else if (annotationType.equals(BodyParam.class)) {
                        methodProxyDetails.setBodyContentMethodParameterIndex(parameterIndex);
                    }
                }
            }
        }

        RestProxy restProxy = new RestProxy(host, httpClient, serializer, interfaceProxyDetails);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, restProxy);
    }
}