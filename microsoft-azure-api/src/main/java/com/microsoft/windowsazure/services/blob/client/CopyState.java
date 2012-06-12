/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.blob.client;

import java.net.URI;
import java.util.Date;

/**
 * Represents the attributes of a copy operation.
 * 
 */
public final class CopyState {
    /**
     * Holds the Name of the Container
     */
    private String copyId;

    /**
     * Holds the time the copy operation completed, whether completion was due to a successful copy, abortion, or a
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
     * Initializes a new instance of the CopyState class
     */
    public CopyState() {
    }

    public String getCopyId() {
        return this.copyId;
    }

    public Date getCompletionTime() {
        return this.completionTime;
    }

    public CopyStatus getStatus() {
        return this.status;
    }

    public URI getSource() {
        return this.source;
    }

    public Long getBytesCopied() {
        return this.bytesCopied;
    }

    public Long getTotalBytes() {
        return this.totalBytes;
    }

    public String getStatusDescription() {
        return this.statusDescription;
    }

    public void setCopyId(final String copyId) {
        this.copyId = copyId;
    }

    public void setCompletionTime(final Date completionTime) {
        this.completionTime = completionTime;
    }

    public void setStatus(final CopyStatus status) {
        this.status = status;
    }

    public void setSource(final URI source) {
        this.source = source;
    }

    public void setBytesCopied(final Long bytesCopied) {
        this.bytesCopied = bytesCopied;
    }

    public void setTotalBytes(final Long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public void setStatusDescription(final String statusDescription) {
        this.statusDescription = statusDescription;
    }
}
