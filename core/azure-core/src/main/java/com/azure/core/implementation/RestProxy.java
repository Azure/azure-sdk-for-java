// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.ServiceClient;
import com.azure.core.annotations.ResumeOperation;
import com.azure.core.credentials.ServiceClientCredentials;
import com.azure.core.exception.HttpRequestException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.CredentialsPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.implementation.http.ContentType;
import com.azure.core.implementation.http.PagedResponseBase;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.implementation.serializer.HttpResponseDecoder;
import com.azure.core.implementation.serializer.HttpResponseDecoder.HttpDecodedResponse;
import com.azure.core.implementation.serializer.SerializerAdapter;
import com.azure.core.implementation.serializer.SerializerEncoding;
import com.azure.core.implementation.serializer.jackson.JacksonAdapter;
import com.azure.core.implementation.util.FluxUtil;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.implementation.util.TypeUtil;
import com.azure.core.util.Context;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Type to create a proxy implementation for an interface describing REST API methods.
 *
 * RestProxy can create proxy implementations for interfaces with methods that return
 * deserialized Java objects as well as asynchronous Single objects that resolve to a
 * deserialized Java object.
 */
public class RestProxy implements InvocationHandler {
    private final HttpPipeline httpPipeline;
    private final SerializerAdapter serializer;
    private final SwaggerInterfaceParser interfaceParser;
    private final HttpResponseDecoder decoder;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP
     *                 requests.
     * @param serializer the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods
     *                        that this RestProxy "implements".
     */
    public RestProxy(HttpPipeline httpPipeline, SerializerAdapter serializer, SwaggerInterfaceParser interfaceParser) {
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;
        this.interfaceParser = interfaceParser;
        this.decoder = new HttpResponseDecoder(this.serializer);
    }

    /**
     * Get the SwaggerMethodParser for the provided method. The Method must exist on the Swagger
     * interface that this RestProxy was created to "implement".
     *
     * @param method the method to get a SwaggerMethodParser for
     * @return the SwaggerMethodParser for the provided method
     */
    private SwaggerMethodParser methodParser(Method method) {
        return interfaceParser.methodParser(method);
    }

    /**
     * Get the SerializerAdapter used by this RestProxy.
     *
     * @return The SerializerAdapter used by this RestProxy
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
    public Mono<HttpResponse> send(HttpRequest request, Context contextData) {
        return httpPipeline.send(request, contextData);
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) {
        try {
            final SwaggerMethodParser methodParser;
            final HttpRequest request;
            if (method.isAnnotationPresent(ResumeOperation.class)) {
                OperationDescription opDesc = ImplUtils.findFirstOfType(args, OperationDescription.class);
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
                final Mono<HttpResponse> asyncResponse = send(request, methodParser.context(args).addData("caller-method", methodParser.fullyQualifiedMethodName()));
                //
                Mono<HttpDecodedResponse> asyncDecodedResponse = this.decoder.decode(asyncResponse, methodParser);
                //
                return handleHttpResponse(request, asyncDecodedResponse, methodParser, methodParser.returnType());
            }

        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }

    /**
     * Create a HttpRequest for the provided Swagger method using the provided arguments.
     *
     * @param methodParser the Swagger method parser to use
     * @param args the arguments to use to populate the method's annotation values
     * @return a HttpRequest
     * @throws IOException thrown if the body contents cannot be serialized
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
        } else {
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
        final HttpRequest request = configRequest(new HttpRequest(methodParser.httpMethod(), url), methodParser, args);

        // Headers from Swagger method arguments always take precedence over inferred headers from body types
        for (final HttpHeader header : methodParser.headers(args)) {
            request.withHeader(header.name(), header.value());
        }

        return request;
    }

    /**
     * Create a HttpRequest for the provided Swagger method using the provided arguments.
     *
     * @param methodParser the Swagger method parser to use
     * @param args the arguments to use to populate the method's annotation values
     * @return a HttpRequest
     * @throws IOException thrown if the body contents cannot be serialized
     */
    @SuppressWarnings("unchecked")
    private HttpRequest createHttpRequest(OperationDescription operationDescription, SwaggerMethodParser methodParser, Object[] args) throws IOException {
        final HttpRequest request = configRequest(new HttpRequest(methodParser.httpMethod(), operationDescription.url()), methodParser, args);

        // Headers from Swagger method arguments always take precedence over inferred headers from body types
        for (final String headerName : operationDescription.headers().keySet()) {
            request.withHeader(headerName, operationDescription.headers().get(headerName));
        }

        return request;
    }

    private HttpRequest configRequest(HttpRequest request, SwaggerMethodParser methodParser, Object[] args) throws IOException {
        final Object bodyContentObject = methodParser.body(args);
        if (bodyContentObject == null) {
            request.headers().put("Content-Length", "0");
        } else {
            String contentType = methodParser.bodyContentType();
            if (contentType == null || contentType.isEmpty()) {
                if (bodyContentObject instanceof byte[] || bodyContentObject instanceof String) {
                    contentType = ContentType.APPLICATION_OCTET_STREAM;
                } else {
                    contentType = ContentType.APPLICATION_JSON;
                }
            }

            request.headers().put("Content-Type", contentType);

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
            } else {
                final String bodyContentString = serializer.serialize(bodyContentObject, SerializerEncoding.fromHeaders(request.headers()));
                request.withBody(bodyContentString);
            }
        }

        return request;
    }

    private Mono<HttpDecodedResponse> ensureExpectedStatus(Mono<HttpDecodedResponse> asyncDecodedResponse, final SwaggerMethodParser methodParser) {
        return asyncDecodedResponse
                .flatMap(decodedHttpResponse -> ensureExpectedStatus(decodedHttpResponse, methodParser, null));
    }

    private static Exception instantiateUnexpectedException(UnexpectedExceptionInformation exception,
                                                            HttpResponse httpResponse,
                                                            String responseContent,
                                                            Object responseDecodedContent) {
        final int responseStatusCode = httpResponse.statusCode();
        String contentType = httpResponse.headerValue("Content-Type");
        String bodyRepresentation;
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            bodyRepresentation = "(" + httpResponse.headerValue("Content-Length") + "-byte body)";
        } else {
            bodyRepresentation = responseContent.isEmpty() ? "(empty body)" : "\"" + responseContent + "\"";
        }

        Exception result;
        try {
            final Constructor<? extends HttpRequestException> exceptionConstructor = exception.exceptionType().getConstructor(String.class, HttpResponse.class, exception.exceptionBodyType());
            result = exceptionConstructor.newInstance("Status code " + responseStatusCode + ", " + bodyRepresentation,
                    httpResponse,
                    responseDecodedContent);
        } catch (ReflectiveOperationException e) {
            String message = "Status code " + responseStatusCode + ", but an instance of "
                    + exception.exceptionType().getCanonicalName() + " cannot be created."
                    + " Response body: " + bodyRepresentation;

            result = new IOException(message, e);
        }
        return result;
    }

    /**
     * Create a publisher that (1) emits error if the provided response {@code decodedResponse} has
     * 'disallowed status code' OR (2) emits provided response if it's status code ia allowed.
     *
     * 'disallowed status code' is one of the status code defined in the provided SwaggerMethodParser
     *  or is in the int[] of additional allowed status codes.
     *
     * @param decodedResponse The HttpResponse to check.
     * @param methodParser The method parser that contains information about the service interface
     *                     method that initiated the HTTP request.
     * @param additionalAllowedStatusCodes Additional allowed status codes that are permitted based
     *                                     on the context of the HTTP request.
     * @return An async-version of the provided decodedResponse.
     */
    public Mono<HttpDecodedResponse> ensureExpectedStatus(final HttpDecodedResponse decodedResponse, final SwaggerMethodParser methodParser, int[] additionalAllowedStatusCodes) {
        final int responseStatusCode = decodedResponse.sourceResponse().statusCode();
        final Mono<HttpDecodedResponse> asyncResult;
        if (!methodParser.isExpectedResponseStatusCode(responseStatusCode, additionalAllowedStatusCodes)) {
            Mono<String> bodyAsString = decodedResponse.sourceResponse().bodyAsString();
            //
            asyncResult = bodyAsString.flatMap((Function<String, Mono<HttpDecodedResponse>>) responseContent -> {
                // bodyAsString() emits non-empty string, now look for decoded version of same string
                Mono<Object> decodedErrorBody = decodedResponse.decodedBody();
                //
                return decodedErrorBody.flatMap((Function<Object, Mono<HttpDecodedResponse>>) responseDecodedErrorObject -> {
                    // decodedBody() emits 'responseDecodedErrorObject' the successfully decoded exception body object
                    Throwable exception = instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                            decodedResponse.sourceResponse(),
                            responseContent,
                            responseDecodedErrorObject);
                    return Mono.error(exception);
                    //
                }).switchIfEmpty(Mono.defer((Supplier<Mono<HttpDecodedResponse>>) () -> {
                    // decodedBody() emits empty, indicate unable to decode 'responseContent',
                    // create exception with un-decodable content string and without exception body object.
                    Throwable exception = instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                            decodedResponse.sourceResponse(),
                            responseContent,
                            null);
                    return Mono.error(exception);
                    //
                }));
            }).switchIfEmpty(Mono.defer((Supplier<Mono<HttpDecodedResponse>>) () -> {
                // bodyAsString() emits empty, indicate no body, create exception empty content string no exception body object.
                Throwable exception = instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode),
                        decodedResponse.sourceResponse(),
                        "",
                        null);
                return Mono.error(exception);
                //
            }));
        } else {
            asyncResult = Mono.just(decodedResponse);
        }
        return asyncResult;
    }

    private Mono<?> handleRestResponseReturnType(HttpDecodedResponse response, SwaggerMethodParser methodParser, Type entityType) {
        Mono<?> asyncResult;

        if (TypeUtil.isTypeOrSubTypeOf(entityType, Response.class)) {
            Type bodyType = TypeUtil.getRestResponseBodyType(entityType);

            if (TypeUtil.isTypeOrSubTypeOf(bodyType, Void.class)) {
                asyncResult = response.sourceResponse().body().ignoreElements()
                        .then(Mono.just(createResponse(response, entityType, null)));
            } else {
                asyncResult = handleBodyReturnType(response, methodParser, bodyType)
                        .map((Function<Object, Response<?>>) bodyAsObject -> createResponse(response, entityType, bodyAsObject))
                        .switchIfEmpty(Mono.defer((Supplier<Mono<Response<?>>>) () -> Mono.just(createResponse(response, entityType, null))));
            }
        } else {
            // For now we're just throwing if the Maybe didn't emit a value.
            asyncResult = handleBodyReturnType(response, methodParser, entityType);
        }

        return asyncResult;
    }

    private Response<?> createResponse(HttpDecodedResponse response, Type entityType, Object bodyAsObject) {
        final HttpResponse httpResponse = response.sourceResponse();
        final HttpRequest httpRequest = httpResponse.request();
        final int responseStatusCode = httpResponse.statusCode();
        final HttpHeaders responseHeaders = httpResponse.headers();

        // determine the type of response class. If the type is the 'RestResponse' interface, we will use the
        // 'RestResponseBase' class instead.
        Class<? extends Response<?>> cls = (Class<? extends Response<?>>) TypeUtil.getRawClass(entityType);
        if (cls.equals(Response.class)) {
            cls = (Class<? extends Response<?>>) (Object) ResponseBase.class;
        } else if (cls.equals(PagedResponse.class)) {
            cls = (Class<? extends Response<?>>) (Object) PagedResponseBase.class;

            if (bodyAsObject != null && !TypeUtil.isTypeOrSubTypeOf(bodyAsObject.getClass(), Page.class)) {
                throw new RuntimeException("Unable to create PagedResponse<T>. Body must be of a type that implements: " + Page.class);
            }
        }

        // we try to find the most specific constructor, which we do in the following order:
        // 1) (HttpRequest httpRequest, int statusCode, HttpHeaders headers, Object body, Object deserializedHeaders)
        // 2) (HttpRequest httpRequest, int statusCode, HttpHeaders headers, Object body)
        // 3) (HttpRequest httpRequest, int statusCode, HttpHeaders headers)
        List<Constructor<?>> ctors = Arrays.stream(cls.getDeclaredConstructors())
                                             .filter(ctor -> {
                                                 int paramCount = ctor.getParameterCount();
                                                 return paramCount >= 3 && paramCount <= 5;
                                             })
                                             .sorted(Comparator.comparingInt(Constructor::getParameterCount))
                                             .collect(Collectors.toList());

        if (ctors.isEmpty()) {
            throw new RuntimeException("Cannot find suitable constructor for class " + cls);
        }

        // try to create an instance using our list of potential candidates
        for (int i = 0; i < ctors.size(); i++) {
            final Constructor<? extends Response<?>> ctor = (Constructor<? extends Response<?>>) ctors.get(i);

            try {
                final int paramCount = ctor.getParameterCount();

                switch (paramCount) {
                    case 3:
                        return ctor.newInstance(httpRequest, responseStatusCode, responseHeaders);
                    case 4:
                        return ctor.newInstance(httpRequest, responseStatusCode, responseHeaders, bodyAsObject);
                    case 5:
                        return ctor.newInstance(httpRequest, responseStatusCode, responseHeaders, bodyAsObject, response.decodedHeaders().block());
                    default:
                        throw new IllegalStateException("Response constructor with expected parameters not found.");
                }
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw reactor.core.Exceptions.propagate(e);
            }
        }
        // error
        throw new RuntimeException("Cannot find suitable constructor for class " + cls);
    }

    protected final Mono<?> handleBodyReturnType(final HttpDecodedResponse response, final SwaggerMethodParser methodParser, final Type entityType) {
        final int responseStatusCode = response.sourceResponse().statusCode();
        final HttpMethod httpMethod = methodParser.httpMethod();
        final Type returnValueWireType = methodParser.returnValueWireType();

        final Mono<?> asyncResult;
        if (httpMethod == HttpMethod.HEAD
                && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE) || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            boolean isSuccess = (responseStatusCode / 100) == 2;
            asyncResult = Mono.just(isSuccess);
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            // Mono<byte[]>
            Mono<byte[]> responseBodyBytesAsync = response.sourceResponse().bodyAsByteArray();
            if (returnValueWireType == Base64Url.class) {
                // Mono<Base64Url>
                responseBodyBytesAsync = responseBodyBytesAsync.map(base64UrlBytes -> new Base64Url(base64UrlBytes).decodedBytes());
            }
            asyncResult = responseBodyBytesAsync;
        } else if (FluxUtil.isFluxByteBuf(entityType)) {
            // Mono<Flux<ByteBuf>>
            asyncResult = Mono.just(response.sourceResponse().body());
        } else {
            // Mono<Object> or Mono<Page<T>>
            asyncResult = response.decodedBody();
        }
        return asyncResult;
    }

    protected Object handleHttpResponse(final HttpRequest httpRequest, Mono<HttpDecodedResponse> asyncDecodedHttpResponse, SwaggerMethodParser methodParser, Type returnType) {
        return handleRestReturnType(asyncDecodedHttpResponse, methodParser, returnType);
    }

    protected Object handleResumeOperation(HttpRequest httpRequest, OperationDescription operationDescription, SwaggerMethodParser methodParser, Type returnType)
        throws Exception {
        throw new Exception("The resume operation is not available in the base RestProxy class.");
    }

    /**
     * Handle the provided asynchronous HTTP response and return the deserialized value.
     *
     * @param asyncHttpDecodedResponse the asynchronous HTTP response to the original HTTP request
     * @param methodParser the SwaggerMethodParser that the request originates from
     * @param returnType the type of value that will be returned
     * @return the deserialized result
     */
    public final Object handleRestReturnType(Mono<HttpDecodedResponse> asyncHttpDecodedResponse, final SwaggerMethodParser methodParser, final Type returnType) {
        final Mono<HttpDecodedResponse> asyncExpectedResponse = ensureExpectedStatus(asyncHttpDecodedResponse, methodParser);
        final Object result;
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class)) {
            final Type monoTypeParam = TypeUtil.getTypeArgument(returnType);
            if (TypeUtil.isTypeOrSubTypeOf(monoTypeParam, Void.class)) {
                // ProxyMethod ReturnType: Mono<Void>
                result = asyncExpectedResponse.then();
            } else {
                // ProxyMethod ReturnType: Mono<? extends RestResponseBase<?, ?>>
                result = asyncExpectedResponse.flatMap(response ->
                        handleRestResponseReturnType(response, methodParser, monoTypeParam));
            }
        } else if (FluxUtil.isFluxByteBuf(returnType)) {
            // ProxyMethod ReturnType: Flux<ByteBuf>
            result = asyncExpectedResponse.flatMapMany(ar -> ar.sourceResponse().body());
        } else if (TypeUtil.isTypeOrSubTypeOf(returnType, void.class) || TypeUtil.isTypeOrSubTypeOf(returnType, Void.class)) {
            // ProxyMethod ReturnType: Void
            asyncExpectedResponse.block();
            result = null;
        } else {
            // ProxyMethod ReturnType: T where T != async (Mono, Flux) or sync Void
            // Block the deserialization until a value T is received
            result = asyncExpectedResponse
                    .flatMap(httpResponse -> handleRestResponseReturnType(httpResponse, methodParser, returnType))
                    .block();
        }
        return result;
    }

    /**
     * Create an instance of the default serializer.
     *
     * @return the default serializer
     */
    public static SerializerAdapter createDefaultSerializer() {
        return new JacksonAdapter();
    }

    /**
     * Create the default HttpPipeline.
     *
     * @return the default HttpPipeline
     */
    public static HttpPipeline createDefaultPipeline() {
        return createDefaultPipeline((HttpPipelinePolicy) null);
    }

    /**
     * Create the default HttpPipeline.
     *
     * @param credentials the credentials to use to apply authentication to the pipeline
     * @return the default HttpPipeline
     */
    public static HttpPipeline createDefaultPipeline(ServiceClientCredentials credentials) {
        return createDefaultPipeline(new CredentialsPolicy(credentials));
    }

    /**
     * Create the default HttpPipeline.
     * @param credentialsPolicy the credentials policy factory to use to apply authentication to the
     *                          pipeline
     * @return the default HttpPipeline
     */
    public static HttpPipeline createDefaultPipeline(HttpPipelinePolicy credentialsPolicy) {
        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new UserAgentPolicy());
        policies.add(new RetryPolicy());
        policies.add(new CookiePolicy());
        if (credentialsPolicy != null) {
            policies.add(credentialsPolicy);
        }

        return HttpPipeline.builder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     * @param <A> the type of the Swagger interface
     * @return a proxy implementation of the provided Swagger interface
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface) {
        return create(swaggerInterface, createDefaultPipeline(), createDefaultSerializer());
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipline that will be used to send Http
     *                 requests
     * @param <A> the type of the Swagger interface
     * @return a proxy implementation of the provided Swagger interface
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline) {
        return create(swaggerInterface, httpPipeline, createDefaultSerializer());
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     * @param serviceClient the ServiceClient that contains the details to use to create the
     *                      RestProxy implementation of the swagger interface
     * @param <A> the type of the Swagger interface
     * @return a proxy implementation of the provided Swagger interface
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, ServiceClient serviceClient) {
        return create(swaggerInterface, serviceClient.httpPipeline(), serviceClient.serializerAdapter());
    }

    /**
     * Create a proxy implementation of the provided Swagger interface.
     *
     * @param swaggerInterface the Swagger interface to provide a proxy implementation for
     * @param httpPipeline the HttpPipelinePolicy and HttpClient pipline that will be used to send Http
     *                 requests
     * @param serializer the serializer that will be used to convert POJOs to and from request and
     *                   response bodies
     * @param <A> the type of the Swagger interface.
     * @return a proxy implementation of the provided Swagger interface
     */
    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, HttpPipeline httpPipeline, SerializerAdapter serializer) {
        final SwaggerInterfaceParser interfaceParser = new SwaggerInterfaceParser(swaggerInterface, serializer);
        final RestProxy restProxy = new RestProxy(httpPipeline, serializer, interfaceParser);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[]{swaggerInterface}, restProxy);
    }
}
