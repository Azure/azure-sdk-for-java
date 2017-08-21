/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.v2.annotations.BodyParam;
import com.microsoft.rest.v2.annotations.DELETE;
import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.HEAD;
import com.microsoft.rest.v2.annotations.HeaderParam;
import com.microsoft.rest.v2.annotations.Headers;
import com.microsoft.rest.v2.annotations.HostParam;
import com.microsoft.rest.v2.annotations.PATCH;
import com.microsoft.rest.v2.annotations.POST;
import com.microsoft.rest.v2.annotations.PUT;
import com.microsoft.rest.v2.annotations.PathParam;
import com.microsoft.rest.v2.annotations.QueryParam;
import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpHeaders;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * This class parses details of a specific Swagger REST API call from a provided Swagger interface
 * method.
 */
class SwaggerMethodParser {
    private final String rawHost;
    private final String fullyQualifiedMethodName;
    private String httpMethod;
    private String relativePath;
    private final List<Substitution> hostSubstitutions = new ArrayList<>();
    private final List<Substitution> pathSubstitutions = new ArrayList<>();
    private final List<Substitution> querySubstitutions = new ArrayList<>();
    private final List<Substitution> headerSubstitutions = new ArrayList<>();
    private final HttpHeaders headers = new HttpHeaders();
    private Integer bodyContentMethodParameterIndex;
    private Type returnType;

    /**
     * Create a new SwaggerMethodParser object using the provided fully qualified method name.
     * @param swaggerMethod The Swagger method to parse.
     * @param rawHost The raw host value from the @Host annotation. Before this can be used as the
     *                host value in an HTTP request, it must be processed through the possible host
     *                substitutions.
     */
    SwaggerMethodParser(Method swaggerMethod, String rawHost) {
        this.rawHost = rawHost;

        final Class<?> swaggerInterface = swaggerMethod.getDeclaringClass();

        fullyQualifiedMethodName = swaggerInterface.getCanonicalName() + "." + swaggerMethod.getName();

        if (swaggerMethod.isAnnotationPresent(GET.class)) {
            setHttpMethodAndRelativePath("GET", swaggerMethod.getAnnotation(GET.class).value());
        }
        else if (swaggerMethod.isAnnotationPresent(PUT.class)) {
            setHttpMethodAndRelativePath("PUT", swaggerMethod.getAnnotation(PUT.class).value());
        }
        else if (swaggerMethod.isAnnotationPresent(HEAD.class)) {
            setHttpMethodAndRelativePath("HEAD", swaggerMethod.getAnnotation(HEAD.class).value());
        }
        else if (swaggerMethod.isAnnotationPresent(DELETE.class)) {
            setHttpMethodAndRelativePath("DELETE", swaggerMethod.getAnnotation(DELETE.class).value());
        }
        else if (swaggerMethod.isAnnotationPresent(POST.class)) {
            setHttpMethodAndRelativePath("POST", swaggerMethod.getAnnotation(POST.class).value());
        }
        else if (swaggerMethod.isAnnotationPresent(PATCH.class)) {
            setHttpMethodAndRelativePath("PATCH", swaggerMethod.getAnnotation(PATCH.class).value());
        }

        returnType = swaggerMethod.getGenericReturnType();

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
                            this.headers.add(headerName, headerValue);
                        }
                    }
                }
            }
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
                    bodyContentMethodParameterIndex = parameterIndex;
                }
            }
        }
    }

    /**
     * Get the fully qualified method that was called to invoke this HTTP request.
     * @return The fully qualified method that was called to invoke this HTTP request.
     */
    public String fullyQualifiedMethodName() {
        return fullyQualifiedMethodName;
    }

    /**
     * Get the HTTP method that will be used to complete the Swagger method's request.
     * @return The HTTP method that will be used to complete the Swagger method's request.
     */
    public String httpMethod() {
        return httpMethod;
    }

    /**
     * Get the scheme to use for HTTP requests for this Swagger method.
     * @param swaggerMethodArguments The arguments to use for scheme/host substitutions.
     * @return The final host to use for HTTP requests for this Swagger method.
     */
    public String scheme(Object[] swaggerMethodArguments) {
        final String substitutedHost = applySubstitutions(rawHost, hostSubstitutions, swaggerMethodArguments);
        return substitutedHost.split("://")[0];
    }

    /**
     * Get the host to use for HTTP requests for this Swagger method.
     * @param swaggerMethodArguments The arguments to use for host substitutions.
     * @return The final host to use for HTTP requests for this Swagger method.
     */
    public String host(Object[] swaggerMethodArguments) {
        final String substitutedHost = applySubstitutions(rawHost, hostSubstitutions, swaggerMethodArguments);
        return substitutedHost.split("://")[1];
    }

    /**
     * Get the path that will be used to complete the Swagger method's request.
     * @param methodArguments The method arguments to use with the path substitutions.
     * @return The path value with its placeholders replaced by the matching substitutions.
     */
    public String path(Object[] methodArguments) {
        return applySubstitutions(relativePath, pathSubstitutions, methodArguments);
    }

    /**
     * Get the encoded query parameters that have been added to this value based on the provided
     * method arguments.
     * @param swaggerMethodArguments The arguments that will be used to create the query parameters'
     *                               values.
     * @return An Iterable with the encoded query parameters.
     */
    public Iterable<EncodedParameter> encodedQueryParameters(Object[] swaggerMethodArguments) {
        return getEncodedParameters(querySubstitutions, swaggerMethodArguments);
    }

    /**
     * Get the headers that have been added to this value based on the provided method arguments.
     * @param swaggerMethodArguments The arguments that will be used to create the headers' values.
     * @return An Iterable with the headers.
     */
    public Iterable<HttpHeader> headers(Object[] swaggerMethodArguments) {
        final HttpHeaders result = new HttpHeaders(headers);

        final Iterable<EncodedParameter> substitutedHeaders = getEncodedParameters(headerSubstitutions, swaggerMethodArguments);
        for (final EncodedParameter substitutedHeader : substitutedHeaders) {
            result.add(substitutedHeader.name(), substitutedHeader.encodedValue());
        }

        return result;
    }

    /**
     * Get the object to be used as the body of the HTTP request.
     * @param swaggerMethodArguments The method arguments to get the body object from.
     * @return The object that will be used as the body of the HTTP request.
     */
    public Object body(Object[] swaggerMethodArguments) {
        Object result = null;

        if (bodyContentMethodParameterIndex != null
                && swaggerMethodArguments != null
                && 0 <= bodyContentMethodParameterIndex && bodyContentMethodParameterIndex < swaggerMethodArguments.length) {
            result = swaggerMethodArguments[bodyContentMethodParameterIndex];
        }

        return result;
    }

    /**
     * Get the return type for the method that this object describes.
     * @return The return type for the method that this object describes.
     */
    public Type returnType() {
        return returnType;
    }

    /**
     * Set both the HTTP method and the path that will be used to complete the Swagger method's
     * request.
     * @param httpMethod The HTTP method that will be used to complete the Swagger method's request.
     * @param relativePath The path in the URL that will be used to complete the Swagger method's
     *                     request.
     */
    private void setHttpMethodAndRelativePath(String httpMethod, String relativePath) {
        this.httpMethod = httpMethod;
        this.relativePath = relativePath;
    }

    private static String applySubstitutions(String originalValue, Iterable<Substitution> substitutions, Object[] methodArguments) {
        String result = originalValue;

        if (methodArguments != null) {
            for (Substitution substitution : substitutions) {
                final int substitutionParameterIndex = substitution.methodParameterIndex();
                if (0 <= substitutionParameterIndex && substitutionParameterIndex < methodArguments.length) {
                    final Object methodArgument = methodArguments[substitutionParameterIndex];

                    String substitutionValue = String.valueOf(methodArgument);
                    if (substitution.shouldEncode()) {
                        substitutionValue = encode(substitutionValue);
                    }

                    result = result.replace("{" + substitution.urlParameterName() + "}", substitutionValue);
                }
            }
        }

        return result;
    }

    private static Iterable<EncodedParameter> getEncodedParameters(Iterable<Substitution> substitutions, Object[] methodArguments) {
        final List<EncodedParameter> result = new ArrayList<>();

        if (substitutions != null) {
            for (Substitution substitution : substitutions) {
                final int parameterIndex = substitution.methodParameterIndex();
                if (0 <= parameterIndex && parameterIndex < methodArguments.length) {
                    final Object methodArgument = methodArguments[substitution.methodParameterIndex()];

                    String parameterValue = String.valueOf(methodArgument);
                    if (substitution.shouldEncode()) {
                        parameterValue = encode(parameterValue);
                    }

                    result.add(new EncodedParameter(substitution.urlParameterName(), parameterValue));
                }
            }
        }

        return result;
    }

    /**
     * URL encode the provided value using the default (UTF-8) encoding.
     * @param segment The value to URL encode.
     * @return The encoded value.
     */
    protected static String encode(String segment) {
        return encode(segment, "UTF-8");
    }

    /**
     * URL encode the provided value using the provided encoding.
     * @param segment The value to URL encode.
     * @return The encoded value.
     */
    protected static String encode(String segment, String encoding) {
        try {
            return URLEncoder.encode(segment, encoding);
        } catch (UnsupportedEncodingException e) {
            return segment;
        }
    }
}
