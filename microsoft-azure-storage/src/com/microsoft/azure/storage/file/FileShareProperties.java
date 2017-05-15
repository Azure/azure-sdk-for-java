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
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents the system properties for a share.
 */
public final class FileShareProperties {

    /**
     * Represents the ETag value for the share.
     */
    private String etag;

    /**
     * Represents the share's last-modified time.
     */
    private Date lastModified;

    /**
     * Represents the limit on the size of files (in GB) stored on the share.
     */
    private Integer shareQuota;

    /**
     * Gets the ETag value of the share.
     * <p>
     * The ETag value is a unique identifier that is updated when a write operation is performed against the share. It
     * may be used to perform operations conditionally, providing concurrency control and improved efficiency.
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
     * Gets the last modified time on the share.
     * 
     * @return A <code>java.util.Date</code> object which represents the last modified time.
     */
    public Date getLastModified() {
        return this.lastModified;
    }

    /**
     * Gets the limit on the size of files (in GB) stored on the share.
     * 
     * @return A <code>java.lang.Integer</code> object which represents the limit on
     *            the size of files stored on the share.
     */
    public Integer getShareQuota() {
        return shareQuota;
    }

    /**
     * Sets the ETag value on the share.
     * 
     * @param etag
     *            A <code>String</code> which represents the ETag to set.
     */
    protected void setEtag(final String etag) {
        this.etag = etag;
    }

    /**
     * Sets the last modified time on the share.
     * 
     * @param lastModified
     *            A <code>java.util.Date</code> object which represents the last modified time to set.
     */
    protected void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Sets the limit on the size of files (in GB) stored on the share.
     * 
     * @param shareQuota
     *            A <code>java.lang.Integer</code> object which represents the limit on
     *            the size of files stored on the share.
     */
    public void setShareQuota(Integer shareQuota) {
        if (shareQuota != null) {
            Utility.assertInBounds("Share Quota", shareQuota, 1, FileConstants.MAX_SHARE_QUOTA);
        }
        this.shareQuota = shareQuota;
    }
}