// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.models;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.utils.CoreUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Represents the context of an HTTP request, including its configuration, parameters, headers, and other details.
 */
public final class HttpRequestContext {

    // Request Configuration
    private String methodName;
    private TypeMirror methodReturnType;
    private final List<MethodParameter> parameters;
    private HttpMethod httpMethod;
    private boolean isConvenience;

    // This is a list of variable names that could be used when creating the HttpRequest.uri.
    // A list of them is needed as these could conflict with the names of parameters in this request method.
    // Every time a 'MethodParameter' is added to this request a remove call will be made to the set.
    // If the set ever becomes empty an exception will be thrown.
    private final LinkedHashSet<String> possibleUriParameterNames
        = new LinkedHashSet<>(Arrays.asList("uri", "requestUri", "httpRequestUri"));
    private String uriParameterName = null;

    private boolean templateHasHost;

    // This comes from the @Host annotation that is applied to the entire service interface, it will likely have one
    // or more substitutions in it, which will be replaced with the appropriate parameter values annotated with @HostParam.
    private String host;

    // This comes from the @HttpRequestInformation.path annotation that is applied to each method in the service interface.
    // It will likely have one or more substitutions in it, which will be replaced with the appropriate parameter values
    // annotated with @PathParam.
    private String path;

    private final Map<String, List<String>> headers;
    private final Map<String, QueryParameter> queryParams;

    private final Map<String, Substitution> substitutions;

    private int[] expectedStatusCodes;
    private TypeMirror defaultExceptionBodyType;
    private boolean isUriNextLink;
    private TypeMirror returnValueWireType;

    /**
     * Constructs a new HttpRequestContext with default values.
     */
    public HttpRequestContext() {
        this.parameters = new ArrayList<>();
        this.headers = new HashMap<>();
        this.queryParams = new LinkedHashMap<>();
        this.substitutions = new HashMap<>();
    }

    private Body body;
    private final Map<Integer, ExceptionBodyTypeInfo> exceptionBodyMappings = new HashMap<>();

    /**
     * Gets the method name.
     *
     * @return the method name.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the method name.
     *
     * @param methodName the method name to set.
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Gets the method return type.
     *
     * @return the method return type.
     */
    public TypeMirror getMethodReturnType() {
        return methodReturnType;
    }

    /**
     * Sets the method return type.
     *
     * @param methodReturnType the method return type to set.
     */
    public void setMethodReturnType(TypeMirror methodReturnType) {
        this.methodReturnType = methodReturnType;
    }

    /**
     * Adds a parameter to the method.
     *
     * @param parameter the parameter to add.
     * @throws IllegalStateException If the addition of the parameter caused there to be no available
     * {@link #getUriParameterName()} choice left.
     */
    public void addParameter(MethodParameter parameter) {
        this.parameters.add(parameter);
        this.possibleUriParameterNames.remove(parameter.getName());
        if (this.possibleUriParameterNames.isEmpty()) {
            throw new IllegalStateException("The acceptable URI variable names weren't sufficient to prevent collision "
                + "with parameter names of the method. File an issue on azure-sdk-for-java to expand the available "
                + "choices to prevent conflicts.");
        }
    }

    /**
     * Gets the list of parameters.
     *
     * @return the list of parameters.
     */
    public List<MethodParameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    /**
     * Gets the variable name that is used when creating the HttpRequest.uri.
     *
     * @return The variable name used when creating the HttpRequest.uri.
     */
    public String getUriParameterName() {
        if (this.uriParameterName == null) {
            this.uriParameterName = this.possibleUriParameterNames.iterator().next();
        }

        return this.uriParameterName;
    }

    /**
     * Indicates whether the interface had a host configured.
     *
     * @return Whether the interface had a host configured.
     */
    public boolean isTemplateHasHost() {
        return templateHasHost;
    }

    /**
     * Sets whether the interface had a host configured.
     *
     * @param templateHasHost Whether the interface had a host configured.
     */
    public void setTemplateHasHost(boolean templateHasHost) {
        this.templateHasHost = templateHasHost;
    }

    /**
     * Gets the host.
     *
     * @return the host.
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the host.
     *
     * @param host the host to set.
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Gets the path.
     *
     * @return the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path.
     *
     * @param path the path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Gets the HTTP method.
     *
     * @return the HTTP method.
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    /**
     * Sets the HTTP method.
     *
     * @param httpMethod the HTTP method to set.
     */
    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    /**
     * Gets the headers.
     *
     * @return the headers.
     */
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    /**
     * Adds a header.
     *
     * @param key the header key.
     * @param value the header value.
     */
    public void addHeader(String key, String value) {
        this.headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    /**
     * Gets the query parameters.
     *
     * @return the query parameters.
     */
    public Map<String, QueryParameter> getQueryParams() {
        return queryParams;
    }

    /**
     * Adds a query parameter.
     *
     * @param key the query parameter key.
     * @param value the query parameter value.
     * @param isMultiple Whether this query parameter list values should be sent as individual query param.
     * @param shouldEncode Whether the query parameter value is URL encoded.
     * @param isStatic Whether the query parameter is a static value.
     */
    public void addQueryParam(String key, String value, boolean isMultiple, boolean shouldEncode, boolean isStatic) {
        QueryParameter existing = queryParams.get(key);
        if (existing != null) {
            existing.addValue(value);
        } else {
            queryParams.put(key, new QueryParameter(value, isMultiple, shouldEncode, isStatic));
        }
    }

    /**
     * Adds a substitution.
     *
     * @param substitution the substitution to add.
     *
     * @throws IllegalArgumentException if a duplicate substitution is added.
     */
    public void addSubstitution(Substitution substitution) {
        if (substitutions.containsKey(substitution.getParameterName())) {
            throw new IllegalArgumentException(
                "Cannot add duplicate substitution for parameter '" + substitution.getParameterName() + "'");
        }
        substitutions.put(substitution.getParameterName(), substitution);
    }

    /**
     * Gets a substitution by parameter name.
     *
     * @param parameterName the parameter name.
     *
     * @return the substitution.
     */
    public Substitution getSubstitution(String parameterName) {
        return substitutions.get(parameterName);
    }

    /**
     * Sets the body.
     *
     * @param body the body to set.
     */
    public void setBody(Body body) {
        this.body = body;
    }

    /**
     * Gets the body.
     *
     * @return the body.
     */
    public Body getBody() {
        return body;
    }

    /**
     * Sets the expected status codes.
     *
     * @param expectedStatusCodes the expected status codes to set.
     */
    public void setExpectedStatusCodes(int[] expectedStatusCodes) {
        if (expectedStatusCodes != null) {
            Arrays.sort(expectedStatusCodes);
        }
        this.expectedStatusCodes = expectedStatusCodes;
    }

    /**
     * Gets the expected status codes.
     *
     * @return the expected status codes.
     */
    public List<Integer> getExpectedStatusCodes() {
        return Arrays.stream(expectedStatusCodes).boxed().collect(Collectors.toList());
    }

    /**
     * Gets the boolean to true if the provided method is a default method
     *
     * @return the boolean to true if the provided method is a default method
     */
    public boolean isConvenience() {
        return isConvenience;
    }

    /**
     * Sets the boolean to true if the provided method is a default method
     *
     * @param isConvenience the provided method is a default method
     */
    public void setIsConvenience(boolean isConvenience) {
        this.isConvenience = isConvenience;
    }

    /**
     * Sets the static headers from an array of strings.
     *
     * @param headers the array of headers to set.
     */
    public void addStaticHeaders(String[] headers) {
        if (CoreUtils.isNullOrEmpty(headers)) {
            return;
        }
        for (String header : headers) {
            String[] parts = header.split(":", 2);
            String key = parts[0].trim();
            String value = parts.length > 1 ? parts[1].trim() : "";
            // Split on comma, trim, and filter out empty values
            List<String> values = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
            this.headers.put(key, values);
        }
    }

    /**
     * Sets the static query parameters from an array of strings.
     *
     * @param queryParams the array of query parameters to set.
     */
    public void addStaticQueryParams(String[] queryParams) {
        if (CoreUtils.isNullOrEmpty(queryParams)) {
            return;
        }
        for (String queryParam : queryParams) {
            String[] parts = queryParam.split("=", 2);
            String key = parts[0].trim();
            String value = parts.length > 1 ? parts[1].trim() : null;
            addQueryParam(key, value, true, false, true);
        }
    }

    /**
     * Adds an exception body mapping.
     *
     * @param statusCode the status code.
     * @param exceptionBodyClassName the exception body class name.
     */
    public void addExceptionBodyMapping(int statusCode, ExceptionBodyTypeInfo exceptionBodyClassName) {
        exceptionBodyMappings.put(statusCode, exceptionBodyClassName);
    }

    /**
     * Gets the exception body mappings.
     *
     * @return the exception body mappings.
     */
    public Map<Integer, ExceptionBodyTypeInfo> getExceptionBodyMappings() {
        return exceptionBodyMappings;
    }

    /**
     * Sets the default exception body type.
     *
     * @param type the default exception body type to set.
     */
    public void setDefaultExceptionBodyType(TypeMirror type) {
        this.defaultExceptionBodyType = type;
    }

    /**
     * Gets the default exception body type.
     *
     * @return the default exception body type.
     */
    public TypeMirror getDefaultExceptionBodyType() {
        return defaultExceptionBodyType;
    }

    /**
     * Sets whether the URI is a {nextlink}.
     *
     * @param isUriNextLink true if it is a {nextlink}, false otherwise.
     */
    public void setIsUriNextLink(boolean isUriNextLink) {
        this.isUriNextLink = isUriNextLink;
    }

    /**
     * Checks if the URI is a {nextlink}.
     *
     * @return true if it is a {nextlink}, false otherwise.
     */
    public boolean isUriNextLink() {
        return isUriNextLink;
    }

    /**
     * Sets the return value wire type.
     *
     * @param returnValueWireType the return value wire type to set.
     */
    public void setReturnValueWireType(TypeMirror returnValueWireType) {
        this.returnValueWireType = returnValueWireType;
    }

    /**
     * Gets the return value wire type.
     *
     * @return the return value wire type.
     */
    public TypeMirror getReturnValueWireType() {
        return returnValueWireType;
    }

    /**
     * Represents a method parameter.
     */
    public static class MethodParameter {
        private final TypeMirror type;
        private final String shortTypeName;
        private final String name;
        private final VariableElement variableElement;

        /**
         * Constructs a new MethodParameter.
         *
         * @param type the type of the parameter.
         * @param shortTypeName the short type name of the parameter.
         * @param name the name of the parameter.
         * @param variableElement The element that created the MethodParameter
         */
        public MethodParameter(TypeMirror type, String shortTypeName, String name, VariableElement variableElement) {
            this.type = type;
            this.shortTypeName = shortTypeName;
            this.name = name;
            this.variableElement = variableElement;
        }

        /**
         * Gets the type mirror.
         *
         * @return the type mirror.
         */
        public TypeMirror getTypeMirror() {
            return type;
        }

        /**
         * Gets the short type name.
         *
         * @return the short type name.
         */
        public String getShortTypeName() {
            return shortTypeName;
        }

        /**
         * Gets the name.
         *
         * @return the name.
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the {@link VariableElement} that resulted in this {@link MethodParameter} to be created.
         * <p>
         * The {@link VariableElement} contains reflection information that may be useful when consuming the
         * {@link MethodParameter}.
         *
         * @return The {@link VariableElement} that resulted in this {@link MethodParameter} to be created.
         */
        public VariableElement getVariableElement() {
            return variableElement;
        }
    }

    /**
     * Represents the body of an HTTP request.
     */
    public static class Body {
        // This is the content type as specified in the @BodyParam annotation
        private final String contentType;

        // This is the type of the parameter that has been annotated with @BodyParam.
        // This is used to determine which setBody method to call on HttpRequest.
        private final TypeMirror parameterType;

        // This is the parameter name, so we can refer to it when setting the body on the HttpRequest.
        private final String parameterName;

        /**
         * Constructs a new Body.
         *
         * @param contentType the content type.
         * @param parameterType the parameter type.
         * @param parameterName the parameter name.
         */
        public Body(String contentType, TypeMirror parameterType, String parameterName) {
            this.contentType = contentType;
            this.parameterType = parameterType;
            this.parameterName = parameterName;
        }

        /**
         * Gets the content type.
         *
         * @return the content type.
         */
        public String getContentType() {
            return contentType;
        }

        /**
         * Gets the parameter type.
         *
         * @return the parameter type.
         */
        public TypeMirror getParameterType() {
            return parameterType;
        }

        /**
         * Gets the parameter name.
         *
         * @return the parameter name.
         */
        public String getParameterName() {
            return parameterName;
        }
    }

    /**
     * Represents a query parameter.
     */
    public static class QueryParameter {
        private final List<String> values;
        private final boolean isMultiple;
        private final boolean shouldEncode;
        private final boolean isStatic;

        /**
         * Constructs a new QueryParameter.
         *
         * @param value the value of the query parameter.
         * @param isMultiple whether the parameter can accept multiple values.
         * @param shouldEncode whether the parameter and value is encoded
         * @param isStatic Whether the query parameter is a static value.
         */
        public QueryParameter(String value, boolean isMultiple, boolean shouldEncode, boolean isStatic) {
            this.values = new ArrayList<>();
            this.values.add(value);
            this.isMultiple = isMultiple;
            this.shouldEncode = shouldEncode;
            this.isStatic = isStatic;
        }

        /**
         * Constructs a new QueryParameter with multiple values.
         *
         * @param values the values of the query parameter.
         * @param isMultiple whether the parameter can accept multiple values.
         * @param shouldEncode whether the parameter and value is encoded
         * @param isStatic Whether the query parameter is a static value.
         */
        public QueryParameter(List<String> values, boolean isMultiple, boolean shouldEncode, boolean isStatic) {
            this.values = new ArrayList<>(values);
            this.isMultiple = isMultiple;
            this.shouldEncode = shouldEncode;
            this.isStatic = isStatic;
        }

        /**
         * Gets the values of the query parameter.
         *
         * @return the values.
         */
        public List<String> getValues() {
            return Collections.unmodifiableList(values);
        }

        /**
         * Adds a value to the query parameter.
         *
         * @param value the value to add.
         */
        public void addValue(String value) {
            this.values.add(value);
        }

        /**
         * Checks if the query parameter can accept multiple values.
         *
         * @return true if it can accept multiple values, false otherwise.
         */
        public boolean isMultiple() {
            return isMultiple;
        }

        /**
         * Checks if the query parameter and value should be encoded.
         *
         * @return true if it should be encoded, false otherwise.
         */
        public boolean shouldEncode() {
            return shouldEncode;
        }

        /**
         * Whether the query parameter is a static value.
         *
         * @return Whether the query parameter is a static value.
         */
        public boolean isStatic() {
            return isStatic;
        }
    }

    /**
     * Represents information about the exception body type used in the HTTP request context.
     * <p>
     * This class encapsulates the {@link TypeMirror} of the exception body and a flag indicating
     * whether the type is the default Object.class.
     * </p>
     */
    public static class ExceptionBodyTypeInfo {
        private final TypeMirror typeMirror;
        private final boolean isDefaultObject;

        /**
         * Constructs a new ExceptionBodyTypeInfo.
         *
         * @param typeMirror the type mirror of the exception body.
         * @param isDefaultObject whether the type is the default object.
         */
        public ExceptionBodyTypeInfo(TypeMirror typeMirror, boolean isDefaultObject) {
            this.typeMirror = typeMirror;
            this.isDefaultObject = isDefaultObject;
        }

        /**
         * Gets the type mirror of the exception body.
         *
         * @return the type mirror.
         */
        public TypeMirror getTypeMirror() {
            return typeMirror;
        }

        /**
         * Checks if the type is the default object.
         *
         * @return true if it is the default object, false otherwise.
         */
        public boolean isDefaultObject() {
            return isDefaultObject;
        }
    }
}
