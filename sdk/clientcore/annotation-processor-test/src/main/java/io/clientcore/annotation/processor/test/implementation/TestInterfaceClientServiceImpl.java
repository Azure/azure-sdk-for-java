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
import io.clientcore.core.utils.GeneratedCodeUtils;
import io.clientcore.core.utils.UriBuilder;
import java.util.HashMap;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Base64;
import java.util.ArrayList;

/**
 * Initializes a new instance of the TestInterfaceClientServiceImpl type.
 */
public class TestInterfaceClientServiceImpl implements TestInterfaceClientService {

    private static final HttpHeaderName A = HttpHeaderName.fromString("a");

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

    @Override
    public Response<Void> testMethod(String uri, ByteBuffer request, String contentType, Long contentLength) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri + "/" + "my/uri/path");
        if (contentLength != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        }
        if (request != null) {
            if (contentType != null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType);
            }
            httpRequest.setBody(BinaryData.fromBytes(io.clientcore.core.implementation.utils.ImplUtils.byteBufferToArray(request.duplicate())));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), null);
        }
    }

    @Override
    public Response<Void> testMethod(String uri, BinaryData data, String contentType, Long contentLength) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri + "/" + "my/uri/path");
        if (contentLength != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        }
        if (data != null) {
            if (contentType != null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType);
            }
            BinaryData binaryData = data;
            if (binaryData.getLength() != null && httpRequest.getHeaders().get(HttpHeaderName.CONTENT_LENGTH) == null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
            }
            httpRequest.setBody(binaryData);
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), null);
        }
    }

    @Override
    public Response<Void> testListNext(String nextLink) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(nextLink);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), null);
        }
    }

    @Override
    public Void testMethodReturnsVoid(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "my/uri/path");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try {
            return null;
        } finally {
            networkResponse.close();
        }
    }

    @Override
    public Response<Foo> getFoo(String key, String label, String syncToken) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse("kv/" + (key == null ? "" : UriEscapers.PATH_ESCAPER.escape(key)));
        GeneratedCodeUtils.addQueryParameter(uri, "label", true, label, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri.toString());
        if (syncToken != null) {
            httpRequest.getHeaders().add(new HttpHeader(SYNC_TOKEN, syncToken));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            Map<Integer, java.lang.reflect.ParameterizedType> statusToExceptionTypeMap = new HashMap<>();
            statusToExceptionTypeMap.put(400, CoreUtils.createParameterizedType(Object.class));
            statusToExceptionTypeMap.put(403, CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.OperationError.class));
            java.lang.reflect.ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.ServiceError.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, statusToExceptionTypeMap, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            Foo deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, io.clientcore.annotation.processor.test.implementation.models.Foo.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public Response<FooListResult> listFooListResult(String uri, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "foos");
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            FooListResult deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, io.clientcore.annotation.processor.test.implementation.models.FooListResult.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public Response<FooListResult> listNextFooListResult(String nextLink, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(nextLink);
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            FooListResult deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, io.clientcore.annotation.processor.test.implementation.models.FooListResult.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public Response<List<Foo>> listFoo(String uri, List<String> tags, List<String> tags2, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(uri + "/" + "foos");
        GeneratedCodeUtils.addQueryParameter(requestUri, "tags", true, tags, true);
        GeneratedCodeUtils.addQueryParameter(requestUri, "tags2", true, tags2, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            List<Foo> deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, CoreUtils.createParameterizedType(java.util.List.class, io.clientcore.annotation.processor.test.implementation.models.Foo.class));
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public Response<List<Foo>> listNextFoo(String nextLink, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(nextLink);
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            List<Foo> deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, CoreUtils.createParameterizedType(java.util.List.class, io.clientcore.annotation.processor.test.implementation.models.Foo.class));
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public Response<Map<String, Foo>> getFooMap(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "foo-map");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            Map<String, Foo> deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, CoreUtils.createParameterizedType(java.util.Map.class, java.lang.String.class, io.clientcore.annotation.processor.test.implementation.models.Foo.class));
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public Response<HttpBinJSON> putResponse(String uri, int putBody, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        SerializationFormat requestSerializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (xmlSerializer.supportsFormat(requestSerializationFormat)) {
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
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public Response<HttpBinJSON> postStreamResponse(String uri, int putBody, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri + "/" + "stream");
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        SerializationFormat requestSerializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (xmlSerializer.supportsFormat(requestSerializationFormat)) {
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
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public byte[] getByteArray(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "bytes/100");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            BinaryData responseBody = networkResponseToClose.getValue();
            byte[] responseBytes = responseBody != null ? responseBody.toBytes() : null;
            boolean quotedBase64 = responseBytes != null && responseBytes.length >= 2 && responseBytes[0] == '"' && responseBytes[responseBytes.length - 1] == '"' && GeneratedCodeUtils.isJsonContentType(networkResponseToClose.getHeaders());
            return quotedBase64 ? Base64.getDecoder().decode(Arrays.copyOfRange(responseBytes, 1, responseBytes.length - 1)) : responseBytes;
        }
    }

    @Override
    public void getNothing(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "bytes/100");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        networkResponse.close();
    }

    @Override
    public HttpBinJSON getAnything(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "anything");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON getAnythingWithPlus(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "anything/with+plus");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON getAnythingWithPathParam(String uri, String pathParam) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "anything/" + (pathParam == null ? "" : UriEscapers.PATH_ESCAPER.escape(pathParam)));
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON getAnythingWithEncodedPathParam(String uri, String pathParam) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "anything/" + (pathParam == null ? "" : pathParam));
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON getAnything(String uri, String a, int b) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(uri + "/" + "anything");
        GeneratedCodeUtils.addQueryParameter(requestUri, "a", true, a, true);
        GeneratedCodeUtils.addQueryParameter(requestUri, "b", true, b, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON getAnythingWithHeaderParam(String uri, String a, int b) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "anything");
        if (a != null) {
            httpRequest.getHeaders().add(new HttpHeader(A, a));
        }
        httpRequest.getHeaders().add(new HttpHeader(B, String.valueOf(b)));
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON getAnythingWithEncoded(String uri, String a, int b) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(uri + "/" + "anything");
        GeneratedCodeUtils.addQueryParameter(requestUri, "a", true, a, false);
        GeneratedCodeUtils.addQueryParameter(requestUri, "b", true, b, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithNoContentTypeAndStringBody(String uri, String body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithNoContentTypeAndByteArrayBody(String uri, byte[] body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromBytes(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithHeaderApplicationJsonContentTypeAndStringBody(String uri, String body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithHeaderApplicationJsonContentTypeAndByteArrayBody(String uri, byte[] body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromBytes(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithHeaderApplicationJsonContentTypeAndCharsetAndStringBody(String uri, String body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json; charset=utf-8");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public Response<HttpBinJSON> putWithHeaderApplicationOctetStreamContentTypeAndStringBody(String uri, String body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public HttpBinJSON putWithHeaderApplicationOctetStreamContentTypeAndByteArrayBody(String uri, byte[] body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromBytes(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public Response<HttpBinJSON> putWithBodyParamApplicationJsonContentTypeAndStringBody(String uri, String body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndCharsetAndStringBody(String uri, String body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json; charset=utf-8");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithBodyParamApplicationJsonContentTypeAndByteArrayBody(String uri, byte[] body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromBytes(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndStringBody(String uri, String body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithBodyParamApplicationOctetStreamContentTypeAndByteArrayBody(String uri, byte[] body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromBytes(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON put(String uri, int putBody, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        SerializationFormat requestSerializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (xmlSerializer.supportsFormat(requestSerializationFormat)) {
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
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON get1(String uri, String queryParam) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(uri + "/" + "anything");
        GeneratedCodeUtils.addQueryParameter(requestUri, "constantParam1", false, "constantValue1", false);
        GeneratedCodeUtils.addQueryParameter(requestUri, "constantParam2", false, "constantValue2", false);
        GeneratedCodeUtils.addQueryParameter(requestUri, "variableParam", true, queryParam, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON get2(String uri, String queryParam) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(uri + "/" + "anything");
        GeneratedCodeUtils.addQueryParameter(requestUri, "param", false, Arrays.asList("constantValue1", "constantValue2"), false);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON get3(String uri, String queryParam) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(uri + "/" + "anything");
        GeneratedCodeUtils.addQueryParameter(requestUri, "param", false, Arrays.asList("constantValue1,constantValue2", "constantValue3"), false);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON get4(String uri) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(uri + "/" + "anything");
        GeneratedCodeUtils.addQueryParameter(requestUri, "queryparamwithequalsandnovalue", false, "", false);
        GeneratedCodeUtils.addQueryParameter(requestUri, "queryparamwithnoequals", false, null, false);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON get5(String uri) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(uri + "/" + "anything");
        GeneratedCodeUtils.addQueryParameter(requestUri, "constantParam1", false, "some=value", false);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON get6(String uri) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(uri + "/" + "anything");
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON get7(String uri) {
        // Append the query parameters.
        UriBuilder requestUri = UriBuilder.parse(uri + "/" + "anything");
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(requestUri.toString());
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public Response<Void> getVoidResponse(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "bytes/100");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), null);
        }
    }

    @Override
    public Response<HttpBinJSON> putBody(String uri, String body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(body, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(body));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public Response<InputStream> getBytes(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "bytes/1024");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 400;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), networkResponse.getValue().toStream());
    }

    @Override
    public byte[] getBytes100(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "bytes/100");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            BinaryData responseBody = networkResponseToClose.getValue();
            byte[] responseBytes = responseBody != null ? responseBody.toBytes() : null;
            boolean quotedBase64 = responseBytes != null && responseBytes.length >= 2 && responseBytes[0] == '"' && responseBytes[responseBytes.length - 1] == '"' && GeneratedCodeUtils.isJsonContentType(networkResponseToClose.getHeaders());
            return quotedBase64 ? Base64.getDecoder().decode(Arrays.copyOfRange(responseBytes, 1, responseBytes.length - 1)) : responseBytes;
        }
    }

    @Override
    public Response<HttpBinJSON> put(String host, BinaryData content, long contentLength) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(host + "/" + "put");
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        if (content != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "text/plain");
            BinaryData binaryData = content;
            if (binaryData.getLength() != null && httpRequest.getHeaders().get(HttpHeaderName.CONTENT_LENGTH) == null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
            }
            httpRequest.setBody(binaryData);
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.core.http.models.Response.class, io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), deserializedResult);
        }
    }

    @Override
    public HttpBinJSON put(String uri, Map<String, String> headerCollection) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (headerCollection != null) {
            for (Map.Entry<?, ?> headerCollectionHeaderEntry : headerCollection.entrySet()) {
                if (headerCollectionHeaderEntry.getKey() != null && headerCollectionHeaderEntry.getValue() != null) {
                    httpRequest.getHeaders().set(HttpHeaderName.fromString("ABC" + headerCollectionHeaderEntry.getKey()), String.valueOf(headerCollectionHeaderEntry.getValue()));
                }
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public Response<Void> submitForm(String uri, String displayName, String alreadyEncoded) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri + "/" + "form");
        List<String> formDataValues = new ArrayList<>();
        if (displayName != null) {
            formDataValues.add("display+name=" + UriEscapers.FORM_ESCAPER.escape(String.valueOf(displayName)));
        }
        if (alreadyEncoded != null) {
            formDataValues.add("alreadyEncoded=" + String.valueOf(alreadyEncoded));
        }
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/x-www-form-urlencoded");
        httpRequest.setBody(BinaryData.fromString(String.join("&", formDataValues)));
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), null);
        }
    }

    @Override
    public Response<Void> submitMultipart(String uri, String mediaType, BinaryData body) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri + "/" + "multipart");
        if (body != null) {
            if (mediaType != null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, mediaType);
            }
            BinaryData binaryData = body;
            if (binaryData.getLength() != null && httpRequest.getHeaders().get(HttpHeaderName.CONTENT_LENGTH) == null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
            }
            httpRequest.setBody(binaryData);
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), null);
        }
    }

    @Override
    public void headvoid(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(uri + "/" + "voideagerreadoom");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        networkResponse.close();
    }

    @Override
    public Void headVoid(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(uri + "/" + "voideagerreadoom");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try {
            return null;
        } finally {
            networkResponse.close();
        }
    }

    @Override
    public Response<Void> headResponseVoid(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(uri + "/" + "voideagerreadoom");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), null);
        }
    }

    @Override
    public Response<Void> head(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(uri + "/" + "anything");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            return new Response<>(networkResponseToClose.getRequest(), responseCode, networkResponseToClose.getHeaders(), null);
        }
    }

    @Override
    public boolean headBoolean(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(uri + "/" + "anything");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try {
            return expectedResponse;
        } finally {
            networkResponse.close();
        }
    }

    @Override
    public void voidHead(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(uri + "/" + "anything");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        networkResponse.close();
    }

    @Override
    public HttpBinJSON put(String uri, int putBody) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        SerializationFormat requestSerializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (xmlSerializer.supportsFormat(requestSerializationFormat)) {
            httpRequest.setBody(BinaryData.fromObject(putBody, xmlSerializer));
        } else {
            httpRequest.setBody(BinaryData.fromObject(putBody, jsonSerializer));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putBodyAndContentLength(String uri, ByteBuffer body, long contentLength) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        if (body != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            httpRequest.setBody(BinaryData.fromBytes(io.clientcore.core.implementation.utils.ImplUtils.byteBufferToArray(body.duplicate())));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            java.lang.reflect.ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithUnexpectedResponse(String uri, String putBody) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (putBody != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(putBody, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(putBody));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithUnexpectedResponseAndExceptionType(String uri, String putBody) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (putBody != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(putBody, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(putBody));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            java.lang.reflect.ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.ServiceError.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithUnexpectedResponseAndDeterminedExceptionType(String uri, String putBody) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (putBody != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(putBody, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(putBody));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            Map<Integer, java.lang.reflect.ParameterizedType> statusToExceptionTypeMap = new HashMap<>();
            statusToExceptionTypeMap.put(200, CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.ServiceError.class));
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, statusToExceptionTypeMap, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithUnexpectedResponseAndFallthroughExceptionType(String uri, String putBody) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (putBody != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(putBody, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(putBody));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            Map<Integer, java.lang.reflect.ParameterizedType> statusToExceptionTypeMap = new HashMap<>();
            statusToExceptionTypeMap.put(400, CoreUtils.createParameterizedType(Object.class));
            statusToExceptionTypeMap.put(403, CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.ServiceError.class));
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, statusToExceptionTypeMap, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putWithUnexpectedResponseAndNoFallthroughExceptionType(String uri, String putBody) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (putBody != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(putBody, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(putBody));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            Map<Integer, java.lang.reflect.ParameterizedType> statusToExceptionTypeMap = new HashMap<>();
            statusToExceptionTypeMap.put(400, CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.ServiceError.class));
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, statusToExceptionTypeMap, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON unexpectedResponseWithStatusCodeAndExceptionType(String uri, String putBody) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (putBody != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(putBody, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(putBody));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 201;
        if (!expectedResponse) {
            Map<Integer, java.lang.reflect.ParameterizedType> statusToExceptionTypeMap = new HashMap<>();
            statusToExceptionTypeMap.put(400, CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.ServiceError.class));
            statusToExceptionTypeMap.put(403, CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.OperationError.class));
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, statusToExceptionTypeMap, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON post(String uri, String postBody) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri + "/" + "post");
        if (postBody != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(postBody, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(postBody));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON delete(String uri, boolean bodyBoolean) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.DELETE).setUri(uri + "/" + "delete");
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
        SerializationFormat requestSerializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (xmlSerializer.supportsFormat(requestSerializationFormat)) {
            httpRequest.setBody(BinaryData.fromObject(bodyBoolean, xmlSerializer));
        } else {
            httpRequest.setBody(BinaryData.fromObject(bodyBoolean, jsonSerializer));
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON patch(String uri, String bodyString) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PATCH).setUri(uri + "/" + "patch");
        if (bodyString != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(bodyString, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromString(bodyString));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON get(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "anything");
        httpRequest.getHeaders().add(new HttpHeader(MYHEADER, "MyHeaderValue"));
        httpRequest.getHeaders().add(new HttpHeader(MYOTHERHEADER, Arrays.asList("My", "Header", "Value")));
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public HttpBinJSON putByteArray(String uri, byte[] bytes) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "put");
        if (bytes != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            if (io.clientcore.core.utils.GeneratedCodeUtils.isJsonContentType(httpRequest.getHeaders())) {
                httpRequest.setBody(BinaryData.fromObject(bytes, jsonSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromBytes(bytes));
            }
        }
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        try (Response<BinaryData> networkResponseToClose = networkResponse) {
            HttpBinJSON deserializedResult;
            ParameterizedType returnType = CoreUtils.createParameterizedType(io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON.class);
            SerializationFormat responseSerializationFormat = CoreUtils.serializationFormatFromContentType(networkResponseToClose.getHeaders());
            if (jsonSerializer.supportsFormat(responseSerializationFormat) || responseSerializationFormat == SerializationFormat.TEXT) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), jsonSerializer, returnType);
            } else if (xmlSerializer.supportsFormat(responseSerializationFormat)) {
                deserializedResult = CoreUtils.decodeNetworkResponse(networkResponseToClose.getValue(), xmlSerializer, returnType);
            } else {
                throw LOGGER.throwableAtError().addKeyValue("serializationFormat", responseSerializationFormat.name()).log("None of the provided serializers support the format.", UnsupportedOperationException::new);
            }
            return deserializedResult;
        }
    }

    @Override
    public void getStatus200(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "status/200");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        networkResponse.close();
    }

    @Override
    public void getStatus200WithExpectedResponse200(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "status/200");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        networkResponse.close();
    }

    @Override
    public void getStatus300(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "status/300");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        networkResponse.close();
    }

    @Override
    public void getStatus300WithExpectedResponse300(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "status/300");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        networkResponse.close();
    }

    @Override
    public void getStatus400(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "status/400");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        networkResponse.close();
    }

    @Override
    public void getStatus400WithExpectedResponse400(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "status/400");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 400;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        networkResponse.close();
    }

    @Override
    public void getStatus500(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "status/500");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode >= 200 && responseCode < 300;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        networkResponse.close();
    }

    @Override
    public void getStatus500WithExpectedResponse500(String uri) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "status/500");
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 500;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        networkResponse.close();
    }

    @Override
    public Response<BinaryData> put(String uri, BinaryData putBody, ServerSentEventListener serverSentEventListener) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri + "/" + "serversentevent");
        if (putBody != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            BinaryData binaryData = putBody;
            if (binaryData.getLength() != null && httpRequest.getHeaders().get(HttpHeaderName.CONTENT_LENGTH) == null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
            }
            httpRequest.setBody(binaryData);
        }
        httpRequest.setServerSentEventListener(serverSentEventListener);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        return networkResponse;
    }

    @Override
    public BinaryData get(String uri, ServerSentEventListener serverSentEventListener) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri + "/" + "serversentevent");
        httpRequest.setServerSentEventListener(serverSentEventListener);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        return networkResponse.getValue();
    }

    @Override
    public Response<BinaryData> post(String uri, BinaryData postBody, ServerSentEventListener serverSentEventListener, RequestContext requestOptions) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri + "/" + "serversentevent");
        if (postBody != null) {
            httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
            BinaryData binaryData = postBody;
            if (binaryData.getLength() != null && httpRequest.getHeaders().get(HttpHeaderName.CONTENT_LENGTH) == null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
            }
            httpRequest.setBody(binaryData);
        }
        httpRequest.setContext(requestOptions);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        httpRequest.setServerSentEventListener(serverSentEventListener);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            // Handle unexpected response
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, null, null, LOGGER);
        }
        return networkResponse;
    }
}
