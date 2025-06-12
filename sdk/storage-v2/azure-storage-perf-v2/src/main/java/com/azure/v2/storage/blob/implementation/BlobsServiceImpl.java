// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.storage.blob.implementation;

import com.azure.v2.storage.blob.models.*;
import com.azure.v2.storage.blob.implementation.BlobsImpl.BlobsService;
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
 * Initializes a new instance of the BlobsServiceImpl type.
 */
public class BlobsServiceImpl implements BlobsService {

    private static final HttpHeaderName X_MS_ACCESS_TIER = HttpHeaderName.fromString("x-ms-access-tier");

    private static final HttpHeaderName X_MS_BLOB_CACHE_CONTROL = HttpHeaderName.fromString("x-ms-blob-cache-control");

    private static final HttpHeaderName X_MS_BLOB_CONTENT_DISPOSITION = HttpHeaderName.fromString("x-ms-blob-content-disposition");

    private static final HttpHeaderName X_MS_BLOB_CONTENT_ENCODING = HttpHeaderName.fromString("x-ms-blob-content-encoding");

    private static final HttpHeaderName X_MS_BLOB_CONTENT_LANGUAGE = HttpHeaderName.fromString("x-ms-blob-content-language");

    private static final HttpHeaderName X_MS_BLOB_CONTENT_MD5 = HttpHeaderName.fromString("x-ms-blob-content-md5");

    private static final HttpHeaderName X_MS_BLOB_CONTENT_TYPE = HttpHeaderName.fromString("x-ms-blob-content-type");

    private static final HttpHeaderName X_MS_CLIENT_REQUEST_ID = HttpHeaderName.fromString("x-ms-client-request-id");

    private static final HttpHeaderName X_MS_CONTENT_CRC64 = HttpHeaderName.fromString("x-ms-content-crc64");

    private static final HttpHeaderName X_MS_COPY_ACTION = HttpHeaderName.fromString("x-ms-copy-action");

    private static final HttpHeaderName X_MS_COPY_SOURCE = HttpHeaderName.fromString("x-ms-copy-source");

    private static final HttpHeaderName X_MS_COPY_SOURCE_AUTHORIZATION = HttpHeaderName.fromString("x-ms-copy-source-authorization");

    private static final HttpHeaderName X_MS_COPY_SOURCE_TAG_OPTION = HttpHeaderName.fromString("x-ms-copy-source-tag-option");

    private static final HttpHeaderName X_MS_DELETE_SNAPSHOTS = HttpHeaderName.fromString("x-ms-delete-snapshots");

    private static final HttpHeaderName X_MS_ENCRYPTION_ALGORITHM = HttpHeaderName.fromString("x-ms-encryption-algorithm");

    private static final HttpHeaderName X_MS_ENCRYPTION_KEY = HttpHeaderName.fromString("x-ms-encryption-key");

    private static final HttpHeaderName X_MS_ENCRYPTION_KEY_SHA256 = HttpHeaderName.fromString("x-ms-encryption-key-sha256");

    private static final HttpHeaderName X_MS_ENCRYPTION_SCOPE = HttpHeaderName.fromString("x-ms-encryption-scope");

    private static final HttpHeaderName X_MS_EXPIRY_OPTION = HttpHeaderName.fromString("x-ms-expiry-option");

    private static final HttpHeaderName X_MS_EXPIRY_TIME = HttpHeaderName.fromString("x-ms-expiry-time");

    private static final HttpHeaderName X_MS_IF_TAGS = HttpHeaderName.fromString("x-ms-if-tags");

    private static final HttpHeaderName X_MS_IMMUTABILITY_POLICY_MODE = HttpHeaderName.fromString("x-ms-immutability-policy-mode");

    private static final HttpHeaderName X_MS_IMMUTABILITY_POLICY_UNTIL_DATE = HttpHeaderName.fromString("x-ms-immutability-policy-until-date");

    private static final HttpHeaderName X_MS_LEASE_ACTION = HttpHeaderName.fromString("x-ms-lease-action");

    private static final HttpHeaderName X_MS_LEASE_BREAK_PERIOD = HttpHeaderName.fromString("x-ms-lease-break-period");

    private static final HttpHeaderName X_MS_LEASE_DURATION = HttpHeaderName.fromString("x-ms-lease-duration");

    private static final HttpHeaderName X_MS_LEASE_ID = HttpHeaderName.fromString("x-ms-lease-id");

    private static final HttpHeaderName X_MS_LEGAL_HOLD = HttpHeaderName.fromString("x-ms-legal-hold");

    private static final HttpHeaderName X_MS_META_ = HttpHeaderName.fromString("x-ms-meta-");

    private static final HttpHeaderName X_MS_PROPOSED_LEASE_ID = HttpHeaderName.fromString("x-ms-proposed-lease-id");

    private static final HttpHeaderName X_MS_RANGE = HttpHeaderName.fromString("x-ms-range");

    private static final HttpHeaderName X_MS_RANGE_GET_CONTENT_CRC64 = HttpHeaderName.fromString("x-ms-range-get-content-crc64");

    private static final HttpHeaderName X_MS_RANGE_GET_CONTENT_MD5 = HttpHeaderName.fromString("x-ms-range-get-content-md5");

    private static final HttpHeaderName X_MS_REHYDRATE_PRIORITY = HttpHeaderName.fromString("x-ms-rehydrate-priority");

    private static final HttpHeaderName X_MS_REQUIRES_SYNC = HttpHeaderName.fromString("x-ms-requires-sync");

    private static final HttpHeaderName X_MS_SEAL_BLOB = HttpHeaderName.fromString("x-ms-seal-blob");

    private static final HttpHeaderName X_MS_SOURCE_CONTENT_MD5 = HttpHeaderName.fromString("x-ms-source-content-md5");

    private static final HttpHeaderName X_MS_SOURCE_IF_MATCH = HttpHeaderName.fromString("x-ms-source-if-match");

    private static final HttpHeaderName X_MS_SOURCE_IF_MODIFIED_SINCE = HttpHeaderName.fromString("x-ms-source-if-modified-since");

    private static final HttpHeaderName X_MS_SOURCE_IF_NONE_MATCH = HttpHeaderName.fromString("x-ms-source-if-none-match");

    private static final HttpHeaderName X_MS_SOURCE_IF_TAGS = HttpHeaderName.fromString("x-ms-source-if-tags");

    private static final HttpHeaderName X_MS_SOURCE_IF_UNMODIFIED_SINCE = HttpHeaderName.fromString("x-ms-source-if-unmodified-since");

    private static final HttpHeaderName X_MS_STRUCTURED_BODY = HttpHeaderName.fromString("x-ms-structured-body");

    private static final HttpHeaderName X_MS_TAGS = HttpHeaderName.fromString("x-ms-tags");

    private static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");

    private static final ClientLogger LOGGER = new ClientLogger(BlobsService.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private BlobsServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of BlobsService that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `BlobsService`;
     */
    public static BlobsService getNewInstance(HttpPipeline httpPipeline) {
        return new BlobsServiceImpl(httpPipeline);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<InputStream> download(String url, String containerName, String blob, String snapshot, String versionId, Integer timeout, String range, String leaseId, Boolean rangeGetContentMD5, Boolean rangeGetContentCRC64, String structuredBodyType, String encryptionKey, String encryptionKeySha256, EncryptionAlgorithmType encryptionAlgorithm, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        String localURL = url;
        if (localURL.contains("?")) {
            localURL = localURL.substring(0, localURL.indexOf("?"));
        }
        UriBuilder uri = UriBuilder.parse(localURL + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" +
                UriEscapers.PATH_ESCAPER.escape(blob) +
                (url.contains("?") ? url.substring(url.indexOf("?")) : ""));
        GeneratedCodeUtils.addQueryParameter(uri, "snapshot", true, snapshot, true);
        GeneratedCodeUtils.addQueryParameter(uri, "versionid", true, versionId, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri(uri.toString());
        if (rangeGetContentCRC64 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_RANGE_GET_CONTENT_CRC64, String.valueOf(rangeGetContentCRC64)));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
        }
        if (encryptionKeySha256 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY_SHA256, encryptionKeySha256));
        }
        if (structuredBodyType != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_STRUCTURED_BODY, structuredBodyType));
        }
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
        }
        if (range != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_RANGE, range));
        }
        if (encryptionKey != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY, encryptionKey));
        }
        if (encryptionAlgorithm != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_ALGORITHM, String.valueOf(encryptionAlgorithm)));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (rangeGetContentMD5 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_RANGE_GET_CONTENT_MD5, String.valueOf(rangeGetContentMD5)));
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
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = (responseCode == 200 || responseCode == 206);
        if (!expectedResponse) {
            ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            networkResponse.close();
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), networkResponse.getValue().toStream());
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> getProperties(String url, String containerName, String blob, String snapshot, String versionId, Integer timeout, String leaseId, String encryptionKey, String encryptionKeySha256, EncryptionAlgorithmType encryptionAlgorithm, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "snapshot", true, snapshot, true);
        GeneratedCodeUtils.addQueryParameter(uri, "versionid", true, versionId, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.HEAD).setUri(uri.toString());
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (encryptionKey != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY, encryptionKey));
        }
        if (encryptionAlgorithm != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_ALGORITHM, String.valueOf(encryptionAlgorithm)));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
        }
        if (encryptionKeySha256 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY_SHA256, encryptionKeySha256));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> delete(String url, String containerName, String blob, String snapshot, String versionId, Integer timeout, String leaseId, DeleteSnapshotsOptionType deleteSnapshots, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, BlobDeleteType blobDeleteType, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "snapshot", true, snapshot, true);
        GeneratedCodeUtils.addQueryParameter(uri, "versionid", true, versionId, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        GeneratedCodeUtils.addQueryParameter(uri, "deletetype", true, blobDeleteType, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.DELETE).setUri(uri.toString());
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
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
        if (deleteSnapshots != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_DELETE_SNAPSHOTS, String.valueOf(deleteSnapshots)));
        }
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> undelete(String url, String containerName, String blob, String comp, Integer timeout, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
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
    public Response<Void> setExpiry(String url, String containerName, String blob, String comp, Integer timeout, String version, String requestId, BlobExpiryOptions expiryOptions, String expiresOn, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
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
        if (expiryOptions != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_EXPIRY_OPTION, String.valueOf(expiryOptions)));
        }
        if (expiresOn != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_EXPIRY_TIME, expiresOn));
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
    public Response<Void> setHttpHeaders(String url, String containerName, String blob, String comp, Integer timeout, String cacheControl, String contentType, String contentMd5, String contentEncoding, String contentLanguage, String leaseId, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String contentDisposition, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
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
        if (contentLanguage != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CONTENT_LANGUAGE, contentLanguage));
        }
        if (cacheControl != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CACHE_CONTROL, cacheControl));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (contentDisposition != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CONTENT_DISPOSITION, contentDisposition));
        }
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
        }
        if (contentMd5 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CONTENT_MD5, contentMd5));
        }
        if (contentType != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CONTENT_TYPE, contentType));
        }
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (contentEncoding != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CONTENT_ENCODING, contentEncoding));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> setImmutabilityPolicy(String url, String containerName, String blob, String comp, Integer timeout, String version, String requestId, DateTimeRfc1123 ifUnmodifiedSince, DateTimeRfc1123 immutabilityPolicyExpiry, BlobImmutabilityPolicyMode immutabilityPolicyMode, String snapshot, String versionId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        GeneratedCodeUtils.addQueryParameter(uri, "snapshot", true, snapshot, true);
        GeneratedCodeUtils.addQueryParameter(uri, "versionid", true, versionId, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (immutabilityPolicyMode != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IMMUTABILITY_POLICY_MODE, String.valueOf(immutabilityPolicyMode)));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (immutabilityPolicyExpiry != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IMMUTABILITY_POLICY_UNTIL_DATE, String.valueOf(immutabilityPolicyExpiry)));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> deleteImmutabilityPolicy(String url, String containerName, String blob, String comp, Integer timeout, String version, String requestId, String snapshot, String versionId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        GeneratedCodeUtils.addQueryParameter(uri, "snapshot", true, snapshot, true);
        GeneratedCodeUtils.addQueryParameter(uri, "versionid", true, versionId, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.DELETE).setUri(uri.toString());
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
    public Response<Void> setLegalHold(String url, String containerName, String blob, String comp, Integer timeout, String version, String requestId, boolean legalHold, String snapshot, String versionId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        GeneratedCodeUtils.addQueryParameter(uri, "snapshot", true, snapshot, true);
        GeneratedCodeUtils.addQueryParameter(uri, "versionid", true, versionId, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        httpRequest.getHeaders().add(new HttpHeader(X_MS_LEGAL_HOLD, String.valueOf(legalHold)));
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
    public Response<Void> setMetadata(String url, String containerName, String blob, String comp, Integer timeout, Map<String, String> metadata, String leaseId, String encryptionKey, String encryptionKeySha256, EncryptionAlgorithmType encryptionAlgorithm, String encryptionScope, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
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
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
        }
        if (encryptionKeySha256 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY_SHA256, encryptionKeySha256));
        }
        if (metadata != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_META_, String.valueOf(metadata)));
        }
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
        }
        if (encryptionKey != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY, encryptionKey));
        }
        if (encryptionAlgorithm != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_ALGORITHM, String.valueOf(encryptionAlgorithm)));
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
        if (encryptionScope != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_SCOPE, encryptionScope));
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
    public Response<Void> acquireLease(String url, String containerName, String blob, String comp, String action, Integer timeout, Integer duration, String proposedLeaseId, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
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
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> releaseLease(String url, String containerName, String blob, String comp, String action, Integer timeout, String leaseId, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
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
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> renewLease(String url, String containerName, String blob, String comp, String action, Integer timeout, String leaseId, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
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
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> changeLease(String url, String containerName, String blob, String comp, String action, Integer timeout, String leaseId, String proposedLeaseId, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
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
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> breakLease(String url, String containerName, String blob, String comp, String action, Integer timeout, Integer breakPeriod, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
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
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> createSnapshot(String url, String containerName, String blob, String comp, Integer timeout, Map<String, String> metadata, String encryptionKey, String encryptionKeySha256, EncryptionAlgorithmType encryptionAlgorithm, String encryptionScope, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String leaseId, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
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
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
        }
        if (encryptionKeySha256 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY_SHA256, encryptionKeySha256));
        }
        if (metadata != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_META_, String.valueOf(metadata)));
        }
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
        }
        if (encryptionKey != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY, encryptionKey));
        }
        if (encryptionAlgorithm != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_ALGORITHM, String.valueOf(encryptionAlgorithm)));
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
        if (encryptionScope != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_SCOPE, encryptionScope));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 201;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> startCopyFromURL(String url, String containerName, String blob, Integer timeout, Map<String, String> metadata, AccessTier tier, RehydratePriority rehydratePriority, DateTimeRfc1123 sourceIfModifiedSince, DateTimeRfc1123 sourceIfUnmodifiedSince, String sourceIfMatch, String sourceIfNoneMatch, String sourceIfTags, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String copySource, String leaseId, String version, String requestId, String blobTagsString, Boolean sealBlob, DateTimeRfc1123 immutabilityPolicyExpiry, BlobImmutabilityPolicyMode immutabilityPolicyMode, Boolean legalHold, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (immutabilityPolicyExpiry != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IMMUTABILITY_POLICY_UNTIL_DATE, String.valueOf(immutabilityPolicyExpiry)));
        }
        if (copySource != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_COPY_SOURCE, copySource));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (sourceIfModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_MODIFIED_SINCE, String.valueOf(sourceIfModifiedSince)));
        }
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
        }
        if (sourceIfMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_MATCH, sourceIfMatch));
        }
        if (legalHold != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEGAL_HOLD, String.valueOf(legalHold)));
        }
        if (metadata != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_META_, String.valueOf(metadata)));
        }
        if (sealBlob != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SEAL_BLOB, String.valueOf(sealBlob)));
        }
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (sourceIfUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_UNMODIFIED_SINCE, String.valueOf(sourceIfUnmodifiedSince)));
        }
        if (immutabilityPolicyMode != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IMMUTABILITY_POLICY_MODE, String.valueOf(immutabilityPolicyMode)));
        }
        if (rehydratePriority != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_REHYDRATE_PRIORITY, String.valueOf(rehydratePriority)));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
        }
        if (tier != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ACCESS_TIER, String.valueOf(tier)));
        }
        if (blobTagsString != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_TAGS, blobTagsString));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (sourceIfTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_TAGS, sourceIfTags));
        }
        if (sourceIfNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_NONE_MATCH, sourceIfNoneMatch));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> copyFromURL(String url, String containerName, String blob, String xMsRequiresSync, Integer timeout, Map<String, String> metadata, AccessTier tier, DateTimeRfc1123 sourceIfModifiedSince, DateTimeRfc1123 sourceIfUnmodifiedSince, String sourceIfMatch, String sourceIfNoneMatch, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String copySource, String leaseId, String version, String requestId, String sourceContentMD5, String blobTagsString, DateTimeRfc1123 immutabilityPolicyExpiry, BlobImmutabilityPolicyMode immutabilityPolicyMode, Boolean legalHold, String copySourceAuthorization, String encryptionScope, BlobCopySourceTagsMode copySourceTags, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (immutabilityPolicyExpiry != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IMMUTABILITY_POLICY_UNTIL_DATE, String.valueOf(immutabilityPolicyExpiry)));
        }
        if (copySource != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_COPY_SOURCE, copySource));
        }
        if (copySourceTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_COPY_SOURCE_TAG_OPTION, String.valueOf(copySourceTags)));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (sourceIfModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_MODIFIED_SINCE, String.valueOf(sourceIfModifiedSince)));
        }
        if (copySourceAuthorization != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_COPY_SOURCE_AUTHORIZATION, copySourceAuthorization));
        }
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
        }
        if (sourceIfMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_MATCH, sourceIfMatch));
        }
        if (legalHold != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEGAL_HOLD, String.valueOf(legalHold)));
        }
        if (metadata != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_META_, String.valueOf(metadata)));
        }
        if (sourceContentMD5 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_CONTENT_MD5, sourceContentMD5));
        }
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (sourceIfUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_UNMODIFIED_SINCE, String.valueOf(sourceIfUnmodifiedSince)));
        }
        if (immutabilityPolicyMode != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IMMUTABILITY_POLICY_MODE, String.valueOf(immutabilityPolicyMode)));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
        }
        if (tier != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ACCESS_TIER, String.valueOf(tier)));
        }
        if (blobTagsString != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_TAGS, blobTagsString));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (sourceIfNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_NONE_MATCH, sourceIfNoneMatch));
        }
        if (xMsRequiresSync != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_REQUIRES_SYNC, xMsRequiresSync));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (encryptionScope != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_SCOPE, encryptionScope));
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
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> abortCopyFromURL(String url, String containerName, String blob, String comp, String copyActionAbortConstant, String copyId, Integer timeout, String leaseId, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "copyid", true, copyId, true);
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
        if (copyActionAbortConstant != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_COPY_ACTION, copyActionAbortConstant));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> setTier(String url, String containerName, String blob, String comp, String snapshot, String versionId, Integer timeout, AccessTier tier, RehydratePriority rehydratePriority, String version, String requestId, String leaseId, String ifTags, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "snapshot", true, snapshot, true);
        GeneratedCodeUtils.addQueryParameter(uri, "versionid", true, versionId, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (rehydratePriority != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_REHYDRATE_PRIORITY, String.valueOf(rehydratePriority)));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (tier != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ACCESS_TIER, String.valueOf(tier)));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = (responseCode == 200 || responseCode == 202);
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> getAccountInfo(String url, String containerName, String blob, String restype, String comp, Integer timeout, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
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
    public Response<InputStream> query(String url, String containerName, String blob, String comp, String snapshot, Integer timeout, String leaseId, String encryptionKey, String encryptionKeySha256, EncryptionAlgorithmType encryptionAlgorithm, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, QueryRequest queryRequest, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "snapshot", true, snapshot, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.POST).setUri(uri.toString());
        if (ifNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_NONE_MATCH, ifNoneMatch));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (ifMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MATCH, ifMatch));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (encryptionKey != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY, encryptionKey));
        }
        if (encryptionAlgorithm != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_ALGORITHM, String.valueOf(encryptionAlgorithm)));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (ifModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_MODIFIED_SINCE, String.valueOf(ifModifiedSince)));
        }
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
        }
        if (encryptionKeySha256 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY_SHA256, encryptionKeySha256));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (ifUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.IF_UNMODIFIED_SINCE, String.valueOf(ifUnmodifiedSince)));
        }
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
        if (queryRequest != null) {
            SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
            if (xmlSerializer.supportsFormat(serializationFormat)) {
                httpRequest.setBody(BinaryData.fromObject(queryRequest, xmlSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromObject(queryRequest, jsonSerializer));
            }
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest);
        int responseCode = networkResponse.getStatusCode();
        boolean expectedResponse = (responseCode == 200 || responseCode == 206);
        if (!expectedResponse) {
            ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
            GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            networkResponse.close();
        }
        return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), networkResponse.getValue().toStream());
    }

    @SuppressWarnings("cast")
    @Override
    public Response<BlobTags> getTags(String url, String containerName, String blob, String comp, Integer timeout, String version, String requestId, String snapshot, String versionId, String ifTags, String leaseId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        GeneratedCodeUtils.addQueryParameter(uri, "snapshot", true, snapshot, true);
        GeneratedCodeUtils.addQueryParameter(uri, "versionid", true, versionId, true);
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
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
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
        BlobTags deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, BlobTags.class);
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
    public Response<Void> setTags(String url, String containerName, String blob, String comp, String version, Integer timeout, String versionId, String transactionalContentMD5, String transactionalContentCrc64, String requestId, String ifTags, String leaseId, BlobTags tags, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        GeneratedCodeUtils.addQueryParameter(uri, "versionid", true, versionId, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (transactionalContentCrc64 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CONTENT_CRC64, transactionalContentCrc64));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (ifTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IF_TAGS, ifTags));
        }
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (transactionalContentMD5 != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_MD5, transactionalContentMD5));
        }
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/xml");
        if (tags != null) {
            SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
            if (xmlSerializer.supportsFormat(serializationFormat)) {
                httpRequest.setBody(BinaryData.fromObject(tags, xmlSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromObject(tags, jsonSerializer));
            }
        }
        httpRequest.setContext(requestContext);
        httpRequest.getContext().getRequestCallback().accept(httpRequest);
        // Send the request through the httpPipeline
        try (Response<BinaryData> networkResponse = this.httpPipeline.send(httpRequest)) {
            int responseCode = networkResponse.getStatusCode();
            boolean expectedResponse = responseCode == 204;
            if (!expectedResponse) {
                ParameterizedType defaultErrorBodyType = CoreUtils.createParameterizedType(StorageError.class);
                GeneratedCodeUtils.handleUnexpectedResponse(responseCode, networkResponse, jsonSerializer, xmlSerializer, defaultErrorBodyType, null, LOGGER);
            }
            return new Response<>(networkResponse.getRequest(), responseCode, networkResponse.getHeaders(), null);
        }
    }
}
