// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.models.binarydata.BinaryData;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientImpl.TestInterfaceClientService;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import java.io.IOException;
import io.clientcore.core.models.CoreException;
import java.util.LinkedHashMap;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.annotation.processor.test.implementation.models.FooListResult;
import java.util.stream.Collectors;
import io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON;
import java.util.Arrays;
import java.io.InputStream;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.serialization.ObjectSerializer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Initializes a new instance of the TestInterfaceClientServiceImpl type.
 */
public class TestInterfaceClientServiceImpl implements TestInterfaceClientService {

    private static final ClientLogger LOGGER = new ClientLogger(TestInterfaceClientService.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private TestInterfaceClientServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of TestInterfaceClientService that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `TestInterfaceClientService`;
     */
    public static TestInterfaceClientService getNewInstance(HttpPipeline httpPipeline) {
        return new TestInterfaceClientServiceImpl(httpPipeline);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Void> testMethod(String uri, ByteBuffer request, String contentType, Long contentLength) {
        String url = uri + "/" + "my/uri/path";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)).add(HttpHeaderName.CONTENT_TYPE, contentType);
        if (request != null) {
            httpRequest.setBody(BinaryData.fromBytes(request.array()));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Void> testMethod(String uri, BinaryData data, String contentType, Long contentLength) {
        String url = uri + "/" + "my/uri/path";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)).add(HttpHeaderName.CONTENT_TYPE, contentType);
        if (data != null) {
            BinaryData binaryData = data;
            if (binaryData.getLength() != null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
                httpRequest.setBody(binaryData);
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Void> testListNext(String nextLink) {
        String url = nextLink;
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Void testMethodReturnsVoid(String uri) {
        String url = uri + "/" + "my/uri/path";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
        return null;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Foo> getFoo(String key, String label, String syncToken) {
        String url = "kv/" + UriEscapers.PATH_ESCAPER.escape(key);
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("label"), UriEscapers.QUERY_ESCAPER.escape(label));
        String newUrl = CoreUtils.appendQueryParams(url, queryParamMap);
        if (newUrl != null) {
            url = newUrl;
        }
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.fromString("Sync-Token"), syncToken);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, Foo.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (Foo) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<FooListResult> listFooListResult(String uri, RequestContext requestContext) {
        String url = uri + "/" + "foos";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, FooListResult.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (FooListResult) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<FooListResult> listNextFooListResult(String nextLink, RequestContext requestContext) {
        String url = nextLink;
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, FooListResult.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (FooListResult) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<List<Foo>> listFoo(String uri, List<String> tags, List<String> tags2, RequestContext requestContext) {
        String url = uri + "/" + "foos";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("tags"), (tags != null ? tags.stream().map(UriEscapers.QUERY_ESCAPER::escape).collect(Collectors.toList()) : null));
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("tags2"), (tags2 != null ? tags2.stream().map(UriEscapers.QUERY_ESCAPER::escape).collect(Collectors.toList()) : null));
        String newUrl = CoreUtils.appendQueryParams(url, queryParamMap);
        if (newUrl != null) {
            url = newUrl;
        }
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(List.class, Foo.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (List<Foo>) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<List<Foo>> listNextFoo(String nextLink, RequestContext requestContext) {
        String url = nextLink;
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(List.class, Foo.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (List<Foo>) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<HttpBinJSON> putResponse(String uri, int putBody, RequestContext requestContext) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (xmlSerializer.supportsFormat(serializationFormat)) {
            httpRequest.setBody(BinaryData.fromObject(putBody, xmlSerializer));
        } else {
            httpRequest.setBody(BinaryData.fromObject(putBody, jsonSerializer));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (HttpBinJSON) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<HttpBinJSON> postStreamResponse(String uri, int putBody, RequestContext requestContext) {
        String url = uri + "/" + "stream";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (xmlSerializer.supportsFormat(serializationFormat)) {
            httpRequest.setBody(BinaryData.fromObject(putBody, xmlSerializer));
        } else {
            httpRequest.setBody(BinaryData.fromObject(putBody, jsonSerializer));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (HttpBinJSON) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public byte[] getByteArray(String uri) {
        String url = uri + "/" + "bytes/100";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        BinaryData responseBody = networkResponse.getValue();
        byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;
        return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public byte[] getByteArray(String scheme, String hostName, int numberOfBytes) {
        String url = "bytes/" + numberOfBytes;
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        BinaryData responseBody = networkResponse.getValue();
        byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;
        return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void getNothing(String uri) {
        String url = uri + "/" + "bytes/100";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON getAnything(String uri) {
        String url = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON getAnythingWithPlus(String uri) {
        String url = uri + "/" + "anything/with+plus";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON getAnythingWithPathParam(String uri, String pathParam) {
        String url = uri + "/" + "anything/" + UriEscapers.PATH_ESCAPER.escape(pathParam);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON getAnythingWithEncodedPathParam(String uri, String pathParam) {
        String url = uri + "/" + "anything/" + pathParam;
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON getAnything(String uri, String a, int b) {
        String url = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("a"), UriEscapers.QUERY_ESCAPER.escape(a));
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("b"), b);
        String newUrl = CoreUtils.appendQueryParams(url, queryParamMap);
        if (newUrl != null) {
            url = newUrl;
        }
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON getAnythingWithHeaderParam(String uri, String a, int b) {
        String url = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.fromString("a"), a).add(HttpHeaderName.fromString("b"), String.valueOf(b));
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON getAnythingWithEncoded(String uri, String a, int b) {
        String url = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("a"), a);
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("b"), b);
        String newUrl = CoreUtils.appendQueryParams(url, queryParamMap);
        if (newUrl != null) {
            url = newUrl;
        }
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithNoContentTypeAndStringBody(String uri, String body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithNoContentTypeAndByteArrayBody(String uri, byte[] body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithHeaderApplicationJsonContentTypeAndStringBody(String uri, String body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithHeaderApplicationJsonContentTypeAndByteArrayBody(String uri, byte[] body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_TYPE, "application/json");
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(String uri, String body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_TYPE, "application/json; charset=utf-8");
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<HttpBinJSON> putWithHeaderApplicationOctetStreamContentTypeAndStringBody(String uri, String body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (HttpBinJSON) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(String uri, byte[] body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<HttpBinJSON> putWithBodyParamApplicationJsonContentTypeAndStringBody(String uri, String body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (HttpBinJSON) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(String uri, String body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json; charset=utf-8");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(String uri, byte[] body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(String uri, String body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(String uri, byte[] body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON put(String uri, int putBody, RequestContext requestContext) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (xmlSerializer.supportsFormat(serializationFormat)) {
            httpRequest.setBody(BinaryData.fromObject(putBody, xmlSerializer));
        } else {
            httpRequest.setBody(BinaryData.fromObject(putBody, jsonSerializer));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON get1(String uri, String queryParam) {
        String url = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("constantParam1", "constantValue1");
        queryParamMap.put("constantParam2", "constantValue2");
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("variableParam"), UriEscapers.QUERY_ESCAPER.escape(queryParam));
        String newUrl = CoreUtils.appendQueryParams(url, queryParamMap);
        if (newUrl != null) {
            url = newUrl;
        }
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON get2(String uri, String queryParam) {
        String url = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("param", Arrays.asList("constantValue1", "constantValue2"));
        String newUrl = CoreUtils.appendQueryParams(url, queryParamMap);
        if (newUrl != null) {
            url = newUrl;
        }
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON get3(String uri, String queryParam) {
        String url = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("param", Arrays.asList("constantValue1,constantValue2", "constantValue3"));
        String newUrl = CoreUtils.appendQueryParams(url, queryParamMap);
        if (newUrl != null) {
            url = newUrl;
        }
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON get4(String uri) {
        String url = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("queryparamwithequalsandnovalue", "");
        queryParamMap.put("queryparamwithnoequals", null);
        String newUrl = CoreUtils.appendQueryParams(url, queryParamMap);
        if (newUrl != null) {
            url = newUrl;
        }
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON get5(String uri) {
        String url = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("constantParam1", "some=value");
        String newUrl = CoreUtils.appendQueryParams(url, queryParamMap);
        if (newUrl != null) {
            url = newUrl;
        }
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON get6(String uri) {
        String url = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        String newUrl = CoreUtils.appendQueryParams(url, queryParamMap);
        if (newUrl != null) {
            url = newUrl;
        }
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON get7(String uri) {
        String url = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        String newUrl = CoreUtils.appendQueryParams(url, queryParamMap);
        if (newUrl != null) {
            url = newUrl;
        }
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Void> getVoidResponse(String uri) {
        String url = uri + "/" + "bytes/100";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<HttpBinJSON> putBody(String uri, String body) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (HttpBinJSON) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<InputStream> getBytes(String uri) {
        String url = uri + "/" + "bytes/1024";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 400;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, InputStream.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (InputStream) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public byte[] getBytes100(String uri) {
        String url = uri + "/" + "bytes/100";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        BinaryData responseBody = networkResponse.getValue();
        byte[] responseBodyBytes = responseBody != null ? responseBody.toBytes() : null;
        return responseBodyBytes != null ? (responseBodyBytes.length == 0 ? null : responseBodyBytes) : null;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<HttpBinJSON> put(String host, BinaryData content, long contentLength) {
        String url = "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength));
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/plain");
        if (content != null) {
            BinaryData binaryData = content;
            if (binaryData.getLength() != null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
                httpRequest.setBody(binaryData);
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (HttpBinJSON) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON put(String uri, Map<String, String> headerCollection) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.fromString("ABC"), String.valueOf(headerCollection));
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void headvoid(String uri) {
        String url = uri + "/" + "voideagerreadoom";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Void headVoid(String uri) {
        String url = uri + "/" + "voideagerreadoom";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
        return null;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Void> headResponseVoid(String uri) {
        String url = uri + "/" + "voideagerreadoom";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<Void> head(String uri) {
        String url = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public boolean headBoolean(String uri) {
        String url = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
        return expectedResponse;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void voidHead(String uri) {
        String url = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON put(String uri, int putBody) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (xmlSerializer.supportsFormat(serializationFormat)) {
            httpRequest.setBody(BinaryData.fromObject(putBody, xmlSerializer));
        } else {
            httpRequest.setBody(BinaryData.fromObject(putBody, jsonSerializer));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putBodyAndContentLength(String uri, ByteBuffer body, long contentLength) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength));
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body.array()));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithUnexpectedResponse(String uri, String putBody) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (putBody != null) {
            httpRequest.setBody(BinaryData.fromString(putBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithUnexpectedResponseAndExceptionType(String uri, String putBody) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (putBody != null) {
            httpRequest.setBody(BinaryData.fromString(putBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithUnexpectedResponseAndDeterminedExceptionType(String uri, String putBody) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (putBody != null) {
            httpRequest.setBody(BinaryData.fromString(putBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithUnexpectedResponseAndFallthroughExceptionType(String uri, String putBody) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (putBody != null) {
            httpRequest.setBody(BinaryData.fromString(putBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putWithUnexpectedResponseAndNoFallthroughExceptionType(String uri, String putBody) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (putBody != null) {
            httpRequest.setBody(BinaryData.fromString(putBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON post(String uri, String postBody) {
        String url = uri + "/" + "post";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (postBody != null) {
            httpRequest.setBody(BinaryData.fromString(postBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON delete(String uri, boolean bodyBoolean) {
        String url = uri + "/" + "delete";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.DELETE).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (xmlSerializer.supportsFormat(serializationFormat)) {
            httpRequest.setBody(BinaryData.fromObject(bodyBoolean, xmlSerializer));
        } else {
            httpRequest.setBody(BinaryData.fromObject(bodyBoolean, jsonSerializer));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON patch(String uri, String bodyString) {
        String url = uri + "/" + "patch";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PATCH).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (bodyString != null) {
            httpRequest.setBody(BinaryData.fromString(bodyString));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON get(String uri) {
        String url = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        httpRequest.getHeaders().add(HttpHeaderName.fromString("MyHeader"), "MyHeaderValue").add(new HttpHeader(HttpHeaderName.fromString("MyOtherHeader"), Arrays.asList("My", "Header", "Value")));
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON putByteArray(String uri, byte[] bytes) {
        String url = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (bytes != null) {
            httpRequest.setBody(BinaryData.fromBytes(bytes));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public HttpBinJSON get(String scheme, String hostPart1, String hostPart2) {
        String url = "get";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void getStatus200(String uri) {
        String url = uri + "/" + "status/200";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        networkResponse.close();
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void getStatus200WithExpectedResponse200(String uri) {
        String url = uri + "/" + "status/200";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void getStatus300(String uri) {
        String url = uri + "/" + "status/300";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        networkResponse.close();
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void getStatus300WithExpectedResponse300(String uri) {
        String url = uri + "/" + "status/300";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 300;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void getStatus400(String uri) {
        String url = uri + "/" + "status/400";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        networkResponse.close();
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void getStatus400WithExpectedResponse400(String uri) {
        String url = uri + "/" + "status/400";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 400;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void getStatus500(String uri) {
        String url = uri + "/" + "status/500";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        networkResponse.close();
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public void getStatus500WithExpectedResponse500(String uri) {
        String url = uri + "/" + "status/500";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 500;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        networkResponse.close();
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<BinaryData> put(String uri, BinaryData putBody, ServerSentEventListener serverSentEventListener) {
        String url = uri + "/" + "serversentevent";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (putBody != null) {
            BinaryData binaryData = putBody;
            if (binaryData.getLength() != null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
                httpRequest.setBody(binaryData);
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, BinaryData.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (BinaryData) result);
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public BinaryData get(String uri, ServerSentEventListener serverSentEventListener) {
        String url = uri + "/" + "serversentevent";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(url);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.models.binarydata.BinaryData.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        networkResponse.close();
        return (io.clientcore.core.models.binarydata.BinaryData) result;
    }

    @SuppressWarnings({ "unchecked", "cast" })
    @Override
    public Response<BinaryData> post(String uri, BinaryData postBody, ServerSentEventListener serverSentEventListener, RequestContext requestOptions) {
        String url = uri + "/" + "serversentevent";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(url);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (postBody != null) {
            BinaryData binaryData = postBody;
            if (binaryData.getLength() != null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
                httpRequest.setBody(binaryData);
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            throw new RuntimeException("Unexpected response code: " + responseCode);
        }
        Object result = null;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, BinaryData.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            result = decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), (BinaryData) result);
    }

    /**
     * Decodes the body of an {@link Response} into the type returned by the called API.
     * @param data The BinaryData to decode.
     * @param serializer The serializer to use.
     * @param returnType The type of the ParameterizedType return value.
     * @return The decoded value.
     * @throws CoreException If the deserialization fails.
     */
    private static Object decodeNetworkResponse(BinaryData data, ObjectSerializer serializer, ParameterizedType returnType) {
        if (data == null) {
            return null;
        }
        try {
            if (List.class.isAssignableFrom((Class<?>) returnType.getRawType())) {
                return serializer.deserializeFromBytes(data.toBytes(), returnType);
            }
            Type token = returnType.getRawType();
            if (Response.class.isAssignableFrom((Class<?>) token)) {
                token = returnType.getActualTypeArguments()[0];
            }
            return serializer.deserializeFromBytes(data.toBytes(), token);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(CoreException.from(e));
        }
    }
}
