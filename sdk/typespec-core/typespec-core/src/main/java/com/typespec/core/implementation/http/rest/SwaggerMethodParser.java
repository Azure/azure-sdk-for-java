// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.http.rest;

import com.typespec.core.annotation.BodyParam;
import com.typespec.core.annotation.Delete;
import com.typespec.core.annotation.ExpectedResponses;
import com.typespec.core.annotation.FormParam;
import com.typespec.core.annotation.Get;
import com.typespec.core.annotation.Head;
import com.typespec.core.annotation.HeaderParam;
import com.typespec.core.annotation.Headers;
import com.typespec.core.annotation.HostParam;
import com.typespec.core.annotation.Options;
import com.typespec.core.annotation.Patch;
import com.typespec.core.annotation.PathParam;
import com.typespec.core.annotation.Post;
import com.typespec.core.annotation.Put;
import com.typespec.core.annotation.QueryParam;
import com.typespec.core.annotation.ReturnValueWireType;
import com.typespec.core.annotation.UnexpectedResponseExceptionType;
import com.typespec.core.exception.HttpResponseException;
import com.typespec.core.http.ContentType;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.rest.Page;
import com.typespec.core.http.rest.RequestOptions;
import com.typespec.core.http.rest.Response;
import com.typespec.core.http.rest.ResponseBase;
import com.typespec.core.http.rest.RestProxy;
import com.typespec.core.http.rest.StreamResponse;
import com.typespec.core.implementation.TypeUtil;
import com.typespec.core.implementation.http.UnexpectedExceptionInformation;
import com.typespec.core.implementation.serializer.HttpResponseDecodeData;
import com.typespec.core.util.Base64Url;
import com.typespec.core.util.BinaryData;
import com.typespec.core.util.Context;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.DateTimeRfc1123;
import com.typespec.core.util.ExpandableStringEnum;
import com.typespec.core.util.UrlBuilder;
import com.typespec.core.util.serializer.SerializerAdapter;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.typespec.core.implementation.TypeUtil.getRawClass;
import static com.typespec.core.implementation.TypeUtil.typeImplementsInterface;

/**
 * This class contains the metadata of a {@link Method} contained in a Swagger interface used to make REST API calls in
 * {@link RestProxy}.
 */
public class SwaggerMethodParser implements HttpResponseDecodeData {
    private static final List<Class<? extends Annotation>> REQUIRED_HTTP_METHODS =
        Arrays.asList(Delete.class, Get.class, Head.class, Options.class, Patch.class, Post.class, Put.class);

    // TODO (alzimmer): There are many optimizations available to SwaggerMethodParser with regards to runtime.
    // The replacement locations and parameter ordering should remain consistent for the lifetime of an application,
    // so these values can be determined once and used for optimizations.
    // For example substitutions should be able to track which location in the raw value they replace without needing
    // to search the raw value on each call.
    private final String rawHost;
    private final String fullyQualifiedMethodName;
    private final HttpMethod httpMethod;
    private final String relativePath;
    final List<RangeReplaceSubstitution> hostSubstitutions = new ArrayList<>();
    private final List<RangeReplaceSubstitution> pathSubstitutions = new ArrayList<>();
    private final List<QuerySubstitution> querySubstitutions = new ArrayList<>();
    private final List<Substitution> formSubstitutions = new ArrayList<>();
    private final List<HeaderSubstitution> headerSubstitutions = new ArrayList<>();
    private final HttpHeaders headers = new HttpHeaders();
    private final Integer bodyContentMethodParameterIndex;
    private final String bodyContentType;
    private final Type bodyJavaType;
    private final BitSet expectedStatusCodes;
    private final Type returnType;
    private final Type returnValueWireType;
    private final UnexpectedResponseExceptionType[] unexpectedResponseExceptionTypes;
    private final int contextPosition;
    private final int requestOptionsPosition;
    private final boolean isReactive;
    private final boolean isStreamResponse;
    private final boolean returnTypeDecodeable;
    private final boolean responseEagerlyRead;
    private final boolean ignoreResponseBody;
    private final boolean headersEagerlyConverted;
    private final String spanName;

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

    @SuppressWarnings("deprecation")
    SwaggerMethodParser(SwaggerInterfaceParser interfaceParser, Method swaggerMethod) {
        this.rawHost = interfaceParser.getHost();

        final Class<?> swaggerInterface = swaggerMethod.getDeclaringClass();

        fullyQualifiedMethodName = swaggerInterface.getName() + "." + swaggerMethod.getName();

        if (swaggerMethod.isAnnotationPresent(Get.class)) {
            this.httpMethod = HttpMethod.GET;
            this.relativePath = swaggerMethod.getAnnotation(Get.class).value();
        } else if (swaggerMethod.isAnnotationPresent(Put.class)) {
            this.httpMethod = HttpMethod.PUT;
            this.relativePath = swaggerMethod.getAnnotation(Put.class).value();
        } else if (swaggerMethod.isAnnotationPresent(Head.class)) {
            this.httpMethod = HttpMethod.HEAD;
            this.relativePath = swaggerMethod.getAnnotation(Head.class).value();
        } else if (swaggerMethod.isAnnotationPresent(Delete.class)) {
            this.httpMethod = HttpMethod.DELETE;
            this.relativePath = swaggerMethod.getAnnotation(Delete.class).value();
        } else if (swaggerMethod.isAnnotationPresent(Post.class)) {
            this.httpMethod = HttpMethod.POST;
            this.relativePath = swaggerMethod.getAnnotation(Post.class).value();
        } else if (swaggerMethod.isAnnotationPresent(Patch.class)) {
            this.httpMethod = HttpMethod.PATCH;
            this.relativePath = swaggerMethod.getAnnotation(Patch.class).value();
        } else if (swaggerMethod.isAnnotationPresent(Options.class)) {
            this.httpMethod = HttpMethod.OPTIONS;
            this.relativePath = swaggerMethod.getAnnotation(Options.class).value();
        } else {
            // Should this also check whether there are multiple HTTP method annotations as well?
            throw new MissingRequiredAnnotationException(REQUIRED_HTTP_METHODS, swaggerMethod);
        }

        returnType = swaggerMethod.getGenericReturnType();

        final ReturnValueWireType returnValueWireTypeAnnotation =
            swaggerMethod.getAnnotation(ReturnValueWireType.class);
        if (returnValueWireTypeAnnotation != null) {
            Class<?> returnValueWireType = returnValueWireTypeAnnotation.value();
            if (returnValueWireType == Base64Url.class || returnValueWireType == DateTimeRfc1123.class) {
                this.returnValueWireType = returnValueWireType;
            } else if (TypeUtil.isTypeOrSubTypeOf(returnValueWireType, List.class)) {
                this.returnValueWireType = returnValueWireType.getGenericInterfaces()[0];
            } else if (TypeUtil.isTypeOrSubTypeOf(returnValueWireType, Page.class)) {
                this.returnValueWireType = returnValueWireType;
            } else {
                this.returnValueWireType = null;
            }
        } else {
            this.returnValueWireType = null;
        }

        if (swaggerMethod.isAnnotationPresent(Headers.class)) {
            final Headers headersAnnotation = swaggerMethod.getAnnotation(Headers.class);
            final String[] headers = headersAnnotation.value();
            for (final String header : headers) {
                final int colonIndex = header.indexOf(":");
                if (colonIndex >= 0) {
                    final String headerName = header.substring(0, colonIndex).trim();
                    if (!headerName.isEmpty()) {
                        final String headerValue = header.substring(colonIndex + 1).trim();
                        if (!headerValue.isEmpty()) {
                            if (headerValue.contains(",")) {
                                // there are multiple values for this header, so we split them out.
                                this.headers.set(headerName, Arrays.asList(headerValue.split(",")));
                            } else {
                                this.headers.set(headerName, headerValue);
                            }
                        }
                    }
                }
            }
        }

        final ExpectedResponses expectedResponses = swaggerMethod.getAnnotation(ExpectedResponses.class);
        if (expectedResponses != null && expectedResponses.value().length > 0) {
            expectedStatusCodes = new BitSet();
            for (int code : expectedResponses.value()) {
                expectedStatusCodes.set(code);
            }
        } else {
            expectedStatusCodes = null;
        }

        unexpectedResponseExceptionTypes = swaggerMethod.getAnnotationsByType(UnexpectedResponseExceptionType.class);

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

        boolean isReactiveMethod = isReactiveType(returnType);
        Class<?>[] parameterTypes = swaggerMethod.getParameterTypes();
        int contextPosition = -1;
        int requestOptionsPosition = -1;
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            // Check for the Context and RequestOptions position.
            // To retain previous behavior, only track the first instance found.
            if (parameterType == Context.class && contextPosition == -1) {
                contextPosition = i;
            } else if (parameterType == RequestOptions.class && requestOptionsPosition == -1) {
                requestOptionsPosition = i;
            }
            isReactiveMethod = isReactiveMethod || isReactiveType(parameterType);
        }

        this.isReactive = isReactiveMethod;
        if (isReactiveMethod) {
            Type reactiveTypeArgument = TypeUtil.getTypeArgument(returnType);
            this.isStreamResponse = isStreamResponseType(reactiveTypeArgument);
            this.headersEagerlyConverted = TypeUtil.isTypeOrSubTypeOf(ResponseBase.class, reactiveTypeArgument);
        } else {
            this.isStreamResponse = isStreamResponseType(returnType);
            this.headersEagerlyConverted = TypeUtil.isTypeOrSubTypeOf(ResponseBase.class, returnType);
        }
        this.contextPosition = contextPosition;
        this.requestOptionsPosition = requestOptionsPosition;

        Type unwrappedReturnType = unwrapReturnType(returnType);
        this.returnTypeDecodeable = isReturnTypeDecodeable(unwrappedReturnType);
        this.responseEagerlyRead = isResponseEagerlyRead(unwrappedReturnType);
        this.ignoreResponseBody = isResponseBodyIgnored(unwrappedReturnType);
        this.spanName = interfaceParser.getServiceName() + "." + swaggerMethod.getName();
    }

    /**
     * Get the fully qualified method that was called to invoke this HTTP request.
     *
     * @return the fully qualified method that was called to invoke this HTTP request
     */
    public String getFullyQualifiedMethodName() {
        return fullyQualifiedMethodName;
    }

    /**
     * Get the HTTP method that will be used to complete the Swagger method's request.
     *
     * @return the HTTP method that will be used to complete the Swagger method's request
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Sets the scheme and host to use for HTTP requests for this Swagger method.
     *
     * @param swaggerMethodArguments The arguments to use for scheme and host substitutions.
     * @param urlBuilder The {@link UrlBuilder} that will have its scheme and host set.
     * @param serializer {@link SerializerAdapter} that is used to encode host substitutions.
     */
    public void setSchemeAndHost(Object[] swaggerMethodArguments, UrlBuilder urlBuilder, SerializerAdapter serializer) {
        setSchemeAndHost(rawHost, hostSubstitutions, swaggerMethodArguments, urlBuilder, serializer);
    }

    static void setSchemeAndHost(String rawHost, List<RangeReplaceSubstitution> hostSubstitutions,
        Object[] swaggerMethodArguments, UrlBuilder urlBuilder, SerializerAdapter serializer) {
        final String substitutedHost = applySubstitutions(rawHost, hostSubstitutions, swaggerMethodArguments,
            serializer);
        int index = substitutedHost.indexOf("://");
        if (index == -1) {
            urlBuilder.setHost(substitutedHost);
        } else {
            urlBuilder.setScheme(substitutedHost.substring(0, index));
            String host = substitutedHost.substring(index + 3);
            if (!CoreUtils.isNullOrEmpty(host)) {
                urlBuilder.setHost(host);
            } else {
                urlBuilder.setHost(substitutedHost);
            }
        }
    }

    /**
     * Get the path that will be used to complete the Swagger method's request.
     *
     * @param methodArguments the method arguments to use with the path substitutions
     * @param serializer {@link SerializerAdapter} that is used to encode path substitutions
     * @return the path value with its placeholders replaced by the matching substitutions
     */
    public String setPath(Object[] methodArguments, SerializerAdapter serializer) {
        return applySubstitutions(relativePath, pathSubstitutions, methodArguments, serializer);
    }

    /**
     * Sets the encoded query parameters that have been added to this value based on the provided method arguments into
     * the passed {@link UrlBuilder}.
     *
     * @param swaggerMethodArguments the arguments that will be used to create the query parameters' values
     * @param urlBuilder The {@link UrlBuilder} where the encoded query parameters will be set.
     * @param serializer {@link SerializerAdapter} that is used to encode the query parameters.
     */
    @SuppressWarnings("unchecked")
    public void setEncodedQueryParameters(Object[] swaggerMethodArguments, UrlBuilder urlBuilder,
        SerializerAdapter serializer) {
        if (swaggerMethodArguments == null) {
            return;
        }

        for (QuerySubstitution substitution : querySubstitutions) {
            final int parameterIndex = substitution.getMethodParameterIndex();
            if (0 <= parameterIndex && parameterIndex < swaggerMethodArguments.length) {
                final Object methodArgument = swaggerMethodArguments[substitution.getMethodParameterIndex()];

                if (substitution.mergeParameters() && methodArgument instanceof List) {
                    List<Object> methodArguments = (List<Object>) methodArgument;
                    for (Object argument : methodArguments) {
                        addSerializedQueryParameter(serializer, argument, substitution.shouldEncode(),
                            urlBuilder, substitution.getUrlParameterName());
                    }
                } else {
                    addSerializedQueryParameter(serializer, methodArgument, substitution.shouldEncode(),
                        urlBuilder, substitution.getUrlParameterName());
                }
            }
        }
    }

    /**
     * Sets the headers that have been added to this value based on the provided method arguments into the passed {@link
     * HttpHeaders}.
     *
     * @param swaggerMethodArguments The arguments that will be used to create the headers' values.
     * @param httpHeaders The {@link HttpHeaders} where the header values will be set.
     * @param serializer {@link SerializerAdapter} that is used to serialize the header values.
     */
    @SuppressWarnings("deprecation")
    public void setHeaders(Object[] swaggerMethodArguments, HttpHeaders httpHeaders, SerializerAdapter serializer) {
        httpHeaders.setAllHttpHeaders(headers);

        if (swaggerMethodArguments == null) {
            return;
        }

        for (HeaderSubstitution headerSubstitution : headerSubstitutions) {
            final int parameterIndex = headerSubstitution.getMethodParameterIndex();
            if (0 <= parameterIndex && parameterIndex < swaggerMethodArguments.length) {
                final Object methodArgument = swaggerMethodArguments[headerSubstitution.getMethodParameterIndex()];
                if (methodArgument instanceof Map) {
                    @SuppressWarnings("unchecked") final Map<String, ?> headerCollection =
                        (Map<String, ?>) methodArgument;
                    final String headerCollectionPrefix = headerSubstitution.getUrlParameterName();
                    for (final Map.Entry<String, ?> headerCollectionEntry : headerCollection.entrySet()) {
                        final String headerName = headerCollectionPrefix + headerCollectionEntry.getKey();
                        final String headerValue = serialize(serializer, headerCollectionEntry.getValue());
                        if (headerValue != null) {
                            httpHeaders.set(headerName, headerValue);
                        }
                    }
                } else {
                    final String headerValue = serialize(serializer, methodArgument);
                    if (headerValue != null) {
                        httpHeaders.set(headerSubstitution.getHeaderName(), headerValue);
                    }
                }
            }
        }
    }

    /**
     * Get the {@link Context} passed into the proxy method.
     *
     * @param swaggerMethodArguments the arguments passed to the proxy method
     * @return the context, or {@link Context#NONE} if no context was provided
     */
    public Context setContext(Object[] swaggerMethodArguments) {
        // Context was never found as a parameter in the Method, therefore always return Context.NONE.
        if (contextPosition < 0) {
            return Context.NONE;
        }

        Context context = (Context) swaggerMethodArguments[contextPosition];
        return (context != null) ? context : Context.NONE;
    }

    /**
     * Get the {@link RequestOptions} passed into the proxy method.
     *
     * @param swaggerMethodArguments the arguments passed to the proxy method
     * @return the request options
     */
    public RequestOptions setRequestOptions(Object[] swaggerMethodArguments) {
        return requestOptionsPosition < 0 ? null : (RequestOptions) swaggerMethodArguments[requestOptionsPosition];
    }

    /**
     * Whether the provided response status code is one of the expected status codes for this Swagger method.
     * <p>
     * 1. If the returned int[] is null, then all 2XX status codes are considered as success code. 2. If the returned
     * int[] is not-null, only the codes in the array are considered as success code.
     *
     * @param statusCode The HTTP status code returned in a response.
     * @return Whether the provided response status code is one of the expected status codes for this Swagger method
     */
    @Override
    public boolean isExpectedResponseStatusCode(final int statusCode) {
        return expectedStatusCodes == null
            ? statusCode < 400
            : expectedStatusCodes.get(statusCode);
    }

    /**
     * Get the {@link UnexpectedExceptionInformation} that will be used to generate a RestException if the HTTP response
     * status code is not one of the expected status codes.
     * <p>
     * If an UnexpectedExceptionInformation is not found for the status code the default UnexpectedExceptionInformation
     * will be returned.
     *
     * @param code Exception HTTP status code return from a REST API.
     * @return the UnexpectedExceptionInformation to generate an exception to throw or return.
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
     * @param swaggerMethodArguments the method arguments to get the value object from
     * @param serializer {@link SerializerAdapter} used to encode the request body if it's an
     * {@code application/x-www-form-urlencoded} request.
     * @return the object that will be used as the body of the HTTP request
     */
    public Object setBody(Object[] swaggerMethodArguments, SerializerAdapter serializer) {
        Object result = null;

        if (bodyContentMethodParameterIndex != null
            && swaggerMethodArguments != null
            && 0 <= bodyContentMethodParameterIndex
            && bodyContentMethodParameterIndex < swaggerMethodArguments.length) {
            result = swaggerMethodArguments[bodyContentMethodParameterIndex];
        }

        if (!CoreUtils.isNullOrEmpty(formSubstitutions) && swaggerMethodArguments != null) {
            result = formSubstitutions.stream()
                .map(substitution -> serializeFormData(serializer, substitution.getUrlParameterName(),
                    swaggerMethodArguments[substitution.getMethodParameterIndex()], substitution.shouldEncode()))
                .filter(Objects::nonNull)
                .collect(Collectors.joining("&"));
        }

        return result;
    }

    /**
     * Get the Content-Type of the body of this Swagger method.
     *
     * @return the Content-Type of the body of this Swagger method
     */
    public String getBodyContentType() {
        return bodyContentType;
    }

    /**
     * Get the return type for the method that this object describes.
     *
     * @return the return type for the method that this object describes.
     */
    @Override
    public Type getReturnType() {
        return returnType;
    }


    /**
     * Get the type of the body parameter to this method, if present.
     *
     * @return the return type of the body parameter to this method
     */
    public Type getBodyJavaType() {
        return bodyJavaType;
    }

    /**
     *
     * Get the type that the return value will be sent across the network as. If returnValueWireType is not null, then
     * the raw HTTP response body will need to parsed to this type and then converted to the actual returnType.
     *
     * @return the type that the raw HTTP response body will be sent as
     */
    @Override
    public Type getReturnValueWireType() {
        return returnValueWireType;
    }

    private static void addSerializedQueryParameter(SerializerAdapter adapter, Object value, boolean shouldEncode,
                                                    UrlBuilder urlBuilder, String parameterName) {

        String parameterValue = serialize(adapter, value);

        if (parameterValue != null) {
            if (shouldEncode) {
                parameterValue = UrlEscapers.QUERY_ESCAPER.escape(parameterValue);
            }

            // add parameter to the urlBuilder
            urlBuilder.addQueryParameter(parameterName, parameterValue);
        }
    }

    private static String serialize(SerializerAdapter serializer, Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        } else if (value.getClass().isPrimitive()
            || value instanceof Number
            || value instanceof Boolean
            || value instanceof Character
            || value instanceof DateTimeRfc1123) {
            return String.valueOf(value);
        } else if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).format(DateTimeFormatter.ISO_INSTANT);
        } else if (value instanceof ExpandableStringEnum<?> || value.getClass().isEnum()) {
            // Enum and ExpandableStringEnum need special handling as these could be wrapping a null String which would
            // be "null" is serialized with JacksonAdapter.
            String stringValue = String.valueOf(value);
            return (stringValue == null) ? "null" : stringValue;
        } else {
            return serializer.serializeRaw(value);
        }
    }

    private static String serializeFormData(SerializerAdapter serializer, String key, Object value,
                                            boolean shouldEncode) {
        if (value == null) {
            return null;
        }

        String encodedKey = UrlEscapers.FORM_ESCAPER.escape(key);
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

    private static String serializeAndEncodeFormValue(SerializerAdapter serializer, Object value,
        boolean shouldEncode) {
        if (value == null) {
            return null;
        }

        String serializedValue = serialize(serializer, value);
        return shouldEncode ? UrlEscapers.FORM_ESCAPER.escape(serializedValue) : serializedValue;
    }

    private static String applySubstitutions(String originalValue, List<RangeReplaceSubstitution> substitutions,
        Object[] methodArguments, SerializerAdapter serializer) {
        if (methodArguments == null || CoreUtils.isNullOrEmpty(substitutions)) {
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
                    substitutionValue = UrlEscapers.PATH_ESCAPER.escape(substitutionValue);
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

        for (UnexpectedResponseExceptionType exceptionAnnotation : unexpectedResponseExceptionTypes) {
            UnexpectedExceptionInformation exception = new UnexpectedExceptionInformation(exceptionAnnotation.value());
            if (exceptionAnnotation.code().length == 0) {
                defaultException = exception;
            } else {
                for (int statusCode : exceptionAnnotation.code()) {
                    exceptionHashMap.put(statusCode, exception);
                }
            }
        }

        if (defaultException == null) {
            defaultException = new UnexpectedExceptionInformation(HttpResponseException.class);
        }

        return exceptionHashMap;
    }

    /**
     * Indicates whether the swagger method is of Reactive type or not.
     * To be classified as reactive method, either the return type or one of the input parameters must
     * be one of {@link Mono}, {@link Flux} or {@link Publisher}.
     *
     * @return the boolean flag indicating whether the swagger method is reactive or not.
     */
    public boolean isReactive() {
        return isReactive;
    }

    boolean isReactiveType(Type type) {
        return (TypeUtil.isTypeOrSubTypeOf(type, Mono.class) || TypeUtil.isTypeOrSubTypeOf(type, Flux.class)
            || TypeUtil.isTypeOrSubTypeOf(type, Publisher.class));
    }

    boolean isStreamResponseType(Type type) {
        return TypeUtil.getRawClass(type).equals(StreamResponse.class);
    }

    /**
     * Indicates whether the swagger method is of Stream Response type or not.
     *
     * @return the boolean flag indicating whether the swagger method is Stream Response return type or not.
     */
    public boolean isStreamResponse() {
        return isStreamResponse;
    }

    @Override
    public boolean isReturnTypeDecodeable() {
        return returnTypeDecodeable;
    }

    @Override
    public boolean isResponseEagerlyRead() {
        return responseEagerlyRead;
    }

    @Override
    public boolean isResponseBodyIgnored() {
        return ignoreResponseBody;
    }

    @Override
    public boolean isHeadersEagerlyConverted() {
        return headersEagerlyConverted;
    }

    /**
     * Gets the name of the span that will be used when this {@link SwaggerMethodParser} is called.
     *
     * @return The span name of this {@link SwaggerMethodParser}.
     */
    public String getSpanName() {
        return spanName;
    }

    public static boolean isReturnTypeDecodeable(Type unwrappedReturnType) {
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

    public static boolean isResponseBodyIgnored(Type unwrappedReturnType) {
        if (unwrappedReturnType == null) {
            return false;
        }

        return TypeUtil.isTypeOrSubTypeOf(unwrappedReturnType, Void.TYPE)
            || TypeUtil.isTypeOrSubTypeOf(unwrappedReturnType, Void.class);
    }

    public static boolean isResponseEagerlyRead(Type unwrappedReturnType) {
        if (unwrappedReturnType == null) {
            return false;
        }

        return isReturnTypeDecodeable(unwrappedReturnType);
    }

    public static Type unwrapReturnType(Type returnType) {
        if (returnType == null) {
            return null;
        }

        // First check if the return type is assignable, is a sub-type, to ResponseBase.
        // If it is begin walking up the super type hierarchy until ResponseBase is the raw type.
        // Then unwrap the second generic type (body type).
        if (TypeUtil.isTypeOrSubTypeOf(returnType, ResponseBase.class)) {
            returnType = walkSuperTypesUntil(returnType, type -> getRawClass(type) == ResponseBase.class);

            return unwrapReturnType(TypeUtil.getTypeArguments(returnType)[1]);
        }

        // Then, like ResponseBase, check if the return type is assignable to Response.
        // If it is begin walking up the super type hierarchy until the raw type implements Response.
        // Then unwrap its only generic type.
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Response.class)) {
            // Handling for Response is slightly different as it is an interface unlike ResponseBase which is a class.
            // The super class hierarchy needs be walked until the super class itself implements Response.
            returnType = walkSuperTypesUntil(returnType, type -> typeImplementsInterface(type, Response.class));

            return unwrapReturnType(TypeUtil.getTypeArgument(returnType));
        }

        // Then check if the return type is a Mono or Flux and unwrap its only generic type.
        if (TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class)) {
            returnType = walkSuperTypesUntil(returnType, type -> getRawClass(type) == Mono.class);

            return unwrapReturnType(TypeUtil.getTypeArgument(returnType));
        }

        if (TypeUtil.isTypeOrSubTypeOf(returnType, Flux.class)) {
            returnType = walkSuperTypesUntil(returnType, type -> getRawClass(type) == Flux.class);

            return unwrapReturnType(TypeUtil.getTypeArgument(returnType));
        }

        // Finally, there is no more unwrapping to perform and return the type as-is.
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
