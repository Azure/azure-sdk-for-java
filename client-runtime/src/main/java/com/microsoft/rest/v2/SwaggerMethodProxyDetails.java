package com.microsoft.rest.v2;

import com.microsoft.rest.v2.annotations.*;

import java.util.HashMap;
import java.util.Map;

public class SwaggerMethodProxyDetails {
    public String method;
    public String relativePath;
    public final Map<String, Integer> hostArgs = new HashMap<>();
    public final Map<String, Integer> pathArgs = new HashMap<>();
    public final Map<String, Integer> queryArgs = new HashMap<>();
    public final Map<String, Integer> headerArgs = new HashMap<>();
    public Integer bodyArg;

    public void addHostParamDetails(HostParam hostParamAnnotation, int parameterIndex) {
        final String hostParameterName = hostParamAnnotation.value();
        hostArgs.put(hostParameterName, hostParamAnnotation.encoded() ? parameterIndex : -parameterIndex);
    }

    public void addPathParamDetails(PathParam pathParamAnnotation, int parameterIndex) {
        final String pathParameterName = pathParamAnnotation.value();
        pathArgs.put(pathParameterName, pathParamAnnotation.encoded() ? parameterIndex : -parameterIndex);
    }

    public void addQueryParamDetails(QueryParam queryParamAnnotation, int parameterIndex) {
        final String queryParameterName = queryParamAnnotation.value();
        queryArgs.put(queryParameterName, queryParamAnnotation.encoded() ? parameterIndex : -parameterIndex);
    }

    public void addHeaderParamDetails(HeaderParam headerParamAnnotation, int parameterIndex) {
        final String queryParameterName = headerParamAnnotation.value();
        queryArgs.put(queryParameterName, parameterIndex);
    }

    public void addBodyParamDetails(BodyParam bodyParamAnnotation, int parameterIndex) {
        bodyArg = parameterIndex;
    }
}
