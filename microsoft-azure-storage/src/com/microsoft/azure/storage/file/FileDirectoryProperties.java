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
package com.microsoft.azure.storage.file;

import java.util.Date;

import com.microsoft.azure.storage.AccessCondition;

/**
 * Represents the system properties for a directory.
 */
public final class FileDirectoryProperties {

    /**
     * Represents the ETag value for the directory.
     */
    private String etag;

    /**
     * Represents the directory's last-modified time.
     */
    private Date lastModified;

    /**
     * Gets the ETag value of the directory.
     * <p>
     * The ETag value is a unique identifier that is updated when a write operation is performed against the directory.
     * It may be used to perform operations conditionally, providing concurrency control and improved efficiency.
     * <p>
     * The {@link AccessCondition#generateIfMatchCondition(String)} and
     * {@link AccessCondition#generateIfNoneMatchCondition(String)} methods take an ETag value and return an
     * {@link AccessCondition} object that may be specified on the request.
     * 
     * @return A <code>String</code> which represents the ETag.
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * Gets the last modified time on the directory.
     * 
     * @return A <code>java.util.Date</code> object which represents the last modified time.
     */
    public Date getLastModified() {
        return this.lastModified;
    }

    /**
     * Sets the ETag value on the directory.
     * 
     * @param etag
     *            A <code>String</code> which represents the ETag to set.
     */
    protected void setEtag(final String etag) {
        this.etag = etag;
    }

    /**
     * Sets the last modified time on the directory.
     * 
     * @param lastModified
     *            A <code>java.util.Date</code> object which represents the last modified time to set.
     */
    protected void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }
}
