package com.microsoft.rest.v2;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class SwaggerMethodProxyDetails {
    private String method;
    private String relativePath;
    private final List<Substitution> hostSubstitutions = new ArrayList<>();
    private final List<Substitution> pathSubstitutions = new ArrayList<>();
    private final List<Substitution> querySubstitutions = new ArrayList<>();
    private final List<Substitution> headerSubstitutions = new ArrayList<>();
    private Integer bodyContentMethodParameterIndex;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getRelativePath() {
        return this.relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public void setMethodAndRelativePath(String method, String relativePath) {
        setMethod(method);
        setRelativePath(relativePath);
    }

    public void addHostSubstitution(String hostParameterName, int methodParameterIndex, boolean shouldEncode) {
        hostSubstitutions.add(new Substitution(hostParameterName, methodParameterIndex, shouldEncode));
    }

    public String applyHostSubstitutions(String host, Object[] methodArguments) {
        return applySubstitutions(host, hostSubstitutions, methodArguments);
    }

    public void addPathSubstitution(String pathParameterName, int methodParameterIndex, boolean shouldEncode) {
        pathSubstitutions.add(new Substitution(pathParameterName, methodParameterIndex, shouldEncode));
    }

    public String getSubstitutedPath(Object[] methodArguments) {
        return applySubstitutions(relativePath, pathSubstitutions, methodArguments);
    }

    public void addQuerySubstitution(String queryParameterName, int methodParameterIndex, boolean shouldEncode) {
        querySubstitutions.add(new Substitution(queryParameterName, methodParameterIndex, shouldEncode));
    }

    public Iterable<EncodedParameter> getEncodedQueryParameters(Object[] methodArguments) {
        return getEncodedParameters(querySubstitutions, methodArguments);
    }

    public void addHeaderSubstitution(String headerParameterName, int methodParameterIndex) {
        headerSubstitutions.add(new Substitution(headerParameterName, methodParameterIndex, false));
    }

    public Iterable<EncodedParameter> getEncodedHeaderParameters(Object[] methodArguments) {
        return getEncodedParameters(headerSubstitutions, methodArguments);
    }

    public void setBodyContentMethodParameterIndex(int bodyMethodParameterIndex) {
        this.bodyContentMethodParameterIndex = bodyMethodParameterIndex;
    }

    public Integer getBodyContentMethodParameterIndex() {
        return bodyContentMethodParameterIndex;
    }

    private static String applySubstitutions(String originalValue, Iterable<Substitution> substitutions, Object[] methodArguments) {
        String result = originalValue;

        for (Substitution substitution : substitutions) {
            final Object methodArgument = methodArguments[substitution.getMethodParameterIndex()];

            String substitutionValue = String.valueOf(methodArgument);
            if (substitution.shouldEncode()) {
                substitutionValue = encode(substitutionValue);
            }

            result = result.replace("{" + substitution.getURLParameterName() + "}", substitutionValue);
        }

        return result;
    }

    private static Iterable<EncodedParameter> getEncodedParameters(Iterable<Substitution> substitutions, Object[] methodArguments) {
        final List<EncodedParameter> result = new ArrayList<>();

        for (Substitution substitution : substitutions) {
            final Object methodArgument = methodArguments[substitution.getMethodParameterIndex()];

            String parameterValue = String.valueOf(methodArgument);
            if (substitution.shouldEncode()) {
                parameterValue = encode(parameterValue);
            }

            result.add(new EncodedParameter(substitution.getURLParameterName(), parameterValue));
        }

        return result;
    }

    protected static String encode(String segment) {
        return encode(segment, "UTF-8");
    }

    protected static String encode(String segment, String encoding) {
        try {
            return URLEncoder.encode(segment, encoding);
        } catch (UnsupportedEncodingException e) {
            return segment;
        }
    }
}
