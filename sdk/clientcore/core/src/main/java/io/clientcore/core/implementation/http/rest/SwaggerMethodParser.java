// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.RestProxy;
import io.clientcore.core.http.annotation.BodyParam;
import io.clientcore.core.http.annotation.FormParam;
import io.clientcore.core.http.annotation.HeaderParam;
import io.clientcore.core.http.annotation.HostParam;
import io.clientcore.core.http.annotation.HttpRequestInformation;
import io.clientcore.core.http.annotation.PathParam;
import io.clientcore.core.http.annotation.QueryParam;
import io.clientcore.core.http.annotation.UnexpectedResponseExceptionDetail;
import io.clientcore.core.http.exception.HttpExceptionType;
import io.clientcore.core.http.models.ContentType;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.implementation.http.UnexpectedExceptionInformation;
import io.clientcore.core.implementation.http.serializer.HttpResponseDecodeData;
import io.clientcore.core.implementation.util.Base64Uri;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import io.clientcore.core.implementation.util.UriBuilder;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.ExpandableEnum;
import io.clientcore.core.util.binarydata.BinaryData;
import io.clientcore.core.util.serializer.ObjectSerializer;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.clientcore.core.implementation.TypeUtil.typeImplementsInterface;
import static io.clientcore.core.implementation.util.ImplUtils.isNullOrEmpty;

/**
 * This class contains the metadata of a {@link Method} contained in a Swagger interface used to make REST API calls in
 * {@link RestProxy}.
 */
public class SwaggerMethodParser implements HttpResponseDecodeData {
    // TODO (alzimmer): There are many optimizations available to SwaggerMethodParser with regards to runtime.
    // The replacement locations and parameter ordering should remain consistent for the lifetime of an application,
    // so these values can be determined once and used for optimizations.
    // For example substitutions should be able to track which location in the raw value they replace without needing
    // to search the raw value on each call.
    private final String rawHost;
    private final String fullyQualifiedMethodName;
    private final ClientLogger methodLogger;
    private final HttpMethod httpMethod;
    private final String relativePath;
    private final Map<String, List<String>> queryParams = new LinkedHashMap<>();
    final List<RangeReplaceSubstitution> hostSubstitutions = new ArrayList<>();
    private final List<RangeReplaceSubstitution> pathSubstitutions = new ArrayList<>();
    private final List<QuerySubstitution> querySubstitutions = new ArrayList<>();
    private final List<Substitution> formSubstitutions = new ArrayList<>();
    private final List<HeaderSubstitution> headerSubstitutions = new ArrayList<>();
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
        this.rawHost = interfaceParser.getHost();
        final Class<?> swaggerInterface = swaggerMethod.getDeclaringClass();
        fullyQualifiedMethodName = swaggerInterface.getName() + "." + swaggerMethod.getName();
        methodLogger = new ClientLogger(fullyQualifiedMethodName);

        if (!swaggerMethod.isAnnotationPresent(HttpRequestInformation.class)) {
            // Should this also check whether there are multiple HTTP method annotations as well?
            throw new MissingRequiredAnnotationException(Collections.singletonList(HttpRequestInformation.class),
                swaggerMethod);
        }

        HttpRequestInformation httpRequestInformation =
            swaggerMethod.getAnnotation(HttpRequestInformation.class);

        this.httpMethod = httpRequestInformation.method();
        this.relativePath = httpRequestInformation.path();

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
                    paramValue = null;
                }

                List<String> currentValues = queryParams.get(paramName);

                if (!isNullOrEmpty(paramValue)) {
                    if (currentValues == null) {
                        currentValues = new ArrayList<>();
                    }

                    currentValues.add(paramValue);

                    queryParams.put(paramName, currentValues);
                } else {
                    queryParams.put(paramName, null);
                }
            }
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

        unexpectedResponseExceptionDetails =
            swaggerMethod.getAnnotationsByType(UnexpectedResponseExceptionDetail.class);

        Integer bodyContentMethodParameterIndex = null;
        String bodyContentType = null;
        Type bodyJavaType = null;
        final Annotation[][] allParametersAnnotations = swaggerMethod.getParameterAnnotations();

        for (int parameterIndex = 0; parameterIndex < allParametersAnnotations.length; ++parameterIndex) {
            final Annotation[] parameterAnnotations = swaggerMethod.getParameterAnnotations()[parameterIndex];

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

                    querySubstitutions.add(new QuerySubstitution(queryParamAnnotation.value(), parameterIndex,
                        !queryParamAnnotation.encoded(), queryParamAnnotation.multipleQueryParams()));
                } else if (annotationType.equals(HeaderParam.class)) {
                    final HeaderParam headerParamAnnotation = (HeaderParam) annotation;

                    headerSubstitutions.add(new HeaderSubstitution(headerParamAnnotation.value(), parameterIndex,
                        false));
                } else if (annotationType.equals(BodyParam.class)) {
                    final BodyParam bodyParamAnnotation = (BodyParam) annotation;
                    bodyContentMethodParameterIndex = parameterIndex;
                    bodyContentType = bodyParamAnnotation.value();
                    bodyJavaType = swaggerMethod.getGenericParameterTypes()[parameterIndex];
                } else if (annotationType.equals(FormParam.class)) {
                    final FormParam formParamAnnotation = (FormParam) annotation;

                    formSubstitutions.add(new Substitution(formParamAnnotation.value(), parameterIndex,
                        !formParamAnnotation.encoded()));

                    bodyContentType = ContentType.APPLICATION_X_WWW_FORM_URLENCODED;
                    bodyJavaType = String.class;
                }
            }
        }

        this.bodyContentMethodParameterIndex = bodyContentMethodParameterIndex;
        this.bodyContentType = bodyContentType;
        this.bodyJavaType = bodyJavaType;
        Class<?>[] parameterTypes = swaggerMethod.getParameterTypes();
        int requestOptionsPosition = -1;
        int serverSentEventListenerPosition = -1;

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];

            // Check for the RequestOptions position.
            // To retain previous behavior, only track the first instance found.
            if (parameterType == RequestOptions.class && requestOptionsPosition == -1) {
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
        return methodLogger;
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
     * @param serializer {@link ObjectSerializer} that is used to encode host substitutions.
     */
    public void setSchemeAndHost(Object[] swaggerMethodArguments, UriBuilder uriBuilder, ObjectSerializer serializer) {
        setSchemeAndHost(rawHost, hostSubstitutions, swaggerMethodArguments, uriBuilder, serializer);
    }

    static void setSchemeAndHost(String rawHost, List<RangeReplaceSubstitution> hostSubstitutions,
                                 Object[] swaggerMethodArguments, UriBuilder uriBuilder, ObjectSerializer serializer) {
        final String substitutedHost =
            applySubstitutions(rawHost, hostSubstitutions, swaggerMethodArguments, serializer);
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
     * @param serializer {@link ObjectSerializer} that is used to encode path substitutions.
     *
     * @return The path value with its placeholders replaced by the matching substitutions.
     */
    public String setPath(Object[] methodArguments, ObjectSerializer serializer) {
        return applySubstitutions(relativePath, pathSubstitutions, methodArguments, serializer);
    }

    /**
     * Sets the encoded query parameters that have been added to this value based on the provided method arguments into
     * the passed {@link UriBuilder}.
     *
     * @param swaggerMethodArguments The arguments that will be used to create the query parameters' values.
     * @param uriBuilder The {@link UriBuilder} where the encoded query parameters will be set.
     * @param serializer {@link ObjectSerializer} that is used to encode the query parameters.
     */
    @SuppressWarnings("unchecked")
    public void setEncodedQueryParameters(Object[] swaggerMethodArguments, UriBuilder uriBuilder,
                                          ObjectSerializer serializer) {
        // First we add the constant query parameters.
        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                uriBuilder.addQueryParameter(entry.getKey(), null);
            } else {
                for (String paramValue : entry.getValue()) {
                    uriBuilder.addQueryParameter(entry.getKey(), paramValue);
                }
            }
        }

        if (swaggerMethodArguments == null) {
            return;
        }

        // Then we add query parameters passed as arguments to the request method. If any of them share a name with the
        // constant query parameters, the constant query parameter will be overwritten.
        for (QuerySubstitution substitution : querySubstitutions) {
            final int parameterIndex = substitution.getMethodParameterIndex();

            if (0 <= parameterIndex && parameterIndex < swaggerMethodArguments.length) {
                final Object methodArgument = swaggerMethodArguments[substitution.getMethodParameterIndex()];

                if (substitution.mergeParameters() && methodArgument instanceof List) {
                    List<Object> methodArguments = (List<Object>) methodArgument;

                    for (Object argument : methodArguments) {
                        addSerializedQueryParameter(serializer, argument, substitution.shouldEncode(), uriBuilder, substitution.getUriParameterName());
                    }
                } else {
                    addSerializedQueryParameter(serializer, methodArgument, substitution.shouldEncode(), uriBuilder, substitution.getUriParameterName());
                }
            }
        }
    }

    /**
     * Sets the headers that have been added to this value based on the provided method arguments into the passed
     * {@link HttpHeaders}.
     *
     * @param swaggerMethodArguments The arguments that will be used to create the headers' values.
     * @param headers The {@link HttpHeaders} where the header values will be set.
     * @param serializer {@link ObjectSerializer} that is used to serialize the header values.
     */
    public void setHeaders(Object[] swaggerMethodArguments, HttpHeaders headers, ObjectSerializer serializer) {
        headers.setAll(requestHeaders);

        if (swaggerMethodArguments == null) {
            return;
        }

        for (HeaderSubstitution headerSubstitution : headerSubstitutions) {
            final int parameterIndex = headerSubstitution.getMethodParameterIndex();

            if (0 <= parameterIndex && parameterIndex < swaggerMethodArguments.length) {
                final Object methodArgument = swaggerMethodArguments[headerSubstitution.getMethodParameterIndex()];

                if (methodArgument instanceof Map) {
                    @SuppressWarnings("unchecked") final Map<HttpHeaderName, ?> headerCollection =
                        (Map<HttpHeaderName, ?>) methodArgument;
                    final String headerCollectionPrefix = headerSubstitution.getUriParameterName();

                    for (final Map.Entry<HttpHeaderName, ?> headerCollectionEntry : headerCollection.entrySet()) {
                        final String headerName = headerCollectionPrefix + headerCollectionEntry.getKey();
                        final String headerValue = serialize(serializer, headerCollectionEntry.getValue());

                        if (headerValue != null) {
                            headers.set(HttpHeaderName.fromString(headerName), headerValue);
                        }
                    }
                } else {
                    final String headerValue = serialize(serializer, methodArgument);

                    if (headerValue != null) {
                        headers.set(headerSubstitution.getHeaderName(), headerValue);
                    }
                }
            }
        }
    }

    /**
     * Get the {@link RequestOptions} passed into the proxy method.
     *
     * @param swaggerMethodArguments The arguments passed to the proxy method.
     *
     * @return The request options.
     */
    public RequestOptions setRequestOptions(Object[] swaggerMethodArguments) {
        return requestOptionsPosition < 0 ? null : (RequestOptions) swaggerMethodArguments[requestOptionsPosition];
    }

    /**
     * Get the {@link ServerSentEventListener} passed into the service API.
     *
     * @param swaggerMethodArguments The arguments passed to the proxy method.
     *
     * @return The server sent event listener.
     */
    public ServerSentEventListener setServerSentEventListener(Object[] swaggerMethodArguments) {
        return serverSentEventListenerPosition < 0 ? null
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
     * @param serializer The {@link ObjectSerializer} used to encode the request body if it's an
     * {@code application/x-www-form-urlencoded} request.
     *
     * @return The object that will be used as the body of the HTTP request.
     */
    public Object setBody(Object[] swaggerMethodArguments, ObjectSerializer serializer) {
        Object result = null;

        if (bodyContentMethodParameterIndex != null
            && swaggerMethodArguments != null
            && 0 <= bodyContentMethodParameterIndex
            && bodyContentMethodParameterIndex < swaggerMethodArguments.length) {

            result = swaggerMethodArguments[bodyContentMethodParameterIndex];
        }

        if (!isNullOrEmpty(formSubstitutions) && swaggerMethodArguments != null) {
            result = formSubstitutions.stream()
                .map(substitution -> serializeFormData(serializer, substitution.getUriParameterName(),
                    swaggerMethodArguments[substitution.getMethodParameterIndex()], substitution.shouldEncode()))
                .filter(Objects::nonNull)
                .collect(Collectors.joining("&"));
        }

        return result;
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

    private static void addSerializedQueryParameter(ObjectSerializer adapter, Object value, boolean shouldEncode,
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

    private static String serialize(ObjectSerializer serializer, Object value) {
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
                serializer.serializeToStream(outputStream, value);

                return outputStream.toString(StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static String serializeFormData(ObjectSerializer serializer, String key, Object value,
                                            boolean shouldEncode) {
        if (value == null) {
            return null;
        }

        String encodedKey = UriEscapers.FORM_ESCAPER.escape(key);

        if (value instanceof List<?>) {
            return ((List<?>) value).stream()
                .map(element -> serializeAndEncodeFormValue(serializer, element, shouldEncode))
                .filter(Objects::nonNull)
                .map(formValue -> encodedKey + "=" + formValue)
                .collect(Collectors.joining("&"));
        } else {
            return encodedKey + "=" + serializeAndEncodeFormValue(serializer, value, shouldEncode);
        }
    }

    private static String serializeAndEncodeFormValue(ObjectSerializer serializer, Object value, boolean shouldEncode) {
        if (value == null) {
            return null;
        }

        String serializedValue = serialize(serializer, value);

        return shouldEncode ? UriEscapers.FORM_ESCAPER.escape(serializedValue) : serializedValue;
    }

    private static String applySubstitutions(String originalValue, List<RangeReplaceSubstitution> substitutions,
                                             Object[] methodArguments, ObjectSerializer serializer) {
        if (methodArguments == null || isNullOrEmpty(substitutions)) {
            return originalValue;
        }

        int originalSize = originalValue.length();
        int substitutionSize = originalSize;
        SortedMap<RangeReplaceSubstitution.Range, String> replacements = new TreeMap<>();

        for (RangeReplaceSubstitution substitution : substitutions) {
            final int substitutionParameterIndex = substitution.getMethodParameterIndex();

            if (substitutionParameterIndex >= 0 && substitutionParameterIndex < methodArguments.length) {
                final Object methodArgument = methodArguments[substitutionParameterIndex];

                String substitutionValue = serialize(serializer, methodArgument);

                if (substitutionValue != null && !substitutionValue.isEmpty() && substitution.shouldEncode()) {
                    substitutionValue = UriEscapers.PATH_ESCAPER.escape(substitutionValue);
                }
                // if a parameter is null, we treat it as empty string. This is
                // assuming no {...} will be allowed otherwise in a path template
                if (substitutionValue == null) {
                    substitutionValue = "";
                }

                for (RangeReplaceSubstitution.Range range : substitution.getRanges()) {
                    substitutionSize += substitutionValue.length() - range.getSize();

                    replacements.put(range, substitutionValue);
                }
            }
        }

        int last = 0;
        StringBuilder builder = new StringBuilder(substitutionSize);

        for (Map.Entry<RangeReplaceSubstitution.Range, String> replacement : replacements.entrySet()) {
            if (last < replacement.getKey().getStart()) {
                builder.append(originalValue, last, replacement.getKey().getStart());
            }

            builder.append(replacement.getValue());

            last = replacement.getKey().getEnd();
        }

        if (last < originalSize) {
            builder.append(originalValue, last, originalSize);
        }

        return builder.toString();
    }

    private Map<Integer, UnexpectedExceptionInformation> processUnexpectedResponseExceptionTypes() {
        HashMap<Integer, UnexpectedExceptionInformation> exceptionHashMap = new HashMap<>();

        for (UnexpectedResponseExceptionDetail exceptionAnnotation : unexpectedResponseExceptionDetails) {
            UnexpectedExceptionInformation exception =
                new UnexpectedExceptionInformation(
                    HttpExceptionType.fromString(exceptionAnnotation.exceptionTypeName()),
                    exceptionAnnotation.exceptionBodyClass());

            if (exceptionAnnotation.statusCode().length == 0) {
                defaultException = exception;
            } else {
                for (int statusCode : exceptionAnnotation.statusCode()) {
                    exceptionHashMap.put(statusCode, exception);
                }
            }
        }

        if (defaultException == null) {
            defaultException = new UnexpectedExceptionInformation(null, null);
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
}
