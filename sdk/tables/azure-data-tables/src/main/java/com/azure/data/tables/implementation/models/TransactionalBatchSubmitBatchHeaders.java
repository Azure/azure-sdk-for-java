// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.Context;
import com.azure.data.tables.implementation.TransactionalBatchImpl;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines headers for the
 * {@link TransactionalBatchImpl#submitTransactionalBatchWithRestResponseAsync(TransactionalBatchRequestBody, String, Context)}
 * operation.
 */
@Fluent
public final class TransactionalBatchSubmitBatchHeaders {
    /*
     * The media type of the body of the response. For transactional batch requests, this is
     * "multipart/mixed; boundary=batchresponse_GUID".
     */
    @JsonProperty(value = "Content-Type")
    private String contentType;

    /*
     * The x-ms-version property.
     */
    @JsonProperty(value = "x-ms-version")
    private String xMsVersion;

    /*
     * The x-ms-request-id property.
     */
    @JsonProperty(value = "x-ms-request-id")
    private String xMsRequestId;

    /*
     * The x-ms-client-request-id property.
     */
    @JsonProperty(value = "x-ms-client-request-id")
    private String xMsClientRequestId;

    /**
     * Get the media type of the body of the response. For transactional batch requests, this is
     * "multipart/mixed; boundary=batchresponse_GUID".
     *
     * @return The content type.
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the contentType property: The media type of the body of the response. For transactional batch requests,
     * this is "multipart/mixed; boundary=batch_GUID".
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
