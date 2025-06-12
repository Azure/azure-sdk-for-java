// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.storage.blob.implementation;

import com.azure.v2.storage.blob.models.*;
import com.azure.v2.storage.blob.implementation.BlockBlobsImpl.BlockBlobsService;
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

import java.lang.reflect.ParameterizedType;
import java.util.Map;

/**
 * Initializes a new instance of the BlockBlobsServiceImpl type.
 */
public class BlockBlobsServiceImpl implements BlockBlobsService {

    private static final HttpHeaderName X_MS_ACCESS_TIER = HttpHeaderName.fromString("x-ms-access-tier");

    private static final HttpHeaderName X_MS_BLOB_CACHE_CONTROL = HttpHeaderName.fromString("x-ms-blob-cache-control");

    private static final HttpHeaderName X_MS_BLOB_CONTENT_DISPOSITION = HttpHeaderName.fromString("x-ms-blob-content-disposition");

    private static final HttpHeaderName X_MS_BLOB_CONTENT_ENCODING = HttpHeaderName.fromString("x-ms-blob-content-encoding");

    private static final HttpHeaderName X_MS_BLOB_CONTENT_LANGUAGE = HttpHeaderName.fromString("x-ms-blob-content-language");

    private static final HttpHeaderName X_MS_BLOB_CONTENT_MD5 = HttpHeaderName.fromString("x-ms-blob-content-md5");

    private static final HttpHeaderName X_MS_BLOB_CONTENT_TYPE = HttpHeaderName.fromString("x-ms-blob-content-type");

    private static final HttpHeaderName X_MS_BLOB_TYPE = HttpHeaderName.fromString("x-ms-blob-type");

    private static final HttpHeaderName X_MS_CLIENT_REQUEST_ID = HttpHeaderName.fromString("x-ms-client-request-id");

    private static final HttpHeaderName X_MS_CONTENT_CRC64 = HttpHeaderName.fromString("x-ms-content-crc64");

    private static final HttpHeaderName X_MS_COPY_SOURCE = HttpHeaderName.fromString("x-ms-copy-source");

    private static final HttpHeaderName X_MS_COPY_SOURCE_AUTHORIZATION = HttpHeaderName.fromString("x-ms-copy-source-authorization");

    private static final HttpHeaderName X_MS_COPY_SOURCE_BLOB_PROPERTIES = HttpHeaderName.fromString("x-ms-copy-source-blob-properties");

    private static final HttpHeaderName X_MS_COPY_SOURCE_TAG_OPTION = HttpHeaderName.fromString("x-ms-copy-source-tag-option");

    private static final HttpHeaderName X_MS_ENCRYPTION_ALGORITHM = HttpHeaderName.fromString("x-ms-encryption-algorithm");

    private static final HttpHeaderName X_MS_ENCRYPTION_KEY = HttpHeaderName.fromString("x-ms-encryption-key");

    private static final HttpHeaderName X_MS_ENCRYPTION_KEY_SHA256 = HttpHeaderName.fromString("x-ms-encryption-key-sha256");

    private static final HttpHeaderName X_MS_ENCRYPTION_SCOPE = HttpHeaderName.fromString("x-ms-encryption-scope");

    private static final HttpHeaderName X_MS_IF_TAGS = HttpHeaderName.fromString("x-ms-if-tags");

    private static final HttpHeaderName X_MS_IMMUTABILITY_POLICY_MODE = HttpHeaderName.fromString("x-ms-immutability-policy-mode");

    private static final HttpHeaderName X_MS_IMMUTABILITY_POLICY_UNTIL_DATE = HttpHeaderName.fromString("x-ms-immutability-policy-until-date");

    private static final HttpHeaderName X_MS_LEASE_ID = HttpHeaderName.fromString("x-ms-lease-id");

    private static final HttpHeaderName X_MS_LEGAL_HOLD = HttpHeaderName.fromString("x-ms-legal-hold");

    private static final HttpHeaderName X_MS_META_ = HttpHeaderName.fromString("x-ms-meta-");

    private static final HttpHeaderName X_MS_SOURCE_CONTENT_CRC64 = HttpHeaderName.fromString("x-ms-source-content-crc64");

    private static final HttpHeaderName X_MS_SOURCE_CONTENT_MD5 = HttpHeaderName.fromString("x-ms-source-content-md5");

    private static final HttpHeaderName X_MS_SOURCE_IF_MATCH = HttpHeaderName.fromString("x-ms-source-if-match");

    private static final HttpHeaderName X_MS_SOURCE_IF_MODIFIED_SINCE = HttpHeaderName.fromString("x-ms-source-if-modified-since");

    private static final HttpHeaderName X_MS_SOURCE_IF_NONE_MATCH = HttpHeaderName.fromString("x-ms-source-if-none-match");

    private static final HttpHeaderName X_MS_SOURCE_IF_TAGS = HttpHeaderName.fromString("x-ms-source-if-tags");

    private static final HttpHeaderName X_MS_SOURCE_IF_UNMODIFIED_SINCE = HttpHeaderName.fromString("x-ms-source-if-unmodified-since");

    private static final HttpHeaderName X_MS_SOURCE_RANGE = HttpHeaderName.fromString("x-ms-source-range");

    private static final HttpHeaderName X_MS_STRUCTURED_BODY = HttpHeaderName.fromString("x-ms-structured-body");

    private static final HttpHeaderName X_MS_STRUCTURED_CONTENT_LENGTH = HttpHeaderName.fromString("x-ms-structured-content-length");

    private static final HttpHeaderName X_MS_TAGS = HttpHeaderName.fromString("x-ms-tags");

    private static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");

    private static final ClientLogger LOGGER = new ClientLogger(BlockBlobsService.class);

    private final HttpPipeline httpPipeline;

    private final JsonSerializer jsonSerializer;

    private final XmlSerializer xmlSerializer;

    private BlockBlobsServiceImpl(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
        this.jsonSerializer = JsonSerializer.getInstance();
        this.xmlSerializer = XmlSerializer.getInstance();
    }

    /**
     * Creates an instance of BlockBlobsService that is capable of sending requests to the service.
     * @param httpPipeline The HTTP pipeline to use for sending requests.
     * @return An instance of `BlockBlobsService`;
     */
    public static BlockBlobsService getNewInstance(HttpPipeline httpPipeline) {
        return new BlockBlobsServiceImpl(httpPipeline);
    }

    @SuppressWarnings("cast")
    @Override
    public Response<Void> upload(String url, String containerName, String blob, String blobType, Integer timeout, String transactionalContentMD5, long contentLength, String contentType, String contentEncoding, String contentLanguage, String contentMd5, String cacheControl, Map<String, String> metadata, String leaseId, String contentDisposition, String encryptionKey, String encryptionKeySha256, EncryptionAlgorithmType encryptionAlgorithm, String encryptionScope, AccessTier tier, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, String blobTagsString, DateTimeRfc1123 immutabilityPolicyExpiry, BlobImmutabilityPolicyMode immutabilityPolicyMode, Boolean legalHold, String transactionalContentCrc64, String structuredBodyType, Long structuredContentLength, BinaryData body, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (cacheControl != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CACHE_CONTROL, cacheControl));
        }
        if (contentLanguage != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CONTENT_LANGUAGE, contentLanguage));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (structuredBodyType != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_STRUCTURED_BODY, structuredBodyType));
        }
        if (encryptionKeySha256 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY_SHA256, encryptionKeySha256));
        }
        if (blobType != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_TYPE, blobType));
        }
        if (immutabilityPolicyMode != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IMMUTABILITY_POLICY_MODE, String.valueOf(immutabilityPolicyMode)));
        }
        if (tier != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ACCESS_TIER, String.valueOf(tier)));
        }
        if (blobTagsString != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_TAGS, blobTagsString));
        }
        if (structuredContentLength != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_STRUCTURED_CONTENT_LENGTH, String.valueOf(structuredContentLength)));
        }
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (immutabilityPolicyExpiry != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IMMUTABILITY_POLICY_UNTIL_DATE, String.valueOf(immutabilityPolicyExpiry)));
        }
        if (transactionalContentCrc64 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CONTENT_CRC64, transactionalContentCrc64));
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
        if (legalHold != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEGAL_HOLD, String.valueOf(legalHold)));
        }
        if (metadata != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_META_, String.valueOf(metadata)));
        }
        if (transactionalContentMD5 != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_MD5, transactionalContentMD5));
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
        if (contentEncoding != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CONTENT_ENCODING, contentEncoding));
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
    public Response<Void> putBlobFromUrl(String url, String containerName, String blob, String blobType, Integer timeout, String transactionalContentMD5, long contentLength, String contentType, String contentEncoding, String contentLanguage, String contentMd5, String cacheControl, Map<String, String> metadata, String leaseId, String contentDisposition, String encryptionKey, String encryptionKeySha256, EncryptionAlgorithmType encryptionAlgorithm, String encryptionScope, AccessTier tier, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, DateTimeRfc1123 sourceIfModifiedSince, DateTimeRfc1123 sourceIfUnmodifiedSince, String sourceIfMatch, String sourceIfNoneMatch, String sourceIfTags, String version, String requestId, String sourceContentMD5, String blobTagsString, String copySource, Boolean copySourceBlobProperties, String copySourceAuthorization, BlobCopySourceTagsMode copySourceTags, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (copySourceTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_COPY_SOURCE_TAG_OPTION, String.valueOf(copySourceTags)));
        }
        if (cacheControl != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CACHE_CONTROL, cacheControl));
        }
        if (contentLanguage != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CONTENT_LANGUAGE, contentLanguage));
        }
        if (sourceIfModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_MODIFIED_SINCE, String.valueOf(sourceIfModifiedSince)));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (sourceIfMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_MATCH, sourceIfMatch));
        }
        if (encryptionKeySha256 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY_SHA256, encryptionKeySha256));
        }
        if (blobType != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_TYPE, blobType));
        }
        if (sourceIfUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_UNMODIFIED_SINCE, String.valueOf(sourceIfUnmodifiedSince)));
        }
        if (tier != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ACCESS_TIER, String.valueOf(tier)));
        }
        if (blobTagsString != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_TAGS, blobTagsString));
        }
        if (sourceIfTags != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_TAGS, sourceIfTags));
        }
        if (copySourceBlobProperties != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_COPY_SOURCE_BLOB_PROPERTIES, String.valueOf(copySourceBlobProperties)));
        }
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (copySource != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_COPY_SOURCE, copySource));
        }
        if (copySourceAuthorization != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_COPY_SOURCE_AUTHORIZATION, copySourceAuthorization));
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
        if (metadata != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_META_, String.valueOf(metadata)));
        }
        if (sourceContentMD5 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_CONTENT_MD5, sourceContentMD5));
        }
        if (transactionalContentMD5 != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_MD5, transactionalContentMD5));
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
        if (sourceIfNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_NONE_MATCH, sourceIfNoneMatch));
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
    public Response<Void> stageBlock(String url, String containerName, String blob, String comp, String blockId, long contentLength, String transactionalContentMD5, String transactionalContentCrc64, Integer timeout, String leaseId, String encryptionKey, String encryptionKeySha256, EncryptionAlgorithmType encryptionAlgorithm, String encryptionScope, String version, String requestId, String structuredBodyType, Long structuredContentLength, BinaryData body, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "blockid", true, blockId, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (encryptionKey != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY, encryptionKey));
        }
        if (structuredContentLength != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_STRUCTURED_CONTENT_LENGTH, String.valueOf(structuredContentLength)));
        }
        if (transactionalContentCrc64 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CONTENT_CRC64, transactionalContentCrc64));
        }
        if (encryptionAlgorithm != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_ALGORITHM, String.valueOf(encryptionAlgorithm)));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (structuredBodyType != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_STRUCTURED_BODY, structuredBodyType));
        }
        if (encryptionKeySha256 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY_SHA256, encryptionKeySha256));
        }
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (encryptionScope != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_SCOPE, encryptionScope));
        }
        if (transactionalContentMD5 != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_MD5, transactionalContentMD5));
        }
        httpRequest.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/octet-stream");
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
    public Response<Void> stageBlockFromURL(String url, String containerName, String blob, String comp, String blockId, long contentLength, String sourceUrl, String sourceRange, String sourceContentMD5, String sourceContentcrc64, Integer timeout, String encryptionKey, String encryptionKeySha256, EncryptionAlgorithmType encryptionAlgorithm, String encryptionScope, String leaseId, DateTimeRfc1123 sourceIfModifiedSince, DateTimeRfc1123 sourceIfUnmodifiedSince, String sourceIfMatch, String sourceIfNoneMatch, String version, String requestId, String copySourceAuthorization, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "blockid", true, blockId, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (sourceContentcrc64 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_CONTENT_CRC64, sourceContentcrc64));
        }
        if (version != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_VERSION, version));
        }
        if (accept != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.ACCEPT, accept));
        }
        if (sourceUrl != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_COPY_SOURCE, sourceUrl));
        }
        if (sourceIfModifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_MODIFIED_SINCE, String.valueOf(sourceIfModifiedSince)));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (copySourceAuthorization != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_COPY_SOURCE_AUTHORIZATION, copySourceAuthorization));
        }
        if (sourceIfMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_MATCH, sourceIfMatch));
        }
        if (encryptionKeySha256 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY_SHA256, encryptionKeySha256));
        }
        if (sourceContentMD5 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_CONTENT_MD5, sourceContentMD5));
        }
        if (sourceIfUnmodifiedSince != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_UNMODIFIED_SINCE, String.valueOf(sourceIfUnmodifiedSince)));
        }
        if (encryptionKey != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY, encryptionKey));
        }
        if (encryptionAlgorithm != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_ALGORITHM, String.valueOf(encryptionAlgorithm)));
        }
        if (sourceIfNoneMatch != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_IF_NONE_MATCH, sourceIfNoneMatch));
        }
        httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength)));
        if (requestId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CLIENT_REQUEST_ID, requestId));
        }
        if (encryptionScope != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_SCOPE, encryptionScope));
        }
        if (sourceRange != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_SOURCE_RANGE, sourceRange));
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
    public Response<Void> commitBlockList(String url, String containerName, String blob, String comp, Integer timeout, String cacheControl, String contentType, String contentEncoding, String contentLanguage, String contentMd5, String transactionalContentMD5, String transactionalContentCrc64, Map<String, String> metadata, String leaseId, String contentDisposition, String encryptionKey, String encryptionKeySha256, EncryptionAlgorithmType encryptionAlgorithm, String encryptionScope, AccessTier tier, DateTimeRfc1123 ifModifiedSince, DateTimeRfc1123 ifUnmodifiedSince, String ifMatch, String ifNoneMatch, String ifTags, String version, String requestId, String blobTagsString, DateTimeRfc1123 immutabilityPolicyExpiry, BlobImmutabilityPolicyMode immutabilityPolicyMode, Boolean legalHold, BlockLookupList blocks, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "timeout", true, timeout, true);
        // Create the HttpRequest.
        HttpRequest httpRequest = new HttpRequest().setMethod(HttpMethod.PUT).setUri(uri.toString());
        if (contentLanguage != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CONTENT_LANGUAGE, contentLanguage));
        }
        if (cacheControl != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CACHE_CONTROL, cacheControl));
        }
        if (leaseId != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEASE_ID, leaseId));
        }
        if (encryptionKeySha256 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ENCRYPTION_KEY_SHA256, encryptionKeySha256));
        }
        if (immutabilityPolicyMode != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_IMMUTABILITY_POLICY_MODE, String.valueOf(immutabilityPolicyMode)));
        }
        if (tier != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_ACCESS_TIER, String.valueOf(tier)));
        }
        if (blobTagsString != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_TAGS, blobTagsString));
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
        if (transactionalContentCrc64 != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_CONTENT_CRC64, transactionalContentCrc64));
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
        if (legalHold != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_LEGAL_HOLD, String.valueOf(legalHold)));
        }
        if (metadata != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_META_, String.valueOf(metadata)));
        }
        if (transactionalContentMD5 != null) {
            httpRequest.getHeaders().add(new HttpHeader(HttpHeaderName.CONTENT_MD5, transactionalContentMD5));
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
        if (contentEncoding != null) {
            httpRequest.getHeaders().add(new HttpHeader(X_MS_BLOB_CONTENT_ENCODING, contentEncoding));
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
        if (blocks != null) {
            SerializationFormat serializationFormat = CoreUtils.serializationFormatFromContentType(httpRequest.getHeaders());
            if (xmlSerializer.supportsFormat(serializationFormat)) {
                httpRequest.setBody(BinaryData.fromObject(blocks, xmlSerializer));
            } else {
                httpRequest.setBody(BinaryData.fromObject(blocks, jsonSerializer));
            }
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
    public Response<BlockList> getBlockList(String url, String containerName, String blob, String comp, String snapshot, BlockListType listType, Integer timeout, String leaseId, String ifTags, String version, String requestId, String accept, RequestContext requestContext) {
        // Append the query parameters.
        UriBuilder uri = UriBuilder.parse(url + "/" + UriEscapers.PATH_ESCAPER.escape(containerName) + "/" + UriEscapers.PATH_ESCAPER.escape(blob));
        GeneratedCodeUtils.addQueryParameter(uri, "comp", true, comp, true);
        GeneratedCodeUtils.addQueryParameter(uri, "snapshot", true, snapshot, true);
        GeneratedCodeUtils.addQueryParameter(uri, "blocklisttype", true, listType, true);
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
        BlockList deserializedResult;
        ParameterizedType returnType = CoreUtils.createParameterizedType(Response.class, BlockList.class);
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
