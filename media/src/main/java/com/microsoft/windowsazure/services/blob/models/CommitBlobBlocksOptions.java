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

import java.util.HashMap;

import com.microsoft.windowsazure.core.utils.AccessConditionHeader;

/**
 * Represents the options that may be set on a
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#commitBlobBlocks(String, String, BlockList, CommitBlobBlocksOptions)
 * commitBlobBlocks} request. These options include an optional server timeout
 * for the operation, the MIME content type and content encoding for the blob,
 * the content language, the MD5 hash, a cache control value, blob metadata, a
 * blob lease ID, and any access conditions for the operation.
 */
public class CommitBlobBlocksOptions extends BlobServiceOptions {
    private String blobContentType;
    private String blobContentEncoding;
    private String blobContentLanguage;
    private String blobContentMD5;
    private String blobCacheControl;
    private HashMap<String, String> metadata = new HashMap<String, String>();
    private String leaseId;
    private AccessConditionHeader accessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link CommitBlobBlocksOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link CommitBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link CommitBlobBlocksOptions} instance.
     */
    @Override
    public CommitBlobBlocksOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the MIME content type value set in this
     * {@link CommitBlobBlocksOptions} instance.
     * 
     * @return A {@link String} containing the MIME content type value set, if
     *         any.
     */
    public String getBlobContentType() {
        return blobContentType;
    }

    /**
     * Sets the optional MIME content type for the blob content. This value will
     * be returned to clients in the <code>Content-Type</code> header of the
     * response when the blob data or blob properties are requested. If no
     * content type is specified, the default content type is
     * <strong>application/octet-stream</strong>.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CommitBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param blobContentType
     *            A {@link String} containing the MIME content type value to
     *            set.
     * @return A reference to this {@link CommitBlobBlocksOptions} instance.
     */
    public CommitBlobBlocksOptions setBlobContentType(String blobContentType) {
        this.blobContentType = blobContentType;
        return this;
    }

    /**
     * Gets the HTTP content encoding value set in this
     * {@link CommitBlobBlocksOptions} instance.
     * 
     * @return A {@link String} containing the HTTP content encoding value set,
     *         if any.
     */
    public String getBlobContentEncoding() {
        return blobContentEncoding;
    }

    /**
     * Sets the optional HTTP content encoding value for the blob content. Use
     * this value to specify any HTTP content encodings applied to the blob,
     * passed as a <code>x-ms-blob-content-encoding</code> header value to the
     * server. This value will be returned to clients in the headers of the
     * response when the blob data or blob properties are requested. Pass an
     * empty value to update a blob to the default value, which will cause no
     * content encoding header to be returned with the blob.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CommitBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param blobContentEncoding
     *            A {@link String} containing the
     *            <code>x-ms-blob-content-encoding</code> header value to set.
     * @return A reference to this {@link CommitBlobBlocksOptions} instance.
     */
    public CommitBlobBlocksOptions setBlobContentEncoding(
            String blobContentEncoding) {
        this.blobContentEncoding = blobContentEncoding;
        return this;
    }

    /**
     * Gets the HTTP content language header value set in this
     * {@link CommitBlobBlocksOptions} instance.
     * 
     * @return A {@link String} containing the HTTP content language header
     *         value set, if any.
     */
    public String getBlobContentLanguage() {
        return blobContentLanguage;
    }

    /**
     * Sets the optional HTTP content language header value for the blob
     * content. Use this value to specify the content language of the blob. This
     * value will be returned to clients in the
     * <code>x-ms-blob-content-language</code> header of the response when the
     * blob data or blob properties are requested.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CommitBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param blobContentLanguage
     *            A {@link String} containing the
     *            <code>x-ms-blob-content-language</code> header value to set.
     * @return A reference to this {@link CommitBlobBlocksOptions} instance.
     */
    public CommitBlobBlocksOptions setBlobContentLanguage(
            String blobContentLanguage) {
        this.blobContentLanguage = blobContentLanguage;
        return this;
    }

    /**
     * Gets the MD5 hash value for the blob content set in this
     * {@link CommitBlobBlocksOptions} instance.
     * 
     * @return A {@link String} containing the MD5 hash value for the blob
     *         content set, if any.
     */
    public String getBlobContentMD5() {
        return blobContentMD5;
    }

    /**
     * Sets the optional MD5 hash value for the blob content. This value will be
     * returned to clients in the <code>x-ms-blob-content-md5</code> header
     * value of the response when the blob data or blob properties are
     * requested. This hash is used to verify the integrity of the blob during
     * transport. When this header is specified, the storage service checks the
     * hash of the content that has arrived with the one that was sent. If the
     * two hashes do not match, the operation will fail with error code 400 (Bad
     * Request), which will cause a ServiceException to be thrown.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CommitBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param blobContentMD5
     *            A {@link String} containing the MD5 hash value for the blob
     *            content to set.
     * @return A reference to this {@link CommitBlobBlocksOptions} instance.
     */
    public CommitBlobBlocksOptions setBlobContentMD5(String blobContentMD5) {
        this.blobContentMD5 = blobContentMD5;
        return this;
    }

    /**
     * Gets the HTTP cache control value set in this
     * {@link CommitBlobBlocksOptions} instance.
     * 
     * @return A {@link String} containing the HTTP cache control value set, if
     *         any.
     */
    public String getBlobCacheControl() {
        return blobCacheControl;
    }

    /**
     * Sets the optional HTTP cache control value for the blob content. The Blob
     * service stores this value but does not use or modify it. This value will
     * be returned to clients in the headers of the response when the blob data
     * or blob properties are requested.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link CommitBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param blobCacheControl
     *            A {@link String} containing the HTTP cache control value to
     *            set.
     * @return A reference to this {@link CommitBlobBlocksOptions} instance.
     */
    public CommitBlobBlocksOptions setBlobCacheControl(String blobCacheControl) {
        this.blobCacheControl = blobCacheControl;
        return this;
    }

    /**
     * Gets the blob metadata collection set in this
     * {@link CommitBlobBlocksOptions} instance.
     * 
     * @return A {@link HashMap} of name-value pairs of {@link String}
     *         containing the blob metadata set, if any.
     */
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Sets the blob metadata collection to associate with the created blob.
     * Metadata is a collection of name-value {@link String} pairs for client
     * use and is opaque to the server. Metadata names must adhere to the naming
     * rules for <a
     * href="http://msdn.microsoft.com/en-us/library/aa664670(VS.71).aspx">C#
     * identifiers</a>.
     * <p>
     * The <em>metadata</em> value only affects calls made on methods where this
     * {@link CommitBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param metadata
     *            A {@link HashMap} of name-value pairs of {@link String}
     *            containing the blob metadata to set.
     * @return A reference to this {@link CommitBlobBlocksOptions} instance.
     */
    public CommitBlobBlocksOptions setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Adds a name-value pair to the blob metadata collection associated with
     * this {@link CommitBlobBlocksOptions} instance.
     * 
     * @param key
     *            A {@link String} containing the name portion of the name-value
     *            pair to add to the metadata collection.
     * @param value
     *            A {@link String} containing the value portion of the
     *            name-value pair to add to the metadata collection.
     * @return A reference to this {@link CommitBlobBlocksOptions} instance.
     */
    public CommitBlobBlocksOptions addMetadata(String key, String value) {
        this.getMetadata().put(key, value);
        return this;
    }

    /**
     * Gets the lease ID for the blob set in this
     * {@link CommitBlobBlocksOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets a lease ID value to match when updating the blob. If the blob has a
     * lease, this parameter must be set with the matching leaseId value for the
     * commit block blobs operation to succeed.
     * <p>
     * The <em>leaseId</em> value only affects calls made on methods where this
     * {@link CommitBlobBlocksOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link CommitBlobBlocksOptions} instance.
     */
    public CommitBlobBlocksOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the access conditions set in this {@link CommitBlobBlocksOptions}
     * instance.
     * 
     * @return An {@link AccessCondition} containing the access conditions set,
     *         if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets the access conditions for updating a blob. By default, the commit
     * block blobs operation will set the container metadata unconditionally.
     * Use this method to specify conditions on the ETag or last modified time
     * value for performing the commit block blobs operation.
     * <p>
     * The <em>accessCondition</em> value only affects calls made on methods
     * where this {@link CommitBlobBlocksOptions} instance is passed as a
     * parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the access conditions to
     *            set.
     * @return A reference to this {@link CommitBlobBlocksOptions} instance.
     */
    public CommitBlobBlocksOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
