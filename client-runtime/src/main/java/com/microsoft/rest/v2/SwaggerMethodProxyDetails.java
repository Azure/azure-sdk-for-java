/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.v2.http.HttpHeader;
import com.microsoft.rest.v2.http.HttpHeaders;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Details of a specific method from an interface that was generated from a Swagger specification.
 * This is generally used by a proxy implementation of the Swagger interface.
 */
class SwaggerMethodProxyDetails {
    private String method;
    private String relativePath;
    private final List<Substitution> hostSubstitutions = new ArrayList<>();
    private final List<Substitution> pathSubstitutions = new ArrayList<>();
    private final List<Substitution> querySubstitutions = new ArrayList<>();
    private final List<Substitution> headerSubstitutions = new ArrayList<>();
    private final HttpHeaders headers = new HttpHeaders();
    private Integer bodyContentMethodParameterIndex;
    private boolean isAsync;
    private Class<?> returnType;

    /**
     * Get the HTTP method that will be used to complete the Swagger method's request.
     * @return The HTTP method that will be used to complete the Swagger method's request.
     */
    public String method() {
        return method;
    }

    /**
     * Set the HTTP method that will be used to complete the Swagger method's request.
     * @param method The HTTP method that will be used to complete the Swagger method's request.
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Get the path that will be used to complete the Swagger method's request.
     * @return The path in the URL that will be used to complete the Swagger method's request.
     */
    public String relativePath() {
        return this.relativePath;
    }

    /**
     * Set the path that will be used to complete the Swagger method's request.
     * @param relativePath  The path in the URL that will be used to complete the Swagger method's
     *                      request.
     */
    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    /**
     * Set both the HTTP method and the path that will be used to complete the Swagger method's
     * request.
     * @param method The HTTP method that will be used to complete the Swagger method's request.
     * @param relativePath The path in the URL that will be used to complete the Swagger method's
     *                     request.
     */
    public void setMethodAndRelativePath(String method, String relativePath) {
        setMethod(method);
        setRelativePath(relativePath);
    }

    /**
     * Add a substitution that will occur within the scope of the URL's host section.
     * @param hostParameterName The name of the placeholder to replace.
     * @param methodParameterIndex The index into the method's arguments to use to replace the
     *                             placeholder.
     * @param shouldEncode Whether or not the method's argument should be encoded at the time of the
     *                     substitution.
     */
    public void addHostSubstitution(String hostParameterName, int methodParameterIndex, boolean shouldEncode) {
        hostSubstitutions.add(new Substitution(hostParameterName, methodParameterIndex, shouldEncode));
    }

    /**
     * Apply the host substitutions that exist in this object to the provided host with the provided
     * method arguments and return the result.
     * @param host The host value to apply the host substitutions to.
     * @param methodArguments The method arguments to use with the substitutions.
     * @return The provided host value with its placeholders replaced by the matching substitutions.
     */
    public String applyHostSubstitutions(String host, Object[] methodArguments) {
        return applySubstitutions(host, hostSubstitutions, methodArguments);
    }

    /**
     * Add a substitution that will occur within the scope of the URL's path section.
     * @param pathParameterName The name of the placeholder to replace.
     * @param methodParameterIndex The index into the method's arguments to use to replace the
     *                             placeholder.
     * @param shouldEncode Whether or not the method's argument should be encoded at the time of the
     *                     substitution.
     */
    public void addPathSubstitution(String pathParameterName, int methodParameterIndex, boolean shouldEncode) {
        pathSubstitutions.add(new Substitution(pathParameterName, methodParameterIndex, shouldEncode));
    }

    /**
     * Apply the path substitutions that exist in this object to the previously set path with the
     * provided method arguments and return the result.
     * @param methodArguments The method arguments to use with the substitutions.
     * @return The path value with its placeholders replaced by the matching substitutions.
     */
    public String getSubstitutedPath(Object[] methodArguments) {
        return applySubstitutions(relativePath, pathSubstitutions, methodArguments);
    }

    /**
     * Add a query parameter that will be added to the HTTP request. The parameter's value will be
     * taken from the method argument at the provided index.
     * @param queryParameterName The name of the query parameter to add.
     * @param methodParameterIndex The index into the method's arguments to use for the query's
     *                             value.
     * @param shouldEncode Whether or not the method's argument should be encoded before it is added
     *                     to the URL.
     */
    public void addQuerySubstitution(String queryParameterName, int methodParameterIndex, boolean shouldEncode) {
        querySubstitutions.add(new Substitution(queryParameterName, methodParameterIndex, shouldEncode));
    }

    /**
     * Get the encoded query parameters that have been added to this value based on the provided
     * method arguments.
     * @param methodArguments The arguments that will be used to create the query parameters'
     *                        values.
     * @return An Iterable with the encoded query parameters.
     */
    public Iterable<EncodedParameter> getEncodedQueryParameters(Object[] methodArguments) {
        return getEncodedParameters(querySubstitutions, methodArguments);
    }

    /**
     * Add a header that will be added to the HTTP request. The parameter's value will be taken from
     * the method argument at the provided index.
     * @param headerParameterName The name of the header to add.
     * @param methodParameterIndex The index into the method's arguments to use for the query's
     *                             value.
     */
    public void addHeaderSubstitution(String headerParameterName, int methodParameterIndex) {
        headerSubstitutions.add(new Substitution(headerParameterName, methodParameterIndex, false));
    }

    /**
     * Get the headers that have been added to this value based on the provided method arguments.
     * @param methodArguments The arguments that will be used to create the headers' values.
     * @return An Iterable with the headers.
     */
    public Iterable<EncodedParameter> getEncodedHeaderParameters(Object[] methodArguments) {
        return getEncodedParameters(headerSubstitutions, methodArguments);
    }

    /**
     * Set the index of the method argument that will be used as the body of the HTTP request.
     * @param bodyMethodParameterIndex The index of the method argument that will be used as the
     *                                 body of the HTTP request.
     */
    public void setBodyContentMethodParameterIndex(int bodyMethodParameterIndex) {
        this.bodyContentMethodParameterIndex = bodyMethodParameterIndex;
    }

    /**
     * Get the index of the method argument that will be used as the body of the HTTP request.
     * @return The index of the method argument that will be used as the body of the HTTP request.
     */
    public Integer bodyContentMethodParameterIndex() {
        return bodyContentMethodParameterIndex;
    }

    /**
     * Add the provided headerName and headerValue to the list of static headers to associated with
     * the HTTP request.
     * @param headerName The name of the header.
     * @param headerValue The value of the header.
     */
    public void addHeader(String headerName, String headerValue) {
        headers.add(headerName, headerValue);
    }

    /**
     * Get the static headers that have been added to the HTTP request.
     * @return The static headers that have been added to the HTTP request.
     */
    public Iterable<HttpHeader> getHeaders() {
        return headers;
    }

    /**
     * Set whether or not this object describes an asynchronous method.
     * @param isAsync Whether or not this object describes an asynchronous method.
     */
    public void setIsAsync(boolean isAsync) {
        this.isAsync = isAsync;
    }

    /**
     * Get whether or not this object describes an asynchronous method.
     * @return Whether or not this object describes an asynchronous method.
     */
    public boolean isAsync() {
        return isAsync;
    }

    /**
     * Set the synchronous return type for the method that this object describes. If the method is
     * asynchronous, then returnType is the type of value that is returned when then asynchronous
     * operation finishes. In other words, returnType is the parameterized type of the Single object
     * that is returned from the method.
     * @param returnType The synchronous return type for the method that this object describes.
     */
    public void setReturnType(Class<?> returnType) {
        this.returnType = returnType;
    }

    /**
     * Get the synchronous return type for the method that this object describes. If the method is
     * asynchronous, then the type of value that is returned when then asynchronous operation
     * finishes will be returned. In other words, returnType is the parameterized type of the Single
     * object that is returned from the method.
     * @return The synchronous return type for the method that this object describes.
     */
    public Class<?> returnType() {
        return returnType;
    }

    private static String applySubstitutions(String originalValue, Iterable<Substitution> substitutions, Object[] methodArguments) {
        String result = originalValue;

        for (Substitution substitution : substitutions) {
            final Object methodArgument = methodArguments[substitution.methodParameterIndex()];

            String substitutionValue = String.valueOf(methodArgument);
            if (substitution.shouldEncode()) {
                substitutionValue = encode(substitutionValue);
            }

            result = result.replace("{" + substitution.urlParameterName() + "}", substitutionValue);
        }

        return result;
    }

    private static Iterable<EncodedParameter> getEncodedParameters(Iterable<Substitution> substitutions, Object[] methodArguments) {
        final List<EncodedParameter> result = new ArrayList<>();

        for (Substitution substitution : substitutions) {
            final Object methodArgument = methodArguments[substitution.methodParameterIndex()];

            String parameterValue = String.valueOf(methodArgument);
            if (substitution.shouldEncode()) {
                parameterValue = encode(parameterValue);
            }

            result.add(new EncodedParameter(substitution.urlParameterName(), parameterValue));
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
