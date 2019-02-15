/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3;

import com.microsoft.rest.v3.annotations.ResumeOperation;
import com.microsoft.rest.v3.credentials.ServiceClientCredentials;
import com.microsoft.rest.v3.http.ContentType;
import com.microsoft.rest.v3.http.ContextData;
import com.microsoft.rest.v3.http.HttpHeader;
import com.microsoft.rest.v3.http.HttpHeaders;
import com.microsoft.rest.v3.http.HttpMethod;
import com.microsoft.rest.v3.http.HttpPipeline;
import com.microsoft.rest.v3.http.policy.HttpPipelinePolicy;
import com.microsoft.rest.v3.http.HttpRequest;
import com.microsoft.rest.v3.http.HttpResponse;
import com.microsoft.rest.v3.http.UrlBuilder;
import com.microsoft.rest.v3.http.policy.CookiePolicy;
import com.microsoft.rest.v3.http.policy.CredentialsPolicy;
import com.microsoft.rest.v3.http.policy.DecodingPolicy;
import com.microsoft.rest.v3.http.HttpPipelineOptions;
import com.microsoft.rest.v3.http.policy.RetryPolicy;
import com.microsoft.rest.v3.http.policy.UserAgentPolicy;
import com.microsoft.rest.v3.protocol.HttpResponseDecoder;
import com.microsoft.rest.v3.protocol.SerializerAdapter;
import com.microsoft.rest.v3.protocol.SerializerEncoding;
import com.microsoft.rest.v3.serializer.JacksonAdapter;
import com.microsoft.rest.v3.util.FluxUtil;
import com.microsoft.rest.v3.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class can be used to create a proxy implementation for a provided Swagger generated
 * interface. RestProxy can create proxy implementations for interfaces with methods that return
 * deserialized Java objects as well as asynchronous Single objects that resolve to a deserialized
 * Java object.
 */
public class RestProxy implements InvocationHandler {
    private final HttpPipeline httpPipeline;
    private final SerializerAdapter serializer;
    private final SwaggerInterfaceParser interfaceParser;

    /**
     * Create a new instance of RestProxy.
     * @param httpPipeline The HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP
     *                 requests.
     * @param serializer The serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser The parser that contains information about the swagger interface that
     *                        this RestProxy "implements".
     */
    public RestProxy(HttpPipeline httpPipeline, SerializerAdapter serializer, SwaggerInterfaceParser interfaceParser) {
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
    public SerializerAdapter serializer() {
        return serializer;
    }

    /**
     * Send the provided request asynchronously, applying any request policies provided to the HttpClient instance.
     *
     * @param request the HTTP request to send
     * @param contextData the context
     * @return a {@link Mono} that emits HttpResponse asynchronously
     */
    public Mono<HttpResponse> sendHttpRequestAsync(HttpRequest request, ContextData contextData) {
        return httpPipeline.send(httpPipeline.newContext(request, contextData));
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) {
        try {
            SwaggerMethodParser methodParser = null;
            HttpRequest request = null;
            if (method.isAnnotationPresent(ResumeOperation.class)) {
                OperationDescription opDesc = (OperationDescription) args[0];
                Method resumeMethod = null;
                Method[] methods = method.getDeclaringClass().getMethods();
                for (Method origMethod : methods) {
                    if (origMethod.getName().equals(opDesc.methodName())) {
                        resumeMethod = origMethod;
                        break;
                    }
                }

                methodParser = methodParser(resumeMethod);
                request = createHttpRequest(opDesc, methodParser, args);
                final Type returnType = methodParser.returnType();
                return handleResumeOperation(request, opDesc, methodParser, returnType);

            } else {
                methodParser = methodParser(method);
                request = createHttpRequest(methodParser, args);
                final Mono<HttpResponse> asyncResponse = sendHttpRequestAsync(request, methodParser.contextData(args).addData("caller-method", methodParser.fullyQualifiedMethodName()));
                final Type returnType = methodParser.returnType();
                return handleAsyncHttpResponse(request, asyncResponse, methodParser, returnType);
            }

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
            urlBuilder.setQueryParameter(queryParameter.name(), queryParameter.encodedValue());
        }

        final URL url = urlBuilder.toURL();
        final HttpRequest request = new HttpRequest(methodParser.httpMethod(), url, new HttpResponseDecoder(methodParser, serializer));

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
            } else if (FluxUtil.isFluxByteBuf(methodParser.bodyJavaType())) {
                // Content-Length or Transfer-Encoding: chunked must be provided by a user-specified header when a Flowable<byte[]> is given for the body.
                //noinspection ConstantConditions
                request.withBody((Flux<ByteBuf>) bodyContentObject);
            } else if (bodyContentObject instanceof byte[]) {
                request.withBody((byte[]) bodyContentObject);
            } else if (bodyContentObject instanceof String) {
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

    /**
     * Create a HttpRequest for the provided Swagger method using the provided arguments.
     * @param methodParser The Swagger method parser to use.
     * @param args The arguments to use to populate the method's annotation values.
     * @return A HttpRequest.
     * @throws IOException Thrown if the body contents cannot be serialized.
     */
    @SuppressWarnings("unchecked")
    private HttpRequest createHttpRequest(OperationDescription operationDescription, SwaggerMethodParser methodParser, Object[] args) throws IOException {
        final HttpRequest request = new HttpRequest(
                methodParser.httpMethod(),
                operationDescription.url(),
                new HttpResponseDecoder(methodParser, serializer));

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
            else if (FluxUtil.isFluxByteBuf(methodParser.bodyJavaType())) {
                // Content-Length or Transfer-Encoding: chunked must be provided by a user-specified header when a Flowable<byte[]> is given for the body.
                //noinspection ConstantConditions
                request.withBody((Flux<ByteBuf>) bodyContentObject);
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
        for (final String headerName : operationDescription.headers().keySet()) {
            request.withHeader(headerName, operationDescription.headers().get(headerName));
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

    private Mono<HttpResponse> ensureExpectedStatus(Mono<HttpResponse> asyncResponse, final SwaggerMethodParser methodParser) {
        return asyncResponse
                .flatMap(httpResponse -> ensureExpectedStatus(httpResponse, methodParser));
    }

    private Mono<HttpResponse> ensureExpectedStatus(final HttpResponse response, final SwaggerMethodParser methodParser) {
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
    public Mono<HttpResponse> ensureExpectedStatus(final HttpResponse response, final SwaggerMethodParser methodParser, int[] additionalAllowedStatusCodes) {
        final int responseStatusCode = response.statusCode();
        final Mono<HttpResponse> asyncResult;
        if (!methodParser.isExpectedResponseStatusCode(responseStatusCode, additionalAllowedStatusCodes)) {
            asyncResult = response.bodyAsString().flatMap((Function<String, Mono<HttpResponse>>) responseBody -> Mono.error(instantiateUnexpectedException(methodParser, response, responseBody)))
                    .switchIfEmpty(Mono.defer((Supplier<Mono<HttpResponse>>) () -> Mono.error(instantiateUnexpectedException(methodParser, response, ""))));
        } else {
            asyncResult = Mono.just(response);
        }
        return asyncResult;
    }

    /**
     * @param entityType the RestResponse subtype to get a constructor for.
     * @return a Constructor which produces an instance of a RestResponse subtype.
     */
    @SuppressWarnings("unchecked")
    public Constructor<? extends RestResponse<?, ?>> getRestResponseConstructor(Type entityType) {
        Class<? extends RestResponse<?, ?>> rawEntityType = (Class<? extends RestResponse<?, ?>>) TypeUtil.getRawClass(entityType);
        try {
            Constructor<? extends RestResponse<?, ?>> ctor = null;
            for (Constructor<?> c : rawEntityType.getDeclaredConstructors()) {
                // Generic constructor arguments turn into Object.
                // Because some child class constructors have a more specific concrete type,
                // there's not a single type we can check for the headers or body parameters.
                if (c.getParameterTypes().length == 5
                        && c.getParameterTypes()[0].equals(HttpRequest.class)
                        && c.getParameterTypes()[1].equals(Integer.TYPE)
                        && c.getParameterTypes()[3].equals(Map.class)) {
                    ctor = (Constructor<? extends RestResponse<?, ?>>) c;
                }
            }
            if (ctor == null) {
                throw new NoSuchMethodException("No appropriate constructor found for type " + rawEntityType.getName());
            }
            return ctor;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private Mono<?> handleRestResponseReturnTypeAsync(HttpResponse response, SwaggerMethodParser methodParser, Type entityType) {
        final int responseStatusCode = response.statusCode();

        try {
            Mono<?> asyncResult;
            if (TypeUtil.isTypeOrSubTypeOf(entityType, RestResponse.class)) {
                Constructor<? extends RestResponse<?, ?>> responseConstructor = getRestResponseConstructor(entityType);

                Type[] deserializedTypes = TypeUtil.getTypeArguments(TypeUtil.getSuperType(entityType, RestResponse.class));

                HttpHeaders responseHeaders = response.headers();
                Object deserializedHeaders = response.deserializedHeaders();

                Type bodyType = deserializedTypes[1];
                if (TypeUtil.isTypeOrSubTypeOf(bodyType, Void.class)) {
                    asyncResult = response.body().ignoreElements()
                            .then(Mono.just(responseConstructor.newInstance(response.request(), responseStatusCode, deserializedHeaders, responseHeaders.toMap(), null)));
                } else {
                    final Map<String, String> rawHeaders = responseHeaders.toMap();
                    //
                    asyncResult = handleBodyReturnTypeAsync(response, methodParser, bodyType)
                            .map((Function<Object, RestResponse<?, ?>>) bodyAsObject -> {
                                try {
                                    return responseConstructor.newInstance(response.request(), responseStatusCode, deserializedHeaders, rawHeaders, bodyAsObject);
                                } catch (IllegalAccessException iae) {
                                    throw reactor.core.Exceptions.propagate(iae);
                                } catch (InvocationTargetException ite) {
                                    throw reactor.core.Exceptions.propagate(ite);
                                } catch (InstantiationException ie) {
                                    throw reactor.core.Exceptions.propagate(ie);
                                }
                            })
                            .switchIfEmpty(Mono.defer((Supplier<Mono<RestResponse<?, ?>>>) () -> {
                                try {
                                return Mono.just(responseConstructor.newInstance(response.request(), responseStatusCode, deserializedHeaders, rawHeaders, null));
                                } catch (IllegalAccessException iae) {
                                    throw reactor.core.Exceptions.propagate(iae);
                                } catch (InvocationTargetException ite) {
                                    throw reactor.core.Exceptions.propagate(ite);
                                } catch (InstantiationException ie) {
                                    throw reactor.core.Exceptions.propagate(ie);
                                }
                            }));
                }

                Type headersType = deserializedTypes[0];
                if (!response.isDecoded() && !TypeUtil.isTypeOrSubTypeOf(headersType, Void.class)) {
                    asyncResult = asyncResult.then(Mono.error(new RestException(
                            "No deserialized headers were found. Please add a DecodingPolicy to the HttpPipeline.",
                            response,
                            (Object) null)));
                }
            } else {
                // For now we're just throwing if the Maybe didn't emit a value.
                asyncResult = handleBodyReturnTypeAsync(response, methodParser, entityType);
            }

            return asyncResult;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    protected final Mono<?> handleBodyReturnTypeAsync(final HttpResponse response, final SwaggerMethodParser methodParser, final Type entityType) {
        final int responseStatusCode = response.statusCode();
        final HttpMethod httpMethod = methodParser.httpMethod();
        final Type returnValueWireType = methodParser.returnValueWireType();

        final Mono<?> asyncResult;
        if (httpMethod == HttpMethod.HEAD
                && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE) || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            boolean isSuccess = (responseStatusCode / 100) == 2;
            asyncResult = Mono.just(isSuccess);
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            Mono<byte[]> responseBodyBytesAsync = response.bodyAsByteArray();
            if (returnValueWireType == Base64Url.class) {
                responseBodyBytesAsync = responseBodyBytesAsync.map(base64UrlBytes -> new Base64Url(base64UrlBytes).decodedBytes());
            }
            asyncResult = responseBodyBytesAsync;
        } else if (FluxUtil.isFluxByteBuf(entityType)) {
            asyncResult = Mono.just(response.body());
        } else if (!response.isDecoded()) {
            asyncResult = Mono.error(new RestException(
                    "No deserialized response body was found. Please add a DecodingPolicy to the HttpPipeline.",
                    response,
                    (Object) null));
        } else {
            Object result = response.deserializedBody();
            if (result == null) {
                asyncResult = Mono.empty();
            } else {
                asyncResult = Mono.just(result);
            }
        }
        return asyncResult;
    }

    protected Object handleAsyncHttpResponse(HttpRequest httpRequest, Mono<HttpResponse> asyncHttpResponse, SwaggerMethodParser methodParser, Type returnType) {
        return handleRestReturnType(httpRequest, asyncHttpResponse, methodParser, returnType);
    }

    protected Object handleResumeOperation(HttpRequest httpRequest, OperationDescription operationDescription, SwaggerMethodParser methodParser, Type returnType)
        throws Exception {
        throw new Exception("The resume operation is not avaiable in the base RestProxy class.");
    }

    /**
     * Handle the provided asynchronous HTTP response and return the deserialized value.
     * @param httpRequest The original HTTP request.
     * @param asyncHttpResponse The asynchronous HTTP response to the original HTTP request.
     * @param methodParser The SwaggerMethodParser that the request originates from.
     * @param returnType The type of value that will be returned.
     * @return The deserialized result.
     */
    public final Object handleRestReturnType(HttpRequest httpRequest, Mono<HttpResponse> asyncHttpResponse, final SwaggerMethodParser methodParser, final Type returnType) {
        Object result;

        final Mono<HttpResponse> asyncExpectedResponse = ensureExpectedStatus(asyncHttpResponse, methodParser);

        if (TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class)) {
            final Type monoTypeParam = TypeUtil.getTypeArgument(returnType);
            if (TypeUtil.isTypeOrSubTypeOf(monoTypeParam, Void.class)) {
                // TODO: Generator: For methods that returns Completable today, Generator has to be updated to emit method return type as Mono<Void>
                result = asyncExpectedResponse.then();
            } else {
                result = asyncExpectedResponse.flatMap(response ->
                        handleRestResponseReturnTypeAsync(response, methodParser, monoTypeParam));
            }
        } else if (FluxUtil.isFluxByteBuf(returnType)) {
            result = asyncExpectedResponse.flatMapMany(HttpResponse::body);
        } else if (TypeUtil.isTypeOrSubTypeOf(returnType, void.class) || TypeUtil.isTypeOrSubTypeOf(returnType, Void.class)) {
            asyncExpectedResponse.block();
            result = null;
        } else {
            // The method return value is not an asynchronous type (Mono or Flux) or synchronous void type so
            // block the deserialization until a value is received.
            result = asyncExpectedResponse
                    .flatMap(httpResponse ->
                            handleRestResponseReturnTypeAsync(httpResponse, methodParser, returnType))
                    .block();
        }
        return result;
    }

    /**
     * Create an instance of the default serializer.
     * @return the default serializer.
     */
    public static SerializerAdapter createDefaultSerializer() {
        return new JacksonAdapter();
    }

    /**
     * Create the default HttpPipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline() {
        return createDefaultPipeline((HttpPipelinePolicy) null);
    }

    /**
     * Create the default HttpPipeline.
     * @param credentials The credentials to use to apply authentication to the pipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(ServiceClientCredentials credentials) {
        return createDefaultPipeline(new CredentialsPolicy(credentials));
    }

    /**
     * Create the default HttpPipeline.
     * @param credentialsPolicy The credentials policy factory to use to apply authentication to the
     *                          pipeline.
     * @return the default HttpPipeline.
     */
    public static HttpPipeline createDefaultPipeline(HttpPipelinePolicy credentialsPolicy) {
        List<HttpPipelinePolicy> policies = new ArrayList<HttpPipelinePolicy>();
        policies.add(new UserAgentPolicy());
        policies.add(new RetryPolicy());
        policies.add(new DecodingPolicy());
        policies.add(new CookiePolicy());
        if (credentialsPolicy != null) {
            policies.add(credentialsPolicy);
        }
        return new HttpPipeline(new HttpPipelineOptions(null),
                policies.toArray(new HttpPipelinePolicy[policies.size()]));
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
     * @param httpPipeline The HttpPipelinePolicy and HttpClient pipline that will be used to send Http
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
     * @param httpPipeline The HttpPipelinePolicy and HttpClient pipline that will be used to send Http
     *                 requests.
     * @param serializer The serializer that will be used to convert POJOs to and from request and
     *                   response bodies.
     * @param <A> The type of the Swagger interface.
     * @return A proxy implementation of the provided Swagger interface.
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, SerializerAdapter serializer) {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface, serializer);
        final RestProxy restProxy = new RestProxy(httpPipeline, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, restProxy);
    }
}