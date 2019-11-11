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
package com.microsoft.windowsazure.services.blob.models;

import com.microsoft.windowsazure.core.utils.AccessConditionHeader;

/**
 * Represents the options that may be set on a
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#createBlobPages(String, String, PageRange, long, java.io.InputStream, CreateBlobPagesOptions)}
 * request. These options include an optional server timeout for the operation,
 * a blob lease ID to create pages in a blob with an active lease, an optional
 * MD5 hash for the content, and any access conditions to satisfy.
 */
public class CreateBlobPagesOptions extends BlobServiceOptions {
    private String leaseId;
    private String contentMD5;
    private AccessConditionHeader accessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link CreateBlobPagesOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link CreateBlobPagesOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link CreateBlobPagesOptions} instance.
     */
    @Override
    public CreateBlobPagesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the lease ID to match for the blob set in this
     * {@link CreateBlobPagesOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets an optional lease ID value to match when getting the blob. If set,
     * the lease must be active and the value must match the lease ID set on the
     * leased blob for the operation to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobPagesOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link CreateBlobPagesOptions} instance.
     */
    public CreateBlobPagesOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the MD5 hash value for the page content set in this
     * {@link CreateBlobPagesOptions} instance.
     * 
     * @return A {@link String} containing the MD5 hash value for the block
     *         content set, if any.
     */
    public String getContentMD5() {
        return contentMD5;
    }

    /**
     * Sets the optional MD5 hash value for the page content. This hash is used
     * to verify the integrity of the blob during transport. When this value is
     * specified, the storage service checks the hash of the content that has
     * arrived with the one that was sent. If the two hashes do not match, the
     * operation will fail with error code 400 (Bad Request), which will cause a
     * ServiceException to be thrown.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobPagesOptions} instance is passed as a parameter.
     * 
     * @param contentMD5
     *            A {@link String} containing the MD5 hash value for the block
     *            content to set.
     * @return A reference to this {@link CreateBlobPagesOptions} instance.
     */
    public CreateBlobPagesOptions setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
        return this;
    }

    /**
     * Gets the access conditions set in this {@link CreateBlobPagesOptions}
     * instance.
     * 
     * @return An {@link AccessCondition} containing the access conditions set,
     *         if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets optional access conditions for getting the blob. The operation will
     * return an error if the access conditions are not met.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobPagesOptions} instance is passed as a parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the access conditions to
     *            set.
     * @return A reference to this {@link CreateBlobPagesOptions} instance.
     */
    public CreateBlobPagesOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
