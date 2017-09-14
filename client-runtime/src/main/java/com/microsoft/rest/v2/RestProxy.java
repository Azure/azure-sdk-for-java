/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.google.common.reflect.TypeToken;
import com.microsoft.rest.RestException;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.v2.http.HttpClient;
import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.UrlBuilder;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.functions.Func1;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

/**
 * This class can be used to create a proxy implementation for a provided Swagger generated
 * interface. RestProxy can create proxy implementations for interfaces with methods that return
 * deserialized Java objects as well as asynchronous Single objects that resolve to a deserialized
 * Java object.
 */
public final class RestProxy implements InvocationHandler {
    private final HttpClient httpClient;
    private final SerializerAdapter<?> serializer;
    private final SwaggerInterfaceParser interfaceParser;
    private final ResponseHandler responseHandler;

    /**
     * The default response handler that will be used to convert HttpResponse objects to the proxy
     * method's return value.
     */
    public static final ResponseHandler DEFAULT_RESPONSE_HANDLER = new ResponseHandler() {
        @Override
        public Object handleSyncResponse(HttpResponse response, SwaggerMethodParser methodParser, Type returnType, SerializerAdapter<?> serializer) throws IOException {
            Object result;

            final TypeToken returnTypeToken = TypeToken.of(returnType);
            final int responseStatusCode = response.statusCode();
            if (!methodParser.isExpectedResponseStatusCode(responseStatusCode)) {
                final Class<? extends RestException> exceptionType = methodParser.exceptionType();
                String responseContent = null;
                try {
                    final Class<?> exceptionBodyType = methodParser.exceptionBodyType();
                    final Constructor<? extends RestException> exceptionConstructor = exceptionType.getConstructor(String.class, HttpResponse.class, exceptionBodyType);

                    try {
                        responseContent = response.bodyAsString();
                    } catch (IOException ignored) {
                    }

                    final Object exceptionBody = responseContent == null || responseContent.isEmpty() ? null : serializer.deserialize(responseContent, exceptionBodyType);

                    throw exceptionConstructor.newInstance("Status code " + responseStatusCode + ", " + responseContent, response, exceptionBody);
                } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                    String message = "Status code " + responseStatusCode + ", but an instance of " + exceptionType.getCanonicalName() + " cannot be created.";
                    if (responseContent != null && responseContent.isEmpty()) {
                        message += " Response content: \"" + responseContent + "\"";
                    }
                    throw new IOException(message, e);
                }
            }

            if (returnType.equals(Void.TYPE) || methodParser.httpMethod().equalsIgnoreCase("HEAD")) {
                result = null;
            } else if (returnTypeToken.isSubtypeOf(InputStream.class)) {
                result = response.bodyAsInputStream();
            } else if (returnTypeToken.isSubtypeOf(byte[].class)) {
                result = response.bodyAsByteArray();
            } else {
                final String responseBodyString = response.bodyAsString();
                result = serializer.deserialize(responseBodyString, returnType);
            }

            return result;
        }

        @Override
        public Object handleAsyncResponse(Single<HttpResponse> asyncResponse, final SwaggerMethodParser methodParser, final SerializerAdapter<?> serializer) {
            Object result;

            final Type returnType = methodParser.returnType();
            final TypeToken returnTypeToken = TypeToken.of(returnType);
            if (returnTypeToken.isSubtypeOf(Completable.class)) {
                result = Completable.fromSingle(asyncResponse);
            }
            else if (returnTypeToken.isSubtypeOf(Single.class)) {
                result = asyncResponse.flatMap(new Func1<HttpResponse, Single<?>>() {
                    @Override
                    public Single<?> call(HttpResponse response) {
                        Single<?> asyncResult;
                        final Type singleReturnType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
                        final TypeToken singleReturnTypeToken = TypeToken.of(singleReturnType);
                        if (methodParser.httpMethod().equalsIgnoreCase("HEAD")) {
                            asyncResult = Single.just(null);
                        } else if (singleReturnTypeToken.isSubtypeOf(InputStream.class)) {
                            asyncResult = response.bodyAsInputStreamAsync();
                        } else if (singleReturnTypeToken.isSubtypeOf(byte[].class)) {
                            asyncResult = response.bodyAsByteArrayAsync();
                        } else {
                            final Single<String> asyncResponseBodyString = response.bodyAsStringAsync();
                            asyncResult = asyncResponseBodyString.flatMap(new Func1<String, Single<Object>>() {
                                @Override
                                public Single<Object> call(String responseBodyString) {
                                    try {
                                        return Single.just(serializer.deserialize(responseBodyString, singleReturnType));
                                    } catch (Throwable e) {
                                        return Single.error(e);
                                    }
                                }
                            });
                        }
                        return asyncResult;
                    }
                });
            }
            else {
                throw new InvalidReturnTypeException("RestProxy does not support swagger interface methods (such as " + methodParser.fullyQualifiedMethodName() + "()) with a return type of " + returnType.toString());
            }

            return result;
        }
    };

    RestProxy(HttpClient httpClient, SerializerAdapter<?> serializer, SwaggerInterfaceParser interfaceParser, ResponseHandler responseHandler) {
        this.httpClient = httpClient;
        this.serializer = serializer;
        this.interfaceParser = interfaceParser;
        this.responseHandler = responseHandler;
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) throws IOException {
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

        Object result;
        final Type returnType = methodParser.returnType();
        final TypeToken returnTypeToken = TypeToken.of(returnType);
        if (returnTypeToken.isSubtypeOf(Completable.class) || returnTypeToken.isSubtypeOf(Single.class) || returnTypeToken.isSubtypeOf(Observable.class)) {
            final Single<HttpResponse> asyncResponse = httpClient.sendRequestAsync(request);
            result = responseHandler.handleAsyncResponse(asyncResponse, methodParser, serializer);
        }
        else {
            final HttpResponse response = httpClient.sendRequest(request);
            result = responseHandler.handleSyncResponse(response, methodParser, methodParser.returnType(), serializer);
        }

        return result;
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
        return create(swaggerInterface, httpClient, serializer, DEFAULT_RESPONSE_HANDLER);
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param httpClient The internal HTTP client that will be used to make REST calls.
     * @param serializer The serializer that will be used to convert POJOs to and from request and
     *                   response bodies.
     * @param responseHandler The object that will be used to handle responses to HTTP requests.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpClient httpClient, SerializerAdapter<?> serializer, ResponseHandler responseHandler) {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface);
        final RestProxy restProxy = new RestProxy(httpClient, serializer, interfaceParser, responseHandler);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, restProxy);
    }

    /**
     * The handler that determines how to deal with an incoming HTTP response from this RestProxy.
     */
    public interface ResponseHandler {
        /**
         * Convert the provided synchronous HttpResponse object into the appropriate return value.
         * @param response The HttpResponse to handle.
         * @param methodParser The SwaggerMethodParser that was used to send the HttpRequest that
         *                     created the HttpResponse passed to this method.
         * @param returnType The type of the return value.
         * @param serializer The serializer that can be used to convert a String to the Swagger
         *                   method's return type.
         * @throws IOException If the response's return status code is not recognized and the
         * response body cannot be converted to the expected error type.
         * @throws RestException If the response's return status code is not recognized and the
         * response body can be converted to the expected error type.
         * @return The return value.
         */
        Object handleSyncResponse(HttpResponse response, SwaggerMethodParser methodParser, Type returnType, SerializerAdapter<?> serializer) throws IOException, RestException;

        /**
         * Convert the provided asynchronous HttpResponse object into the appropriate asynchronous
         * return value.
         * @param response The asynchronous HttpResponse to handle.
         * @param methodParser The SwaggerMethodParser that was used to send the HttpRequest that
         *                     created the HttpResponse passed to this method.
         * @param serializer The serializer that can be used to convert a String to the Swagger
         *                   method's return type.
         * @return The asynchronous return value.
         */
        Object handleAsyncResponse(Single<HttpResponse> response, SwaggerMethodParser methodParser, SerializerAdapter<?> serializer);
    }
}