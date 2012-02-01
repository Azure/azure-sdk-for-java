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

import java.util.Date;

import com.microsoft.windowsazure.services.core.storage.AccessCondition;

/**
 * Represents the system properties for a container.
 */
public final class BlobContainerProperties {

    /**
     * Represents the ETag value for the container.
     * <p>
     * The ETag value is a unique identifier that is updated when a write operation is performed against the container.
     * It may be used to perform operations conditionally, providing concurrency control and improved efficiency.
     * <p>
     * The {@link AccessCondition#ifMatch} and {@link AccessCondition#ifNoneMatch} methods take an ETag value and return
     * an {@link AccessCondition} object that may be specified on the request.
     */
    private String etag;

    /**
     * Represents the container's last-modified time.
     */
    private Date lastModified;

    /**
     * @return the etag
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * @return the lastModified
     */
    public Date getLastModified() {
        return this.lastModified;
    }

    /**
     * @param etag
     *            the etag to set
     */
    public void setEtag(final String etag) {
        this.etag = etag;
    }

    /**
     * @param lastModified
     *            the lastModified to set
     */
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }
}
