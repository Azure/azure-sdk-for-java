// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.storage.blob.implementation;

import com.azure.v2.storage.blob.models.*;
import com.azure.v2.storage.blob.implementation.ServicesImpl.ServicesService;
import com.azure.v2.storage.blob.models.*;
import io.clientcore.core.http.models.*;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.GeneratedCodeUtils;
import io.clientcore.core.utils.UriBuilder;

import java.io.InputStream;
import java.lang.reflect.ParameterizedType;

/**
 * Initializes a new instance of the ServicesServiceImpl type.
 */
public class ServicesServiceImpl implements ServicesService {

    private static final HttpHeaderName X_MS_CLIENT_REQUEST_ID = HttpHeaderName.fromString("x-ms-client-request-id");

    private static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");

    private static final ClientLogger LOGGER = new ClientLogger(ServicesService.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private ServicesServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of ServicesService that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `ServicesService`;
     */
    public static ServicesService getNewInstance(HttpPipeline httpPipeline) {
        return new ServicesServiceImpl(httpPipeline);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> setProperties(String url, String restype, String comp, Integer timeout, String version, String requestId, BlobServiceProperties blobServiceProperties, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/");
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
        if (blobServiceProperties != null) {
            SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
            if (xmlSerializer.supportsFormat(serializationFormat)) {
                httpRequest.setBody(BinaryData.fromObject(blobServiceProperties, xmlSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromObject(blobServiceProperties, jsonSerializer));
            }
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 202;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<BlobServiceProperties> getProperties(String url, String restype, String comp, Integer timeout, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/");
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            networkResponse.close();
        }
        BlobServiceProperties deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, BlobServiceProperties.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + ".");
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<BlobServiceStatistics> getStatistics(String url, String restype, String comp, Integer timeout, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/");
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            networkResponse.close();
        }
        BlobServiceStatistics deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, BlobServiceStatistics.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + ".");
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<BlobContainersSegment> listBlobContainersSegment(String url, String comp, String prefix, String marker, Integer maxresults, String listBlobContainersIncludeType, Integer timeout, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/");
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "prefix", true, prefix, true);
        GeneratedCodeUtils.addQueryParameter(uri, "marker", true, marker, true);
        GeneratedCodeUtils.addQueryParameter(uri, "maxresults", true, maxresults, true);
        GeneratedCodeUtils.addQueryParameter(uri, "include", true, listBlobContainersIncludeType, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            networkResponse.close();
        }
        BlobContainersSegment deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, BlobContainersSegment.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + ".");
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<UserDelegationKey> getUserDelegationKey(String url, String restype, String comp, Integer timeout, String version, String requestId, KeyInfo keyInfo, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/");
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
        if (keyInfo != null) {
            SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
            if (xmlSerializer.supportsFormat(serializationFormat)) {
                httpRequest.setBody(BinaryData.fromObject(keyInfo, xmlSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromObject(keyInfo, jsonSerializer));
            }
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            networkResponse.close();
        }
        UserDelegationKey deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, UserDelegationKey.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + ".");
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> getAccountInfo(String url, String restype, String comp, Integer timeout, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/");
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<InputStream> submitBatch(String url, String comp, long contentLength, String multipartContentType, Integer timeout, String version, String requestId, BinaryData body, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/");
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (multipartContentType != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_TYPE, multipartContentType));
        }
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
        if (body != null) {
            BinaryData binaryData = body;
            if (binaryData.getLength() != null) {
                httpRequest.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(binaryData.getLength()));
                httpRequest.setBody(binaryData);
            }
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 202;
        if (!expectedResponse) {
            ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            networkResponse.close();
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), networkResponse.getValue().toStream());
    }

    @SuppressWarnings("cast")
    @Override
    public Response<FilterBlobSegment> filterBlobs(String url, String comp, Integer timeout, String version, String requestId, String where, String marker, Integer maxresults, String include, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/");
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        GeneratedCodeUtils.addQueryParameter(uri, "where", true, where, true);
        GeneratedCodeUtils.addQueryParameter(uri, "marker", true, marker, true);
        GeneratedCodeUtils.addQueryParameter(uri, "maxresults", true, maxresults, true);
        GeneratedCodeUtils.addQueryParameter(uri, "include", true, include, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            networkResponse.close();
        }
        FilterBlobSegment deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, FilterBlobSegment.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + ".");
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<BlobContainersSegment> listBlobContainersSegmentNext(String nextLink, String url, String version, String requestId, String accept, RequestContext requestContext) {
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(nextLink);
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = responseCode == 200;
        if (!expectedResponse) {
            ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            networkResponse.close();
        }
        BlobContainersSegment deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, BlobContainersSegment.class);
        SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
        if (jsonSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), jsonSerializer, returnType);
        } else if (xmlSerializer.supportsFormat(serializationFormat)) {
            deserializedResult = CoreUtils.decodeNetworkResponse(networkResponse.getValue(), xmlSerializer, returnType);
        } else {
            throw new UnsupportedOperationException("None of the provided serializers support the format: " + serializationFormat + ".");
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), deserializedResult);
    }
}
