/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import java.net.URI;
import java.util.Date;

/**
 * Represents the attributes of a copy operation.
 */
public final class CopyState {
    /**
     * Holds the ID for the copy operation.
     */
    private String copyId;

    /**
     * Holds the time the copy operation completed, whether completion was due to a successful copy, an abort, or a
     * failure.
     */
    private Date completionTime;

    /**
     * Holds the status of the copy operation.
     */
    private CopyStatus status;

    /**
     * Holds the source URI of a copy operation.
     */
    private URI source;

    /**
     * Holds the number of bytes copied in the operation so far.
     */
    private Long bytesCopied;

    /**
     * Holds the total number of bytes in the source of the copy.
     */
    private Long totalBytes;

    /**
     * Holds the description of the current status.
     */
    private String statusDescription;

    /**
     * Gets the ID of the copy operation.
     * 
     * @return A <code>String</code> which represents the ID of the copy operation.
     */
    public String getCopyId() {
        return this.copyId;
    }

    /**
     * Gets the time that the copy operation completed.
     * 
     * @return A {@link java.util.Date} object which represents the time that the copy operation completed.
     */
    public Date getCompletionTime() {
        return this.completionTime;
    }

    /**
     * Gets the status of the copy operation.
     * 
     * @return A <code>{@link CopyStatus}</code> object representing the status of the copy operation.
     */
    public CopyStatus getStatus() {
        return this.status;
    }

    /**
     * Gets the source URI of the copy operation.
     * 
     * @return A {@link java.net.URI} object which represents the source URI of the copy operation in a string.
     */
    public URI getSource() {
        return this.source;
    }

    /**
     * Gets the number of bytes copied in the operation so far.
     * 
     * @return A <code>long</code> which represents the number of bytes copied.
     */
    public Long getBytesCopied() {
        return this.bytesCopied;
    }

    /**
     * Gets the number of bytes total number of bytes to copy.
     * 
     * @return A <code>long</code> which represents the total number of bytes to copy/ 
     */
    public Long getTotalBytes() {
        return this.totalBytes;
    }

    /**
     * Gets the status description of the copy operation.
     * 
     * @return A <code>String</code> which represents the status description.
     */
    public String getStatusDescription() {
        return this.statusDescription;
    }

    /**
     * Sets the ID of the copy operation.
     * 
     * @param copyId
     *        A <code>String</code> which specifies the ID of the copy operation to set.
     * 
     */
    void setCopyId(final String copyId) {
        this.copyId = copyId;
    }

    /**
     * Sets the time that the copy operation completed.
     * 
     * @param completionTime
     *        A {@link java.util.Date} object which specifies the time when the copy operation completed.
     */
    void setCompletionTime(final Date completionTime) {
        this.completionTime = completionTime;
    }

    /**
     * Sets the status of the copy operation.
     * 
     * @param status
     *        A <code>{@link CopyStatus}</code> object specifies the status of the copy operation.
     */
    void setStatus(final CopyStatus status) {
        this.status = status;
    }

    /**
     * Sets the source URI of the copy operation.
     * 
     * @param source
     *        A {@link java.net.URI} object which specifies the source URI.
     */
    void setSource(final URI source) {
        this.source = source;
    }

    /**
     * Sets the number of bytes copied so far.
     * 
     * @param bytesCopied
     *        A <code>long</code> which specifies the number of bytes copied.
     */
    void setBytesCopied(final Long bytesCopied) {
        this.bytesCopied = bytesCopied;
    }

    /**
     * Sets the total number of bytes in the source to copy.
     * 
     * @param totalBytes
     *        A <code>long</code> which specifies the number of bytes to copy.
     */
    void setTotalBytes(final Long totalBytes) {
        this.totalBytes = totalBytes;
    }

    /**
     * Sets the current status of the copy operation.
     * 
     * @param statusDescription
     *        A <code>String</code> which specifies the status description.
     */
    void setStatusDescription(final String statusDescription) {
        this.statusDescription = statusDescription;
    }}