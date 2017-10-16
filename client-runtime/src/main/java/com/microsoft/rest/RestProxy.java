/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest;

import com.google.common.reflect.TypeToken;
import com.microsoft.rest.http.ContentType;
import com.microsoft.rest.http.HttpHeaders;
import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.http.HttpClient;
import com.microsoft.rest.http.HttpHeader;
import com.microsoft.rest.http.HttpRequest;
import com.microsoft.rest.http.HttpResponse;
import com.microsoft.rest.http.UrlBuilder;
import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

import java.io.ByteArrayInputStream;
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
     * Use this RestProxy's serializer to deserialize the provided String into the provided Type.
     * @param value The String value to deserialize.
     * @param resultType The Type of the object to return.
     * @param <T> The type of the object to return. This should be the same as the resultType parameter.
     * @return The deserialized version of the provided String value.
     * @throws IOException if there is an error deserializing.
     */
    public <T> T deserialize(String value, Type resultType) throws IOException {
        return serializer.deserialize(value, resultType);
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

        final Single<HttpResponse> asyncResponse = sendHttpRequestAsync(request);

        final Type returnType = methodParser.returnType();
        return handleAsyncHttpResponse(request, asyncResponse, methodParser, returnType);
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
                final String bodyContentString = serializer.serialize(bodyContentObject, bodyEncoding(request.headers()));
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
                final String bodyContentString = serializer.serialize(bodyContentObject, bodyEncoding(request.headers()));
                request.withBody(bodyContentString, contentType);
            }
        }

        return request;
    }

    private Exception instantiateUnexpectedException(SwaggerMethodParser methodParser, HttpResponse response, String responseContent) {
        final int responseStatusCode = response.statusCode();
        final Class<? extends RestException> exceptionType = methodParser.exceptionType();
        final Class<?> exceptionBodyType = methodParser.exceptionBodyType();

        Exception result;
        try {
            final Constructor<? extends RestException> exceptionConstructor = exceptionType.getConstructor(String.class, HttpResponse.class, exceptionBodyType);
            final Object exceptionBody = responseContent == null || responseContent.isEmpty() ? null : serializer.deserialize(responseContent, exceptionBodyType, bodyEncoding(response.headers()));

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

    Single<HttpResponse> ensureExpectedStatus(Single<HttpResponse> asyncResponse, final SwaggerMethodParser methodParser) {
        return asyncResponse
                .flatMap(new Func1<HttpResponse, Single<? extends HttpResponse>>() {
                    @Override
                    public Single<? extends HttpResponse> call(HttpResponse httpResponse) {
                        return ensureExpectedStatus(httpResponse, methodParser);
                    }
                });
    }

    Single<HttpResponse> ensureExpectedStatus(final HttpResponse response, final SwaggerMethodParser methodParser) {
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

    private Single<?> toProxyReturnValueAsync(final HttpResponse response, final String httpMethod, final Type entityType) {
        final TypeToken entityTypeToken = TypeToken.of(entityType);
        final int responseStatusCode = response.statusCode();
        final Single<?> asyncResult;
        if (entityTypeToken.isSubtypeOf(void.class) || entityTypeToken.isSubtypeOf(Void.class)) {
            asyncResult = Single.just(null);
        } else if (httpMethod.equalsIgnoreCase("HEAD")) {
            if (entityTypeToken.isSubtypeOf(boolean.class) || entityTypeToken.isSubtypeOf(Boolean.class)) {
                boolean isSuccess = (responseStatusCode / 100) == 2;
                asyncResult = Single.just(isSuccess);
            }
            else {
                asyncResult = Single.just(null);
            }
        } else if (entityTypeToken.isSubtypeOf(InputStream.class)) {
            asyncResult = response.bodyAsInputStreamAsync();
        } else if (entityTypeToken.isSubtypeOf(byte[].class)) {
            asyncResult = response.bodyAsByteArrayAsync();
        } else {
            asyncResult = response
                    .bodyAsStringAsync()
                    .flatMap(new Func1<String, Single<Object>>() {
                        @Override
                        public Single<Object> call(String responseBodyString) {
                                try {
                                    return Single.just(toProxyReturnValue(response, responseBodyString, httpMethod, entityType));
                                } catch (Throwable e) {
                                    return Single.error(e);
                                }
                            }
                        });
        }
        return asyncResult;
    }

    private SerializerAdapter.Encoding bodyEncoding(HttpHeaders headers) {
        String mimeContentType = headers.value("Content-Type");
        if (mimeContentType != null) {
            String[] parts = mimeContentType.split(";");
            if (parts[0].equalsIgnoreCase("application/xml") || parts[0].equalsIgnoreCase("text/xml")) {
                return SerializerAdapter.Encoding.XML;
            }
        }

        return SerializerAdapter.Encoding.JSON;
    }

    private Object toProxyReturnValue(HttpResponse httpResponse, String httpResponseBody, String httpMethod, Type entityType) throws IOException {
        final TypeToken entityTypeToken = TypeToken.of(entityType);
        Object result = null;
        if (entityTypeToken.isSubtypeOf(void.class) || entityTypeToken.isSubtypeOf(Void.class)) {
            result = null;
        } else if (httpMethod.equalsIgnoreCase("HEAD")
                && (entityTypeToken.isSubtypeOf(boolean.class) || entityTypeToken.isSubtypeOf(Boolean.class))) {
            result = httpResponse.statusCode() / 100 == 2;
        } else if (entityTypeToken.isSubtypeOf(InputStream.class)) {
            result = new ByteArrayInputStream(httpResponseBody.getBytes());
        } else if (entityTypeToken.isSubtypeOf(byte[].class)) {
            result = httpResponseBody.getBytes();
        } else {
            if (httpResponseBody != null && !httpResponseBody.isEmpty()) {
                result = serializer.deserialize(httpResponseBody, entityType, bodyEncoding(httpResponse.headers()));
            }
        }
        return result;
    }

    protected Object handleAsyncHttpResponse(HttpRequest httpRequest, Single<HttpResponse> asyncHttpResponse, SwaggerMethodParser methodParser, Type returnType) {
        return handleAsyncHttpResponseInner(httpRequest, asyncHttpResponse, methodParser, returnType);
    }

    /**
     * Handle the provided asynchronous HTTP response and return the deserialized value.
     * @param httpRequest The original HTTP request.
     * @param asyncHttpResponse The asynchronous HTTP response to the original HTTP request.
     * @param methodParser The SwaggerMethodParser that the request originates from.
     * @param returnType The type of value that will be returned.
     * @return The deserialized result.
     */
    public final Object handleAsyncHttpResponseInner(HttpRequest httpRequest, Single<HttpResponse> asyncHttpResponse, final SwaggerMethodParser methodParser, final Type returnType) {
        Object result;

        final TypeToken returnTypeToken = TypeToken.of(returnType);

        final Single<HttpResponse> asyncExpectedResponse = ensureExpectedStatus(asyncHttpResponse, methodParser);

        if (returnTypeToken.isSubtypeOf(Completable.class)) {
            result = Completable.fromSingle(asyncExpectedResponse);
        }
        else if (returnTypeToken.isSubtypeOf(Single.class)) {
            final Type singleTypeParam = ((ParameterizedType) returnType).getActualTypeArguments()[0];
            result = asyncExpectedResponse.flatMap(new Func1<HttpResponse, Single<?>>() {
                @Override
                public Single<?> call(HttpResponse response) {
                    return toProxyReturnValueAsync(response, methodParser.httpMethod(), singleTypeParam);
                }
            });
        }
        else if (returnTypeToken.isSubtypeOf(Observable.class)) {
            throw new InvalidReturnTypeException("RestProxy does not support swagger interface methods (such as " + methodParser.fullyQualifiedMethodName() + "()) with a return type of " + returnType.toString());
        }
        else {
            // The return value is not an asynchronous type (Completable, Single, or Observable), so
            // block the deserialization until a value is received.
            result = asyncExpectedResponse
                    .flatMap(new Func1<HttpResponse, Single<?>>() {
                        @Override
                        public Single<?> call(HttpResponse httpResponse) {
                            return toProxyReturnValueAsync(httpResponse, methodParser.httpMethod(), returnType);
                        }
                    })
                    .toBlocking().value();
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