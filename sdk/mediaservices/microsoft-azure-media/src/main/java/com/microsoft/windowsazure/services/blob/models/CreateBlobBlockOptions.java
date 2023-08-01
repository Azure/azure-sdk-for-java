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


/**
 * Represents the options that may be set on a
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#createBlobBlock(String, String, String, java.io.InputStream, CreateBlobBlockOptions)
 * createBlobBlock} request. These options include an optional server timeout
 * for the operation, the lease ID if the blob has an active lease, and the MD5
 * hash value for the block content.
 */
public class CreateBlobBlockOptions extends BlobServiceOptions {
    private String leaseId;
    private String contentMD5;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link CreateBlobBlockOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link CreateBlobBlockOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link CreateBlobBlockOptions} instance.
     */
    @Override
    public CreateBlobBlockOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the lease ID to match for the blob set in this
     * {@link CreateBlobBlockOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets a lease ID value to match when updating the blob. This value must
     * match the lease ID set on a leased blob for an update to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobBlockOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link CreateBlobBlockOptions} instance.
     */
    public CreateBlobBlockOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the MD5 hash value for the block content set in this
     * {@link CreateBlobBlockOptions} instance.
     * 
     * @return A {@link String} containing the MD5 hash value for the block
     *         content set, if any.
     */
    public String getContentMD5() {
        return contentMD5;
    }

    /**
     * Sets the optional MD5 hash value for the block content. This hash is used
     * to verify the integrity of the blob during transport. When this value is
     * specified, the storage service checks the hash of the content that has
     * arrived with the one that was sent. If the two hashes do not match, the
     * operation will fail with error code 400 (Bad Request), which will cause a
     * ServiceException to be thrown.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CreateBlobBlockOptions} instance is passed as a parameter.
     * 
     * @param contentMD5
     *            A {@link String} containing the MD5 hash value for the block
     *            content to set.
     * @return A reference to this {@link CreateBlobBlockOptions} instance.
     */
    public CreateBlobBlockOptions setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
        return this;
    }
}
