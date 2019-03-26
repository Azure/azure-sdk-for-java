/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation;

import com.azure.common.implementation.exception.MissingRequiredAnnotationException;
import com.azure.common.http.rest.RestException;
import com.azure.common.annotations.BodyParam;
import com.azure.common.annotations.DELETE;
import com.azure.common.annotations.ExpectedResponses;
import com.azure.common.annotations.GET;
import com.azure.common.annotations.HEAD;
import com.azure.common.annotations.HeaderParam;
import com.azure.common.annotations.Headers;
import com.azure.common.annotations.HostParam;
import com.azure.common.annotations.PATCH;
import com.azure.common.annotations.POST;
import com.azure.common.annotations.PUT;
import com.azure.common.annotations.PathParam;
import com.azure.common.annotations.QueryParam;
import com.azure.common.annotations.ReturnValueWireType;
import com.azure.common.annotations.UnexpectedResponseExceptionType;
import com.azure.common.http.ContextData;
import com.azure.common.http.HttpHeader;
import com.azure.common.http.HttpHeaders;
import com.azure.common.http.HttpMethod;
import com.azure.common.http.rest.RestResponse;
import com.azure.common.implementation.serializer.HttpResponseDecodeData;
import com.azure.common.implementation.serializer.SerializerAdapter;
import com.azure.common.implementation.util.TypeUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The type to parse details of a specific Swagger REST API call from a provided Swagger interface
 * method.
 */
public class SwaggerMethodParser implements HttpResponseDecodeData {
    private final SerializerAdapter serializer;
    private final String rawHost;
    private final String fullyQualifiedMethodName;
    private HttpMethod httpMethod;
    private String relativePath;
    private final List<Substitution> hostSubstitutions = new ArrayList<>();
    private final List<Substitution> pathSubstitutions = new ArrayList<>();
    private final List<Substitution> querySubstitutions = new ArrayList<>();
    private final List<Substitution> headerSubstitutions = new ArrayList<>();
    private final HttpHeaders headers = new HttpHeaders();
    private Integer bodyContentMethodParameterIndex;
    private String bodyContentType;
    private Type bodyJavaType;
    private int[] expectedStatusCodes;
    private Type returnType;
    private Type returnValueWireType;
    private Class<? extends RestException> exceptionType;
    private Class<?> exceptionBodyType;

    /**
     * Create a SwaggerMethodParser object using the provided fully qualified method name.
     *
     * @param swaggerMethod the Swagger method to parse.
     * @param rawHost the raw host value from the @Host annotation. Before this can be used as the
     *                host value in an HTTP request, it must be processed through the possible host
     *                substitutions.
     */
    SwaggerMethodParser(Method swaggerMethod, SerializerAdapter serializer, String rawHost) {
        this.serializer = serializer;
        this.rawHost = rawHost;

        final Class<?> swaggerInterface = swaggerMethod.getDeclaringClass();

        fullyQualifiedMethodName = swaggerInterface.getName() + "." + swaggerMethod.getName();

        if (swaggerMethod.isAnnotationPresent(GET.class)) {
            setHttpMethodAndRelativePath(HttpMethod.GET, swaggerMethod.getAnnotation(GET.class).value());
        }
        else if (swaggerMethod.isAnnotationPresent(PUT.class)) {
            setHttpMethodAndRelativePath(HttpMethod.PUT, swaggerMethod.getAnnotation(PUT.class).value());
        }
        else if (swaggerMethod.isAnnotationPresent(HEAD.class)) {
            setHttpMethodAndRelativePath(HttpMethod.HEAD, swaggerMethod.getAnnotation(HEAD.class).value());
        }
        else if (swaggerMethod.isAnnotationPresent(DELETE.class)) {
            setHttpMethodAndRelativePath(HttpMethod.DELETE, swaggerMethod.getAnnotation(DELETE.class).value());
        }
        else if (swaggerMethod.isAnnotationPresent(POST.class)) {
            setHttpMethodAndRelativePath(HttpMethod.POST, swaggerMethod.getAnnotation(POST.class).value());
        }
        else if (swaggerMethod.isAnnotationPresent(PATCH.class)) {
            setHttpMethodAndRelativePath(HttpMethod.PATCH, swaggerMethod.getAnnotation(PATCH.class).value());
        }
        else {
            final ArrayList<Class<? extends Annotation>> requiredAnnotationOptions = new ArrayList<>();
            requiredAnnotationOptions.add(GET.class);
            requiredAnnotationOptions.add(PUT.class);
            requiredAnnotationOptions.add(HEAD.class);
            requiredAnnotationOptions.add(DELETE.class);
            requiredAnnotationOptions.add(POST.class);
            requiredAnnotationOptions.add(PATCH.class);
            throw new MissingRequiredAnnotationException(requiredAnnotationOptions, swaggerMethod);
        }

        returnType = swaggerMethod.getGenericReturnType();

        final ReturnValueWireType returnValueWireTypeAnnotation = swaggerMethod.getAnnotation(ReturnValueWireType.class);
        if (returnValueWireTypeAnnotation != null) {
            Class<?> returnValueWireType = returnValueWireTypeAnnotation.value();
            if (returnValueWireType == Base64Url.class || returnValueWireType == UnixTime.class || returnValueWireType == DateTimeRfc1123.class) {
                this.returnValueWireType = returnValueWireType;
            }
            else {
                if (TypeUtil.isTypeOrSubTypeOf(returnValueWireType, List.class)) {
                    this.returnValueWireType = returnValueWireType.getGenericInterfaces()[0];
                }
            }
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
                            this.headers.set(headerName, headerValue);
                        }
                    }
                }
            }
        }

        final ExpectedResponses expectedResponses = swaggerMethod.getAnnotation(ExpectedResponses.class);
        if (expectedResponses != null) {
            expectedStatusCodes = expectedResponses.value();
        }

        final UnexpectedResponseExceptionType unexpectedResponseExceptionType = swaggerMethod.getAnnotation(UnexpectedResponseExceptionType.class);
        if (unexpectedResponseExceptionType == null) {
            exceptionType = RestException.class;
        }
        else {
            exceptionType = unexpectedResponseExceptionType.value();
        }

        try {
            final Method exceptionBodyMethod = exceptionType.getDeclaredMethod("body");
            exceptionBodyType = exceptionBodyMethod.getReturnType();
        } catch (NoSuchMethodException e) {
            // Should always have a body() method. Register Object as a fallback plan.
            exceptionBodyType = Object.class;
        }

        final Annotation[][] allParametersAnnotations = swaggerMethod.getParameterAnnotations();
        for (int parameterIndex = 0; parameterIndex < allParametersAnnotations.length; ++parameterIndex) {
            final Annotation[] parameterAnnotations = swaggerMethod.getParameterAnnotations()[parameterIndex];
            for (final Annotation annotation : parameterAnnotations) {
                final Class<? extends Annotation> annotationType = annotation.annotationType();
                if (annotationType.equals(HostParam.class)) {
                    final HostParam hostParamAnnotation = (HostParam) annotation;
                    hostSubstitutions.add(new Substitution(hostParamAnnotation.value(), parameterIndex, !hostParamAnnotation.encoded()));
                }
                else if (annotationType.equals(PathParam.class)) {
                    final PathParam pathParamAnnotation = (PathParam) annotation;
                    pathSubstitutions.add(new Substitution(pathParamAnnotation.value(), parameterIndex, !pathParamAnnotation.encoded()));
                }
                else if (annotationType.equals(QueryParam.class)) {
                    final QueryParam queryParamAnnotation = (QueryParam) annotation;
                    querySubstitutions.add(new Substitution(queryParamAnnotation.value(), parameterIndex, !queryParamAnnotation.encoded()));
                }
                else if (annotationType.equals(HeaderParam.class)) {
                    final HeaderParam headerParamAnnotation = (HeaderParam) annotation;
                    headerSubstitutions.add(new Substitution(headerParamAnnotation.value(), parameterIndex, false));
                }
                else if (annotationType.equals(BodyParam.class)) {
                    final BodyParam bodyParamAnnotation = (BodyParam) annotation;
                    bodyContentMethodParameterIndex = parameterIndex;
                    bodyContentType = bodyParamAnnotation.value();
                    bodyJavaType = swaggerMethod.getGenericParameterTypes()[parameterIndex];
                }
            }
        }
    }

    /**
     * Get the fully qualified method that was called to invoke this HTTP request.
     *
     * @return the fully qualified method that was called to invoke this HTTP request
     */
    public String fullyQualifiedMethodName() {
        return fullyQualifiedMethodName;
    }

    /**
     * Get the HTTP method that will be used to complete the Swagger method's request.
     *
     * @return the HTTP method that will be used to complete the Swagger method's request
     */
    public HttpMethod httpMethod() {
        return httpMethod;
    }

    /**
     * Get the HTTP response status codes that are expected when a request is sent out for this
     * Swagger method. If the returned int[] is null, then all status codes less than 400 are
     * allowed.
     *
     * @return the expected HTTP response status codes for this Swagger method or null if all status
     * codes less than 400 are allowed.
     */
    @Override
    public int[] expectedStatusCodes() {
        return expectedStatusCodes;
    }

    /**
     * Get the scheme to use for HTTP requests for this Swagger method.
     *
     * @param swaggerMethodArguments the arguments to use for scheme/host substitutions.
     * @return the final host to use for HTTP requests for this Swagger method.
     */
    public String scheme(Object[] swaggerMethodArguments) {
        final String substitutedHost = applySubstitutions(rawHost, hostSubstitutions, swaggerMethodArguments, UrlEscapers.PATH_ESCAPER);
        final String[] substitutedHostParts = substitutedHost.split("://");
        return substitutedHostParts.length < 1 ? null : substitutedHostParts[0];
    }

    /**
     * Get the host to use for HTTP requests for this Swagger method.
     *
     * @param swaggerMethodArguments the arguments to use for host substitutions
     * @return the final host to use for HTTP requests for this Swagger method
     */
    public String host(Object[] swaggerMethodArguments) {
        final String substitutedHost = applySubstitutions(rawHost, hostSubstitutions, swaggerMethodArguments, UrlEscapers.PATH_ESCAPER);
        final String[] substitutedHostParts = substitutedHost.split("://");
        return substitutedHostParts.length < 2 ? substitutedHost : substitutedHost.split("://")[1];
    }

    /**
     * Get the path that will be used to complete the Swagger method's request.
     *
     * @param methodArguments the method arguments to use with the path substitutions
     * @return the path value with its placeholders replaced by the matching substitutions
     */
    public String path(Object[] methodArguments) {
        return applySubstitutions(relativePath, pathSubstitutions, methodArguments, UrlEscapers.PATH_ESCAPER);
    }

    /**
     * Get the encoded query parameters that have been added to this value based on the provided
     * method arguments.
     *
     * @param swaggerMethodArguments the arguments that will be used to create the query parameters'
     *                               values
     * @return an Iterable with the encoded query parameters
     */
    public Iterable<EncodedParameter> encodedQueryParameters(Object[] swaggerMethodArguments) {
        final List<EncodedParameter> result = new ArrayList<>();
        if (querySubstitutions != null) {
            final PercentEscaper escaper = UrlEscapers.QUERY_ESCAPER;

            for (Substitution querySubstitution : querySubstitutions) {
                final int parameterIndex = querySubstitution.methodParameterIndex();
                if (0 <= parameterIndex && parameterIndex < swaggerMethodArguments.length) {
                    final Object methodArgument = swaggerMethodArguments[querySubstitution.methodParameterIndex()];
                    String parameterValue = serialize(methodArgument);
                    if (parameterValue != null) {
                        if (querySubstitution.shouldEncode() && escaper != null) {
                            parameterValue = escaper.escape(parameterValue);
                        }

                        result.add(new EncodedParameter(querySubstitution.urlParameterName(), parameterValue));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the headers that have been added to this value based on the provided method arguments.
     * @param swaggerMethodArguments The arguments that will be used to create the headers' values.
     * @return An Iterable with the headers.
     */
    public Iterable<HttpHeader> headers(Object[] swaggerMethodArguments) {
        final HttpHeaders result = new HttpHeaders(headers);

        if (headerSubstitutions != null) {
            for (Substitution headerSubstitution : headerSubstitutions) {
                final int parameterIndex = headerSubstitution.methodParameterIndex();
                if (0 <= parameterIndex && parameterIndex < swaggerMethodArguments.length) {
                    final Object methodArgument = swaggerMethodArguments[headerSubstitution.methodParameterIndex()];
                    if (methodArgument instanceof Map) {
                        final Map<String, ?> headerCollection = (Map<String, ?>) methodArgument;
                        final String headerCollectionPrefix = headerSubstitution.urlParameterName();
                        for (final Map.Entry<String, ?> headerCollectionEntry : headerCollection.entrySet()) {
                            final String headerName = headerCollectionPrefix + headerCollectionEntry.getKey();
                            final String headerValue = serialize(headerCollectionEntry.getValue());
                            result.set(headerName, headerValue);
                        }
                    } else {
                        final String headerName = headerSubstitution.urlParameterName();
                        final String headerValue = serialize(methodArgument);
                        result.set(headerName, headerValue);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Get the {@link ContextData} passed into the proxy method.
     *
     * @param swaggerMethodArguments the arguments passed to the proxy method
     * @return the context, or null if no context was provided
     */
    public ContextData contextData(Object[] swaggerMethodArguments) {
        Object firstArg = swaggerMethodArguments != null && swaggerMethodArguments.length > 0 ? swaggerMethodArguments[0] : null;
        if (firstArg instanceof ContextData) {
            return (ContextData) firstArg;
        } else {
            return ContextData.NONE;
        }
    }

    /**
     * Get whether or not the provided response status code is one of the expected status codes for
     * this Swagger method.
     *
     * @param responseStatusCode the status code that was returned in the HTTP response
     * @param additionalAllowedStatusCodes an additional set of allowed status codes that will be
     *                                     merged with the existing set of allowed status codes for
     *                                     this query
     * @return whether or not the provided response status code is one of the expected status codes
     * for this Swagger method
     */
    public boolean isExpectedResponseStatusCode(int responseStatusCode, int[] additionalAllowedStatusCodes) {
        boolean result;

        if (expectedStatusCodes == null) {
            result = (responseStatusCode < 400);
        }
        else {
            result = contains(expectedStatusCodes, responseStatusCode)
                    || contains(additionalAllowedStatusCodes, responseStatusCode);
        }

        return result;
    }

    private static boolean contains(int[] values, int searchValue) {
        boolean result = false;

        if (values != null && values.length > 0) {
            for (int value : values) {
                if (searchValue == value) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Get the type of RestException that will be thrown if the HTTP response's status code is not
     * one of the expected status codes.
     *
     * @return the type of RestException that will be thrown if the HTTP response's status code is
     * not one of the expected status codes
     */
    public Class<? extends RestException> exceptionType() {
        return exceptionType;
    }

    /**
     * Get the type of body Object that a thrown RestException will contain if the HTTP response's
     * status code is not one of the expected status codes.
     *
     * @return the type of body Object that a thrown RestException will contain if the HTTP
     * response's status code is not one of the expected status codes
     */
    @Override
    public Class<?> exceptionBodyType() {
        return exceptionBodyType;
    }

    /**
     * Get the object to be used as the body of the HTTP request.
     *
     * @param swaggerMethodArguments the method arguments to get the body object from
     * @return the object that will be used as the body of the HTTP request
     */
    public Object body(Object[] swaggerMethodArguments) {
        Object result = null;

        if (bodyContentMethodParameterIndex != null
                && swaggerMethodArguments != null
                && 0 <= bodyContentMethodParameterIndex
                && bodyContentMethodParameterIndex < swaggerMethodArguments.length) {
            result = swaggerMethodArguments[bodyContentMethodParameterIndex];
        }

        return result;
    }

    /**
     * Get the Content-Type of the body of this Swagger method.
     *
     * @return the Content-Type of the body of this Swagger method
     */
    public String bodyContentType() {
        return bodyContentType;
    }

    /**
     * Get the return type for the method that this object describes.
     *
     * @return the return type for the method that this object describes.
     */
    @Override
    public Type returnType() {
        return returnType;
    }


    /**
     * Get the type of the body parameter to this method, if present.
     *
     * @return the return type of the body parameter to this method
     */
    public Type bodyJavaType() {
        return bodyJavaType;
    }

    /**
     * Get the type that the return value will be send across the network as. If returnValueWireType
     * is not null, then the raw HTTP response body will need to parsed to this type and then
     * converted to the actual returnType.
     *
     * @return the type that the raw HTTP response body will be sent as
     */
    @Override
    public Type returnValueWireType() {
        return returnValueWireType;
    }

    /**
     * Checks whether or not the Swagger method expects the response to contain a body.
     *
     * @return true if Swagger method expects the response to contain a body, false otherwise
     */
    public boolean expectsResponseBody() {
        boolean result = true;

        if (TypeUtil.isTypeOrSubTypeOf(returnType, Void.class)) {
            result = false;
        }
        else if (TypeUtil.isTypeOrSubTypeOf(returnType, Mono.class) || TypeUtil.isTypeOrSubTypeOf(returnType, Flux.class)) {
            final ParameterizedType asyncReturnType = (ParameterizedType) returnType;
            final Type syncReturnType = asyncReturnType.getActualTypeArguments()[0];
            if (TypeUtil.isTypeOrSubTypeOf(syncReturnType, Void.class)) {
                result = false;
            } else if (TypeUtil.isTypeOrSubTypeOf(syncReturnType, RestResponse.class)) {
                result = TypeUtil.restResponseTypeExpectsBody((ParameterizedType) TypeUtil.getSuperType(syncReturnType, RestResponse.class));
            }
        } else if (TypeUtil.isTypeOrSubTypeOf(returnType, RestResponse.class)) {
            result = TypeUtil.restResponseTypeExpectsBody((ParameterizedType) returnType);
        }

        return result;
    }

    /**
     * Set both the HTTP method and the path that will be used to complete the Swagger method's
     * request.
     *
     * @param httpMethod the HTTP method that will be used to complete the Swagger method's request
     * @param relativePath the path in the URL that will be used to complete the Swagger method's
     *                     request
     */
    private void setHttpMethodAndRelativePath(HttpMethod httpMethod, String relativePath) {
        this.httpMethod = httpMethod;
        this.relativePath = relativePath;
    }

    String serialize(Object value) {
        String result = null;
        if (value != null) {
            if (value instanceof String) {
                result = (String) value;
            }
            else {
                result = serializer.serializeRaw(value);
            }
        }
        return result;
    }

    private String applySubstitutions(String originalValue, Iterable<Substitution> substitutions, Object[] methodArguments, PercentEscaper escaper) {
        String result = originalValue;

        if (methodArguments != null) {
            for (Substitution substitution : substitutions) {
                final int substitutionParameterIndex = substitution.methodParameterIndex();
                if (0 <= substitutionParameterIndex && substitutionParameterIndex < methodArguments.length) {
                    final Object methodArgument = methodArguments[substitutionParameterIndex];

                    String substitutionValue = serialize(methodArgument);
                    if (substitutionValue != null && !substitutionValue.isEmpty() && substitution.shouldEncode() && escaper != null) {
                        substitutionValue = escaper.escape(substitutionValue);
                    }

                    result = result.replace("{" + substitution.urlParameterName() + "}", substitutionValue);
                }
            }
        }

        return result;
    }
}
