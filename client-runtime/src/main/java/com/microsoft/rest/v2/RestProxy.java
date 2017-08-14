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
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
        final String methodName = method.getName();
        final SwaggerMethodProxyDetails methodDetails = interfaceDetails.getMethodProxyDetails(methodName);

        RequestBody requestBody = null;
        final Integer bodyContentMethodParameterIndex = methodDetails.getBodyContentMethodParameterIndex();
        if (bodyContentMethodParameterIndex != null) {
            final Object bodyContentObject = args[bodyContentMethodParameterIndex];
            if (bodyContentObject != null) {
                // TODO: what's the actual media type?
                final MediaType mediaType = MediaType.parse("application/json");

                final SerializerAdapter<?> serializer = restClient.serializerAdapter();
                final String bodyContentString = serializer.serialize(bodyContentObject);

                requestBody = RequestBody.create(mediaType, bodyContentString);
            }
        }

        final String actualHost = methodDetails.applyHostSubstitutions(host, args);
        final String actualPath = methodDetails.getSubstitutedPath(args);

        String[] parts = actualHost.split("://");
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(parts[0])
                .host(parts[1])
                .addEncodedPathSegments(actualPath);

        for (EncodedParameter queryParameter : methodDetails.getEncodedQueryParameters(args)) {
            urlBuilder.addEncodedQueryParameter(queryParameter.getName(), queryParameter.getEncodedValue());
        }

        final String httpMethod = methodDetails.getMethod();
        final Request.Builder requestBuilder = new Builder()
                .method(httpMethod, requestBody)
                .url(urlBuilder.build());

        for (final EncodedParameter headerParameter : methodDetails.getEncodedHeaderParameters(args)) {
            requestBuilder.header(headerParameter.getName(), headerParameter.getEncodedValue());
        }

        final Request request = requestBuilder.build();
        final OkHttpClient httpClient = restClient.httpClient();
        final Call call = httpClient.newCall(request);

        final Response response = call.execute();

        final Class<?> returnType = method.getReturnType();
        final ResponseBody responseBody = response.body();
        if (returnType.equals(Void.TYPE) || responseBody == null) {
            return null;
        } else if (returnType.isAssignableFrom(InputStream.class)) {
            return responseBody.byteStream();
        } else if (returnType.isAssignableFrom(byte[].class)) {
            return responseBody.bytes();
        } else {
            final String responseBodyString = responseBody.string();
            final SerializerAdapter<?> serializer = restClient.serializerAdapter();
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
        final Host hostAnnotation = swaggerInterface.getAnnotation(Host.class);
        final String baseUrl = (hostAnnotation != null ? hostAnnotation.value() : restClient.retrofit().baseUrl().toString());

        final SwaggerInterfaceProxyDetails interfaceProxyDetails = new SwaggerInterfaceProxyDetails();
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

        RestProxy restProxy = new RestProxy(baseUrl, restClient, interfaceProxyDetails);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, restProxy);
    }
}