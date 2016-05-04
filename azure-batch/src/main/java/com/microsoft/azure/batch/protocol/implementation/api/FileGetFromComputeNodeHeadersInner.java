/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import com.microsoft.rest.DateTimeRfc1123;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines headers for GetFromComputeNode operation.
 */
public class FileGetFromComputeNodeHeadersInner {
    /**
     * Gets the ClientRequestId provided by the client during the request, if
     * present and requested to be returned.
     */
    @JsonProperty(value = "client-request-id")
    private String clientRequestId;

    /**
     * Gets the value that uniquely identifies a request.
     */
    @JsonProperty(value = "request-id")
    private String requestId;

    /**
     * Gets the content of the ETag HTTP response header.
     */
    @JsonProperty(value = "ETag")
    private String eTag;

    /**
     * Gets the content of the Last-Modified HTTP response header.
     */
    @JsonProperty(value = "Last-Modified")
    private DateTimeRfc1123 lastModified;

    /**
     * Gets the file creation time.
     */
    @JsonProperty(value = "ocp-creation-time")
    private DateTimeRfc1123 ocpCreationTime;

    /**
     * Gets whether the object represents a directory.
     */
    @JsonProperty(value = "ocp-batch-file-isdirectory")
    private Boolean ocpBatchFileIsdirectory;

    /**
     * Gets the URL of the file.
     */
    @JsonProperty(value = "ocp-batch-file-url")
    private String ocpBatchFileUrl;

    /**
     * Gets the file mode attribute in octal format.
     */
    @JsonProperty(value = "ocp-batch-file-mode")
    private String ocpBatchFileMode;

    /**
     * Gets the content type of the file.
     */
    @JsonProperty(value = "Content-Type")
    private String contentType;

    /**
     * Gets the length of the file.
     */
    @JsonProperty(value = "Content-Length")
    private Long contentLength;

    /**
     * Get the clientRequestId value.
     *
     * @return the clientRequestId value
     */
    public String clientRequestId() {
        return this.clientRequestId;
    }

    /**
     * Set the clientRequestId value.
     *
     * @param clientRequestId the clientRequestId value to set
     * @return the FileGetFromComputeNodeHeadersInner object itself.
     */
    public FileGetFromComputeNodeHeadersInner setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
        return this;
    }

    /**
     * Get the requestId value.
     *
     * @return the requestId value
     */
    public String requestId() {
        return this.requestId;
    }

    /**
     * Set the requestId value.
     *
     * @param requestId the requestId value to set
     * @return the FileGetFromComputeNodeHeadersInner object itself.
     */
    public FileGetFromComputeNodeHeadersInner setRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * Get the eTag value.
     *
     * @return the eTag value
     */
    public String eTag() {
        return this.eTag;
    }

    /**
     * Set the eTag value.
     *
     * @param eTag the eTag value to set
     * @return the FileGetFromComputeNodeHeadersInner object itself.
     */
    public FileGetFromComputeNodeHeadersInner setETag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    /**
     * Get the lastModified value.
     *
     * @return the lastModified value
     */
    public DateTime lastModified() {
        if (this.lastModified == null) {
            return null;
        }
        return this.lastModified.getDateTime();
    }

    /**
     * Set the lastModified value.
     *
     * @param lastModified the lastModified value to set
     * @return the FileGetFromComputeNodeHeadersInner object itself.
     */
    public FileGetFromComputeNodeHeadersInner setLastModified(DateTime lastModified) {
        this.lastModified = new DateTimeRfc1123(lastModified);
        return this;
    }

    /**
     * Get the ocpCreationTime value.
     *
     * @return the ocpCreationTime value
     */
    public DateTime ocpCreationTime() {
        if (this.ocpCreationTime == null) {
            return null;
        }
        return this.ocpCreationTime.getDateTime();
    }

    /**
     * Set the ocpCreationTime value.
     *
     * @param ocpCreationTime the ocpCreationTime value to set
     * @return the FileGetFromComputeNodeHeadersInner object itself.
     */
    public FileGetFromComputeNodeHeadersInner setOcpCreationTime(DateTime ocpCreationTime) {
        this.ocpCreationTime = new DateTimeRfc1123(ocpCreationTime);
        return this;
    }

    /**
     * Get the ocpBatchFileIsdirectory value.
     *
     * @return the ocpBatchFileIsdirectory value
     */
    public Boolean ocpBatchFileIsdirectory() {
        return this.ocpBatchFileIsdirectory;
    }

    /**
     * Set the ocpBatchFileIsdirectory value.
     *
     * @param ocpBatchFileIsdirectory the ocpBatchFileIsdirectory value to set
     * @return the FileGetFromComputeNodeHeadersInner object itself.
     */
    public FileGetFromComputeNodeHeadersInner setOcpBatchFileIsdirectory(Boolean ocpBatchFileIsdirectory) {
        this.ocpBatchFileIsdirectory = ocpBatchFileIsdirectory;
        return this;
    }

    /**
     * Get the ocpBatchFileUrl value.
     *
     * @return the ocpBatchFileUrl value
     */
    public String ocpBatchFileUrl() {
        return this.ocpBatchFileUrl;
    }

    /**
     * Set the ocpBatchFileUrl value.
     *
     * @param ocpBatchFileUrl the ocpBatchFileUrl value to set
     * @return the FileGetFromComputeNodeHeadersInner object itself.
     */
    public FileGetFromComputeNodeHeadersInner setOcpBatchFileUrl(String ocpBatchFileUrl) {
        this.ocpBatchFileUrl = ocpBatchFileUrl;
        return this;
    }

    /**
     * Get the ocpBatchFileMode value.
     *
     * @return the ocpBatchFileMode value
     */
    public String ocpBatchFileMode() {
        return this.ocpBatchFileMode;
    }

    /**
     * Set the ocpBatchFileMode value.
     *
     * @param ocpBatchFileMode the ocpBatchFileMode value to set
     * @return the FileGetFromComputeNodeHeadersInner object itself.
     */
    public FileGetFromComputeNodeHeadersInner setOcpBatchFileMode(String ocpBatchFileMode) {
        this.ocpBatchFileMode = ocpBatchFileMode;
        return this;
    }

    /**
     * Get the contentType value.
     *
     * @return the contentType value
     */
    public String contentType() {
        return this.contentType;
    }

    /**
     * Set the contentType value.
     *
     * @param contentType the contentType value to set
     * @return the FileGetFromComputeNodeHeadersInner object itself.
     */
    public FileGetFromComputeNodeHeadersInner setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the contentLength value.
     *
     * @return the contentLength value
     */
    public Long contentLength() {
        return this.contentLength;
    }

    /**
     * Set the contentLength value.
     *
     * @param contentLength the contentLength value to set
     * @return the FileGetFromComputeNodeHeadersInner object itself.
     */
    public FileGetFromComputeNodeHeadersInner setContentLength(Long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

}
