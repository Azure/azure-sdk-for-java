// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.exception.HttpExceptionType;
import io.clientcore.core.http.exception.HttpResponseException;
import io.clientcore.core.http.models.ContentType;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.ReflectionSerializable;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.implementation.http.UnexpectedExceptionInformation;
import io.clientcore.core.implementation.http.serializer.MalformedValueException;
import io.clientcore.core.implementation.util.UrlBuilder;
import io.clientcore.core.json.JsonSerializable;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class RestProxyBase {
    static final ResponseConstructorsCache RESPONSE_CONSTRUCTORS_CACHE = new ResponseConstructorsCache();
    private static final ResponseExceptionConstructorCache RESPONSE_EXCEPTION_CONSTRUCTOR_CACHE =
        new ResponseExceptionConstructorCache();

    // RestProxy is a commonly used class, use a static logger.
    static final ClientLogger LOGGER = new ClientLogger(RestProxyBase.class);

    final HttpPipeline httpPipeline;
    final ObjectSerializer serializer;
    final SwaggerInterfaceParser interfaceParser;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline The HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param serializer The serializer that will be used to convert response bodies to POJOs.
     * @param interfaceParser The parser that contains information about the interface describing REST API methods that
     * this RestProxy "implements".
     */
    public RestProxyBase(HttpPipeline httpPipeline, ObjectSerializer serializer,
                         SwaggerInterfaceParser interfaceParser) {
        this.httpPipeline = httpPipeline;
        this.serializer = serializer;
        this.interfaceParser = interfaceParser;
    }

    public final Object invoke(Object proxy, RequestOptions options, SwaggerMethodParser methodParser, Object[] args) {
        try {
            HttpRequest request = createHttpRequest(methodParser, serializer, args)
                .setRequestOptions(options)
                .setServerSentEventListener(methodParser.setServerSentEventListener(args));

            return invoke(proxy, methodParser, request);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    protected abstract Object invoke(Object proxy, SwaggerMethodParser methodParser, HttpRequest request);

    public abstract void updateRequest(RequestDataConfiguration requestDataConfiguration,
                                       ObjectSerializer objectSerializer) throws IOException;

    @SuppressWarnings({"unchecked"})
    public Response<?> createResponseIfNecessary(Response<?> response, Type entityType, Object bodyAsObject) {
        final Class<? extends Response<?>> clazz = (Class<? extends Response<?>>) TypeUtil.getRawClass(entityType);

        // Inspection of the response type needs to be performed to determine the course of action: either return the
        // Response or rely on reflection to create an appropriate Response subtype.
        if (clazz.equals(Response.class)) {
            // Return the Response.
            return response;
        } else {
            // Otherwise, rely on reflection, for now, to get the best constructor to use to create the Response
            // subtype.
            //
            // Ideally, in the future the SDKs won't need to dabble in reflection here as the Response subtypes should
            // be given a way to register their constructor as a callback method that consumes Response and the body as
            // an Object.
            ReflectiveInvoker constructorReflectiveInvoker = RESPONSE_CONSTRUCTORS_CACHE.get(clazz);

            return RESPONSE_CONSTRUCTORS_CACHE.invoke(constructorReflectiveInvoker, response, bodyAsObject);
        }
    }

    /**
     * Create an HttpRequest for the provided Swagger method using the provided arguments.
     *
     * @param methodParser The Swagger method parser to use.
     * @param args The arguments to use to populate the method's annotation values.
     *
     * @return An HttpRequest.
     *
     * @throws IOException If the body contents cannot be serialized.
     */
    HttpRequest createHttpRequest(SwaggerMethodParser methodParser, ObjectSerializer objectSerializer, Object[] args)
        throws IOException {

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

            // Set the path after host, concatenating the path segment in the host.
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
        HttpHeaders httpHeaders = request.getHeaders();

        methodParser.setHeaders(args, httpHeaders, serializer);

        return request;
    }

    private HttpRequest configRequest(HttpRequest request, SwaggerMethodParser methodParser,
                                      ObjectSerializer objectSerializer, Object[] args) throws IOException {
        final Object bodyContentObject = methodParser.setBody(args, serializer);

        if (bodyContentObject == null) {
            request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "0");
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

            request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType);

            if (bodyContentObject instanceof BinaryData) {
                BinaryData binaryData = (BinaryData) bodyContentObject;

                if (binaryData.getLength() != null) {
                    request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, binaryData.getLength().toString());
                }

                // The request body is not read here. BinaryData lazily converts the underlying content which is then
                // read by HttpClient implementations when sending the request to the service. There is no memory
                // copy that happens here. Sources like InputStream or File will not be eagerly copied into memory
                // until it's required by the HttpClient implementations.
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
                objectSerializer);
        }

        return request;
    }

    /**
     * Creates an HttpResponseException exception using the details provided in http response and its content.
     *
     * @param unexpectedExceptionInformation The exception holding UnexpectedException's details.
     * @param response The http response to parse when constructing exception
     * @param responseBody The response body to use when constructing exception
     * @param responseDecodedBody The decoded response content to use when constructing exception
     *
     * @return The {@link HttpResponseException} created from the provided details.
     */
    public static HttpResponseException instantiateUnexpectedException(UnexpectedExceptionInformation unexpectedExceptionInformation,
                                                                       Response<?> response, BinaryData responseBody,
                                                                       Object responseDecodedBody) {
        StringBuilder exceptionMessage = new StringBuilder("Status code ")
            .append(response.getStatusCode())
            .append(", ");

        final String contentType = response.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE);

        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            String contentLength = response.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH);

            exceptionMessage.append("(")
                .append(contentLength)
                .append("-byte body)");
        } else if (responseBody == null || responseBody.toBytes().length == 0) {
            exceptionMessage.append("(empty body)");
        } else {
            exceptionMessage.append('\"')
                .append(new String(responseBody.toBytes(), StandardCharsets.UTF_8))
                .append('\"');
        }

        // If the decoded response body is on of these exception types there was a failure in creating the actual
        // exception body type. In this case return an HttpResponseException to maintain the exception having a
        // reference to the Response and information about what caused the deserialization failure.
        if (responseDecodedBody instanceof IOException
            || responseDecodedBody instanceof MalformedValueException
            || responseDecodedBody instanceof IllegalStateException) {

            return new HttpResponseException(exceptionMessage.toString(), response, null,
                (Throwable) responseDecodedBody);
        }

        HttpExceptionType exceptionType = unexpectedExceptionInformation.getExceptionType();

        return new HttpResponseException(exceptionMessage.toString(), response, exceptionType,
            responseDecodedBody);
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
     * Serializes the {@code jsonSerializable} as an instance of {@code JsonSerializable}.
     *
     * @param jsonSerializable The {@code JsonSerializable} body content.
     *
     * @return The {@link ByteBuffer} representing the serialized {@code jsonSerializable}.
     *
     * @throws IOException If an error occurs during serialization.
     */
    static ByteBuffer serializeAsJsonSerializable(JsonSerializable<?> jsonSerializable) throws IOException {
        return ReflectionSerializable.serializeJsonSerializableToByteBuffer(jsonSerializable);
    }
}
