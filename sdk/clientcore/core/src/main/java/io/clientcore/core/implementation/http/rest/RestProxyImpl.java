// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.exception.HttpExceptionType;
import io.clientcore.core.http.exception.HttpResponseException;
import io.clientcore.core.http.models.ContentType;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ResponseBodyMode;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.ReflectionSerializable;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.implementation.http.HttpResponseAccessHelper;
import io.clientcore.core.implementation.http.UnexpectedExceptionInformation;
import io.clientcore.core.implementation.http.serializer.CompositeSerializer;
import io.clientcore.core.implementation.http.serializer.MalformedValueException;
import io.clientcore.core.implementation.util.Base64Uri;
import io.clientcore.core.implementation.util.ImplUtils;
import io.clientcore.core.implementation.util.UriBuilder;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.binarydata.InputStreamBinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;
import io.clientcore.core.util.serializer.SerializationFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static io.clientcore.core.http.models.ResponseBodyMode.DESERIALIZE;
import static io.clientcore.core.implementation.http.serializer.HttpResponseBodyDecoder.decodeByteArray;

public class RestProxyImpl {
    static final ResponseConstructorsCache RESPONSE_CONSTRUCTORS_CACHE = new ResponseConstructorsCache();

    // RestProxy is a commonly used class, use a static logger.
    static final ClientLogger LOGGER = new ClientLogger(RestProxyImpl.class);

    final HttpPipeline httpPipeline;
    final CompositeSerializer serializer;
    final SwaggerInterfaceParser interfaceParser;

    /**
     * Create a RestProxy.
     *
     * @param httpPipeline The HttpPipelinePolicy and HttpClient httpPipeline that will be used to send HTTP requests.
     * @param interfaceParser The parser that contains information about the interface describing REST API methods
     * to be used.
     * @param serializers The serializers that will be used to convert response bodies to POJOs.
     */
    public RestProxyImpl(HttpPipeline httpPipeline, SwaggerInterfaceParser interfaceParser,
        ObjectSerializer... serializers) {
        this.httpPipeline = httpPipeline;
        this.interfaceParser = interfaceParser;
        this.serializer = new CompositeSerializer(Arrays.asList(serializers));
    }

    /**
     * Invokes the provided method using the provided arguments.
     *
     * @param proxy The proxy object to invoke the method on.
     * @param options The RequestOptions to use for the request.
     * @param methodParser The SwaggerMethodParser that contains information about the method to invoke.
     * @param args The arguments to use when invoking the method.
     * @return The result of invoking the method.
     * @throws UncheckedIOException When an I/O error occurs.
     * @throws RuntimeException When a URI syntax error occurs.
     */
    @SuppressWarnings({ "try", "unused" })
    public final Object invoke(Object proxy, RequestOptions options, SwaggerMethodParser methodParser, Object[] args) {
        try {
            HttpRequest request = createHttpRequest(methodParser, serializer, args).setRequestOptions(options)
                .setServerSentEventListener(methodParser.setServerSentEventListener(args));

            // If there is 'RequestOptions' apply its request callback operations before validating the body.
            // This is because the callbacks may mutate the request body.
            if (request.getRequestOptions() != null) {
                request.getRequestOptions().getRequestCallback().accept(request);
            }

            if (request.getBody() != null) {
                request.setBody(RestProxyImpl.validateLength(request));
            }

            final Response<?> response = httpPipeline.send(request);

            return handleRestReturnType(response, methodParser, methodParser.getReturnType(), serializer);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        } catch (URISyntaxException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    /**
     * Validates the Length of the input request matches its configured Content Length.
     *
     * @param request the input request to validate.
     * @return the requests body as BinaryData on successful validation.
     */
    static BinaryData validateLength(final HttpRequest request) {
        final BinaryData binaryData = request.getBody();

        if (binaryData == null) {
            return null;
        }

        final long expectedLength = Long.parseLong(request.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH));

        if (binaryData instanceof InputStreamBinaryData) {
            InputStream inputStream = binaryData.toStream();
            LengthValidatingInputStream lengthValidatingInputStream
                = new LengthValidatingInputStream(inputStream, expectedLength);

            return BinaryData.fromStream(lengthValidatingInputStream, expectedLength);
        } else {
            if (binaryData.getLength() == null) {
                byte[] b = binaryData.toBytes();

                validateLengthInternal(b.length, expectedLength);

                return BinaryData.fromBytes(b);
            } else {
                validateLengthInternal(binaryData.getLength(), expectedLength);

                return binaryData;
            }
        }
    }

    private static void validateLengthInternal(long length, long expectedLength) {
        if (length > expectedLength) {
            throw new IllegalStateException(bodyTooLarge(length, expectedLength));
        }

        if (length < expectedLength) {
            throw new IllegalStateException(bodyTooSmall(length, expectedLength));
        }
    }

    static String bodyTooLarge(long length, long expectedLength) {
        return "Request body emitted " + length + " bytes, more than the expected " + expectedLength + " bytes.";
    }

    static String bodyTooSmall(long length, long expectedLength) {
        return "Request body emitted " + length + " bytes, less than the expected " + expectedLength + " bytes.";
    }

    /**
     * Create an HttpRequest for the provided Swagger method using the provided arguments.
     *
     * @param methodParser The Swagger method parser to use.
     * @param args The arguments to use to populate the method's annotation values.
     * @return An HttpRequest.
     * @throws IOException If the body contents cannot be serialized.
     */
    private static HttpRequest createHttpRequest(SwaggerMethodParser methodParser, CompositeSerializer serializer,
        Object[] args) throws IOException, URISyntaxException {

        // Sometimes people pass in a full URI for the value of their PathParam annotated argument.
        // This definitely happens in paging scenarios. In that case, just use the full URI and
        // ignore the Host annotation.
        final String path = methodParser.setPath(args, serializer);
        final UriBuilder pathUriBuilder = UriBuilder.parse(path);
        final UriBuilder uriBuilder;

        if (pathUriBuilder.getScheme() != null) {
            uriBuilder = pathUriBuilder;
        } else {
            uriBuilder = new UriBuilder();

            methodParser.setSchemeAndHost(args, uriBuilder, serializer);

            // Set the path after host, concatenating the path segment in the host.
            if (path != null && !path.isEmpty() && !"/".equals(path)) {
                String hostPath = uriBuilder.getPath();

                if (hostPath == null || hostPath.isEmpty() || "/".equals(hostPath) || path.contains("://")) {
                    uriBuilder.setPath(path);
                } else {
                    if (path.startsWith("/")) {
                        uriBuilder.setPath(hostPath + path);
                    } else {
                        uriBuilder.setPath(hostPath + "/" + path);
                    }
                }
            }
        }

        methodParser.setEncodedQueryParameters(args, uriBuilder, serializer);

        final URI uri = uriBuilder.toUri();
        final HttpRequest request
            = configRequest(new HttpRequest(methodParser.getHttpMethod(), uri), methodParser, serializer, args);
        // Headers from Swagger method arguments always take precedence over inferred headers from body types
        HttpHeaders httpHeaders = request.getHeaders();

        methodParser.setHeaders(args, httpHeaders, serializer);

        return request;
    }

    private static HttpRequest configRequest(HttpRequest request, SwaggerMethodParser methodParser,
        CompositeSerializer serializer, Object[] args) throws IOException {
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

            updateRequest(new RequestDataConfiguration(request, methodParser, isJson, bodyContentObject), serializer);
        }

        return request;
    }

    /**
     * Create a publisher that (1) emits error if the provided response {@code decodedResponse} has 'disallowed status
     * code' OR (2) emits provided response if it's status code ia allowed.
     *
     * <p>'disallowed status code' is one of the status code defined in the provided SwaggerMethodParser or is in the int[]
     * of additional allowed status codes.</p>
     *
     * @param response The Response to check.
     * @param methodParser The method parser that contains information about the service interface method that initiated
     * the HTTP request.
     * @return The decodedResponse.
     */
    private static Response<?> ensureExpectedStatus(Response<?> response, SwaggerMethodParser methodParser,
        CompositeSerializer serializer) {
        int responseStatusCode = response.getStatusCode();

        // If the response was success or configured to not return an error status when the request fails, return it.
        if (methodParser.isExpectedResponseStatusCode(responseStatusCode)) {
            return response;
        }

        // Otherwise, the response wasn't successful and the error object needs to be parsed.
        if (response.getBody() == null || response.getBody().toBytes().length == 0) {
            // No body, create an exception response with an empty body.
            throw instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode), response,
                null, null);
        } else {
            // Create an exception response containing the decoded response body.
            throw instantiateUnexpectedException(methodParser.getUnexpectedException(responseStatusCode), response,
                response.getBody(), decodeByteArray(response.getBody(), response, serializer, methodParser));
        }
    }

    /**
     * Creates an HttpResponseException exception using the details provided in http response and its content.
     *
     * @param unexpectedExceptionInformation The exception holding UnexpectedException's details.
     * @param response The http response to parse when constructing exception
     * @param responseBody The response body to use when constructing exception
     * @param responseDecodedBody The decoded response content to use when constructing exception
     * @return The {@link HttpResponseException} created from the provided details.
     */
    private static HttpResponseException instantiateUnexpectedException(
        UnexpectedExceptionInformation unexpectedExceptionInformation, Response<?> response, BinaryData responseBody,
        Object responseDecodedBody) {
        StringBuilder exceptionMessage
            = new StringBuilder("Status code ").append(response.getStatusCode()).append(", ");

        final String contentType = response.getHeaders().getValue(HttpHeaderName.CONTENT_TYPE);

        if ("application/octet-stream".equalsIgnoreCase(contentType)) {
            String contentLength = response.getHeaders().getValue(HttpHeaderName.CONTENT_LENGTH);

            exceptionMessage.append("(").append(contentLength).append("-byte body)");
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

        return new HttpResponseException(exceptionMessage.toString(), response, exceptionType, responseDecodedBody);
    }

    private static Object handleRestResponseReturnType(Response<?> response, SwaggerMethodParser methodParser,
        Type entityType, CompositeSerializer serializer) {
        if (TypeUtil.isTypeOrSubTypeOf(entityType, Response.class)) {
            final Type bodyType = TypeUtil.getRestResponseBodyType(entityType);

            if (TypeUtil.isTypeOrSubTypeOf(bodyType, Void.class)) {
                try {
                    response.close();
                } catch (IOException e) {
                    throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
                }

                return createResponseIfNecessary(response, entityType, null);
            } else {
                ResponseBodyMode responseBodyMode = null;
                RequestOptions requestOptions = response.getRequest().getRequestOptions();

                if (requestOptions != null) {
                    responseBodyMode = requestOptions.getResponseBodyMode();
                }

                if (responseBodyMode == DESERIALIZE) {
                    HttpResponseAccessHelper.setValue((HttpResponse<?>) response,
                        handleResponseBody(response, methodParser, bodyType, response.getBody(), serializer));
                } else {
                    HttpResponseAccessHelper.setBodyDeserializer((HttpResponse<?>) response,
                        (body) -> handleResponseBody(response, methodParser, bodyType, body, serializer));
                }

                Response<?> responseToReturn = createResponseIfNecessary(response, entityType, response.getBody());

                if (responseToReturn == null) {
                    return createResponseIfNecessary(response, entityType, null);
                }

                return responseToReturn;
            }
        } else {
            // When not handling a Response subtype, we need to eagerly read the response body to construct the correct
            // return type.
            return handleResponseBody(response, methodParser, entityType, response.getBody(), serializer);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private static Response<?> createResponseIfNecessary(Response<?> response, Type entityType, Object bodyAsObject) {
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

    private static Object handleResponseBody(Response<?> response, SwaggerMethodParser methodParser, Type entityType,
        BinaryData responseBody, CompositeSerializer serializer) {
        final int responseStatusCode = response.getStatusCode();
        final HttpMethod httpMethod = methodParser.getHttpMethod();
        final Type returnValueWireType = methodParser.getReturnValueWireType();

        final Object result;

        if (httpMethod == HttpMethod.HEAD
            && (TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.TYPE)
                || TypeUtil.isTypeOrSubTypeOf(entityType, Boolean.class))) {
            result = (responseStatusCode / 100) == 2;
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, byte[].class)) {
            byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;

            if (returnValueWireType == Base64Uri.class) {
                responseBodyBytes = new Base64Uri(responseBodyBytes).decodedBytes();
            }

            result = responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, InputStream.class)) {
            result = responseBody.toStream();
        } else if (TypeUtil.isTypeOrSubTypeOf(entityType, BinaryData.class)) {
            // BinaryData
            //
            // The raw response is directly used to create an instance of BinaryData which then provides
            // different methods to read the response. The reading of the response is delayed until BinaryData
            // is read and depending on which format the content is converted into, the response is not necessarily
            // fully copied into memory resulting in lesser overall memory usage.
            result = responseBody;
        } else {
            result = decodeByteArray(responseBody, response, serializer, methodParser);
        }

        return result;
    }

    /**
     * Handle the provided HTTP response and return the deserialized value.
     *
     * @param response The HTTP response to the original HTTP request.
     * @param methodParser The SwaggerMethodParser that the request originates from.
     * @param returnType The type of value that will be returned.
     * @return The deserialized result.
     */
    private static Object handleRestReturnType(Response<?> response, SwaggerMethodParser methodParser, Type returnType,
        CompositeSerializer serializer) {
        final Response<?> expectedResponse = ensureExpectedStatus(response, methodParser, serializer);
        final Object result;

        if (TypeUtil.isTypeOrSubTypeOf(returnType, void.class) || TypeUtil.isTypeOrSubTypeOf(returnType, Void.class)) {
            try {
                expectedResponse.close();
            } catch (IOException e) {
                throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
            }

            result = null;
        } else {
            result = handleRestResponseReturnType(response, methodParser, returnType, serializer);
        }

        return result;
    }

    private static void updateRequest(RequestDataConfiguration requestDataConfiguration,
        CompositeSerializer serializer) {

        boolean isJson = requestDataConfiguration.isJson();
        HttpRequest request = requestDataConfiguration.getHttpRequest();
        Object bodyContentObject = requestDataConfiguration.getBodyContent();

        if (bodyContentObject == null) {
            return;
        }

        // Attempt to use JsonSerializable or XmlSerializable in a separate block.
        if (ReflectionSerializable.supportsJsonSerializable(bodyContentObject.getClass())) {
            request.setBody(BinaryData.fromObject(bodyContentObject));
            return;
        }

        if (isJson) {
            request.setBody(
                BinaryData.fromObject(bodyContentObject, serializer.getSerializerForFormat(SerializationFormat.JSON)));
        } else if (bodyContentObject instanceof byte[]) {
            request.setBody(BinaryData.fromBytes((byte[]) bodyContentObject));
        } else if (bodyContentObject instanceof String) {
            request.setBody(BinaryData.fromString((String) bodyContentObject));
        } else if (bodyContentObject instanceof ByteBuffer) {
            if (((ByteBuffer) bodyContentObject).hasArray()) {
                request.setBody(BinaryData.fromBytes(((ByteBuffer) bodyContentObject).array()));
            } else {
                byte[] array = new byte[((ByteBuffer) bodyContentObject).remaining()];

                ((ByteBuffer) bodyContentObject).get(array);
                request.setBody(BinaryData.fromBytes(array));
            }
        } else {
            request.setBody(BinaryData.fromObject(bodyContentObject,
                serializer.getSerializerForFormat(serializationFormatFromContentType(request.getHeaders()))));
        }
    }

    /**
     * Determines the serializer encoding to use based on the Content-Type header.
     *
     * @param headers the headers to get the Content-Type to check the encoding for.
     * @return the serializer encoding to use for the body. {@link SerializationFormat#JSON} if there is no Content-Type
     * header or an unrecognized Content-Type encoding is given.
     */
    public static SerializationFormat serializationFormatFromContentType(HttpHeaders headers) {
        if (headers == null) {
            return SerializationFormat.JSON;
        }

        String contentType = headers.getValue(HttpHeaderName.CONTENT_TYPE);
        if (ImplUtils.isNullOrEmpty(contentType)) {
            // When in doubt, JSON!
            return SerializationFormat.JSON;
        }

        int contentTypeEnd = contentType.indexOf(';');
        contentType = (contentTypeEnd == -1) ? contentType : contentType.substring(0, contentTypeEnd);
        SerializationFormat encoding = checkForKnownEncoding(contentType);
        if (encoding != null) {
            return encoding;
        }

        int contentTypeTypeSplit = contentType.indexOf('/');
        if (contentTypeTypeSplit == -1) {
            return SerializationFormat.JSON;
        }

        // Check the suffix if it does not match the full types.
        // Suffixes are defined by the Structured Syntax Suffix Registry
        // https://www.rfc-editor.org/rfc/rfc6839
        final String subtype = contentType.substring(contentTypeTypeSplit + 1);
        final int lastIndex = subtype.lastIndexOf('+');
        if (lastIndex == -1) {
            return SerializationFormat.JSON;
        }

        // Only XML and JSON are supported suffixes, there is no suffix for TEXT.
        final String mimeTypeSuffix = subtype.substring(lastIndex + 1);
        if ("xml".equalsIgnoreCase(mimeTypeSuffix)) {
            return SerializationFormat.XML;
        } else if ("json".equalsIgnoreCase(mimeTypeSuffix)) {
            return SerializationFormat.JSON;
        }

        return SerializationFormat.JSON;
    }

    /*
     * There is a limited set of serialization encodings that are known ahead of time. Instead of using a TreeMap with
     * a case-insensitive comparator, use an optimized search specifically for the known encodings.
     */
    private static SerializationFormat checkForKnownEncoding(String contentType) {
        int length = contentType.length();

        // Check the length of the content type first as it is a quick check.
        if (length != 8 && length != 9 && length != 10 && length != 15 && length != 16) {
            return null;
        }

        if ("text/".regionMatches(true, 0, contentType, 0, 5)) {
            if (length == 8) {
                if ("xml".regionMatches(true, 0, contentType, 5, 3)) {
                    return SerializationFormat.XML;
                } else if ("csv".regionMatches(true, 0, contentType, 5, 3)) {
                    return SerializationFormat.TEXT;
                } else if ("css".regionMatches(true, 0, contentType, 5, 3)) {
                    return SerializationFormat.TEXT;
                }
            } else if (length == 9 && "html".regionMatches(true, 0, contentType, 5, 4)) {
                return SerializationFormat.TEXT;
            } else if (length == 10 && "plain".regionMatches(true, 0, contentType, 5, 5)) {
                return SerializationFormat.TEXT;
            } else if (length == 15 && "javascript".regionMatches(true, 0, contentType, 5, 10)) {
                return SerializationFormat.TEXT;
            }
        } else if ("application/".regionMatches(true, 0, contentType, 0, 12)) {
            if (length == 16 && "json".regionMatches(true, 0, contentType, 12, 4)) {
                return SerializationFormat.JSON;
            } else if (length == 15 && "xml".regionMatches(true, 0, contentType, 12, 3)) {
                return SerializationFormat.XML;
            }
        }

        return null;
    }
}
