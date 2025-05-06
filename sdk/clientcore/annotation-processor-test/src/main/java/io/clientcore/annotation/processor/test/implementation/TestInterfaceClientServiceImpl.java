// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test.implementation;

import io.clientcore.annotation.processor.test.implementation.models.Foo;
import io.clientcore.annotation.processor.test.implementation.models.FooListResult;
import io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.models.ServerSentEventListener;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.models.binarydata.BinaryData;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import io.clientcore.annotation.processor.test.implementation.TestInterfaceClientImpl.TestInterfaceClientService;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.http.models.HttpResponseException;
import java.util.LinkedHashMap;
import java.lang.reflect.ParameterizedType;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Initializes a new instance of the TestInterfaceClientServiceImpl type.
 */
public class TestInterfaceClientServiceImpl implements TestInterfaceClientService {

    private static final HttpHeaderName A = HttpHeaderName.fromString("a");

    private static final HttpHeaderName ABC = HttpHeaderName.fromString("ABC");

    private static final HttpHeaderName B = HttpHeaderName.fromString("b");

    private static final HttpHeaderName MYHEADER = HttpHeaderName.fromString("MyHeader");

    private static final HttpHeaderName MYOTHERHEADER = HttpHeaderName.fromString("MyOtherHeader");

    private static final HttpHeaderName SYNC_TOKEN = HttpHeaderName.fromString("Sync-Token");

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

    @SuppressWarnings("cast")
    @Override
    public Response<Void> testMethod(String uri, ByteBuffer request, String contentType, Long contentLength) {
        String requestUri = uri + "/" + "my/uri/path";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(requestUri);
        if (contentLength != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        }
        if (contentType != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, contentType));
        }
        if (request != null) {
            httpRequest.setBody(BinaryData.fromBytes(request.array()));
        }
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                String errorMessage = networkResponse.getValue().toString();
                throw new HttpResponseException(errorMessage, networkResponse, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> testMethod(String uri, BinaryData data, String contentType, Long contentLength) {
        String requestUri = uri + "/" + "my/uri/path";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(requestUri);
        if (contentLength != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        }
        if (contentType != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, contentType));
        }
        if (data != null) {
            BinaryData binaryData = data;
            if (binaryData.getLength() != null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
                httpRequest.setBody(binaryData);
            }
        }
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                String errorMessage = networkResponse.getValue().toString();
                throw new HttpResponseException(errorMessage, networkResponse, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> testListNext(String nextLink) {
        String uri = nextLink;
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                String errorMessage = networkResponse.getValue().toString();
                throw new HttpResponseException(errorMessage, networkResponse, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Void testMethodReturnsVoid(String uri) {
        String requestUri = uri + "/" + "my/uri/path";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                String errorMessage = networkResponse.getValue().toString();
                throw new HttpResponseException(errorMessage, networkResponse, null);
            }
            return null;
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Foo> getFoo(String key, String label, String syncToken) {
        String uri = "kv/" + UriEscapers.PATH_ESCAPER.escape(key);
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("label"), UriEscapers.QUERY_ESCAPER.escape(label));
        uri = CoreUtils.appendQueryParams(uri, queryParamMap);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri);
        if (syncToken != null) {
            httpRequest.getHeaders().add(new HttpHeader(SYNC_TOKEN, syncToken));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        Foo deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, Foo.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<FooListResult> listFooListResult(String uri, RequestContext requestContext) {
        String requestUri = uri + "/" + "foos";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        FooListResult deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, FooListResult.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<FooListResult> listNextFooListResult(String nextLink, RequestContext requestContext) {
        String uri = nextLink;
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri);
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        FooListResult deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, FooListResult.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<List<Foo>> listFoo(String uri, List<String> tags, List<String> tags2, RequestContext requestContext) {
        String requestUri = uri + "/" + "foos";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("tags"), (tags != null ? tags.stream().map(UriEscapers.QUERY_ESCAPER::escape).collect(Collectors.toList()) : null));
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("tags2"), (tags2 != null ? tags2.stream().map(UriEscapers.QUERY_ESCAPER::escape).collect(Collectors.toList()) : null));
        requestUri = CoreUtils.appendQueryParams(requestUri, queryParamMap);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        List<Foo> deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(List.class, Foo.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<List<Foo>> listNextFoo(String nextLink, RequestContext requestContext) {
        String uri = nextLink;
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri);
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        List<Foo> deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(List.class, Foo.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<HttpBinJSON> putResponse(String uri, int putBody, RequestContext requestContext) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
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
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<HttpBinJSON> postStreamResponse(String uri, int putBody, RequestContext requestContext) {
        String requestUri = uri + "/" + "stream";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(requestUri);
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
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public byte[] getByteArray(String uri) {
        String requestUri = uri + "/" + "bytes/100";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                String errorMessage = networkResponse.getValue().toString();
                throw new HttpResponseException(errorMessage, networkResponse, null);
            }
            BinaryData responseBody = networkResponse.getValue();
            return responseBody != null ? responseBody.toBytes() : null;
        }
    }

    @SuppressWarnings("cast")
    @Override
    public void getNothing(String uri) {
        String requestUri = uri + "/" + "bytes/100";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON getAnything(String uri) {
        String requestUri = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON getAnythingWithPlus(String uri) {
        String requestUri = uri + "/" + "anything/with+plus";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON getAnythingWithPathParam(String uri, String pathParam) {
        String requestUri = uri + "/" + "anything/" + UriEscapers.PATH_ESCAPER.escape(pathParam);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON getAnythingWithEncodedPathParam(String uri, String pathParam) {
        String requestUri = uri + "/" + "anything/" + pathParam;
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON getAnything(String uri, String a, int b) {
        String requestUri = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("a"), UriEscapers.QUERY_ESCAPER.escape(a));
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("b"), b);
        requestUri = CoreUtils.appendQueryParams(requestUri, queryParamMap);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON getAnythingWithHeaderParam(String uri, String a, int b) {
        String requestUri = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        if (a != null) {
            httpRequest.getHeaders().add(new HttpHeader(A, a));
        }
        httpRequest.getHeaders().add(new HttpHeader(B, String.valueOf(b)));
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON getAnythingWithEncoded(String uri, String a, int b) {
        String requestUri = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("a"), a);
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("b"), b);
        requestUri = CoreUtils.appendQueryParams(requestUri, queryParamMap);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithNoContentTypeAndStringBody(String uri, String body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithNoContentTypeAndByteArrayBody(String uri, byte[] body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithHeaderApplicationJsonContentTypeAndStringBody(String uri, String body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithHeaderApplicationJsonContentTypeAndByteArrayBody(String uri, byte[] body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, "application/json"));
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(String uri, String body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, "application/json; charset=utf-8"));
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public Response<HttpBinJSON> putWithHeaderApplicationOctetStreamContentTypeAndStringBody(String uri, String body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, "application/octet-stream"));
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(String uri, byte[] body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, "application/octet-stream"));
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public Response<HttpBinJSON> putWithBodyParamApplicationJsonContentTypeAndStringBody(String uri, String body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(String uri, String body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json; charset=utf-8");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(String uri, byte[] body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(String uri, String body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(String uri, byte[] body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON put(String uri, int putBody, RequestContext requestContext) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
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
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON get1(String uri, String queryParam) {
        String requestUri = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("constantParam1", "constantValue1");
        queryParamMap.put("constantParam2", "constantValue2");
        queryParamMap.put(UriEscapers.QUERY_ESCAPER.escape("variableParam"), UriEscapers.QUERY_ESCAPER.escape(queryParam));
        requestUri = CoreUtils.appendQueryParams(requestUri, queryParamMap);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON get2(String uri, String queryParam) {
        String requestUri = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("param", Arrays.asList("constantValue1", "constantValue2"));
        requestUri = CoreUtils.appendQueryParams(requestUri, queryParamMap);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON get3(String uri, String queryParam) {
        String requestUri = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("param", Arrays.asList("constantValue1,constantValue2", "constantValue3"));
        requestUri = CoreUtils.appendQueryParams(requestUri, queryParamMap);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON get4(String uri) {
        String requestUri = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("queryparamwithequalsandnovalue", "");
        queryParamMap.put("queryparamwithnoequals", null);
        requestUri = CoreUtils.appendQueryParams(requestUri, queryParamMap);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON get5(String uri) {
        String requestUri = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        queryParamMap.put("constantParam1", "some=value");
        requestUri = CoreUtils.appendQueryParams(requestUri, queryParamMap);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON get6(String uri) {
        String requestUri = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        requestUri = CoreUtils.appendQueryParams(requestUri, queryParamMap);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON get7(String uri) {
        String requestUri = uri + "/" + "anything";
        // Append non-null query parameters
        LinkedHashMap<String, Object> queryParamMap = new LinkedHashMap<>();
        requestUri = CoreUtils.appendQueryParams(requestUri, queryParamMap);
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> getVoidResponse(String uri) {
        String requestUri = uri + "/" + "bytes/100";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                String errorMessage = networkResponse.getValue().toString();
                throw new HttpResponseException(errorMessage, networkResponse, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<HttpBinJSON> putBody(String uri, String body) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromString(body));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<InputStream> getBytes(String uri) {
        String requestUri = uri + "/" + "bytes/1024";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 400;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), networkResponse.getValue().toStream());
    }

    @SuppressWarnings("cast")
    @Override
    public byte[] getBytes100(String uri) {
        String requestUri = uri + "/" + "bytes/100";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                String errorMessage = networkResponse.getValue().toString();
                throw new HttpResponseException(errorMessage, networkResponse, null);
            }
            BinaryData responseBody = networkResponse.getValue();
            return responseBody != null ? responseBody.toBytes() : null;
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<HttpBinJSON> put(String host, BinaryData content, long contentLength) {
        String uri = host + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri);
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
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
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON put(String uri, Map<String, String> headerCollection) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        if (headerCollection != null) {
            httpRequest.getHeaders().add(new HttpHeader(ABC, String.valueOf(headerCollection)));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public void headvoid(String uri) {
        String requestUri = uri + "/" + "voideagerreadoom";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public Void headVoid(String uri) {
        String requestUri = uri + "/" + "voideagerreadoom";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(requestUri);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                String errorMessage = networkResponse.getValue().toString();
                throw new HttpResponseException(errorMessage, networkResponse, null);
            }
            return null;
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> headResponseVoid(String uri) {
        String requestUri = uri + "/" + "voideagerreadoom";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(requestUri);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                String errorMessage = networkResponse.getValue().toString();
                throw new HttpResponseException(errorMessage, networkResponse, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> head(String uri) {
        String requestUri = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(requestUri);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                String errorMessage = networkResponse.getValue().toString();
                throw new HttpResponseException(errorMessage, networkResponse, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public boolean headBoolean(String uri) {
        String requestUri = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(requestUri);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                String errorMessage = networkResponse.getValue().toString();
                throw new HttpResponseException(errorMessage, networkResponse, null);
            }
            return expectedResponse;
        }
    }

    @SuppressWarnings("cast")
    @Override
    public void voidHead(String uri) {
        String requestUri = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON put(String uri, int putBody) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
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
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putBodyAndContentLength(String uri, ByteBuffer body, long contentLength) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (body != null) {
            httpRequest.setBody(BinaryData.fromBytes(body.array()));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithUnexpectedResponse(String uri, String putBody) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (putBody != null) {
            httpRequest.setBody(BinaryData.fromString(putBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithUnexpectedResponseAndExceptionType(String uri, String putBody) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (putBody != null) {
            httpRequest.setBody(BinaryData.fromString(putBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithUnexpectedResponseAndDeterminedExceptionType(String uri, String putBody) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (putBody != null) {
            httpRequest.setBody(BinaryData.fromString(putBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithUnexpectedResponseAndFallthroughExceptionType(String uri, String putBody) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (putBody != null) {
            httpRequest.setBody(BinaryData.fromString(putBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putWithUnexpectedResponseAndNoFallthroughExceptionType(String uri, String putBody) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (putBody != null) {
            httpRequest.setBody(BinaryData.fromString(putBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON post(String uri, String postBody) {
        String requestUri = uri + "/" + "post";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (postBody != null) {
            httpRequest.setBody(BinaryData.fromString(postBody));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON delete(String uri, boolean bodyBoolean) {
        String requestUri = uri + "/" + "delete";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.DELETE).setUri(requestUri);
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
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON patch(String uri, String bodyString) {
        String requestUri = uri + "/" + "patch";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PATCH).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (bodyString != null) {
            httpRequest.setBody(BinaryData.fromString(bodyString));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON get(String uri) {
        String requestUri = uri + "/" + "anything";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        httpRequest.getHeaders().add(new HttpHeader(MYHEADER, "MyHeaderValue"));
        httpRequest.getHeaders().add(new HttpHeader(MYOTHERHEADER, Arrays.asList("My", "Header", "Value")));
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public HttpBinJSON putByteArray(String uri, byte[] bytes) {
        String requestUri = uri + "/" + "put";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        if (bytes != null) {
            httpRequest.setBody(BinaryData.fromBytes(bytes));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        HttpBinJSON deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(HttpBinJSON.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new RuntimeException(new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + "."));
        }
        return deserializedResult;
    }

    @SuppressWarnings("cast")
    @Override
    public void getStatus200(String uri) {
        String requestUri = uri + "/" + "status/200";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public void getStatus200WithExpectedResponse200(String uri) {
        String requestUri = uri + "/" + "status/200";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public void getStatus300(String uri) {
        String requestUri = uri + "/" + "status/300";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public void getStatus300WithExpectedResponse300(String uri) {
        String requestUri = uri + "/" + "status/300";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 300;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public void getStatus400(String uri) {
        String requestUri = uri + "/" + "status/400";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public void getStatus400WithExpectedResponse400(String uri) {
        String requestUri = uri + "/" + "status/400";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 400;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public void getStatus500(String uri) {
        String requestUri = uri + "/" + "status/500";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public void getStatus500WithExpectedResponse500(String uri) {
        String requestUri = uri + "/" + "status/500";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 500;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        networkResponse.close();
    }

    @SuppressWarnings("cast")
    @Override
    public Response<BinaryData> put(String uri, BinaryData putBody, ServerSentEventListener serverSentEventListener) {
        String requestUri = uri + "/" + "serversentevent";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(requestUri);
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
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        return networkResponse;
    }

    @SuppressWarnings("cast")
    @Override
    public BinaryData get(String uri, ServerSentEventListener serverSentEventListener) {
        String requestUri = uri + "/" + "serversentevent";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        return networkResponse.getValue();
    }

    @SuppressWarnings("cast")
    @Override
    public Response<BinaryData> post(String uri, BinaryData postBody, ServerSentEventListener serverSentEventListener, RequestContext requestOptions) {
        String requestUri = uri + "/" + "serversentevent";
        // Create the HTTP request
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(requestUri);
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
            String errorMessage = networkResponse.getValue().toString();
            networkResponse.close();
            throw new HttpResponseException(errorMessage, networkResponse, null);
        }
        return networkResponse;
    }
}
