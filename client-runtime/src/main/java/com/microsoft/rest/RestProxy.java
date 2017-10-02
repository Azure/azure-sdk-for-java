/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest;

import com.google.common.reflect.TypeToken;
import com.microsoft.rest.http.ContentType;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.http.HttpHeader;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import com.microsoft.rest.http.UrlBuilder;
import rx.Completable;
import rx.Single;
import rx.exceptions.Exceptions;
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
public class RestProxy implements InvocationHandler {
    private final HttpClient httpClient;
    private final SerializerAdapter<?> serializer;
    private final SwaggerInterfaceParser interfaceParser;

    /**
     * Create a new instance of RestProxy.
     * @param httpClient The HttpClient that will be used by this RestProxy to send HttpRequests.
     * @param serializer The serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser The parser that contains information about the swagger interface that
     *                        this RestProxy "implements".
     */
    public RestProxy(HttpClient httpClient, SerializerAdapter<?> serializer, SwaggerInterfaceParser interfaceParser) {
        this.httpClient = httpClient;
        this.serializer = serializer;
        this.interfaceParser = interfaceParser;
    }

    /**
     * Get the SwaggerMethodParser for the provided method. The Method must exist on the Swagger
     * interface that this RestProxy was created to "implement".
     * @param method The method to get a SwaggerMethodParser for.
     * @return The SwaggerMethodParser for the provided method.
     */
    private SwaggerMethodParser methodParser(Method method) {
        return interfaceParser.methodParser(method);
    }

    /**
     * Get the SerializerAdapter used by this RestProxy.
     * @return The SerializerAdapter used by this RestProxy.
     */
    protected SerializerAdapter<?> serializer() {
        return serializer;
    }

    /**
     * Send the provided request and block until the response is received.
     * @param request The HTTP request to send.
     * @return The HTTP response received.
     * @throws IOException On network issues.
     */
    public HttpResponse sendHttpRequest(HttpRequest request) throws IOException {
        return httpClient.sendRequest(request);
    }

    /**
     * Send the provided request asynchronously, applying any request policies provided to the HttpClient instance.
     * @param request The HTTP request to send.
     * @return A {@link Single} representing the HTTP response that will arrive asynchronously.
     */
    public Single<HttpResponse> sendHttpRequestAsync(HttpRequest request) {
        return httpClient.sendRequestAsync(request);
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) throws IOException, InterruptedException {
        final SwaggerMethodParser methodParser = methodParser(method);

        final HttpRequest request = createHttpRequest(methodParser, args);

        Object result;
        if (methodParser.isAsync()) {
            final Single<HttpResponse> asyncResponse = sendHttpRequestAsync(request);
            result = handleAsyncHttpResponse(request, asyncResponse, methodParser);
        }
        else {
            final HttpResponse response = sendHttpRequest(request);
            result = handleSyncHttpResponse(request, response, methodParser);
        }

        return result;
    }

    /**
     * Create a HttpRequest for the provided Swagger method using the provided arguments.
     * @param methodParser The Swagger method parser to use.
     * @param args The arguments to use to populate the method's annotation values.
     * @return A HttpRequest.
     * @throws IOException Thrown if the body contents cannot be serialized.
     */
    private HttpRequest createHttpRequest(SwaggerMethodParser methodParser, Object[] args) throws IOException {
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
            String contentType = methodParser.bodyContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = request.headers().value("Content-Type");
            }
            if (contentType == null || contentType.isEmpty()) {
                if (bodyContentObject instanceof byte[] || bodyContentObject instanceof String) {
                    contentType = ContentType.APPLICATION_OCTET_STREAM;
                }
                else {
                    contentType = ContentType.APPLICATION_JSON;
                }
            }

            request.headers().set("Content-Type", contentType);

            boolean isJson = false;
            final String[] contentTypeParts = contentType.split(";");
            for (String contentTypePart : contentTypeParts) {
                if (contentTypePart.trim().equalsIgnoreCase(ContentType.APPLICATION_JSON)) {
                    isJson = true;
                    break;
                }
            }

            if (isJson) {
                final String bodyContentString = serializer.serialize(bodyContentObject);
                request.withBody(bodyContentString, contentType);
            }
            else if (bodyContentObject instanceof byte[]) {
                request.withBody((byte[]) bodyContentObject, contentType);
            }
            else if (bodyContentObject instanceof String) {
                final String bodyContentString = (String) bodyContentObject;
                if (!bodyContentString.isEmpty()) {
                    request.withBody((String) bodyContentObject, contentType);
                }
            }
            else {
                final String bodyContentString = serializer.serialize(bodyContentObject);
                request.withBody(bodyContentString, contentType);
            }
        }

        return request;
    }

    protected Object handleSyncHttpResponse(HttpRequest httpRequest, HttpResponse httpResponse, SwaggerMethodParser methodParser) throws IOException, InterruptedException {
        final Type returnType = methodParser.returnType();
        return handleSyncHttpResponse(httpRequest, httpResponse, methodParser, returnType);
    }

    protected Object handleSyncHttpResponse(HttpRequest httpRequest, HttpResponse httpResponse, SwaggerMethodParser methodParser, Type returnType) throws IOException {
        ensureExpectedStatus(httpResponse, methodParser).toBlocking().value();
        return toProxyReturnValue(httpResponse, methodParser, returnType).toBlocking().value();
    }

    private Exception instantiateUnexpectedException(SwaggerMethodParser methodParser, HttpResponse response, String responseContent) {
        final int responseStatusCode = response.statusCode();
        final Class<? extends RestException> exceptionType = methodParser.exceptionType();
        final Class<?> exceptionBodyType = methodParser.exceptionBodyType();

        Exception result;
        try {
            final Constructor<? extends RestException> exceptionConstructor = exceptionType.getConstructor(String.class, HttpResponse.class, exceptionBodyType);
            final Object exceptionBody = responseContent == null || responseContent.isEmpty() ? null : serializer.deserialize(responseContent, exceptionBodyType);

            result = exceptionConstructor.newInstance("Status code " + responseStatusCode + ", " + responseContent, response, exceptionBody);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            String message = "Status code " + responseStatusCode + ", but an instance of " + exceptionType.getCanonicalName() + " cannot be created.";
            if (responseContent != null && !responseContent.isEmpty()) {
                message += " Response content: \"" + responseContent + "\"";
            }
            result = new IOException(message, e);
        } catch (IOException e) {
            result = e;
        }

        return result;
    }

    private Single<HttpResponse> ensureExpectedStatus(final HttpResponse response, final SwaggerMethodParser methodParser) {
        final int responseStatusCode = response.statusCode();
        final Single<HttpResponse> asyncResult;
        if (!methodParser.isExpectedResponseStatusCode(responseStatusCode)) {
            asyncResult = response.bodyAsStringAsync().map(new Func1<String, HttpResponse>() {
                @Override
                public HttpResponse call(String responseBody) {
                    throw Exceptions.propagate(instantiateUnexpectedException(methodParser, response, responseBody));
                }
            });
        } else {
             asyncResult = Single.just(response);
        }

        return asyncResult;
    }

    private Single<?> toProxyReturnValue(HttpResponse response, SwaggerMethodParser methodParser, final Type entityType) {
        final TypeToken entityTypeToken = TypeToken.of(entityType);
        final Single<?> asyncResult;
        if (entityTypeToken.isSubtypeOf(void.class) || entityTypeToken.isSubtypeOf(Void.class)) {
            asyncResult = Single.just(null);
        } else if (methodParser.httpMethod().equalsIgnoreCase("HEAD")
                && (entityTypeToken.isSubtypeOf(boolean.class) || entityTypeToken.isSubtypeOf(Boolean.class))) {
            boolean isSuccess = response.statusCode() / 100 == 2;
            asyncResult = Single.just(isSuccess);
        } else if (entityTypeToken.isSubtypeOf(InputStream.class)) {
            asyncResult = response.bodyAsInputStreamAsync();
        } else if (entityTypeToken.isSubtypeOf(byte[].class)) {
            asyncResult = response.bodyAsByteArrayAsync();
        } else {
            final Single<String> asyncResponseBodyString = response.bodyAsStringAsync();
            asyncResult = asyncResponseBodyString.flatMap(new Func1<String, Single<Object>>() {
                @Override
                public Single<Object> call(String responseBodyString) {
                    try {
                        Object responseBodyObject = null;
                        if (responseBodyString != null && !responseBodyString.isEmpty()) {
                            responseBodyObject = serializer.deserialize(responseBodyString, entityType);
                        }
                        return Single.just(responseBodyObject);
                    } catch (Throwable e) {
                        return Single.error(e);
                    }
                }
            });
        }
        return asyncResult;
    }

    protected Object handleAsyncHttpResponse(HttpRequest httpRequest, Single<HttpResponse> asyncHttpResponse, final SwaggerMethodParser methodParser) {
        Object result;

        final Type returnType = methodParser.returnType();
        final TypeToken returnTypeToken = TypeToken.of(returnType);

        final Single<HttpResponse> asyncExpectedResponse = asyncHttpResponse.flatMap(new Func1<HttpResponse, Single<HttpResponse>>() {
            @Override
            public Single<HttpResponse> call(HttpResponse response) {
                return ensureExpectedStatus(response, methodParser);
            }
        });

        if (returnTypeToken.isSubtypeOf(Completable.class)) {
            result = Completable.fromSingle(asyncExpectedResponse);
        }
        else if (returnTypeToken.isSubtypeOf(Single.class)) {
            final Type singleTypeParam = ((ParameterizedType) methodParser.returnType()).getActualTypeArguments()[0];
            result = asyncExpectedResponse.flatMap(new Func1<HttpResponse, Single<?>>() {
                @Override
                public Single<?> call(HttpResponse response) {
                    return toProxyReturnValue(response, methodParser, singleTypeParam);
                }
            });
        }
        else {
            throw new InvalidReturnTypeException("RestProxy does not support swagger interface methods (such as " + methodParser.fullyQualifiedMethodName() + "()) with a return type of " + returnType.toString());
        }

        return result;
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param baseURL The base URL for the service.
     * @param httpClient The internal HTTP client that will be used to make REST calls.
     * @param serializer The serializer that will be used to convert POJOs to and from request and
     *                   response bodies.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, String baseURL, HttpClient httpClient, SerializerAdapter<?> serializer) {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface, baseURL);
        final RestProxy restProxy = new RestProxy(httpClient, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, restProxy);
    }
}