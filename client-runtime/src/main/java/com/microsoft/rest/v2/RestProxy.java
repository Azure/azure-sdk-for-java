/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.RestClient;
import com.microsoft.rest.protocol.SerializerAdapter;
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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * This class can be used to create a proxy implementation for a provided Swagger generated
 * interface.
 */
public final class RestProxy implements InvocationHandler {
    private final HttpClient httpClient;
    private final SerializerAdapter<?> serializer;
    private final SwaggerInterfaceParser interfaceParser;

    private RestProxy(HttpClient httpClient, SerializerAdapter<?> serializer, SwaggerInterfaceParser interfaceParser) {
        this.httpClient = httpClient;
        this.serializer = serializer;
        this.interfaceParser = interfaceParser;
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable {
        final SwaggerMethodParser methodParser = interfaceParser.methodParser(method);

        final UrlBuilder urlBuilder = new UrlBuilder()
                .withScheme(methodParser.scheme(args))
                .withHost(methodParser.host(args))
                .withPath(methodParser.path(args));

        for (final EncodedParameter queryParameter : methodParser.encodedQueryParameters(args)) {
            urlBuilder.withQueryParameter(queryParameter.name(), queryParameter.encodedValue());
        }

        final String url = urlBuilder.toString();
        final HttpRequest request = new HttpRequest(methodParser.fullyQualifiedMethodName(), methodParser.httpMethod(), url);

        for (final HttpHeader header : methodParser.headers(args)) {
            request.withHeader(header.name(), header.value());
        }

        final Object bodyContentObject = methodParser.body(args);
        if (bodyContentObject != null) {
            final String mimeType = "application/json";
            final String bodyContentString = serializer.serialize(bodyContentObject);
            request.withBody(bodyContentString, mimeType);
        }

        Object result = null;
        if (!methodParser.isAsync()) {
            final HttpResponse response = httpClient.sendRequest(request);

            final Class<?> returnType = methodParser.returnType();
            if (returnType.equals(Void.TYPE) || methodParser.httpMethod().equalsIgnoreCase("HEAD")) {
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
                    final Class<?> singleReturnType = methodParser.returnType();
                    if (methodParser.httpMethod().equalsIgnoreCase("HEAD")) {
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
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface);
        final RestProxy restProxy = new RestProxy(httpClient, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, restProxy);
    }
}