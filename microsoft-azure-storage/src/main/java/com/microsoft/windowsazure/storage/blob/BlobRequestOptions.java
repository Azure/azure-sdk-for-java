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

package com.microsoft.windowsazure.storage.blob;

import com.microsoft.windowsazure.storage.Constants;
import com.microsoft.windowsazure.storage.RequestOptions;
import com.microsoft.windowsazure.storage.core.SR;
import com.microsoft.windowsazure.storage.core.Utility;

/**
 * Represents a set of options that may be specified on a request.
 */
public final class BlobRequestOptions extends RequestOptions {

    /**
     * Represents the concurrent number of simultaneous requests per operation. If it's null, it will be set to the
     * value specified by the cloud blob client's {@link CloudBlobClient#getConcurrentRequestCount} method during upload
     * operations.
     */
    private Integer concurrentRequestCount = null;

    /**
     * Specifies whether a range PUT or GET operation will use the Content-MD5 header to enforce transactional security.
     * All partial blob uploads or downloads will be restricted to 4 MB. The default value is <code>false</code>.
     */
    private Boolean useTransactionalContentMD5 = null;

    /**
     * Specifies whether the blob's ContentMD5 header should be set on uploads. This field is not supported for page
     * blobs. The default value is <code>false</code>.
     */
    private Boolean storeBlobContentMD5 = null;

    /**
     * Specifies whether download and {@link BlobInputStream} methods should ignore the blob's ContentMD5 header. The
     * default value is <code>false</code>.
     */
    private Boolean disableContentMD5Validation = null;

    /**
     * Holds the maximum size of a blob in bytes that may be uploaded as a single blob.
     */
    private Integer singleBlobPutThresholdInBytes = null;

    /**
     * Creates an instance of the <code>BlobRequestOptions</code> class.
     */
    public BlobRequestOptions() {
        // Empty Default Ctor
    }

    /**
     * Creates an instance of the <code>BlobRequestOptions</code> class by copying values from another
     * <code>BlobRequestOptions</code> instance.
     * 
     * @param other
     *            A <code>BlobRequestOptions</code> object that represents the blob request options to copy.
     */
    public BlobRequestOptions(final BlobRequestOptions other) {
        super(other);
        if (other != null) {
            this.setConcurrentRequestCount(other.getConcurrentRequestCount());
            this.setUseTransactionalContentMD5(other.getUseTransactionalContentMD5());
            this.setStoreBlobContentMD5(other.getStoreBlobContentMD5());
            this.setDisableContentMD5Validation(other.getDisableContentMD5Validation());
            this.setSingleBlobPutThresholdInBytes(other.getSingleBlobPutThresholdInBytes());
        }
    }

    /**
     * Uses the concurrent request count from the specified client if <code>null</code>, sets a default value for
     * everything else, and sets defaults as defined in the parent class.
     * 
     * @param options
     *            The input options to copy from when applying defaults
     * @param blobType
     *            BlobType of the current operation
     * @param client
     *            A {@link CloudBlobClient} object that represents the service client used to set the default timeout
     *            interval and retry policy, if they are <code>null</code>. Additionally, if the
     *            {@link #concurrentRequestCount} field's value is null, it will be set to the value specified by the
     *            cloud blob client's {@link CloudBlobClient#getConcurrentRequestCount} method.
     */
    protected static final BlobRequestOptions applyDefaults(final BlobRequestOptions options, final BlobType blobType,
            final CloudBlobClient client) {
        BlobRequestOptions modifiedOptions = new BlobRequestOptions(options);
        return BlobRequestOptions.applyDefaultsInternal(modifiedOptions, blobType, client);
    }

    protected static final BlobRequestOptions applyDefaultsInternal(final BlobRequestOptions modifiedOptions,
            final BlobType blobtype, final CloudBlobClient client) {
        Utility.assertNotNull("modifiedOptions", modifiedOptions);
        RequestOptions.applyBaseDefaultsInternal(modifiedOptions, client);

        if (modifiedOptions.getConcurrentRequestCount() == null) {
            modifiedOptions.setConcurrentRequestCount(client.getConcurrentRequestCount());
        }

        if (modifiedOptions.getUseTransactionalContentMD5() == null) {
            modifiedOptions.setUseTransactionalContentMD5(false);
        }

        if (modifiedOptions.getStoreBlobContentMD5() == null) {
            modifiedOptions.setStoreBlobContentMD5(blobtype == BlobType.BLOCK_BLOB);
        }

        if (modifiedOptions.getDisableContentMD5Validation() == null) {
            modifiedOptions.setDisableContentMD5Validation(false);
        }

        if (modifiedOptions.getSingleBlobPutThresholdInBytes() == null) {
            modifiedOptions.setSingleBlobPutThresholdInBytes(client.getSingleBlobPutThresholdInBytes());
        }

        return modifiedOptions;
    }

    /**
     * @return the concurrentRequestCount
     */
    public Integer getConcurrentRequestCount() {
        return this.concurrentRequestCount;
    }

    /**
     * @return the useTransactionalContentMD5
     */
    public Boolean getUseTransactionalContentMD5() {
        return this.useTransactionalContentMD5;
    }

    /**
     * @return the storeBlobContentMD5
     */
    public Boolean getStoreBlobContentMD5() {
        return this.storeBlobContentMD5;
    }

    /**
     * @return the disableContentMD5Validation
     */
    protected Boolean getDisableContentMD5Validation() {
        return this.disableContentMD5Validation;
    }

    /**
     * Returns the threshold size used for writing a single blob.
     * 
     * @return The maximum size, in bytes, of a blob that may be uploaded as a single blob, ranging from 1 to 64 MB
     *         inclusive. The default value is 32 MBs.
     *         <p>
     *         If a blob size is above the threshold, it will be uploaded as blocks.
     */
    public Integer getSingleBlobPutThresholdInBytes() {
        return this.singleBlobPutThresholdInBytes;
    }

    /**
     * @param concurrentRequestCount
     *            the concurrentRequestCount to set
     */
    public void setConcurrentRequestCount(final Integer concurrentRequestCount) {
        this.concurrentRequestCount = concurrentRequestCount;
    }

    /**
     * @param useTransactionalContentMD5
     *            the useTransactionalContentMD5 to set
     */
    public void setUseTransactionalContentMD5(final Boolean useTransactionalContentMD5) {
        this.useTransactionalContentMD5 = useTransactionalContentMD5;
    }

    /**
     * @param storeBlobContentMD5
     *            the storeBlobContentMD5 to set
     */
    public void setStoreBlobContentMD5(final Boolean storeBlobContentMD5) {
        this.storeBlobContentMD5 = storeBlobContentMD5;
    }

    /**
     * @param disableContentMD5Validation
     *            the disableContentMD5Validation to set
     */
    public void setDisableContentMD5Validation(final Boolean disableContentMD5Validation) {
        this.disableContentMD5Validation = disableContentMD5Validation;
    }

    /**
     * Sets the threshold size used for writing a single blob to use.
     * 
     * @param singleBlobPutThresholdInBytes
     *            The maximum size, in bytes, of a blob that may be uploaded as a single blob, ranging from 1 MB to 64
     *            MB inclusive. If a blob size is above the threshold, it will be uploaded as blocks.
     * 
     * @throws IllegalArgumentException
     *             If <code>minimumReadSize</code> is less than 1 MB or greater than 64 MB.
     */
    public void setSingleBlobPutThresholdInBytes(final Integer singleBlobPutThresholdInBytes) {
        if (singleBlobPutThresholdInBytes != null
                && (singleBlobPutThresholdInBytes > BlobConstants.MAX_SINGLE_UPLOAD_BLOB_SIZE_IN_BYTES || singleBlobPutThresholdInBytes < 1 * Constants.MB)) {
            throw new IllegalArgumentException(String.format(Utility.LOCALE_US, SR.ARGUMENT_OUT_OF_RANGE_ERROR,
                    "singleBlobPutThresholdInBytes", singleBlobPutThresholdInBytes.toString()));
        }

        this.singleBlobPutThresholdInBytes = singleBlobPutThresholdInBytes;
    }
}
