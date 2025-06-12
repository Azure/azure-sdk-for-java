// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.storage.blob.implementation;

import com.azure.v2.storage.blob.implementation.models.BlobSignedIdentifierWrapper;
import com.azure.v2.storage.blob.models.*;
import com.azure.v2.storage.blob.implementation.ContainersImpl.ContainersService;
import com.azure.v2.storage.blob.models.*;
import io.clientcore.core.http.models.*;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.implementation.utils.UriEscapers;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.serialization.json.JsonSerializer;
import io.clientcore.core.serialization.xml.XmlSerializer;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.DateTimeRfc1123;
import io.clientcore.core.utils.GeneratedCodeUtils;
import io.clientcore.core.utils.UriBuilder;

import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

/**
 * Initializes a new instance of the ContainersServiceImpl type.
 */
public class ContainersServiceImpl implements ContainersService {

    private static final HttpHeaderName X_MS_BLOB_PUBLIC_ACCESS = HttpHeaderName.fromString("x-ms-blob-public-access");

    private static final HttpHeaderName X_MS_CLIENT_REQUEST_ID = HttpHeaderName.fromString("x-ms-client-request-id");

    private static final HttpHeaderName X_MS_DEFAULT_ENCRYPTION_SCOPE = HttpHeaderName.fromString("x-ms-default-encryption-scope");

    private static final HttpHeaderName X_MS_DELETED_CONTAINER_NAME = HttpHeaderName.fromString("x-ms-deleted-container-name");

    private static final HttpHeaderName X_MS_DELETED_CONTAINER_VERSION = HttpHeaderName.fromString("x-ms-deleted-container-version");

    private static final HttpHeaderName X_MS_DENY_ENCRYPTION_SCOPE_OVERRIDE = HttpHeaderName.fromString("x-ms-deny-encryption-scope-override");

    private static final HttpHeaderName X_MS_LEASE_ACTION = HttpHeaderName.fromString("x-ms-lease-action");

    private static final HttpHeaderName X_MS_LEASE_BREAK_PERIOD = HttpHeaderName.fromString("x-ms-lease-break-period");

    private static final HttpHeaderName X_MS_LEASE_DURATION = HttpHeaderName.fromString("x-ms-lease-duration");

    private static final HttpHeaderName X_MS_LEASE_ID = HttpHeaderName.fromString("x-ms-lease-id");

    private static final HttpHeaderName X_MS_META_ = HttpHeaderName.fromString("x-ms-meta-");

    private static final HttpHeaderName X_MS_PROPOSED_LEASE_ID = HttpHeaderName.fromString("x-ms-proposed-lease-id");

    private static final HttpHeaderName X_MS_SOURCE_CONTAINER_NAME = HttpHeaderName.fromString("x-ms-source-container-name");

    private static final HttpHeaderName X_MS_SOURCE_LEASE_ID = HttpHeaderName.fromString("x-ms-source-lease-id");

    private static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");

    private static final ClientLogger LOGGER = new ClientLogger(ContainersService.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private ContainersServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of ContainersService that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `ContainersService`;
     */
    public static ContainersService getNewInstance(HttpPipeline httpPipeline) {
        return new ContainersServiceImpl(httpPipeline);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> create(String url, String containerName, String restype, Integer timeout, Map<String, String> metadata, PublicAccessType access, String version, String requestId, String defaultEncryptionScope, Boolean encryptionScopeOverridePrevented, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (defaultEncryptionScope != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_DEFAULT_ENCRYPTION_SCOPE, defaultEncryptionScope));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (encryptionScopeOverridePrevented != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_DENY_ENCRYPTION_SCOPE_OVERRIDE, String.valueOf(encryptionScopeOverridePrevented)));
        }
        if (access != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_PUBLIC_ACCESS, String.valueOf(access)));
        }
        if (metadata != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_META_, String.valueOf(metadata)));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 201;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> getProperties(String url, String containerName, String restype, Integer timeout, String leaseId, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> delete(String url, String containerName, String restype, Integer timeout, String leaseId, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.DELETE).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (ifUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_UNMODIFIED_SINCE, String.valueOf(ifUnmodifiedSince)));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 202;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> setMetadata(String url, String containerName, String restype, String comp, Integer timeout, String leaseId, Map<String, String> metadata, DateTimeRfc1123 ifModifiedSince, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
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
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (metadata != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_META_, String.valueOf(metadata)));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<BlobSignedIdentifierWrapper> getAccessPolicy(String url, String containerName, String restype, String comp, Integer timeout, String leaseId, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
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
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
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
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            networkResponse.close();
        }
        BlobSignedIdentifierWrapper deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, BlobSignedIdentifierWrapper.class);
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
    public Response<Void> setAccessPolicy(String url, String containerName, String restype, String comp, Integer timeout, String leaseId, PublicAccessType access, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String version, String requestId, BlobSignedIdentifierWrapper containerAcl, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
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
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (access != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_PUBLIC_ACCESS, String.valueOf(access)));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (ifUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_UNMODIFIED_SINCE, String.valueOf(ifUnmodifiedSince)));
        }
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
        if (containerAcl != null) {
            SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
            if (xmlSerializer.supportsFormat(serializationFormat)) {
                httpRequest.setBody(BinaryData.fromObject(containerAcl, xmlSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromObject(containerAcl, jsonSerializer));
            }
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> restore(String url, String containerName, String restype, String comp, Integer timeout, String version, String requestId, String deletedContainerName, String deletedContainerVersion, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (deletedContainerName != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_DELETED_CONTAINER_NAME, deletedContainerName));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (deletedContainerVersion != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_DELETED_CONTAINER_VERSION, deletedContainerVersion));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 201;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> rename(String url, String containerName, String restype, String comp, Integer timeout, String version, String requestId, String sourceContainerName, String sourceLeaseId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
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
        if (sourceContainerName != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_CONTAINER_NAME, sourceContainerName));
        }
        if (sourceLeaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_LEASE_ID, sourceLeaseId));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<InputStream> submitBatch(String url, String containerName, String restype, String comp, long contentLength, String multipartContentType, Integer timeout, String version, String requestId, BinaryData body, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
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
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            networkResponse.close();
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), networkResponse.getValue().toStream());
    }

    @SuppressWarnings("cast")
    @Override
    public Response<FilterBlobSegment> filterBlobs(String url, String containerName, String restype, String comp, Integer timeout, String version, String requestId, String where, String marker, Integer maxresults, String include, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
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
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
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
    public Response<Void> acquireLease(String url, String containerName, String comp, String restype, String action, Integer timeout, Integer duration, String proposedLeaseId, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (proposedLeaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_PROPOSED_LEASE_ID, proposedLeaseId));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (duration != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_DURATION, String.valueOf(duration)));
        }
        if (action != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ACTION, action));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (ifUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_UNMODIFIED_SINCE, String.valueOf(ifUnmodifiedSince)));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 201;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> releaseLease(String url, String containerName, String comp, String restype, String action, Integer timeout, String leaseId, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (action != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ACTION, action));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (ifUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_UNMODIFIED_SINCE, String.valueOf(ifUnmodifiedSince)));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> renewLease(String url, String containerName, String comp, String restype, String action, Integer timeout, String leaseId, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (action != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ACTION, action));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (ifUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_UNMODIFIED_SINCE, String.valueOf(ifUnmodifiedSince)));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> breakLease(String url, String containerName, String comp, String restype, String action, Integer timeout, Integer breakPeriod, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (breakPeriod != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_BREAK_PERIOD, String.valueOf(breakPeriod)));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (action != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ACTION, action));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (ifUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_UNMODIFIED_SINCE, String.valueOf(ifUnmodifiedSince)));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 202;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> changeLease(String url, String containerName, String comp, String restype, String action, Integer timeout, String leaseId, String proposedLeaseId, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (proposedLeaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_PROPOSED_LEASE_ID, proposedLeaseId));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (action != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ACTION, action));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (ifUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_UNMODIFIED_SINCE, String.valueOf(ifUnmodifiedSince)));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 200;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<ListBlobsFlatSegmentResponse> listBlobFlatSegment(String url, String containerName, String restype, String comp, String prefix, String marker, Integer maxresults, String include, Integer timeout, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "prefix", true, prefix, true);
        GeneratedCodeUtils.addQueryParameter(uri, "marker", true, marker, true);
        GeneratedCodeUtils.addQueryParameter(uri, "maxresults", true, maxresults, true);
        GeneratedCodeUtils.addQueryParameter(uri, "include", true, include, true);
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
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            networkResponse.close();
        }
        ListBlobsFlatSegmentResponse deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, ListBlobsFlatSegmentResponse.class);
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
    public Response<ListBlobsHierarchySegmentResponse> listBlobHierarchySegment(String url, String containerName, String restype, String comp, String prefix, String delimiter, String marker, Integer maxresults, String include, Integer timeout, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
        GeneratedCodeUtils.addQueryParameter(uri, "restype", true, restype, true);
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "prefix", true, prefix, true);
        GeneratedCodeUtils.addQueryParameter(uri, "delimiter", true, delimiter, true);
        GeneratedCodeUtils.addQueryParameter(uri, "marker", true, marker, true);
        GeneratedCodeUtils.addQueryParameter(uri, "maxresults", true, maxresults, true);
        GeneratedCodeUtils.addQueryParameter(uri, "include", true, include, true);
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
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            networkResponse.close();
        }
        ListBlobsHierarchySegmentResponse deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, ListBlobsHierarchySegmentResponse.class);
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
    public Response<Void> getAccountInfo(String url, String containerName, String restype, String comp, Integer timeout, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }
}
