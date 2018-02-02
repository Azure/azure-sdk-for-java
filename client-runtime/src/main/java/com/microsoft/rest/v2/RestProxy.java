/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.google.common.reflect.TypeToken;
import com.microsoft.rest.v2.credentials.ServiceClientCredentials;
import com.microsoft.rest.v2.http.ContentType;
import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpMethod;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpPipelineBuilder;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.UrlBuilder;
import com.microsoft.rest.v2.policy.CookiePolicyFactory;
import com.microsoft.rest.v2.policy.CredentialsPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RetryPolicyFactory;
import com.microsoft.rest.v2.policy.UserAgentPolicyFactory;
import com.microsoft.rest.v2.protocol.HttpResponseDecoder;
import com.microsoft.rest.v2.protocol.SerializerAdapter;
import com.microsoft.rest.v2.protocol.SerializerEncoding;
import com.microsoft.rest.v2.serializer.JacksonAdapter;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;
import org.reactivestreams.Publisher;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * This class can be used to create a proxy implementation for a provided Swagger generated
 * interface. RestProxy can create proxy implementations for interfaces with methods that return
 * deserialized Java objects as well as asynchronous Single objects that resolve to a deserialized
 * Java object.
 */
public class RestProxy implements InvocationHandler {
    private final HttpPipeline httpPipeline;
    private final SerializerAdapter<?> serializer;
    private final SwaggerInterfaceParser interfaceParser;

    /**
     * Create a new instance of RestProxy.
     * @param httpPipeline The RequestPolicy and HttpClient httpPipeline that will be used to send HTTP
     *                 requests.
     * @param serializer The serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser The parser that contains information about the swagger interface that
     *                        this RestProxy "implements".
     */
    public RestProxy(HttpPipeline httpPipeline, SerializerAdapter<?> serializer, SwaggerInterfaceParser interfaceParser) {
        this.httpPipeline = httpPipeline;
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
    public SerializerAdapter<?> serializer() {
        return serializer;
    }

    /**
     * Send the provided request asynchronously, applying any request policies provided to the HttpClient instance.
     * @param request The HTTP request to send.
     * @return A {@link Single} representing the HTTP response that will arrive asynchronously.
     */
    public Single<HttpResponse> sendHttpRequestAsync(HttpRequest request) {
        return httpPipeline.sendRequestAsync(request);
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) {
        try {
            final SwaggerMethodParser methodParser = methodParser(method);

            final HttpRequest request = createHttpRequest(methodParser, args);

            final Single<HttpResponse> asyncResponse = sendHttpRequestAsync(request);

            final Type returnType = methodParser.returnType();
            return handleAsyncHttpResponse(request, asyncResponse, methodParser, returnType);
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }

    /**
     * Create a HttpRequest for the provided Swagger method using the provided arguments.
     * @param methodParser The Swagger method parser to use.
     * @param args The arguments to use to populate the method's annotation values.
     * @return A HttpRequest.
     * @throws IOException Thrown if the body contents cannot be serialized.
     */
    @SuppressWarnings("unchecked")
    private HttpRequest createHttpRequest(SwaggerMethodParser methodParser, Object[] args) throws IOException {
        UrlBuilder urlBuilder;

        // Sometimes people pass in a full URL for the value of their PathParam annotated argument.
        // This definitely happens in paging scenarios. In that case, just use the full URL and
        // ignore the Host annotation.
        final String path = methodParser.path(args);
        final UrlBuilder pathUrlBuilder = UrlBuilder.parse(path);
        if (pathUrlBuilder.scheme() != null) {
            urlBuilder = pathUrlBuilder;
        }
        else {
            urlBuilder = new UrlBuilder();

            // We add path to the UrlBuilder first because this is what is
            // provided to the HTTP Method annotation. Any path substitutions
            // from other substitution annotations will overwrite this.
            urlBuilder.withPath(path);

            final String scheme = methodParser.scheme(args);
            urlBuilder.withScheme(scheme);

            final String host = methodParser.host(args);
            urlBuilder.withHost(host);
        }

        for (final EncodedParameter queryParameter : methodParser.encodedQueryParameters(args)) {
            urlBuilder.addQueryParameter(queryParameter.name(), queryParameter.encodedValue());
        }

        final URL url = urlBuilder.toURL();
        final HttpRequest request = new HttpRequest(methodParser.fullyQualifiedMethodName(), methodParser.httpMethod(), url, new HttpResponseDecoder(methodParser, serializer));

        final Object bodyContentObject = methodParser.body(args);
        if (bodyContentObject == null) {
            request.headers().set("Content-Length", "0");
        } else {
            String contentType = methodParser.bodyContentType();
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
                final String bodyContentString = serializer.serialize(bodyContentObject, SerializerEncoding.JSON);
                request.withBody(bodyContentString);
            }
            else if (isFlowableByteArray(TypeToken.of(methodParser.bodyJavaType()))) {
                // Content-Length or Transfer-Encoding: chunked must be provided by a user-specified header when a Flowable<byte[]> is given for the body.
                //noinspection ConstantConditions
                request.withBody((Flowable<byte[]>) bodyContentObject);
            }
            else if (bodyContentObject instanceof byte[]) {
                request.withBody((byte[]) bodyContentObject);
            }
            else if (bodyContentObject instanceof String) {
                final String bodyContentString = (String) bodyContentObject;
                if (!bodyContentString.isEmpty()) {
                    request.withBody(bodyContentString);
                }
            }
            else {
                final String bodyContentString = serializer.serialize(bodyContentObject, SerializerEncoding.fromHeaders(request.headers()));
                request.withBody(bodyContentString);
            }
        }

        // Headers from Swagger method arguments always take precedence over inferred headers from body types
        for (final HttpHeader header : methodParser.headers(args)) {
            request.withHeader(header.name(), header.value());
        }

        return request;
    }

    private Exception instantiateUnexpectedException(SwaggerMethodParser methodParser, HttpResponse response, String responseContent) {
        final int responseStatusCode = response.statusCode();
        final Class<? extends RestException> exceptionType = methodParser.exceptionType();
        final Class<?> exceptionBodyType = methodParser.exceptionBodyType();

        String contentType = response.headerValue("Content-Type");
        String bodyRepresentation;
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            bodyRepresentation = "(" + response.headerValue("Content-Length") + "-byte body)";
        } else {
            bodyRepresentation = responseContent.isEmpty() ? "(empty body)" : "\"" + responseContent + "\"";
        }

        Exception result;
        try {
            final Constructor<? extends RestException> exceptionConstructor = exceptionType.getConstructor(String.class, HttpResponse.class, exceptionBodyType);
            result = exceptionConstructor.newInstance("Status code " + responseStatusCode + ", " + bodyRepresentation, response, response.deserializedBody());
        } catch (ReflectiveOperationException e) {
            String message = "Status code " + responseStatusCode + ", but an instance of "
                    + exceptionType.getCanonicalName() + " cannot be created."
                    + " Response body: " + bodyRepresentation;

            result = new IOException(message, e);
        }

        return result;
    }

    private Single<HttpResponse> ensureExpectedStatus(Single<HttpResponse> asyncResponse, final SwaggerMethodParser methodParser) {
        return asyncResponse
                .flatMap(new Function<HttpResponse, Single<? extends HttpResponse>>() {
                    @Override
                    public Single<? extends HttpResponse> apply(HttpResponse httpResponse) {
                        return ensureExpectedStatus(httpResponse, methodParser);
                    }
                });
    }

    private Single<HttpResponse> ensureExpectedStatus(final HttpResponse response, final SwaggerMethodParser methodParser) {
        return ensureExpectedStatus(response, methodParser, null);
    }

    /**
     * Ensure that the provided HttpResponse has a status code that is defined in the provided
     * SwaggerMethodParser or is in the int[] of additional allowed status codes. If the
     * HttpResponse's status code is not allowed, then an exception will be thrown.
     * @param response The HttpResponse to check.
     * @param methodParser The method parser that contains information about the service interface
     *                     method that initiated the HTTP request.
     * @param additionalAllowedStatusCodes Additional allowed status codes that are permitted based
     *                                     on the context of the HTTP request.
     * @return An async-version of the provided HttpResponse.
     */
    public Single<HttpResponse> ensureExpectedStatus(final HttpResponse response, final SwaggerMethodParser methodParser, int[] additionalAllowedStatusCodes) {
        final int responseStatusCode = response.statusCode();
        final Single<HttpResponse> asyncResult;
        if (!methodParser.isExpectedResponseStatusCode(responseStatusCode, additionalAllowedStatusCodes)) {
            asyncResult = response.bodyAsStringAsync().flatMap(new Function<String, Single<HttpResponse>>() {
                @Override
                public Single<HttpResponse> apply(String responseBody) throws Exception {
                    return Single.error(instantiateUnexpectedException(methodParser, response, responseBody));
                }
            });
        } else {
            asyncResult = Single.just(response);
        }

        return asyncResult;
    }

    private Single<?> handleRestResponseReturnTypeAsync(HttpResponse response, SwaggerMethodParser methodParser, Type entityType) throws IOException {
        final TypeToken entityTypeToken = TypeToken.of(entityType);
        final int responseStatusCode = response.statusCode();

        final Single<?> asyncResult;
        if (entityTypeToken.isSubtypeOf(RestResponse.class)) {
            final Type[] deserializedTypes = getTypeArguments(entityType);
            final Type bodyType = deserializedTypes[1];
            final HttpHeaders responseHeaders = response.headers();
            final Object deserializedHeaders = response.deserializedHeaders();

            final TypeToken bodyTypeToken = TypeToken.of(bodyType);
            if (bodyTypeToken.isSubtypeOf(Void.class)) {
                asyncResult = Single.just(new RestResponse<>(responseStatusCode, deserializedHeaders, responseHeaders.toMap(), null));
            } else {
                final Map<String, String> rawHeaders = responseHeaders.toMap();

                asyncResult = handleBodyReturnTypeAsync(response, methodParser, bodyType)
                        .map(new Function<Object, RestResponse<?, ?>>() {
                            @Override
                            public RestResponse<?, ?> apply(Object body) {
                                return new RestResponse<>(responseStatusCode, deserializedHeaders, rawHeaders, body);
                            }
                        }).toSingle(new RestResponse<>(responseStatusCode, deserializedHeaders, rawHeaders, null));
            }
        } else {
            // For now we're just throwing if the Maybe didn't emit a value.
            asyncResult = handleBodyReturnTypeAsync(response, methodParser, entityType).toSingle();
        }
        return asyncResult;
    }

    private boolean isFlowableByteArray(TypeToken entityTypeToken) {
        if (entityTypeToken.isSubtypeOf(Flowable.class)) {
            final Type innerType = ((ParameterizedType) entityTypeToken.getType()).getActualTypeArguments()[0];
            final TypeToken innerTypeToken = TypeToken.of(innerType);
            if (innerTypeToken.isSubtypeOf(byte[].class)) {
                return true;
            }
        }
        return false;
    }

    protected final Maybe<?> handleBodyReturnTypeAsync(final HttpResponse response, final SwaggerMethodParser methodParser, final Type entityType) {
        final TypeToken entityTypeToken = TypeToken.of(entityType);
        final int responseStatusCode = response.statusCode();
        final HttpMethod httpMethod = methodParser.httpMethod();
        final Type returnValueWireType = methodParser.returnValueWireType();

        final Maybe<?> asyncResult;
        if (httpMethod == HttpMethod.HEAD
                && (entityTypeToken.isSubtypeOf(boolean.class) || entityTypeToken.isSubtypeOf(Boolean.class))) {
            boolean isSuccess = (responseStatusCode / 100) == 2;
            asyncResult = Maybe.just(isSuccess);
        } else if (entityTypeToken.isSubtypeOf(byte[].class)) {
            Maybe<byte[]> responseBodyBytesAsync = response.bodyAsByteArrayAsync().toMaybe();
            if (returnValueWireType == Base64Url.class) {
                responseBodyBytesAsync = responseBodyBytesAsync.map(new Function<byte[], byte[]>() {
                    @Override
                    public byte[] apply(byte[] base64UrlBytes) {
                        return new Base64Url(base64UrlBytes).decodedBytes();
                    }
                });
            }
            asyncResult = responseBodyBytesAsync;
        } else if (isFlowableByteArray(entityTypeToken)) {
            asyncResult = Maybe.just(response.streamBodyAsync());
        } else {
            Object result = response.deserializedBody();
            if (result == null) {
                asyncResult = Maybe.empty();
            } else {
                asyncResult = Maybe.just(result);
            }
        }

        return asyncResult;
    }

    protected Object handleAsyncHttpResponse(HttpRequest httpRequest, Single<HttpResponse> asyncHttpResponse, SwaggerMethodParser methodParser, Type returnType) {
        return handleRestReturnType(httpRequest, asyncHttpResponse, methodParser, returnType);
    }

    private static final Function<Throwable, Single<?>> WARN_MISSING_DECODING = new Function<Throwable, Single<?>>() {
        @Override
        public Single<?> apply(Throwable throwable) throws Exception {
            if (throwable instanceof NoSuchElementException) {
                return Single.error(new IllegalStateException("No decoded response body was found. DecodingPolicyFactory may be missing from the pipeline.", throwable));
            } else {
                return Single.error(throwable);
            }
        }
    };

    /**
     * Handle the provided asynchronous HTTP response and return the deserialized value.
     * @param httpRequest The original HTTP request.
     * @param asyncHttpResponse The asynchronous HTTP response to the original HTTP request.
     * @param methodParser The SwaggerMethodParser that the request originates from.
     * @param returnType The type of value that will be returned.
     * @return The deserialized result.
     */
    public final Object handleRestReturnType(HttpRequest httpRequest, Single<HttpResponse> asyncHttpResponse, final SwaggerMethodParser methodParser, final Type returnType) {
        Object result;

        final TypeToken returnTypeToken = TypeToken.of(returnType);

        final Single<HttpResponse> asyncExpectedResponse = ensureExpectedStatus(asyncHttpResponse, methodParser);

        if (returnTypeToken.isSubtypeOf(Completable.class)) {
            result = Completable.fromSingle(asyncExpectedResponse);
        }
        else if (returnTypeToken.isSubtypeOf(Single.class)) {
            final Type singleTypeParam = getTypeArgument(returnType);
            result = asyncExpectedResponse.flatMap(new Function<HttpResponse, Single<?>>() {
                @Override
                public Single<?> apply(HttpResponse response) throws Exception {
                    return handleRestResponseReturnTypeAsync(response, methodParser, singleTypeParam);
                }
            }).onErrorResumeNext(WARN_MISSING_DECODING);
        }
        else if (returnTypeToken.isSubtypeOf(Observable.class)) {
            throw new InvalidReturnTypeException("RestProxy does not support swagger interface methods (such as " + methodParser.fullyQualifiedMethodName() + "()) with a return type of " + returnType.toString());
        }
        else if (isFlowableByteArray(returnTypeToken)) {
            result = asyncExpectedResponse.flatMapPublisher(new Function<HttpResponse, Publisher<?>>() {
                @Override
                public Publisher<?> apply(HttpResponse httpResponse) throws Exception {
                    return httpResponse.streamBodyAsync();
                }
            });
        }
        else if (returnTypeToken.isSubtypeOf(void.class) || returnTypeToken.isSubtypeOf(Void.class)) {
            asyncExpectedResponse.blockingGet();
            result = null;
        } else {
            // The return value is not an asynchronous type (Completable, Single, or Observable), so
            // block the deserialization until a value is received.
            result = asyncExpectedResponse
                    .flatMap(new Function<HttpResponse, Single<?>>() {
                        @Override
                        public Single<?> apply(HttpResponse httpResponse) throws Exception {
                            return handleRestResponseReturnTypeAsync(httpResponse, methodParser, returnType);
                        }
                    }).onErrorResumeNext(WARN_MISSING_DECODING).blockingGet();
        }

        return result;
    }

    private static Type[] getTypeArguments(Type type) {
        return ((ParameterizedType) type).getActualTypeArguments();
    }

    private static Type getTypeArgument(Type type) {
        return getTypeArguments(type)[0];
    }

    /**
     * Create an instance of the default serializer.
     * @return the default serializer.
     */
    public static SerializerAdapter<?> createDefaultSerializer() {
        return new JacksonAdapter();
    }

    /**
     * Create the default HttpPipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline() {
        return createDefaultPipeline((RequestPolicyFactory) null);
    }

    /**
     * Create the default HttpPipeline.
     * @param credentials The credentials to use to apply authentication to the pipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(ServiceClientCredentials credentials) {
        return createDefaultPipeline(new CredentialsPolicyFactory(credentials));
    }

    /**
     * Create the default HttpPipeline.
     * @param credentialsPolicy The credentials policy factory to use to apply authentication to the
     *                          pipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(RequestPolicyFactory credentialsPolicy) {
        final HttpPipelineBuilder builder = new HttpPipelineBuilder();
        builder.withRequestPolicy(new UserAgentPolicyFactory());
        builder.withRequestPolicy(new RetryPolicyFactory());
        builder.withRequestPolicy(new CookiePolicyFactory());
        if (credentialsPolicy != null) {
            builder.withRequestPolicy(credentialsPolicy);
        }
        return builder.build();
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface) {
        return create(swaggerInterface, createDefaultPipeline(), createDefaultSerializer());
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param httpPipeline The RequestPolicy and HttpClient pipline that will be used to send Http
     *                 requests.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline) {
        return create(swaggerInterface, httpPipeline, createDefaultSerializer());
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param serviceClient The ServiceClient that contains the details to use to create the
     *                      RestProxy implementation of the swagger interface.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, ServiceClient serviceClient) {
        return create(swaggerInterface, serviceClient.httpPipeline(), serviceClient.serializerAdapter());
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     * @param swaggerInterface The Swagger interface to provide a proxy implementation for.
     * @param httpPipeline The RequestPolicy and HttpClient pipline that will be used to send Http
     *                 requests.
     * @param serializer The serializer that will be used to convert POJOs to and from request and
     *                   response bodies.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, SerializerAdapter<?> serializer) {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface);
        final RestProxy restProxy = new RestProxy(httpPipeline, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, restProxy);
    }
}