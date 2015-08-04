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

import com.microsoft.azure.storage.Constants;
import com.microsoft.azure.storage.RequestOptions;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a set of options that may be specified on a request.
 */
public final class BlobRequestOptions extends RequestOptions {

    /**
     * Indicates whether a conditional failure should be absorbed on a retry attempt for the request. This option 
     * is only used by {@link CloudAppendBlob} in upload and openWrite methods. By default, it is set to 
     * <code>false</code>. Set this to <code>true</code> only for single writer scenario.
     */
    private Boolean absorbConditionalErrorsOnRetry = null;
    
    /**
     * Represents the concurrent number of simultaneous requests per operation. The default value is 1.
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
        // Empty Default Constructor.
    }

    /**
     * Creates an instance of the <code>BlobRequestOptions</code> class by copying values from another
     * <code>BlobRequestOptions</code> instance.
     * 
     * @param other
     *            A {@link BlobRequestOptions} object which represents the blob request options to copy.
     */
    public BlobRequestOptions(final BlobRequestOptions other) {
        super(other);
        if (other != null) {
            this.setAbsorbConditionalErrorsOnRetry(other.getAbsorbConditionalErrorsOnRetry());
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
    protected static final BlobRequestOptions populateAndApplyDefaults(final BlobRequestOptions options,
            final BlobType blobType, final CloudBlobClient client) {
        return BlobRequestOptions.populateAndApplyDefaults(options, blobType, client, true);
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
     * @param setStartTime
     *            whether to initialize the startTimeInMs field, or not
     */
    protected static final BlobRequestOptions populateAndApplyDefaults(final BlobRequestOptions options,
            final BlobType blobType, final CloudBlobClient client, final boolean setStartTime) {
        BlobRequestOptions modifiedOptions = new BlobRequestOptions(options);
        BlobRequestOptions.populateRequestOptions(modifiedOptions, client.getDefaultRequestOptions(), setStartTime);
        BlobRequestOptions.applyDefaults(modifiedOptions, blobType);
        return modifiedOptions;
    }

    /**
     * Applies defaults to the options passed in.
     * 
     * @param modifiedOptions
     *          The options to apply defaults to.
     */
    protected static void applyDefaults(final BlobRequestOptions modifiedOptions, final BlobType blobtype) {
        Utility.assertNotNull("modifiedOptions", modifiedOptions);
        RequestOptions.applyBaseDefaultsInternal(modifiedOptions);
        
        if (modifiedOptions.getAbsorbConditionalErrorsOnRetry() == null) {
            modifiedOptions.setAbsorbConditionalErrorsOnRetry(false);
        }
        
        if (blobtype == BlobType.APPEND_BLOB) {
            // Append blobs must be done in serial.
            modifiedOptions.setConcurrentRequestCount(1);
        } else if (modifiedOptions.getConcurrentRequestCount() == null) {
            modifiedOptions.setConcurrentRequestCount(BlobConstants.DEFAULT_CONCURRENT_REQUEST_COUNT);
        } 

        if (modifiedOptions.getSingleBlobPutThresholdInBytes() == null) {
            modifiedOptions.setSingleBlobPutThresholdInBytes(BlobConstants.DEFAULT_SINGLE_BLOB_PUT_THRESHOLD_IN_BYTES);
        }

        if (modifiedOptions.getUseTransactionalContentMD5() == null) {
            modifiedOptions.setUseTransactionalContentMD5(false);
        }

        if (modifiedOptions.getStoreBlobContentMD5() == null) {
            if (blobtype != BlobType.UNSPECIFIED) {
                modifiedOptions.setStoreBlobContentMD5(blobtype == BlobType.BLOCK_BLOB);   
            }
        }

        if (modifiedOptions.getDisableContentMD5Validation() == null) {
            modifiedOptions.setDisableContentMD5Validation(false);
        }
    }

    /**
     * Populates any null fields in the first requestOptions object with values from the second requestOptions object.
     */
    private static void populateRequestOptions(BlobRequestOptions modifiedOptions,
            final BlobRequestOptions clientOptions, final boolean setStartTime) {
        RequestOptions.populateRequestOptions(modifiedOptions, clientOptions, setStartTime);
        
        if (modifiedOptions.getAbsorbConditionalErrorsOnRetry() == null) {
            modifiedOptions.setAbsorbConditionalErrorsOnRetry(clientOptions.getAbsorbConditionalErrorsOnRetry());
        }
        
        if (modifiedOptions.getConcurrentRequestCount() == null) {
            modifiedOptions.setConcurrentRequestCount(clientOptions.getConcurrentRequestCount());
        }

        if (modifiedOptions.getSingleBlobPutThresholdInBytes() == null) {
            modifiedOptions.setSingleBlobPutThresholdInBytes(clientOptions.getSingleBlobPutThresholdInBytes());
        }

        if (modifiedOptions.getUseTransactionalContentMD5() == null) {
            modifiedOptions.setUseTransactionalContentMD5(clientOptions.getUseTransactionalContentMD5());
        }

        if (modifiedOptions.getStoreBlobContentMD5() == null) {
            modifiedOptions.setStoreBlobContentMD5(clientOptions.getStoreBlobContentMD5());
        }

        if (modifiedOptions.getDisableContentMD5Validation() == null) {
            modifiedOptions.setDisableContentMD5Validation(clientOptions.getDisableContentMD5Validation());
        }
    }

    /**
     * Indicates whether a conditional failure should be absorbed on a retry attempt for the request. For more 
     * information about absorb conditinal errors on retry defaults, see {@link #setAbsorbConditionalErrorsOnRetry(Boolean)}.
     * 
     * @return the absorbConditionalErrorsOnRetry
     */
    public Boolean getAbsorbConditionalErrorsOnRetry() {
        return this.absorbConditionalErrorsOnRetry;
    }
    
    /**
     * Gets the concurrent number of simultaneous requests per operation. For more information about concurrent request
     * count defaults, see {@link #setConcurrentRequestCount(Integer)}.
     * 
     * @return the concurrentRequestCount
     */
    public Integer getConcurrentRequestCount() {
        return this.concurrentRequestCount;
    }

    /**
     * Gets whether a range PUT or GET operation will use the Content-MD5 header to enforce transactional security.
     * All partial blob uploads or downloads will be restricted to 4 MB. For more information about transactional
     * content MD5 defaults, see {@link #setUseTransactionalContentMD5(Boolean)}.
     * 
     * @return the useTransactionalContentMD5
     */
    public Boolean getUseTransactionalContentMD5() {
        return this.useTransactionalContentMD5;
    }

    /**
     * Gets whether the blob's ContentMD5 header should be set on uploads. This field is not supported for page
     * blobs. For more information about storing blob content MD5 defaults, see {@link #setStoreBlobContentMD5(Boolean)}
     * .
     * 
     * @return the storeBlobContentMD5
     */
    public Boolean getStoreBlobContentMD5() {
        return this.storeBlobContentMD5;
    }

    /**
     * Gets whether download and {@link BlobInputStream} methods should ignore the blob's ContentMD5 header. For more
     * information about disabling content MD5 validation defaults, see {@link #setDisableContentMD5Validation(Boolean)}
     * .
     * 
     * @return the disableContentMD5Validation
     */
    public Boolean getDisableContentMD5Validation() {
        return this.disableContentMD5Validation;
    }

    /**
     * Gets the threshold size used for writing a single blob. For more information about the threshold size defaults,
     * see {@link #setSingleBlobPutThresholdInBytes(Integer)}.
     * 
     * @return The maximum size, in bytes, of a blob that may be uploaded as a single blob, ranging from 1 to 64 MB
     *         inclusive. If a blob size is above the threshold, it will be uploaded as blocks.
     */
    public Integer getSingleBlobPutThresholdInBytes() {
        return this.singleBlobPutThresholdInBytes;
    }

    /**
     * Sets whether a conditional failure should be absorbed on a retry attempt for the request. This option 
     * is only used by {@link CloudAppendBlob} in upload and openWrite methods. By default, it is set to 
     * <code>false</code>. Set this to <code>true</code> only for single writer scenario.
     * <p>
     * You can change the absorbConditionalErrorsOnRetry value on this request by setting this property. You can also 
     * change the value on the {@link CloudBlobClient#getDefaultRequestOptions()} object so that all subsequent requests 
     * made via the service client will use that absorbConditionalErrorsOnRetry value.
     * 
     * @param absorbConditionalErrorsOnRetry
     *            the absorbConditionalErrorsOnRetry to set
     */
    public void setAbsorbConditionalErrorsOnRetry(final Boolean absorbConditionalErrorsOnRetry) {
        this.absorbConditionalErrorsOnRetry = absorbConditionalErrorsOnRetry;
    }
    
    /**
     * Sets the concurrent number of simultaneous requests per operation.
     * <p>
     * The default concurrent request count is set in the client and is by default 1, indicating no concurrency. You can
     * change the concurrent request count on this request by setting this property. You can also change the value on
     * the {@link CloudBlobClient#getDefaultRequestOptions()} object so that all subsequent requests made via the
     * service client will use that concurrent request count.
     * 
     * @param concurrentRequestCount
     *            the concurrentRequestCount to set
     */
    public void setConcurrentRequestCount(final Integer concurrentRequestCount) {
        this.concurrentRequestCount = concurrentRequestCount;
    }

    /**
     * Sets whether a range PUT or GET operation will use the Content-MD5 header to enforce transactional security.
     * All partial blob uploads or downloads will be restricted to 4 MB.
     * <p>
     * The default useTransactionalContentMD5 value is set in the client and is by default <code>false</code>. You can
     * change the useTransactionalContentMD5 value on this request by setting this property. You can also change the
     * value on the {@link CloudBlobClient#getDefaultRequestOptions()} object so that all subsequent requests made via
     * the service client will use that useTransactionalContentMD5 value.
     * 
     * @param useTransactionalContentMD5
     *            the useTransactionalContentMD5 to set
     */
    public void setUseTransactionalContentMD5(final Boolean useTransactionalContentMD5) {
        this.useTransactionalContentMD5 = useTransactionalContentMD5;
    }

    /**
     * Sets whether the blob's ContentMD5 header should be set on uploads. This field is not supported for page
     * blobs.
     * <p>
     * The default storeBlobContentMD5 value is set in the client and is by default <code>true</code> for block blobs.
     * You can change the storeBlobContentMD5 value on this request by setting this property. You can also change the
     * value on the {@link CloudBlobClient#getDefaultRequestOptions()} object so that all subsequent requests made via
     * the service client will use that storeBlobContentMD5 value.
     * 
     * @param storeBlobContentMD5
     *            the storeBlobContentMD5 to set
     */
    public void setStoreBlobContentMD5(final Boolean storeBlobContentMD5) {
        this.storeBlobContentMD5 = storeBlobContentMD5;
    }

    /**
     * Sets whether download and {@link BlobInputStream} methods should ignore the blob's ContentMD5 header.
     * <p>
     * The default disableContentMD5Validation value is set in the client and is by default <code>false</code>. You can
     * change the disableContentMD5Validation value on this request by setting this property. You can also change the
     * value on the {@link CloudBlobClient#getDefaultRequestOptions()} object so that all subsequent requests made via
     * the service client will use that disableContentMD5Validation value.
     * 
     * @param disableContentMD5Validation
     *            the disableContentMD5Validation to set
     */
    public void setDisableContentMD5Validation(final Boolean disableContentMD5Validation) {
        this.disableContentMD5Validation = disableContentMD5Validation;
    }

    /**
     * Sets the threshold size used for writing a single blob to use.
     * <p>
     * The default threshold size is set in the client and is by default 32MB. You can change the threshold size on this
     * request by setting this property. You can also change the value on the
     * {@link CloudBlobClient#getDefaultRequestOptions()} object so that all subsequent requests made via the service
     * client will use that threshold size.
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
