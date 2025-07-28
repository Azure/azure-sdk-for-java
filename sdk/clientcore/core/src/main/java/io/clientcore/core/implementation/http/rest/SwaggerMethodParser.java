// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.RestProxy;
import io.clientcore.core.http.annotations.BodyParam;
import io.clientcore.core.http.annotations.FormParam;
import io.clientcore.core.http.annotations.HeaderParam;
import io.clientcore.core.http.annotations.HostParam;
import io.clientcore.core.http.annotations.HttpRequestInformation;
import io.clientcore.core.http.annotations.PathParam;
import io.clientcore.core.http.annotations.QueryParam;
import io.clientcore.core.http.annotations.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.implementation.http.ContentType;
import io.clientcore.core.implementation.http.UnexpectedExceptionInformation;
import io.clientcore.core.implementation.http.serializer.CompositeSerializer;
import io.clientcore.core.implementation.http.serializer.HttpResponseDecodeData;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.utils.Base64Uri;
import io.clientcore.core.utils.DateTimeRfc1123;
import io.clientcore.core.utils.ExpandableEnum;
import io.clientcore.core.utils.UriBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.clientcore.core.implementation.TypeUtil.typeImplementsInterface;
import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * This class contains the metadata of a {@link Method} contained in a Swagger interface used to make REST API calls in
 * {@link RestProxy}.
 */
public class SwaggerMethodParser implements HttpResponseDecodeData {
    private final String fullyQualifiedMethodName;
    private final ClientLogger logger;
    private final HttpMethod httpMethod;
    private final List<UrlSegment> hostTemplate;
    private final List<UrlSegment> pathTemplate;
    private final List<FormParameterSerializer> formParameterSerializers;
    private final List<QueryParameterProcessor> queryParameterProcessors;
    private final List<HeaderProcessor> headerProcessors;
    private final String staticQueryParams;
    private final HttpHeaders requestHeaders = new HttpHeaders();
    private final Integer bodyContentMethodParameterIndex;
    private final String bodyContentType;
    private final Type bodyJavaType;
    private final BitSet expectedStatusCodes;
    private final Type returnType;
    private final Type returnValueWireType;
    private final UnexpectedResponseExceptionDetail[] unexpectedResponseExceptionDetails;
    private final int requestOptionsPosition;
    private final boolean returnTypeDecodable;
    private final boolean headersEagerlyConverted;
    private final int serverSentEventListenerPosition;

    private Map<Integer, UnexpectedExceptionInformation> exceptionMapping;
    private UnexpectedExceptionInformation defaultException;

    /**
     * Create a SwaggerMethodParser object using the provided fully qualified method name.
     *
     * @param swaggerMethod the Swagger method to parse.
     */
    public SwaggerMethodParser(Method swaggerMethod) {
        this(SwaggerInterfaceParser.getInstance(swaggerMethod.getDeclaringClass()), swaggerMethod);
    }

    SwaggerMethodParser(SwaggerInterfaceParser interfaceParser, Method swaggerMethod) {
        final String rawHost = interfaceParser.getHost();
        final Class<?> swaggerInterface = swaggerMethod.getDeclaringClass();
        fullyQualifiedMethodName = swaggerInterface.getName() + "." + swaggerMethod.getName();
        logger = new ClientLogger(fullyQualifiedMethodName);

        if (!swaggerMethod.isAnnotationPresent(HttpRequestInformation.class)) {
            // Should this also check whether there are multiple HTTP method annotations as well?
            throw new MissingRequiredAnnotationException(Collections.singletonList(HttpRequestInformation.class),
                swaggerMethod);
        }

        HttpRequestInformation httpRequestInformation = swaggerMethod.getAnnotation(HttpRequestInformation.class);

        this.httpMethod = httpRequestInformation.method();
        final String relativePath = httpRequestInformation.path();

        returnType = swaggerMethod.getGenericReturnType();

        final String[] requestHeaders = httpRequestInformation.headers();

        if (requestHeaders != null) {
            for (final String requestHeader : requestHeaders) {
                final int colonIndex = requestHeader.indexOf(":");

                if (colonIndex >= 0) {
                    final String headerName = requestHeader.substring(0, colonIndex).trim();

                    if (!headerName.isEmpty()) {
                        final String headerValue = requestHeader.substring(colonIndex + 1).trim();

                        if (!headerValue.isEmpty()) {
                            if (headerValue.contains(",")) {
                                // There are multiple values for this header, so we split them out.
                                this.requestHeaders.set(HttpHeaderName.fromString(headerName),
                                    Arrays.asList(headerValue.split(",")));
                            } else {
                                this.requestHeaders.set(HttpHeaderName.fromString(headerName), headerValue);
                            }
                        }
                    }
                }
            }
        }

        final String[] requestQueryParams = httpRequestInformation.queryParams();

        if (requestQueryParams != null) {
            // Use a temporary UriBuilder to correctly format the static query part.
            UriBuilder staticUriBuilder = new UriBuilder();
            for (final String queryParam : requestQueryParams) {
                if (isNullOrEmpty(queryParam)) {
                    throw new IllegalStateException("Query parameters cannot be null or empty.");
                }

                // We take the first equals sign as the delimiter between the name and value of the query parameter.
                // If more than one equals sign is present, the rest of the string is considered part of the value.
                final int equalsIndex = queryParam.indexOf("=");
                final String paramName;
                final String paramValue;

                if (equalsIndex >= 0) {
                    paramName = UriEscapers.QUERY_ESCAPER.escape(queryParam.substring(0, equalsIndex));

                    if (!paramName.isEmpty()) {
                        paramValue = UriEscapers.QUERY_ESCAPER.escape(queryParam.substring(equalsIndex + 1));
                    } else {
                        throw new IllegalStateException("Names for query parameters cannot be empty.");
                    }
                } else {
                    // No equals sign was found, so the entire string is considered the name of the query parameter.
                    paramName = UriEscapers.QUERY_ESCAPER.escape(queryParam);
                    paramValue = "";
                }

                staticUriBuilder.addQueryParameter(paramName, paramValue);
            }
            this.staticQueryParams = staticUriBuilder.getQueryString();
        } else {
            this.staticQueryParams = null;
        }

        Class<?> returnValueWireType = httpRequestInformation.returnValueWireType();

        if (returnValueWireType == Base64Uri.class || returnValueWireType == DateTimeRfc1123.class) {
            this.returnValueWireType = returnValueWireType;
        } else if (TypeUtil.isTypeOrSubTypeOf(returnValueWireType, List.class)) {
            this.returnValueWireType = returnValueWireType.getGenericInterfaces()[0];
        } else {
            this.returnValueWireType = null;
        }

        final int[] expectedResponses = httpRequestInformation.expectedStatusCodes();

        if (expectedResponses.length > 0) {
            expectedStatusCodes = new BitSet();

            for (int code : expectedResponses) {
                expectedStatusCodes.set(code);
            }
        } else {
            expectedStatusCodes = null;
        }

        unexpectedResponseExceptionDetails
            = swaggerMethod.getAnnotationsByType(UnexpectedResponseExceptionDetail.class);

        Integer bodyContentMethodParameterIndex = null;
        String bodyContentType = null;
        Type bodyJavaType = null;
        final Annotation[][] allParametersAnnotations = swaggerMethod.getParameterAnnotations();

        List<RangeReplaceSubstitution> pathSubstitutions = new ArrayList<>();
        List<RangeReplaceSubstitution> hostSubstitutions = new ArrayList<>();
        List<Substitution> formSubstitutions = new ArrayList<>();
        List<QueryParameterProcessor> queryProcessors = new ArrayList<>();
        List<HeaderProcessor> headerProcessors = new ArrayList<>();

        for (int parameterIndex = 0; parameterIndex < allParametersAnnotations.length; ++parameterIndex) {
            final Annotation[] parameterAnnotations = swaggerMethod.getParameterAnnotations()[parameterIndex];
            Type parameterType = swaggerMethod.getGenericParameterTypes()[parameterIndex];

            for (final Annotation annotation : parameterAnnotations) {
                final Class<? extends Annotation> annotationType = annotation.annotationType();

                if (annotationType.equals(HostParam.class)) {
                    final HostParam hostParamAnnotation = (HostParam) annotation;
                    hostSubstitutions.add(new RangeReplaceSubstitution(hostParamAnnotation.value(), parameterIndex,
                        !hostParamAnnotation.encoded(), rawHost));
                } else if (annotationType.equals(PathParam.class)) {
                    final PathParam pathParamAnnotation = (PathParam) annotation;
                    pathSubstitutions.add(new RangeReplaceSubstitution(pathParamAnnotation.value(), parameterIndex,
                        !pathParamAnnotation.encoded(), relativePath));
                } else if (annotationType.equals(QueryParam.class)) {
                    final QueryParam queryParamAnnotation = (QueryParam) annotation;
                    if (queryParamAnnotation.multipleQueryParams()) {
                        queryProcessors.add(new ListQueryParameterProcessor(queryParamAnnotation.value(),
                            parameterIndex, !queryParamAnnotation.encoded()));
                    } else {
                        queryProcessors.add(new SingleQueryParameterProcessor(queryParamAnnotation.value(),
                            parameterIndex, !queryParamAnnotation.encoded()));
                    }
                } else if (annotationType.equals(HeaderParam.class)) {
                    final HeaderParam headerParamAnnotation = (HeaderParam) annotation;
                    if (TypeUtil.isTypeOrSubTypeOf(parameterType, Map.class)) {
                        headerProcessors.add(new HeaderMapProcessor(headerParamAnnotation.value(), parameterIndex));
                    } else {
                        headerProcessors.add(new SimpleHeaderProcessor(headerParamAnnotation.value(), parameterIndex));
                    }
                } else if (annotationType.equals(BodyParam.class)) {
                    final BodyParam bodyParamAnnotation = (BodyParam) annotation;
                    bodyContentMethodParameterIndex = parameterIndex;
                    bodyContentType = bodyParamAnnotation.value();
                    bodyJavaType = swaggerMethod.getGenericParameterTypes()[parameterIndex];
                } else if (annotationType.equals(FormParam.class)) {
                    final FormParam formParamAnnotation = (FormParam) annotation;
                    formSubstitutions.add(
                        new Substitution(formParamAnnotation.value(), parameterIndex, !formParamAnnotation.encoded()));

                    bodyContentType = ContentType.APPLICATION_X_WWW_FORM_URLENCODED;
                    bodyJavaType = String.class;
                }
            }
        }

        this.queryParameterProcessors = Collections.unmodifiableList(queryProcessors);
        this.headerProcessors = Collections.unmodifiableList(headerProcessors);

        final Map<String, Integer> hostPlaceholderArgs = new HashMap<>();
        final Map<String, Boolean> hostPlaceholderEncodes = new HashMap<>();
        for (RangeReplaceSubstitution sub : hostSubstitutions) {
            hostPlaceholderArgs.put(sub.getUriParameterName(), sub.getMethodParameterIndex());
            hostPlaceholderEncodes.put(sub.getUriParameterName(), sub.shouldEncode());
        }
        this.hostTemplate = parseUrlTemplate(rawHost, hostPlaceholderArgs, hostPlaceholderEncodes);

        final Map<String, Integer> pathPlaceholderArgs = new HashMap<>();
        final Map<String, Boolean> pathPlaceholderEncodes = new HashMap<>();
        for (RangeReplaceSubstitution sub : pathSubstitutions) {
            pathPlaceholderArgs.put(sub.getUriParameterName(), sub.getMethodParameterIndex());
            pathPlaceholderEncodes.put(sub.getUriParameterName(), sub.shouldEncode());
        }
        this.pathTemplate = parseUrlTemplate(relativePath, pathPlaceholderArgs, pathPlaceholderEncodes);

        this.bodyContentMethodParameterIndex = bodyContentMethodParameterIndex;
        this.bodyContentType = bodyContentType;
        this.bodyJavaType = bodyJavaType;
        Class<?>[] parameterTypes = swaggerMethod.getParameterTypes();
        int requestOptionsPosition = -1;
        int serverSentEventListenerPosition = -1;

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];

            // Check for the RequestContext position.
            // To retain previous behavior, only track the first instance found.
            if (parameterType == RequestContext.class && requestOptionsPosition == -1) {
                requestOptionsPosition = i;
            } else if (parameterType == ServerSentEventListener.class) {
                serverSentEventListenerPosition = i;
            }
        }

        this.requestOptionsPosition = requestOptionsPosition;
        this.serverSentEventListenerPosition = serverSentEventListenerPosition;
        this.headersEagerlyConverted = TypeUtil.isTypeOrSubTypeOf(Response.class, returnType);
        Type unwrappedReturnType = unwrapReturnType(returnType);
        this.returnTypeDecodable = isReturnTypeDecodable(unwrappedReturnType);

        if (!formSubstitutions.isEmpty()) {
            List<FormParameterSerializer> serializers = new ArrayList<>();
            for (Substitution substitution : formSubstitutions) {
                serializers.add(new FormParameterSerializer(substitution.getUriParameterName(),
                    substitution.getMethodParameterIndex(), substitution.shouldEncode()));
            }
            this.formParameterSerializers = Collections.unmodifiableList(serializers);
        } else {
            this.formParameterSerializers = null;
        }
    }

    private static List<UrlSegment> parseUrlTemplate(String template, Map<String, Integer> placeholderArgs,
        Map<String, Boolean> placeholderEncodes) {
        if (isNullOrEmpty(template)) {
            return Collections.emptyList();
        }

        List<UrlSegment> segments = new ArrayList<>();
        int lastIndex = 0;
        int openBraceIndex = template.indexOf('{');

        while (openBraceIndex != -1) {
            int closeBraceIndex = template.indexOf('}', openBraceIndex);
            if (closeBraceIndex == -1) {
                // Unmatched brace, treat the rest as a literal.
                break;
            }

            // Add the literal part before the placeholder.
            if (openBraceIndex > lastIndex) {
                segments.add(new StaticSegment(template.substring(lastIndex, openBraceIndex)));
            }

            // Add the placeholder segment.
            String placeholderName = template.substring(openBraceIndex + 1, closeBraceIndex);
            if (placeholderArgs.containsKey(placeholderName)) {
                int argIndex = placeholderArgs.get(placeholderName);
                boolean shouldEncode = placeholderEncodes.get(placeholderName);
                String fullPlaceholderText = template.substring(openBraceIndex, closeBraceIndex + 1);
                segments.add(new PlaceholderSegment(argIndex, shouldEncode, fullPlaceholderText));
            } else {
                // Placeholder isn't found in args, treat it as a literal.
                segments.add(new StaticSegment(template.substring(openBraceIndex, closeBraceIndex + 1)));
            }

            lastIndex = closeBraceIndex + 1;
            openBraceIndex = template.indexOf('{', lastIndex);
        }

        // Add any remaining literal text after the last placeholder.
        if (lastIndex < template.length()) {
            segments.add(new StaticSegment(template.substring(lastIndex)));
        }

        return segments;
    }

    private String buildFromTemplate(List<UrlSegment> template, Object[] methodArguments,
        CompositeSerializer serializer) {
        StringBuilder builder = new StringBuilder();
        for (UrlSegment segment : template) {
            segment.appendTo(builder, methodArguments, serializer);
        }
        return builder.toString();
    }

    /**
     * Get the fully qualified method that was called to invoke this HTTP request.
     *
     * @return The fully qualified method that was called to invoke this HTTP request.
     */
    public String getFullyQualifiedMethodName() {
        return fullyQualifiedMethodName;
    }

    /**
     * Gets the {@link ClientLogger} that will be used to log during the request and response.
     *
     * @return The {@link ClientLogger} that will be used to log during the request and response.
     */
    public ClientLogger getMethodLogger() {
        return logger;
    }

    /**
     * Get the HTTP method that will be used to complete the Swagger method's request.
     *
     * @return The HTTP method that will be used to complete the Swagger method's request.
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Sets the scheme and host to use for HTTP requests for this Swagger method.
     *
     * @param swaggerMethodArguments The arguments to use for scheme and host substitutions.
     * @param uriBuilder The {@link UriBuilder} that will have its scheme and host set.
     * @param serializer {@link CompositeSerializer} that is used to encode host substitutions.
     */
    public void setSchemeAndHost(Object[] swaggerMethodArguments, UriBuilder uriBuilder,
        CompositeSerializer serializer) {
        final String substitutedHost = buildFromTemplate(hostTemplate, swaggerMethodArguments, serializer);
        int index = substitutedHost.indexOf("://");

        if (index == -1) {
            uriBuilder.setHost(substitutedHost);
        } else {
            uriBuilder.setScheme(substitutedHost.substring(0, index));

            String host = substitutedHost.substring(index + 3);

            if (!isNullOrEmpty(host)) {
                uriBuilder.setHost(host);
            } else {
                uriBuilder.setHost(substitutedHost);
            }
        }
    }

    /**
     * Get the path that will be used to complete the Swagger method's request.
     *
     * @param methodArguments The method arguments to use with the path substitutions.
     * @param serializer {@link CompositeSerializer} that is used to encode path substitutions.
     *
     * @return The path value with its placeholders replaced by the matching substitutions.
     */
    public String setPath(Object[] methodArguments, CompositeSerializer serializer) {
        return buildFromTemplate(pathTemplate, methodArguments, serializer);
    }

    /**
     * Sets the encoded query parameters that have been added to this value based on the provided method arguments into
     * the passed {@link UriBuilder}.
     *
     * @param swaggerMethodArguments The arguments that will be used to create the query parameters' values.
     * @param uriBuilder The {@link UriBuilder} where the encoded query parameters will be set.
     * @param serializer {@link CompositeSerializer} that is used to encode the query parameters.
     */
    public void setEncodedQueryParameters(Object[] swaggerMethodArguments, UriBuilder uriBuilder,
        CompositeSerializer serializer) {
        // First, we add the constant query parameters.
        if (!isNullOrEmpty(staticQueryParams)) {
            uriBuilder.setQuery(staticQueryParams);
        }

        if (swaggerMethodArguments == null) {
            return;
        }

        for (QueryParameterProcessor processor : queryParameterProcessors) {
            processor.process(swaggerMethodArguments, uriBuilder, serializer);
        }
    }

    /**
     * Sets the headers that have been added to this value based on the provided method arguments into the passed
     * {@link HttpHeaders}.
     *
     * @param swaggerMethodArguments The arguments that will be used to create the headers' values.
     * @param headers The {@link HttpHeaders} where the header values will be set.
     * @param serializer {@link CompositeSerializer} that is used to serialize the header values.
     */
    public void setHeaders(Object[] swaggerMethodArguments, HttpHeaders headers, CompositeSerializer serializer) {
        headers.setAll(requestHeaders);

        if (swaggerMethodArguments == null) {
            return;
        }

        for (HeaderProcessor processor : headerProcessors) {
            processor.process(swaggerMethodArguments, headers, serializer);
        }
    }

    /**
     * Get the {@link RequestContext} passed into the proxy method.
     *
     * @param swaggerMethodArguments The arguments passed to the proxy method.
     *
     * @return The request options.
     */
    public RequestContext setRequestContext(Object[] swaggerMethodArguments) {
        return requestOptionsPosition < 0 ? null : (RequestContext) swaggerMethodArguments[requestOptionsPosition];
    }

    /**
     * Get the {@link ServerSentEventListener} passed into the service API.
     *
     * @param swaggerMethodArguments The arguments passed to the proxy method.
     *
     * @return The server sent event listener.
     */
    public ServerSentEventListener setServerSentEventListener(Object[] swaggerMethodArguments) {
        return serverSentEventListenerPosition < 0
            ? null
            : (ServerSentEventListener) swaggerMethodArguments[serverSentEventListenerPosition];
    }

    /**
     * Whether the provided response status code is one of the expected status codes for this Swagger method.
     *
     * <ol>
     *     <li>If the returned {@code int[]} is {@code null}, then all {@code 2XX} status codes are considered as
     * success code.</li>
    *         <li>If the returned {@code int[]} is not {@code null}, only the codes in the array are considered as success
     * code.</li>
     * </ol>
     *
     * @param statusCode The HTTP status code returned in a response.
     *
     * @return Whether the provided response status code is one of the expected status codes for this Swagger method.
     */
    @Override
    public boolean isExpectedResponseStatusCode(final int statusCode) {
        return expectedStatusCodes == null ? statusCode < 400 : expectedStatusCodes.get(statusCode);
    }

    /**
     * Get the {@link UnexpectedExceptionInformation} that will be used to generate a RestException if the HTTP response
     * status code is not one of the expected status codes.
     *
     * <p>If an UnexpectedExceptionInformation is not found for the status code the default
     * UnexpectedExceptionInformation will be returned.
     *
     * @param code Exception HTTP status code return from a REST API.
     *
     * @return The {@link UnexpectedExceptionInformation} to generate an exception to throw or return.
     */
    @Override
    public UnexpectedExceptionInformation getUnexpectedException(int code) {
        if (exceptionMapping == null) {
            exceptionMapping = processUnexpectedResponseExceptionTypes();
        }

        return exceptionMapping.getOrDefault(code, defaultException);
    }

    /**
     * Get the object to be used as the value of the HTTP request.
     *
     * @param swaggerMethodArguments The method arguments to get the value object from.
     * @param serializer The {@link CompositeSerializer} used to encode the request body if it's an
     * {@code application/x-www-form-urlencoded} request.
     *
     * @return The object that will be used as the body of the HTTP request.
     */
    public Object setBody(Object[] swaggerMethodArguments, CompositeSerializer serializer) {
        if (this.formParameterSerializers != null) {
            if (swaggerMethodArguments == null) {
                return null;
            }

            return formParameterSerializers.stream()
                .map(fps -> fps.serialize(swaggerMethodArguments, serializer))
                .filter(Objects::nonNull)
                .collect(Collectors.joining("&"));
        }

        if (bodyContentMethodParameterIndex != null
            && swaggerMethodArguments != null
            && 0 <= bodyContentMethodParameterIndex
            && bodyContentMethodParameterIndex < swaggerMethodArguments.length) {

            return swaggerMethodArguments[bodyContentMethodParameterIndex];
        }

        return null;
    }

    /**
     * Get the Content-Type of the body of this Swagger method.
     *
     * @return The Content-Type of the body of this Swagger method.
     */
    public String getBodyContentType() {
        return bodyContentType;
    }

    /**
     * Get the return type for the method that this object describes.
     *
     * @return The return type for the method that this object describes.
     */
    @Override
    public Type getReturnType() {
        return returnType;
    }

    /**
     * Get the type of the body parameter to this method, if present.
     *
     * @return The return type of the body parameter to this method.
     */
    public Type getBodyJavaType() {
        return bodyJavaType;
    }

    /**
     * Get the type that the return value will be sent across the network as. If returnValueWireType is not null, then
     * the raw HTTP response body will need to parsed to this type and then converted to the actual returnType.
     *
     * @return The type that the raw HTTP response body will be sent as.
     */
    @Override
    public Type getReturnValueWireType() {
        return returnValueWireType;
    }

    private static void addSerializedQueryParameter(CompositeSerializer adapter, Object value, boolean shouldEncode,
        UriBuilder uriBuilder, String parameterName) {
        String parameterValue = serialize(adapter, value);

        if (parameterValue != null) {
            if (shouldEncode) {
                parameterValue = UriEscapers.QUERY_ESCAPER.escape(parameterValue);
            }

            // Add parameter to the uriBuilder.
            uriBuilder.addQueryParameter(parameterName, parameterValue);
        }
    }

    private static String serialize(CompositeSerializer serializer, Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof ExpandableEnum) {
            value = serialize(serializer, ((ExpandableEnum<?>) value).getValue());
        }

        if (value instanceof String) {
            return (String) value;
        } else if (value.getClass().isPrimitive()
            || value.getClass().isEnum()
            || value instanceof Number
            || value instanceof Boolean
            || value instanceof Character
            || value instanceof DateTimeRfc1123) {

            return String.valueOf(value);
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).format(DateTimeFormatter.ISO_INSTANT);
        } else {
            try (AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream()) {
                serializer.serializeToStream(outputStream, value, SerializationFormat.JSON);

                return outputStream.toString(StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw CoreException.from(e);
            }
        }
    }

    private static String serializeAndEncodeFormValue(CompositeSerializer serializer, Object value,
        boolean shouldEncode) {
        if (value == null) {
            return null;
        }

        String serializedValue = serialize(serializer, value);

        return shouldEncode ? UriEscapers.FORM_ESCAPER.escape(serializedValue) : serializedValue;
    }

    private Map<Integer, UnexpectedExceptionInformation> processUnexpectedResponseExceptionTypes() {
        HashMap<Integer, UnexpectedExceptionInformation> exceptionHashMap = new HashMap<>();

        for (UnexpectedResponseExceptionDetail exceptionAnnotation : unexpectedResponseExceptionDetails) {
            UnexpectedExceptionInformation exception
                = new UnexpectedExceptionInformation(exceptionAnnotation.exceptionBodyClass());

            if (exceptionAnnotation.statusCode().length == 0) {
                defaultException = exception;
            } else {
                for (int statusCode : exceptionAnnotation.statusCode()) {
                    exceptionHashMap.put(statusCode, exception);
                }
            }
        }

        if (defaultException == null) {
            defaultException = new UnexpectedExceptionInformation(null);
        }

        return exceptionHashMap;
    }

    @Override
    public boolean isReturnTypeDecodable() {
        return returnTypeDecodable;
    }

    @Override
    public boolean isHeadersEagerlyConverted() {
        return headersEagerlyConverted;
    }

    public static boolean isReturnTypeDecodable(Type unwrappedReturnType) {
        if (unwrappedReturnType == null) {
            return false;
        }

        return !TypeUtil.isTypeOrSubTypeOf(unwrappedReturnType, BinaryData.class)
            && !TypeUtil.isTypeOrSubTypeOf(unwrappedReturnType, byte[].class)
            && !TypeUtil.isTypeOrSubTypeOf(unwrappedReturnType, ByteBuffer.class)
            && !TypeUtil.isTypeOrSubTypeOf(unwrappedReturnType, InputStream.class)
            && !TypeUtil.isTypeOrSubTypeOf(unwrappedReturnType, Void.TYPE)
            && !TypeUtil.isTypeOrSubTypeOf(unwrappedReturnType, Void.class);
    }

    public static Type unwrapReturnType(Type returnType) {
        if (returnType == null) {
            return null;
        }

        // Then check if the return type is assignable to Response. If it is, begin walking up the super type hierarchy
        // until the raw type implements Response. Then unwrap its only generic type.
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Response.class)) {
            // Handling for Response is slightly different as it is an interface unlike HttpResponse which is a class.
            // The super class hierarchy needs be walked until the super class itself implements Response.
            returnType = walkSuperTypesUntil(returnType, type -> typeImplementsInterface(type, Response.class));

            return unwrapReturnType(TypeUtil.getTypeArgument(returnType));
        }

        // Finally, there is no more unwrapping to perform and return The type as-is.
        return returnType;
    }

    /*
     * Helper method that walks up the super types until the type is an instance of the Class.
     */
    private static Type walkSuperTypesUntil(Type type, Predicate<Type> untilChecker) {
        while (!untilChecker.test(type)) {
            type = TypeUtil.getSuperType(type);
        }

        return type;
    }

    /**
     * An interface representing a segment of a URL template that can be appended to a StringBuilder.
     */
    private interface UrlSegment {
        /**
         * Appends the segment's value to the StringBuilder.
         *
         * @param builder The StringBuilder to append to.
         * @param methodArguments The arguments passed to the Swagger method.
         * @param serializer The serializer used for converting arguments to strings.
         */
        void appendTo(StringBuilder builder, Object[] methodArguments, CompositeSerializer serializer);
    }

    /**
     * A UrlSegment that represents a static, literal part of the URL.
     */
    private static class StaticSegment implements UrlSegment {
        private final String text;

        StaticSegment(String text) {
            this.text = text;
        }

        @Override
        public void appendTo(StringBuilder builder, Object[] methodArguments, CompositeSerializer serializer) {
            builder.append(text);
        }
    }

    /**
     * A UrlSegment that represents a dynamic placeholder that will be replaced with a method argument.
     */
    private static class PlaceholderSegment implements UrlSegment {
        private final int argumentIndex;
        private final boolean shouldEncode;
        private final String placeholderText; // The original text

        PlaceholderSegment(int argumentIndex, boolean shouldEncode, String placeholderText) {
            this.argumentIndex = argumentIndex;
            this.shouldEncode = shouldEncode;
            this.placeholderText = placeholderText;
        }

        @Override
        public void appendTo(StringBuilder builder, Object[] methodArguments, CompositeSerializer serializer) {
            if (methodArguments == null || argumentIndex < 0 || argumentIndex >= methodArguments.length) {
                builder.append(this.placeholderText);
                return;
            }

            final Object methodArgument = methodArguments[argumentIndex];
            String substitutionValue = serialize(serializer, methodArgument);

            if (substitutionValue != null && !substitutionValue.isEmpty() && shouldEncode) {
                substitutionValue = UriEscapers.PATH_ESCAPER.escape(substitutionValue);
            }
            // If a parameter is null, we treat it as empty string. This is
            // assuming no {...} will be allowed otherwise in a path template
            if (substitutionValue == null) {
                substitutionValue = "";
            }

            builder.append(substitutionValue);
        }
    }

    /**
     * Handles the serialization of a single form parameter, including list expansion and null-value skipping.
     */
    private static class FormParameterSerializer {
        private final String parameterName;
        private final int argumentIndex;
        private final boolean shouldEncode;

        FormParameterSerializer(String parameterName, int argumentIndex, boolean shouldEncode) {
            this.parameterName = parameterName;
            this.argumentIndex = argumentIndex;
            this.shouldEncode = shouldEncode;
        }

        /**
         * Serializes the method argument into a URL-encoded form string.
         *
         * @param methodArguments The arguments passed to the Swagger method.
         * @param serializer The serializer for converting values.
         * @return The serialized form parameter string, or null if the argument is null.
         */
        String serialize(Object[] methodArguments, CompositeSerializer serializer) {
            if (methodArguments == null || argumentIndex < 0 || argumentIndex >= methodArguments.length) {
                return null;
            }

            final Object methodArgument = methodArguments[argumentIndex];
            if (methodArgument == null) {
                return null;
            }

            String encodedKey = UriEscapers.FORM_ESCAPER.escape(parameterName);

            if (methodArgument instanceof List) {
                List<?> valueAsList = (List<?>) methodArgument;
                List<String> parts = new ArrayList<>();
                for (Object element : valueAsList) {
                    if (element != null) {
                        String formValue = serializeAndEncodeFormValue(serializer, element, shouldEncode);
                        parts.add(encodedKey + "=" + formValue);
                    }
                }

                return parts.isEmpty() ? null : String.join("&", parts);
            } else {
                String formValue = serializeAndEncodeFormValue(serializer, methodArgument, shouldEncode);
                return encodedKey + "=" + formValue;
            }
        }
    }

    /**
     * An interface for processing a query parameter.
     */
    private interface QueryParameterProcessor {
        void process(Object[] methodArguments, UriBuilder uriBuilder, CompositeSerializer serializer);
    }

    /**
     * An interface for processing a header parameter.
     */
    private interface HeaderProcessor {
        void process(Object[] methodArguments, HttpHeaders headers, CompositeSerializer serializer);
    }

    /**
     * Processes a single, non-list query parameter.
     */
    private static class SingleQueryParameterProcessor implements QueryParameterProcessor {
        private final String parameterName;
        private final int argumentIndex;
        private final boolean shouldEncode;

        SingleQueryParameterProcessor(String parameterName, int argumentIndex, boolean shouldEncode) {
            this.parameterName = parameterName;
            this.argumentIndex = argumentIndex;
            this.shouldEncode = shouldEncode;
        }

        @Override
        public void process(Object[] methodArguments, UriBuilder uriBuilder, CompositeSerializer serializer) {
            if (argumentIndex >= 0 && argumentIndex < methodArguments.length) {
                addSerializedQueryParameter(serializer, methodArguments[argumentIndex], shouldEncode, uriBuilder,
                    parameterName);
            }
        }
    }

    /**
     * Processes a list-based query parameter.
     */
    private static class ListQueryParameterProcessor implements QueryParameterProcessor {
        private final String parameterName;
        private final int argumentIndex;
        private final boolean shouldEncode;

        ListQueryParameterProcessor(String parameterName, int argumentIndex, boolean shouldEncode) {
            this.parameterName = parameterName;
            this.argumentIndex = argumentIndex;
            this.shouldEncode = shouldEncode;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void process(Object[] methodArguments, UriBuilder uriBuilder, CompositeSerializer serializer) {
            if (argumentIndex >= 0 && argumentIndex < methodArguments.length) {
                final Object methodArgument = methodArguments[argumentIndex];
                if (methodArgument instanceof List) {
                    for (Object element : (List<Object>) methodArgument) {
                        addSerializedQueryParameter(serializer, element, shouldEncode, uriBuilder, parameterName);
                    }
                } else {
                    addSerializedQueryParameter(serializer, methodArgument, shouldEncode, uriBuilder, parameterName);
                }
            }
        }
    }

    /**
     * Processes a single, non-map header.
     */
    private static class SimpleHeaderProcessor implements HeaderProcessor {
        private final HttpHeaderName headerName;
        private final int argumentIndex;

        SimpleHeaderProcessor(String headerName, int argumentIndex) {
            this.headerName = HttpHeaderName.fromString(headerName);
            this.argumentIndex = argumentIndex;
        }

        @Override
        public void process(Object[] methodArguments, HttpHeaders headers, CompositeSerializer serializer) {
            if (argumentIndex >= 0 && argumentIndex < methodArguments.length) {
                final String headerValue = serialize(serializer, methodArguments[argumentIndex]);
                if (headerValue != null) {
                    headers.set(headerName, headerValue);
                }
            }
        }
    }

    /**
     * Processes a map-based header collection.
     */
    private static class HeaderMapProcessor implements HeaderProcessor {
        private final String headerCollectionPrefix;
        private final int argumentIndex;

        HeaderMapProcessor(String headerCollectionPrefix, int argumentIndex) {
            this.headerCollectionPrefix = headerCollectionPrefix;
            this.argumentIndex = argumentIndex;
        }

        @Override
        public void process(Object[] methodArguments, HttpHeaders headers, CompositeSerializer serializer) {
            if (argumentIndex >= 0 && argumentIndex < methodArguments.length) {
                final Object methodArgument = methodArguments[argumentIndex];
                if (methodArgument instanceof Map) {
                    final Map<?, ?> headerCollection = (Map<?, ?>) methodArgument;

                    for (final Map.Entry<?, ?> headerEntry : headerCollection.entrySet()) {
                        final String headerName = headerCollectionPrefix + headerEntry.getKey();
                        final String headerValue = serialize(serializer, headerEntry.getValue());

                        if (headerValue != null) {
                            headers.set(HttpHeaderName.fromString(headerName), headerValue);
                        }
                    }
                }
            }
        }
    }
}
