// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.implementation.http.rest;

import com.generic.core.annotation.Delete;
import com.generic.core.annotation.Get;
import com.generic.core.annotation.Head;
import com.generic.core.annotation.Options;
import com.generic.core.annotation.Patch;
import com.generic.core.annotation.Post;
import com.generic.core.annotation.Put;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.RequestOptions;
import com.generic.core.http.RestProxy;
import com.generic.core.implementation.TypeUtil;
import com.generic.core.implementation.http.UnexpectedExceptionInformation;
import com.generic.core.implementation.http.serializer.HttpResponseDecodeData;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Context;
import com.generic.core.implementation.util.CoreUtils;
import com.generic.core.util.serializer.JsonSerializer;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
//    private final String rawHost;
//    private final String fullyQualifiedMethodName;
//    private final HttpMethod httpMethod;
//    private final String relativePath;
    final List<RangeReplaceSubstitution> hostSubstitutions = new ArrayList<>();
    private final List<RangeReplaceSubstitution> pathSubstitutions = new ArrayList<>();
    private final List<QuerySubstitution> querySubstitutions = new ArrayList<>();
    private final List<Substitution> formSubstitutions = new ArrayList<>();
    private final List<HeaderSubstitution> headerSubstitutions = new ArrayList<>();
//    private final Integer bodyContentMethodParameterIndex;
//    private final String bodyContentType;
//    private final Type bodyJavaType;
//    private final BitSet expectedStatusCodes;
//    private final Type returnType;
//    private final Type returnValueWireType;
//    private final UnexpectedResponseExceptionType[] unexpectedResponseExceptionTypes;
//    private final int contextPosition;
//    private final int requestOptionsPosition;
//    private final boolean isReactive;
//    private final boolean isStreamResponse;
//    private final boolean returnTypeDecodeable;
//    private final boolean responseEagerlyRead;
//    private final boolean ignoreResponseBody;
//    private final boolean headersEagerlyConverted;
//    private final String spanName;

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

    }

    /**
     * Get the fully qualified method that was called to invoke this HTTP request.
     *
     * @return the fully qualified method that was called to invoke this HTTP request
     */
    public String getFullyQualifiedMethodName() {
        return null;
    }

    /**
     * Get the HTTP method that will be used to complete the Swagger method's request.
     *
     * @return the HTTP method that will be used to complete the Swagger method's request
     */
    public HttpMethod getHttpMethod() {
        return null;
    }

    /**
     * Sets the scheme and host to use for HTTP requests for this Swagger method.
     *
     * @param swaggerMethodArguments The arguments to use for scheme and host substitutions.
     * @param serializer             {@link JsonSerializer} that is used to encode host substitutions.
     */
    public void setSchemeAndHost(Object[] swaggerMethodArguments, JsonSerializer serializer) {
//        setSchemeAndHost(rawHost, hostSubstitutions, swaggerMethodArguments, serializer);
    }

    /**
     * Get the path that will be used to complete the Swagger method's request.
     *
     * @param methodArguments the method arguments to use with the path substitutions
     * @param serializer {@link JsonSerializer} that is used to encode path substitutions
     * @return the path value with its placeholders replaced by the matching substitutions
     */
    public String setPath(Object[] methodArguments, JsonSerializer serializer) {
//        return applySubstitutions(relativePath, pathSubstitutions, methodArguments, serializer);
        return null;
    }

    /**
     * Get the {@link Context} passed into the proxy method.
     *
     * @param swaggerMethodArguments the arguments passed to the proxy method
     * @return the context, or {@link Context#NONE} if no context was provided
     */
    public Context setContext(Object[] swaggerMethodArguments) {
        // Context was never found as a parameter in the Method, therefore always return Context.NONE.

        return null;
    }

    /**
     * Get the {@link RequestOptions} passed into the proxy method.
     *
     * @param swaggerMethodArguments the arguments passed to the proxy method
     * @return the request options
     */
    public RequestOptions setRequestOptions(Object[] swaggerMethodArguments) {
        return null;
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
        return false;
    }



    /**
     * Get the object to be used as the value of the HTTP request.
     *
     * @param swaggerMethodArguments the method arguments to get the value object from
     * @param serializer {@link JsonSerializer} used to encode the request body if it's an
     * {@code application/x-www-form-urlencoded} request.
     * @return the object that will be used as the body of the HTTP request
     */
    public Object setBody(Object[] swaggerMethodArguments, JsonSerializer serializer) {
        Object result = null;



        return result;
    }

    /**
     * Get the Content-Type of the body of this Swagger method.
     *
     * @return the Content-Type of the body of this Swagger method
     */
    public String getBodyContentType() {
        return null;
    }

    /**
     * Get the return type for the method that this object describes.
     *
     * @return the return type for the method that this object describes.
     */
    @Override
    public Type getReturnType() {
        return null;
    }


    /**
     * Get the type of the body parameter to this method, if present.
     *
     * @return the return type of the body parameter to this method
     */
    public Type getBodyJavaType() {
        return null;
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
        return null;
    }

    private static String serialize(JsonSerializer serializer, Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return (String) value;
        }
        return null;
    }

    private static String serializeFormData(JsonSerializer serializer, String key, Object value,
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

    private static String serializeAndEncodeFormValue(JsonSerializer serializer, Object value,
        boolean shouldEncode) {
        if (value == null) {
            return null;
        }

        String serializedValue = serialize(serializer, value);
        return shouldEncode ? UrlEscapers.FORM_ESCAPER.escape(serializedValue) : serializedValue;
    }

    private static String applySubstitutions(String originalValue, List<RangeReplaceSubstitution> substitutions,
        Object[] methodArguments, JsonSerializer serializer) {
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

//        for (UnexpectedResponseExceptionType exceptionAnnotation : unexpectedResponseExceptionTypes) {
//            UnexpectedExceptionInformation exception = new UnexpectedExceptionInformation(exceptionAnnotation.value());
//            if (exceptionAnnotation.code().length == 0) {
//                defaultException = exception;
//            } else {
//                for (int statusCode : exceptionAnnotation.code()) {
//                    exceptionHashMap.put(statusCode, exception);
//                }
//            }
//        }
//
//        if (defaultException == null) {
//            defaultException = new UnexpectedExceptionInformation(HttpResponseException.class);
//        }

        return exceptionHashMap;
    }

    @Override
    public boolean isReturnTypeDecodeable() {
        return false;
    }

    @Override
    public boolean isResponseEagerlyRead() {
        return false;
    }

    @Override
    public boolean isResponseBodyIgnored() {
        return false;
    }

    @Override
    public boolean isHeadersEagerlyConverted() {
        return false;
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
