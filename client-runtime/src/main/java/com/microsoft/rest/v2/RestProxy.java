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
import com.microsoft.rest.v2.annotations.Headers;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.HostParam;
import com.microsoft.rest.v2.annotations.PATCH;
import com.microsoft.rest.v2.annotations.POST;
import com.microsoft.rest.v2.annotations.PUT;
import com.microsoft.rest.v2.annotations.PathParam;
import com.microsoft.rest.v2.annotations.QueryParam;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.OkHttpClient;
import com.microsoft.rest.v2.http.UrlBuilder;
import rx.Completable;
import rx.Single;
import rx.functions.Func1;

import java.io.IOException;
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
    public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable {
        final String methodName = method.getName();
        final SwaggerMethodProxyDetails methodDetails = interfaceDetails.getMethodProxyDetails(methodName);

        final String actualHost = methodDetails.applyHostSubstitutions(host, args);
        final String[] hostParts = actualHost.split("://");
        final String actualPath = methodDetails.getSubstitutedPath(args);

        final UrlBuilder urlBuilder = new UrlBuilder()
                .withScheme(hostParts[0])
                .withHost(hostParts[1])
                .withPath(actualPath);

        for (EncodedParameter queryParameter : methodDetails.getEncodedQueryParameters(args)) {
            urlBuilder.withQueryParameter(queryParameter.name(), queryParameter.encodedValue());
        }

        final String url = urlBuilder.toString();
        final HttpRequest request = new HttpRequest(methodDetails.fullyQualifiedMethodName(), methodDetails.httpMethod(), url);

        for (final EncodedParameter headerParameter : methodDetails.getEncodedHeaderParameters(args)) {
            request.withHeader(headerParameter.name(), headerParameter.encodedValue());
        }

        for (final HttpHeader header : methodDetails.getHeaders()) {
            request.withHeader(header.name(), header.value());
        }

        final Integer bodyContentMethodParameterIndex = methodDetails.bodyContentMethodParameterIndex();
        if (bodyContentMethodParameterIndex != null) {
            final Object bodyContentObject = args[bodyContentMethodParameterIndex];
            if (bodyContentObject != null) {
                final String bodyContentString = serializer.serialize(bodyContentObject);
                request.withBody(bodyContentString, "application/json");
            }
        }

        Object result = null;
        if (!methodDetails.isAsync()) {
            final HttpResponse response = httpClient.sendRequest(request);

            final Class<?> returnType = methodDetails.returnType();
            if (returnType.equals(Void.TYPE) || !response.hasBody() || methodDetails.httpMethod().equalsIgnoreCase("HEAD")) {
                result = null;
            } else if (returnType.isAssignableFrom(InputStream.class)) {
                result = response.bodyAsInputStream();
            } else if (returnType.isAssignableFrom(byte[].class)) {
                result = response.bodyAsByteArray();
            } else {
                final String responseBodyString = response.bodyAsString();
                result = serializer.deserialize(responseBodyString, returnType);
            }
        }
        else {
            final Single<? extends HttpResponse> asyncResponse = httpClient.sendRequestAsync(request);
            final Class<?> methodReturnType = method.getReturnType();
            if (methodReturnType.equals(Single.class)) {
                result = asyncResponse.flatMap(new Func1<HttpResponse, Single<?>>() {
                    @Override
                    public Single<?> call(HttpResponse response) {
                        Single<?> asyncResult;
                        final Class<?> singleReturnType = methodDetails.returnType();
                        if (methodDetails.httpMethod().equalsIgnoreCase("HEAD")) {
                            asyncResult = Single.just(null);
                        } else if (singleReturnType.isAssignableFrom(InputStream.class)) {
                            asyncResult = response.bodyAsInputStreamAsync();
                        } else if (singleReturnType.isAssignableFrom(byte[].class)) {
                            asyncResult = response.bodyAsByteArrayAsync();
                        } else {
                            final Single<String> asyncResponseBodyString = response.bodyAsStringAsync();
                            asyncResult = asyncResponseBodyString.flatMap(new Func1<String, Single<Object>>() {
                                @Override
                                public Single<Object> call(String responseBodyString) {
                                    try {
                                        return Single.just(serializer.deserialize(responseBodyString, singleReturnType));
                                    }
                                    catch (IOException e) {
                                        return Single.error(e);
                                    }
                                }
                            });
                        }
                        return asyncResult;
                    }
                });
            }
            else if (method.getReturnType().equals(Completable.class)) {
                result = Completable.fromSingle(asyncResponse);
            }
        }

        return result;
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
        final String interfaceName = swaggerInterface.getCanonicalName();
        final SwaggerInterfaceProxyDetails interfaceProxyDetails = new SwaggerInterfaceProxyDetails(interfaceName);

        String host = null;
        final Host hostAnnotation = swaggerInterface.getAnnotation(Host.class);
        if (hostAnnotation != null) {
            host = hostAnnotation.value();
        }

        final Method[] declaredMethods = swaggerInterface.getDeclaredMethods();
        for (Method method : declaredMethods) {
            final SwaggerMethodProxyDetails methodProxyDetails = interfaceProxyDetails.getMethodProxyDetails(method.getName());

            if (method.isAnnotationPresent(GET.class)) {
                methodProxyDetails.setHttpMethodAndRelativePath("GET", method.getAnnotation(GET.class).value());
            }
            else if (method.isAnnotationPresent(PUT.class)) {
                methodProxyDetails.setHttpMethodAndRelativePath("PUT", method.getAnnotation(PUT.class).value());
            }
            else if (method.isAnnotationPresent(HEAD.class)) {
                methodProxyDetails.setHttpMethodAndRelativePath("HEAD", method.getAnnotation(HEAD.class).value());
            }
            else if (method.isAnnotationPresent(DELETE.class)) {
                methodProxyDetails.setHttpMethodAndRelativePath("DELETE", method.getAnnotation(DELETE.class).value());
            }
            else if (method.isAnnotationPresent(POST.class)) {
                methodProxyDetails.setHttpMethodAndRelativePath("POST", method.getAnnotation(POST.class).value());
            }
            else if (method.isAnnotationPresent(PATCH.class)) {
                methodProxyDetails.setHttpMethodAndRelativePath("PATCH", method.getAnnotation(PATCH.class).value());
            }

            if (method.isAnnotationPresent(Headers.class)) {
                final Headers headersAnnotation = method.getAnnotation(Headers.class);
                final String[] headers = headersAnnotation.value();
                for (final String header : headers) {
                    final int colonIndex = header.indexOf(":");
                    if (colonIndex >= 0) {
                        final String headerName = header.substring(0, colonIndex).trim();
                        if (!headerName.isEmpty()) {
                            final String headerValue = header.substring(colonIndex + 1).trim();
                            if (!headerValue.isEmpty()) {
                                methodProxyDetails.addHeader(headerName, headerValue);
                            }
                        }
                    }
                }
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

            final Class<?> returnType = method.getReturnType();
            final boolean isAsync = (returnType == Single.class || returnType == Completable.class);
            methodProxyDetails.setIsAsync(isAsync);
            if (!isAsync) {
                methodProxyDetails.setReturnType(returnType);
            }
            else {
                final String asyncMethodName = method.getName();
                final String syncMethodName = asyncMethodName.endsWith("Async") ? asyncMethodName.substring(0, asyncMethodName.length() - 5) : asyncMethodName;

                for (Method possibleSyncMethod : declaredMethods) {
                    if (possibleSyncMethod.getName().equalsIgnoreCase(syncMethodName) && possibleSyncMethod.getReturnType() != Single.class) {
                        methodProxyDetails.setReturnType(possibleSyncMethod.getReturnType());
                        break;
                    }
                }
            }
        }

        RestProxy restProxy = new RestProxy(host, httpClient, serializer, interfaceProxyDetails);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, restProxy);
    }
}