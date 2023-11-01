// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.exception.ClientAuthenticationException;
import com.generic.core.exception.HttpResponseException;
import com.generic.core.exception.ResourceExistsException;
import com.generic.core.exception.ResourceModifiedException;
import com.generic.core.exception.ResourceNotFoundException;
import com.generic.core.exception.TooManyRedirectsException;
import com.generic.core.http.Response;
import com.generic.core.http.SimpleResponse;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.implementation.ReflectionSerializable;
import com.generic.core.implementation.ReflectiveInvoker;
import com.generic.core.implementation.TypeUtil;
import com.generic.core.implementation.http.UnexpectedExceptionInformation;
import com.generic.core.implementation.http.serializer.HttpResponseDecoder;
import com.generic.core.implementation.http.serializer.MalformedValueException;
import com.generic.core.implementation.util.UrlBuilder;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Context;
import com.generic.core.models.Headers;
import com.generic.core.util.logging.ClientLogger;
import com.generic.core.util.serializer.ObjectSerializer;
import com.generic.json.JsonSerializable;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.function.Consumer;

import static com.generic.core.implementation.http.ContentType.APPLICATION_JSON;
import static com.generic.core.implementation.http.ContentType.APPLICATION_OCTET_STREAM;

public abstract class RestProxyBase {
    static final ResponseConstructorsCache RESPONSE_CONSTRUCTORS_CACHE = new ResponseConstructorsCache();
    private static final ResponseExceptionConstructorCache RESPONSE_EXCEPTION_CONSTRUCTOR_CACHE =
        new ResponseExceptionConstructorCache();

    // RestProxy is a commonly used class, use a static logger.
    static final ClientLogger LOGGER = new ClientLogger(RestProxyBase.class);

    final HttpPipeline httpPipeline;
    final ObjectSerializer serializer;
    final SwaggerInterfaceParser interfaceParser;
    final HttpResponseDecoder decoder;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline the HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer the serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser the parser that contains information about the interface describing REST API methods that
     * this RestProxy "implements".
     */
    public RestProxyBase(HttpPipeline httpPipeline, ObjectSerializer serializer,
                         SwaggerInterfaceParser interfaceParser) {
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;
        this.interfaceParser = interfaceParser;
        this.decoder = new HttpResponseDecoder(this.serializer);
    }

    public final Object invoke(Object proxy, final Method method, RequestOptions options,
                               EnumSet<ErrorOptions> errorOptions, Consumer<HttpRequest> requestCallback,
                               SwaggerMethodParser methodParser, Object[] args) {
        try {
            HttpRequest request = createHttpRequest(methodParser, serializer, args);
            Context context = methodParser.setContext(args);
            context = RestProxyUtils.mergeRequestOptionsContext(context, options);
            context = context.addData("caller-method", methodParser.getFullyQualifiedMethodName());

            if (methodParser.isResponseEagerlyRead()) {
                context = context.addData("eagerly-read-response", true);
            }

            if (methodParser.isResponseBodyIgnored()) {
                context = context.addData("ignore-response-body", true);
            }

            if (methodParser.isHeadersEagerlyConverted()) {
                context = context.addData("eagerly-convert-headers", true);
            }

            return invoke(proxy, method, options, errorOptions, requestCallback, methodParser, request, context);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    protected abstract Object invoke(Object proxy, Method method, RequestOptions options,
                                     EnumSet<ErrorOptions> errorOptions, Consumer<HttpRequest> httpRequestConsumer,
                                     SwaggerMethodParser methodParser, HttpRequest request, Context context);

    public abstract void updateRequest(RequestDataConfiguration requestDataConfiguration,
                                       ObjectSerializer objectSerializer) throws IOException;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Response createResponse(HttpResponseDecoder.HttpDecodedResponse response, Type entityType,
                                   Object bodyAsObject) {
        final Class<? extends Response<?>> cls = (Class<? extends Response<?>>) TypeUtil.getRawClass(entityType);

        final HttpResponse httpResponse = response.getSourceResponse();
        final HttpRequest request = httpResponse.getRequest();
        final int statusCode = httpResponse.getStatusCode();
        final Headers headers = httpResponse.getHeaders();

        // Inspection of the response type needs to be performed to determine which course of action should be taken to
        // instantiate the Response<?> from the HttpResponse.
        //
        // If the type is either the Response or PagedResponse interface from generic-core a new instance of either
        // ResponseBase or PagedResponseBase can be returned.
        if (cls.equals(Response.class)) {
            // For Response return a new instance of ResponseBase cast to the class.
            return cls.cast(new SimpleResponse<>(request, statusCode, headers, bodyAsObject));
        }

        // Otherwise, rely on reflection, for now, to get the best constructor to use to create the Response subtype.
        //
        // Ideally, in the future the SDKs won't need to dabble in reflection here as the Response subtypes should be
        // given a way to register their constructor as a callback method that consumes HttpDecodedResponse and the
        // body as an Object.
        ReflectiveInvoker constructorReflectiveInvoker = RESPONSE_CONSTRUCTORS_CACHE.get(cls);

        return RESPONSE_CONSTRUCTORS_CACHE.invoke(constructorReflectiveInvoker, response, bodyAsObject);
    }

    /**
     * Create a HttpRequest for the provided Swagger method using the provided arguments.
     *
     * @param methodParser the Swagger method parser to use
     * @param args the arguments to use to populate the method's annotation values
     *
     * @return a HttpRequest
     *
     * @throws IOException thrown if the body contents cannot be serialized
     */
    HttpRequest createHttpRequest(SwaggerMethodParser methodParser, ObjectSerializer objectSerializer,
                                  Object[] args) throws IOException {
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
        final HttpRequest request =
            configRequest(new HttpRequest(methodParser.getHttpMethod(), url), methodParser, objectSerializer, args);

        // Headers from Swagger method arguments always take precedence over inferred headers from body types
        Headers httpHeaders = request.getHeaders();
        methodParser.setHeaders(args, httpHeaders, serializer);

        return request;
    }

    private HttpRequest configRequest(final HttpRequest request, final SwaggerMethodParser methodParser,
                                      ObjectSerializer objectSerializer, final Object[] args) throws IOException {

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
                    contentType = APPLICATION_OCTET_STREAM;
                } else {
                    contentType = APPLICATION_JSON;
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

            // TODO(jogiles): This feels hacky.
            boolean isJson = false;
            final String[] contentTypeParts = contentType.split(";");

            for (final String contentTypePart : contentTypeParts) {
                if (contentTypePart.trim().equalsIgnoreCase(APPLICATION_JSON)) {
                    isJson = true;

                    break;
                }
            }

            updateRequest(new RequestDataConfiguration(request, methodParser, isJson, bodyContentObject),
                objectSerializer);
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
     *
     * @return the Unexpected Exception
     */
    public static HttpResponseException instantiateUnexpectedException(UnexpectedExceptionInformation exception,
                                                                       HttpResponse httpResponse,
                                                                       byte[] responseContent,
                                                                       Object responseDecodedContent) {
        StringBuilder exceptionMessage = new StringBuilder("Status code ")
            .append(httpResponse.getStatusCode())
            .append(", ");

        final String contentType = httpResponse.getHeaderValue(HttpHeaderName.CONTENT_TYPE);

        if (APPLICATION_OCTET_STREAM.equalsIgnoreCase(contentType)) {
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

        // For HttpResponseException types that exist in generic-core, call the constructor directly.
        Class<? extends HttpResponseException> exceptionType = exception.getExceptionType();

        if (exceptionType == HttpResponseException.class) {
            return new HttpResponseException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ClientAuthenticationException.class) {
            return new ClientAuthenticationException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceExistsException.class) {
            return new ResourceExistsException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceModifiedException.class) {
            return new ResourceModifiedException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == ResourceNotFoundException.class) {
            return new ResourceNotFoundException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else if (exceptionType == TooManyRedirectsException.class) {
            return new TooManyRedirectsException(exceptionMessage.toString(), httpResponse, responseDecodedContent);
        } else {
            // Finally, if the HttpResponseException subclass doesn't exist in generic-core, use reflection to create a
            // new instance of it.
            try {
                ReflectiveInvoker reflectiveInvoker =
                    RESPONSE_EXCEPTION_CONSTRUCTOR_CACHE.get(exceptionType, exception.getExceptionBodyType());

                return ResponseExceptionConstructorCache.invoke(reflectiveInvoker, exceptionMessage.toString(),
                    httpResponse, responseDecodedContent);
            } catch (RuntimeException e) {
                // And if reflection fails, return an HttpResponseException.
                exceptionMessage.append(". An instance of ")
                    .append(exceptionType.getCanonicalName())
                    .append(" couldn't be created.");

                HttpResponseException exception1 =
                    new HttpResponseException(exceptionMessage.toString(), httpResponse, responseDecodedContent);

                exception1.addSuppressed(e);

                return exception1;
            }
        }
    }

    /**
     * Whether {@code JsonSerializable} is supported and the {@code bodyContentClass} is an instance of it.
     *
     * @param bodyContentClass The body content class.
     *
     * @return Whether {@code bodyContentClass} can be used as {@code JsonSerializable}.
     */
    static boolean supportsJsonSerializable(Class<?> bodyContentClass) {
        return ReflectionSerializable.supportsJsonSerializable(bodyContentClass);
    }

    /**
     * Serializes the {@code jsonSerializable} as an instance of {@link JsonSerializable}.
     *
     * @param jsonSerializable The {@link JsonSerializable} body content.
     *
     * @return The {@link ByteBuffer} representing the serialized {@code jsonSerializable}.
     *
     * @throws IOException If an error occurs during serialization.
     */
    static ByteBuffer serializeAsJsonSerializable(JsonSerializable<?> jsonSerializable) throws IOException {
        return ReflectionSerializable.serializeJsonSerializableToByteBuffer(jsonSerializable);
    }
}

