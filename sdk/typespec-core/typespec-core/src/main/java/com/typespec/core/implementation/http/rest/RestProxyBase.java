// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.exception.ClientAuthenticationException;
import com.typespec.core.exception.DecodeException;
import com.typespec.core.exception.HttpResponseException;
import com.typespec.core.exception.ResourceExistsException;
import com.typespec.core.exception.ResourceModifiedException;
import com.typespec.core.exception.ResourceNotFoundException;
import com.typespec.core.exception.TooManyRedirectsException;
import com.typespec.core.http.ContentType;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.rest.Page;
import com.typespec.core.http.rest.PagedResponse;
import com.typespec.core.http.rest.PagedResponseBase;
import com.typespec.core.http.rest.RequestOptions;
import com.typespec.core.http.rest.Response;
import com.typespec.core.http.rest.ResponseBase;
import com.typespec.core.implementation.ReflectionSerializable;
import com.typespec.core.implementation.TypeUtil;
import com.typespec.core.implementation.http.UnexpectedExceptionInformation;
import com.typespec.core.implementation.serializer.HttpResponseDecoder;
import com.typespec.core.implementation.serializer.MalformedValueException;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.Context;
import com.typespec.core.util.UrlBuilder;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.core.util.serializer.SerializerAdapter;
import com.typespec.core.util.tracing.Tracer;
import com.typespec.json.JsonSerializable;
import reactor.core.Exceptions;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.function.Consumer;

import static com.typespec.core.util.FluxUtil.monoError;

public abstract class RestProxyBase {
    static final String MUST_IMPLEMENT_PAGE_ERROR =
        "Unable to create PagedResponse<T>. Body must be of a type that implements: " + Page.class;

    static final ResponseConstructorsCache RESPONSE_CONSTRUCTORS_CACHE = new ResponseConstructorsCache();
    private static final ResponseExceptionConstructorCache RESPONSE_EXCEPTION_CONSTRUCTOR_CACHE =
        new ResponseExceptionConstructorCache();

    // RestProxy is a commonly used class, use a static logger.
    static final ClientLogger LOGGER = new ClientLogger(RestProxyBase.class);

    final HttpPipeline httpPipeline;
    final SerializerAdapter serializer;
    final SwaggerInterfaceParser interfaceParser;
    final HttpResponseDecoder decoder;
    protected final Tracer tracer;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     * this RestProxy "implements".
     */
    public RestProxyBase(HttpPipeline httpPipeline, SerializerAdapter serializer,
        SwaggerInterfaceParser interfaceParser) {
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;
        this.interfaceParser = interfaceParser;
        this.decoder = new HttpResponseDecoder(this.serializer);
        this.tracer = httpPipeline.getTracer();
    }

    public final Object invoke(Object proxy, final Method method, RequestOptions options,
        EnumSet<ErrorOptions> errorOptions, Consumer<HttpRequest> requestCallback, SwaggerMethodParser methodParser,
        boolean isAsync, Object[] args) {
        RestProxyUtils.validateResumeOperationIsNotPresent(method);

        try {
            HttpRequest request = createHttpRequest(methodParser, serializer, isAsync, args);

            Context context = methodParser.setContext(args);
            context = RestProxyUtils.mergeRequestOptionsContext(context, options);

            context = context.addData("caller-method", methodParser.getFullyQualifiedMethodName());

            if (methodParser.isResponseEagerlyRead()) {
                context = context.addData("azure-eagerly-read-response", true);
            }

            if (methodParser.isResponseBodyIgnored()) {
                context = context.addData("azure-ignore-response-body", true);
            }

            if (methodParser.isHeadersEagerlyConverted()) {
                context = context.addData("azure-eagerly-convert-headers", true);
            }

            return invoke(proxy, method, options, errorOptions, requestCallback, methodParser, request, context);

        } catch (IOException e) {
            if (isAsync) {
                return monoError(LOGGER, Exceptions.propagate(e));
            } else {
                throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
            }
        }
    }

    protected abstract Object invoke(Object proxy, Method method, RequestOptions options,
        EnumSet<ErrorOptions> errorOptions, Consumer<HttpRequest> httpRequestConsumer, SwaggerMethodParser methodParser,
        HttpRequest request, Context context);

    public abstract void updateRequest(RequestDataConfiguration requestDataConfiguration,
        SerializerAdapter serializerAdapter) throws IOException;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Response createResponse(HttpResponseDecoder.HttpDecodedResponse response, Type entityType,
        Object bodyAsObject) {
        final Class<? extends Response<?>> cls = (Class<? extends Response<?>>) TypeUtil.getRawClass(entityType);

        final HttpResponse httpResponse = response.getSourceResponse();
        final HttpRequest request = httpResponse.getRequest();
        final int statusCode = httpResponse.getStatusCode();
        final HttpHeaders headers = httpResponse.getHeaders();
        final Object decodedHeaders = response.getDecodedHeaders();
        // Inspection of the response type needs to be performed to determine which course of action should be taken to
        // instantiate the Response<?> from the HttpResponse.
        //
        // If the type is either the Response or PagedResponse interface from azure-core a new instance of either
        // ResponseBase or PagedResponseBase can be returned.
        if (cls.equals(Response.class)) {
            // For Response return a new instance of ResponseBase cast to the class.
            return cls.cast(new ResponseBase<>(request, statusCode, headers, bodyAsObject, decodedHeaders));
        } else if (cls.equals(PagedResponse.class)) {
            // For PagedResponse return a new instance of PagedResponseBase cast to the class.
            //
            // PagedResponse needs an additional check that the bodyAsObject implements Page.
            //
            // If the bodyAsObject is null use the constructor that take items and continuation token with null.
            // Otherwise, use the constructor that take Page.
            if (bodyAsObject != null && !TypeUtil.isTypeOrSubTypeOf(bodyAsObject.getClass(), Page.class)) {
                throw LOGGER.logExceptionAsError(new RuntimeException(MUST_IMPLEMENT_PAGE_ERROR));
            } else if (bodyAsObject == null) {
                return cls.cast(new PagedResponseBase<>(request, statusCode, headers, null, null, decodedHeaders));
            } else {
                return cls.cast(new PagedResponseBase<>(request, statusCode, headers, (Page<?>) bodyAsObject,
                    decodedHeaders));
            }
        }

        // Otherwise, rely on reflection, for now, to get the best constructor to use to create the Response subtype.
        //
        // Ideally, in the future the SDKs won't need to dabble in reflection here as the Response subtypes should be
        // given a way to register their constructor as a callback method that consumes HttpDecodedResponse and the
        // body as an Object.
        MethodHandle constructorHandle = RESPONSE_CONSTRUCTORS_CACHE.get(cls);
        return RESPONSE_CONSTRUCTORS_CACHE.invoke(constructorHandle, response, bodyAsObject);
    }

    /**
     * Starts the tracing span for the current service call, additionally set metadata attributes on the span by passing
     * additional context information.
     *
     * @param method Service method being called.
     * @param context Context information about the current service call.
     * @return The updated context containing the span context.
     */
    Context startTracingSpan(SwaggerMethodParser method, Context context) {
        if (isTracingEnabled(context)) {
            Object tracingContextObj = context.getData("TRACING_CONTEXT").orElse(null);
            Context tracingContext = tracingContextObj instanceof Context ? (Context) tracingContextObj : context;
            return tracer.start(method.getSpanName(), tracingContext);
        }

        return context;
    }

    protected boolean isTracingEnabled(Context context) {
        // First check if tracing is enabled. This is an optimized operation, so it is done first.
        // Then check if this method disabled tracing. This requires walking a linked list, so do it last.
        return tracer.isEnabled() && !(boolean) context.getData(Tracer.DISABLE_TRACING_KEY).orElse(false);
    }

    /**
     * Create a HttpRequest for the provided Swagger method using the provided arguments.
     *
     * @param methodParser the Swagger method parser to use
     * @param args the arguments to use to populate the method's annotation values
     * @return a HttpRequest
     * @throws IOException thrown if the body contents cannot be serialized
     */
    HttpRequest createHttpRequest(SwaggerMethodParser methodParser, SerializerAdapter serializerAdapter,
        boolean isAsync, Object[] args) throws IOException {
        // Sometimes people pass in a full URL for the value of their PathParam annotated argument.
        // This definitely happens in paging scenarios. In that case, just use the full URL and
        // ignore the Host annotation.
        final String path = methodParser.setPath(args, serializer);
        final UrlBuilder pathUrlBuilder = UrlBuilder.parse(path);

        final UrlBuilder urlBuilder;
        if (pathUrlBuilder.getScheme() != null) {
            urlBuilder = pathUrlBuilder;
        } else {
            urlBuilder = new UrlBuilder();

            methodParser.setSchemeAndHost(args, urlBuilder, serializer);

            // Set the path after host, concatenating the path
            // segment in the host.
            if (path != null && !path.isEmpty() && !"/".equals(path)) {
                String hostPath = urlBuilder.getPath();
                if (hostPath == null || hostPath.isEmpty() || "/".equals(hostPath) || path.contains("://")) {
                    urlBuilder.setPath(path);
                } else {
                    if (path.startsWith("/")) {
                        urlBuilder.setPath(hostPath + path);
                    } else {
                        urlBuilder.setPath(hostPath + "/" + path);
                    }
                }
            }
        }

        methodParser.setEncodedQueryParameters(args, urlBuilder, serializer);

        final URL url = urlBuilder.toUrl();
        final HttpRequest request = configRequest(new HttpRequest(methodParser.getHttpMethod(), url),
            methodParser, serializerAdapter, isAsync, args);

        // Headers from Swagger method arguments always take precedence over inferred headers from body types
        HttpHeaders httpHeaders = request.getHeaders();
        methodParser.setHeaders(args, httpHeaders, serializer);

        return request;
    }

    private HttpRequest configRequest(final HttpRequest request, final SwaggerMethodParser methodParser,
        SerializerAdapter serializerAdapter, boolean isAsync, final Object[] args) throws IOException {
        final Object bodyContentObject = methodParser.setBody(args, serializer);
        if (bodyContentObject == null) {
            request.setHeader(HttpHeaderName.CONTENT_LENGTH, "0");
        } else {
            // We read the content type from the @BodyParam annotation
            String contentType = methodParser.getBodyContentType();

            // If this is null or empty, the service interface definition is incomplete and should
            // be fixed to ensure correct definitions are applied
            if (contentType == null || contentType.isEmpty()) {
                if (bodyContentObject instanceof byte[] || bodyContentObject instanceof String) {
                    contentType = ContentType.APPLICATION_OCTET_STREAM;
                } else {
                    contentType = ContentType.APPLICATION_JSON;
                }
            }

            request.setHeader(HttpHeaderName.CONTENT_TYPE, contentType);
            if (bodyContentObject instanceof BinaryData) {
                BinaryData binaryData = (BinaryData) bodyContentObject;
                if (binaryData.getLength() != null) {
                    request.setHeader(HttpHeaderName.CONTENT_LENGTH, binaryData.getLength().toString());
                }
                // The request body is not read here. The call to `toFluxByteBuffer()` lazily converts the underlying
                // content of BinaryData to a Flux<ByteBuffer> which is then read by HttpClient implementations when
                // sending the request to the service. There is no memory copy that happens here. Sources like
                // InputStream, File and Flux<ByteBuffer> will not be eagerly copied into memory until it's required
                // by the HttpClient implementations.
                request.setBody(binaryData);
                return request;
            }

            // TODO(jogiles) this feels hacky
            boolean isJson = false;
            final String[] contentTypeParts = contentType.split(";");
            for (final String contentTypePart : contentTypeParts) {
                if (contentTypePart.trim().equalsIgnoreCase(ContentType.APPLICATION_JSON)) {
                    isJson = true;
                    break;
                }
            }
            updateRequest(new RequestDataConfiguration(request, methodParser, isJson, bodyContentObject),
                serializerAdapter);
        }

        return request;
    }

    /**
     * Creates the Unexpected Exception using the details provided in http response and its content.
     *
     * @param exception the exception holding UnexpectedException's details.
     * @param httpResponse the http response to parse when constructing exception
     * @param responseContent the response body to use when constructing exception
     * @param responseDecodedContent the decoded response content to use when constructing exception
     * @return the Unexpected Exception
     */
    public static HttpResponseException instantiateUnexpectedException(UnexpectedExceptionInformation exception,
        HttpResponse httpResponse, byte[] responseContent, Object responseDecodedContent) {
        StringBuilder exceptionMessage = new StringBuilder("Status code ")
            .append(httpResponse.getStatusCode())
            .append(", ");

        final String contentType = httpResponse.getHeaderValue(HttpHeaderName.CONTENT_TYPE);
        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            String contentLength = httpResponse.getHeaderValue(HttpHeaderName.CONTENT_LENGTH);
            exceptionMessage.append("(").append(contentLength).append("-byte body)");
        } else if (responseContent == null || responseContent.length == 0) {
            exceptionMessage.append("(empty body)");
        } else {
            exceptionMessage.append('\"').append(new String(responseContent, StandardCharsets.UTF_8)).append('\"');
        }

        // If the decoded response content is on of these exception types there was a failure in creating the actual
        // exception body type. In this case return an HttpResponseException to maintain the exception having a
        // reference to the HttpResponse and information about what caused the deserialization failure.
        if (responseDecodedContent instanceof IOException
            || responseDecodedContent instanceof MalformedValueException
            || responseDecodedContent instanceof IllegalStateException) {
            return new HttpResponseException(exceptionMessage.toString(), httpResponse,
                (Throwable) responseDecodedContent);
        }

        // For HttpResponseException types that exist in azure-core, call the constructor directly.
        Class<? extends HttpResponseException> exceptionType = exception.getExceptionType();
        if (exceptionType == HttpResponseException.class) {
            return new HttpResponseException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ClientAuthenticationException.class) {
            return new ClientAuthenticationException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == DecodeException.class) {
            return new DecodeException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceExistsException.class) {
            return new ResourceExistsException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceModifiedException.class) {
            return new ResourceModifiedException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceNotFoundException.class) {
            return new ResourceNotFoundException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == TooManyRedirectsException.class) {
            return new TooManyRedirectsException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else {
            // Finally, if the HttpResponseException subclass doesn't exist in azure-core, use reflection to create a
            // new instance of it.
            try {
                MethodHandle handle = RESPONSE_EXCEPTION_CONSTRUCTOR_CACHE.get(exceptionType,
                    exception.getExceptionBodyType());
                return ResponseExceptionConstructorCache.invoke(handle, exceptionMessage.toString(), httpResponse,
                    responseDecodedContent);
            } catch (RuntimeException e) {
                // And if reflection fails, return an HttpResponseException.
                exceptionMessage.append(". An instance of ")
                    .append(exceptionType.getCanonicalName())
                    .append(" couldn't be created.");
                HttpResponseException exception1 = new HttpResponseException(exceptionMessage.toString(), httpResponse,
                    responseDecodedContent);
                exception1.addSuppressed(e);
                return exception1;
            }
        }
    }

    /**
     * Whether {@code JsonSerializable} is supported and the {@code bodyContentClass} is an instance of it.
     *
     * @param bodyContentClass The body content class.
     * @return Whether {@code bodyContentClass} can be used as {@code JsonSerializable}.
     */
    static boolean supportsJsonSerializable(Class<?> bodyContentClass) {
        return ReflectionSerializable.supportsJsonSerializable(bodyContentClass);
    }

    /**
     * Serializes the {@code jsonSerializable} as an instance of {@code JsonSerializable}.
     *
     * @param jsonSerializable The {@code JsonSerializable} body content.
     * @return The {@link ByteBuffer} representing the serialized {@code jsonSerializable}.
     * @throws IOException If an error occurs during serialization.
     */
    static ByteBuffer serializeAsJsonSerializable(JsonSerializable<?> jsonSerializable) throws IOException {
        return ReflectionSerializable.serializeJsonSerializableToByteBuffer(jsonSerializable);
    }

    /**
     * Whether {@code XmlSerializable} is supported and the {@code bodyContentClass} is an instance of it.
     *
     * @param bodyContentClass The body content class.
     * @return Whether {@code bodyContentClass} can be used as {@code XmlSerializable}.
     */
    static boolean supportsXmlSerializable(Class<?> bodyContentClass) {
        return ReflectionSerializable.supportsXmlSerializable(bodyContentClass);
    }

    /**
     * Serializes the {@code bodyContent} as an instance of {@code XmlSerializable}.
     *
     * @param bodyContent The {@code XmlSerializable} body content.
     * @return The {@link ByteBuffer} representing the serialized {@code bodyContent}.
     * @throws IOException If the XmlWriter fails to close properly.
     */
    static ByteBuffer serializeAsXmlSerializable(Object bodyContent) throws IOException {
        return ReflectionSerializable.serializeXmlSerializableToByteBuffer(bodyContent);
    }

    @SuppressWarnings("unchecked")
    static <E extends Exception> void sneakyThrows(Exception e) throws E {
        throw (E) e;
    }
}

