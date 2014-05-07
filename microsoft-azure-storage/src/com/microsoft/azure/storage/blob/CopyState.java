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
     * Holds the name of the container.
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
     * Initializes a new instance of the CopyState class.
     */
    public CopyState() {
    }

    /**
     * Gets the copy ID of the container.
     * 
     * @return A string containing the copy ID of the container.
     */
    public String getCopyId() {
        return this.copyId;
    }

    /**
     * Gets the time that the copy operation completed.
     * 
     * @return The time that the copy operation completed.
     */
    public Date getCompletionTime() {
        return this.completionTime;
    }

    /**
     * Gets the status of the copy operation.
     * 
     * @return A <code>CopyStatus</code> object representing the status of the copy operation.
     */
    public CopyStatus getStatus() {
        return this.status;
    }

    /**
     * Gets the source URI of the copy operation.
     * 
     * @return The source URI of the copy operation in a string.
     */
    public URI getSource() {
        return this.source;
    }

    /**
     * Gets the number of bytes copied in the operation so far.
     * 
     * @return The number of bytes copied so far.
     */
    public Long getBytesCopied() {
        return this.bytesCopied;
    }

    public Long getTotalBytes() {
        return this.totalBytes;
    }

    /**
     * Gets the status description of the copy operation.
     * 
     * @return A string containing the status description.
     */
    public String getStatusDescription() {
        return this.statusDescription;
    }

    /**
     * Sets the copy ID of the container.
     * 
     * @param copyId
     *            The copy ID of the container to set.
     * 
     */
    protected void setCopyId(final String copyId) {
        this.copyId = copyId;
    }

    /**
     * Sets the time that the copy operation completed.
     * 
     * @param completionTime
     *            The completion time to set.
     */
    protected void setCompletionTime(final Date completionTime) {
        this.completionTime = completionTime;
    }

    /**
     * Sets the status of the copy operation.
     * 
     * @param status
     *            The copy operation status to set, as a <code>CopyStatus</code> object.
     */
    protected void setStatus(final CopyStatus status) {
        this.status = status;
    }

    /**
     * Sets the source URI of the copy operation.
     * 
     * @param source
     *            The source URI to set.
     */
    protected void setSource(final URI source) {
        this.source = source;
    }

    /**
     * Sets the number of bytes copied so far.
     * 
     * @param bytesCopied
     *            The number of bytes copied to set.
     */
    protected void setBytesCopied(final Long bytesCopied) {
        this.bytesCopied = bytesCopied;
    }

    /**
     * Sets the total number of bytes in the source to copy.
     * 
     * @param totalBytes
     *            The number of bytes to set.
     */
    protected void setTotalBytes(final Long totalBytes) {
        this.totalBytes = totalBytes;
    }

    /**
     * Sets the current status of the copy operation.
     * 
     * @param statusDescription
     *            The current status to set.
     */
    protected void setStatusDescription(final String statusDescription) {
        this.statusDescription = statusDescription;
    }
}
