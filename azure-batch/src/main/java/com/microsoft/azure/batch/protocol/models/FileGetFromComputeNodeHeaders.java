/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.microsoft.rest.DateTimeRfc1123;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines headers for GetFromComputeNode operation.
 */
public class FileGetFromComputeNodeHeaders {
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
    public String getClientRequestId() {
        return this.clientRequestId;
    }

    /**
     * Set the clientRequestId value.
     *
     * @param clientRequestId the clientRequestId value to set
     */
    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    /**
     * Get the requestId value.
     *
     * @return the requestId value
     */
    public String getRequestId() {
        return this.requestId;
    }

    /**
     * Set the requestId value.
     *
     * @param requestId the requestId value to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Get the eTag value.
     *
     * @return the eTag value
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Set the eTag value.
     *
     * @param eTag the eTag value to set
     */
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    /**
     * Get the lastModified value.
     *
     * @return the lastModified value
     */
    public DateTime getLastModified() {
        if (this.lastModified == null) {
            return null;
        }
        return this.lastModified.getDateTime();
    }

    /**
     * Set the lastModified value.
     *
     * @param lastModified the lastModified value to set
     */
    public void setLastModified(DateTime lastModified) {
        this.lastModified = new DateTimeRfc1123(lastModified);
    }

    /**
     * Get the ocpCreationTime value.
     *
     * @return the ocpCreationTime value
     */
    public DateTime getOcpCreationTime() {
        if (this.ocpCreationTime == null) {
            return null;
        }
        return this.ocpCreationTime.getDateTime();
    }

    /**
     * Set the ocpCreationTime value.
     *
     * @param ocpCreationTime the ocpCreationTime value to set
     */
    public void setOcpCreationTime(DateTime ocpCreationTime) {
        this.ocpCreationTime = new DateTimeRfc1123(ocpCreationTime);
    }

    /**
     * Get the ocpBatchFileIsdirectory value.
     *
     * @return the ocpBatchFileIsdirectory value
     */
    public Boolean getOcpBatchFileIsdirectory() {
        return this.ocpBatchFileIsdirectory;
    }

    /**
     * Set the ocpBatchFileIsdirectory value.
     *
     * @param ocpBatchFileIsdirectory the ocpBatchFileIsdirectory value to set
     */
    public void setOcpBatchFileIsdirectory(Boolean ocpBatchFileIsdirectory) {
        this.ocpBatchFileIsdirectory = ocpBatchFileIsdirectory;
    }

    /**
     * Get the ocpBatchFileUrl value.
     *
     * @return the ocpBatchFileUrl value
     */
    public String getOcpBatchFileUrl() {
        return this.ocpBatchFileUrl;
    }

    /**
     * Set the ocpBatchFileUrl value.
     *
     * @param ocpBatchFileUrl the ocpBatchFileUrl value to set
     */
    public void setOcpBatchFileUrl(String ocpBatchFileUrl) {
        this.ocpBatchFileUrl = ocpBatchFileUrl;
    }

    /**
     * Get the ocpBatchFileMode value.
     *
     * @return the ocpBatchFileMode value
     */
    public String getOcpBatchFileMode() {
        return this.ocpBatchFileMode;
    }

    /**
     * Set the ocpBatchFileMode value.
     *
     * @param ocpBatchFileMode the ocpBatchFileMode value to set
     */
    public void setOcpBatchFileMode(String ocpBatchFileMode) {
        this.ocpBatchFileMode = ocpBatchFileMode;
    }

    /**
     * Get the contentType value.
     *
     * @return the contentType value
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the contentType value.
     *
     * @param contentType the contentType value to set
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Get the contentLength value.
     *
     * @return the contentLength value
     */
    public Long getContentLength() {
        return this.contentLength;
    }

    /**
     * Set the contentLength value.
     *
     * @param contentLength the contentLength value to set
     */
    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

}
