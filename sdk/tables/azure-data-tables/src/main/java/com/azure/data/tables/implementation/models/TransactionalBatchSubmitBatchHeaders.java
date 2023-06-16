// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.Context;
import com.azure.data.tables.implementation.TransactionalBatchImpl;

/**
 * Defines headers for the
 * {@link TransactionalBatchImpl#submitTransactionalBatchWithRestResponseAsync(TransactionalBatchRequestBody, String,
 * Context)} operation.
 */
@Fluent
public final class TransactionalBatchSubmitBatchHeaders {
    private static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");
    private static final HttpHeaderName X_MS_REQUEST_ID = HttpHeaderName.fromString("x-ms-request-id");

    /*
     * The media type of the body of the response. For transactional batch requests, this is
     * "multipart/mixed; boundary=batchresponse_GUID".
     */
    private String contentType;

    /*
     * The x-ms-version property.
     */
    private String xMsVersion;

    /*
     * The x-ms-request-id property.
     */
    private String xMsRequestId;

    /*
     * The x-ms-client-request-id property.
     */
    private String xMsClientRequestId;

    // HttpHeaders containing the raw property values.

    /**
     * Creates an instance of TablesQueryEntitiesHeaders class.
     *
     * @param rawHeaders The raw HttpHeaders that will be used to create the property values.
     */
    public TransactionalBatchSubmitBatchHeaders(HttpHeaders rawHeaders) {
        this.contentType = rawHeaders.getValue(HttpHeaderName.CONTENT_TYPE);
        this.xMsVersion = rawHeaders.getValue(X_MS_VERSION);
        this.xMsRequestId = rawHeaders.getValue(X_MS_REQUEST_ID);
        this.xMsClientRequestId = rawHeaders.getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID);
    }

    /**
     * Get the media type of the body of the response. For transactional batch requests, this is "multipart/mixed;
     * boundary=batchresponse_GUID".
     *
     * @return The content type.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the contentType property: The media type of the body of the response. For transactional batch requests, this
     * is "multipart/mixed; boundary=batch_GUID".
     *
     * @param contentType the contentType value to set.
     * @return The updated {@link TransactionalBatchSubmitBatchHeaders} object.
     */
    public TransactionalBatchSubmitBatchHeaders setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the {@code x-ms-version} property.
     *
     * @return The {@code x-ms-version}.
     */
    public String getXMsVersion() {
        return this.xMsVersion;
    }

    /**
     * Set the {@code x-ms-version} property.
     *
     * @param xMsVersion The {@code x-ms-version} to set.
     * @return The updated {@link TransactionalBatchSubmitBatchHeaders} object.
     */
    public TransactionalBatchSubmitBatchHeaders setXMsVersion(String xMsVersion) {
        this.xMsVersion = xMsVersion;
        return this;
    }

    /**
     * Get the {@code x-ms-request-id} property.
     *
     * @return The {@code x-ms-request-id}.
     */
    public String getXMsRequestId() {
        return this.xMsRequestId;
    }

    /**
     * Set the {@code x-ms-request-id} property.
     *
     * @param xMsRequestId The {@code x-ms-request-id} to set.
     * @return The updated {@link TransactionalBatchSubmitBatchHeaders} object.
     */
    public TransactionalBatchSubmitBatchHeaders setXMsRequestId(String xMsRequestId) {
        this.xMsRequestId = xMsRequestId;
        return this;
    }

    /**
     * Get the {@code x-ms-client-request-id} property.
     *
     * @return The {@code x-ms-client-request-id}.
     */
    public String getXMsClientRequestId() {
        return this.xMsClientRequestId;
    }

    /**
     * Set the {@code x-ms-client-request-id} property.
     *
     * @param xMsClientRequestId The {@code x-ms-client-request-id} to set.
     * @return The updated {@link TransactionalBatchSubmitBatchHeaders} object.
     */
    public TransactionalBatchSubmitBatchHeaders setXMsClientRequestId(String xMsClientRequestId) {
        this.xMsClientRequestId = xMsClientRequestId;
        return this;
    }
}
