/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2;

import com.microsoft.rest.RestClient;
import com.microsoft.rest.v2.annotations.BodyParam;
import com.microsoft.rest.v2.annotations.DELETE;
import com.microsoft.rest.v2.annotations.GET;
import com.microsoft.rest.v2.annotations.HEAD;
import com.microsoft.rest.v2.annotations.HeaderParam;
import com.microsoft.rest.v2.annotations.Host;
import com.microsoft.rest.v2.annotations.HostParam;
import com.microsoft.rest.v2.annotations.PATCH;
import com.microsoft.rest.v2.annotations.POST;
import com.microsoft.rest.v2.annotations.PUT;
import com.microsoft.rest.v2.annotations.PathParam;
import com.microsoft.rest.v2.annotations.QueryParam;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

// TODO: Convert this to RxNetty and finish
public class RestProxy implements InvocationHandler {
    private final String host;
    private final RestClient restClient;
    private final Map<String, SwaggerProxyDetails> matrix;

    private RestProxy(String host, RestClient restClient, Map<String, SwaggerProxyDetails> matrix) {
        this.host = host;
        this.restClient = restClient;
        this.matrix = matrix;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final SwaggerProxyDetails info = matrix.get(method.getName());
        RequestBody requestBody = null;
        if (info.bodyArg != null) {
            if (args[info.bodyArg] != null) {
                // TODO: what's the actual media type?
                requestBody = RequestBody.create(MediaType.parse("application/json"), restClient.serializerAdapter().serialize(args[info.bodyArg]));
            }
        }
        String actualHost = host;
        String actualPath = info.relativePath;
        for (Map.Entry<String, Integer> hostArg : info.hostArgs.entrySet()) {
            String hostValue;
            if (hostArg.getValue() < 0) {
                hostValue = encode(String.valueOf(args[-hostArg.getValue()]));
            } else {
                hostValue = String.valueOf(args[hostArg.getValue()]);
            }
            actualHost = actualHost.replace("{" + hostArg.getKey() + "}", hostValue);
        }
        for (Map.Entry<String, Integer> pathArg : info.pathArgs.entrySet()) {
            String pathValue;
            if (pathArg.getValue() < 0) {
                pathValue = encode(String.valueOf(args[-pathArg.getValue()]));
            } else {
                pathValue = String.valueOf(args[pathArg.getValue()]);
            }
            actualPath = actualPath.replace("{" + pathArg.getKey() + "}", pathValue);
        }

        String[] parts = actualHost.split("://");
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(parts[0])
                .host(parts[1])
                .addEncodedPathSegments(actualPath);

        for (Map.Entry<String, Integer> queryArg : info.queryArgs.entrySet()) {
            String queryValue;
            if (queryArg.getValue() < 0) {
                queryValue = encode(String.valueOf(args[-queryArg.getValue()]));
            } else {
                queryValue = String.valueOf(args[queryArg.getValue()]);
            }
            urlBuilder.addEncodedQueryParameter(queryArg.getKey(), queryValue);
        }

        Request.Builder requestBuilder = new Builder().method(info.method, requestBody)
                .url(urlBuilder.build());

        for (Map.Entry<String, Integer> headerArg : info.headerArgs.entrySet()) {
            String headerValue;
            if (headerArg.getValue() < 0) {
                headerValue = encode(String.valueOf(args[-headerArg.getValue()]));
            } else {
                headerValue = String.valueOf(args[headerArg.getValue()]);
            }
            requestBuilder.header(headerArg.getKey(), headerValue);
        }

        Response response = restClient.httpClient().newCall(requestBuilder.build()).execute();
        if (method.getReturnType().equals(Void.TYPE) || response.body() == null) {
            return null;
        } else if (method.getReturnType().isAssignableFrom(InputStream.class)) {
            return response.body().byteStream();
        } else if (method.getReturnType().isAssignableFrom(byte[].class)) {
            return response.body().bytes();
        } else {
            return restClient.serializerAdapter().deserialize(response.body().string(), method.getReturnType());
        }
    }

    @SuppressWarnings("unchecked")
    public static <A> A create(Class<A> swaggerInterface, RestClient restClient) {
        String host = restClient.retrofit().baseUrl().host();
        String protocol = restClient.retrofit().baseUrl().scheme();
        if (swaggerInterface.isAnnotationPresent(Host.class)) {
            host = swaggerInterface.getAnnotation(Host.class).value();
            if (!host.contains("://")) {
                host = protocol + "://" + host;
            }
        }
        Map<String, SwaggerProxyDetails> matrix = new HashMap<>();
        for (Method method : swaggerInterface.getDeclaredMethods()) {
            SwaggerProxyDetails info = new SwaggerProxyDetails();
            matrix.put(method.getName(), info);

            for (int i = 0; i != method.getParameterAnnotations().length; i++) {
                Annotation[] annotations = method.getParameterAnnotations()[i];
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType().equals(HostParam.class)) {
                        String name = ((HostParam) annotation).value();
                        // TODO: mark encoded using a better approach than positive/negative int
                        info.hostArgs.put(name, ((HostParam) annotation).encoded() ? i : -i);
                    }
                    if (annotation.annotationType().equals(PathParam.class)) {
                        String name = ((PathParam) annotation).value();
                        info.pathArgs.put(name, ((PathParam) annotation).encoded() ? i : -i);
                    }
                    if (annotation.annotationType().equals(QueryParam.class)) {
                        String name = ((QueryParam) annotation).value();
                        info.queryArgs.put(name, ((PathParam) annotation).encoded() ? i : -i);
                    }
                    if (annotation.annotationType().equals(HeaderParam.class)) {
                        String name = ((HeaderParam) annotation).value();
                        info.headerArgs.put(name, i);
                    }
                    if (annotation.annotationType().equals(BodyParam.class)) {
                        info.bodyArg = i;
                    }
                }
            }

            if (method.isAnnotationPresent(GET.class)) {
                info.method = "GET";
                info.relativePath = method.getAnnotation(GET.class).value();
            }
            if (method.isAnnotationPresent(PUT.class)) {
                info.method = "PUT";
                info.relativePath = method.getAnnotation(PUT.class).value();
            }
            if (method.isAnnotationPresent(HEAD.class)) {
                info.method = "HEAD";
                info.relativePath = method.getAnnotation(HEAD.class).value();
            }
            if (method.isAnnotationPresent(DELETE.class)) {
                info.method = "DELETE";
                info.relativePath = method.getAnnotation(DELETE.class).value();
            }
            if (method.isAnnotationPresent(POST.class)) {
                info.method = "POST";
                info.relativePath = method.getAnnotation(POST.class).value();
            }
            if (method.isAnnotationPresent(PATCH.class)) {
                info.method = "PATCH";
                info.relativePath = method.getAnnotation(PATCH.class).value();
            }
        }
        RestProxy restProxy = new RestProxy(host, restClient, matrix);
        return (A) Proxy.newProxyInstance(swaggerInterface.getClassLoader(), new Class[] { swaggerInterface }, restProxy);
    }

    private static final class SwaggerProxyDetails {
        private String method;
        private String relativePath;
        private Map<String, Integer> hostArgs;
        private Map<String, Integer> pathArgs;
        private Map<String, Integer> queryArgs;
        private Map<String, Integer> headerArgs;
        private Integer bodyArg;

        private SwaggerProxyDetails() {
            hostArgs = new HashMap<>();
            pathArgs = new HashMap<>();
            queryArgs = new HashMap<>();
            headerArgs = new HashMap<>();
        }
    }

    private static String encode(String segment) {
        try {
            return URLEncoder.encode(segment, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return segment;
        }
    }
}