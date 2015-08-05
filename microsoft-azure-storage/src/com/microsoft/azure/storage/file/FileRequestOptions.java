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

import java.io.FileInputStream;

import com.microsoft.azure.storage.LocationMode;
import com.microsoft.azure.storage.RequestOptions;
import com.microsoft.azure.storage.ServiceClient;
import com.microsoft.azure.storage.core.SR;
import com.microsoft.azure.storage.core.Utility;

/**
 * Represents a set of options that may be specified on a request.
 */
public final class FileRequestOptions extends RequestOptions {

    /**
     * Represents the concurrent number of simultaneous requests per operation. If it's null, it will be set to the
     * value specified by the cloud file client's {@link CloudFileClient#getConcurrentRequestCount} method during upload
     * operations.
     */
    private Integer concurrentRequestCount = null;

    /**
     * Specifies whether a range PUT or GET operation will use the Content-MD5 header to enforce transactional security.
     * All partial file uploads or downloads will be restricted to 4 MB. The default value is <code>false</code>.
     */
    private Boolean useTransactionalContentMD5 = null;

    /**
     * Specifies whether the file's ContentMD5 header should be set on uploads.
     * The default value is <code>false</code>.
     */
    private Boolean storeFileContentMD5 = null;

    /**
     * Specifies whether download and {@link FileInputStream} methods should ignore the file's ContentMD5 header. The
     * default value is <code>false</code>.
     */
    private Boolean disableContentMD5Validation = null;

    /**
     * Creates an instance of the <code>FileRequestOptions</code> class.
     */
    public FileRequestOptions() {
        // Empty Default Constructor.
    }

    /**
     * Creates an instance of the <code>FileRequestOptions</code> class by copying values from another
     * <code>FileRequestOptions</code> instance.
     * 
     * @param other
     *            A {@link FileRequestOptions} object which represents the file request options to copy.
     */
    public FileRequestOptions(final FileRequestOptions other) {
        super(other);
        if (other != null) {
            this.setConcurrentRequestCount(other.getConcurrentRequestCount());
            this.setDisableContentMD5Validation(other.getDisableContentMD5Validation());
            this.setStoreFileContentMD5(other.getStoreFileContentMD5());
            this.setUseTransactionalContentMD5(other.getUseTransactionalContentMD5());
        }
    }

    /**
     * Uses the concurrent request count from the specified client if <code>null</code>, sets a default value for
     * everything else, and sets defaults as defined in the parent class.
     * 
     * @param options
     *            The input options to copy from when applying defaults
     * @param client
     *            A {@link CloudFileClient} object that represents the service client used to set the default timeout
     *            interval and retry policy, if they are <code>null</code>. Additionally, if the
     *            {@link #concurrentRequestCount} field's value is null, it will be set to the value specified by the
     *            cloud file client's {@link CloudFileClient#getConcurrentRequestCount} method.
     */
    protected static final FileRequestOptions populateAndApplyDefaults(final FileRequestOptions options,
            final CloudFileClient client) {
        return FileRequestOptions.populateAndApplyDefaults(options, client, true);
    }
    
    /**
     * Uses the concurrent request count from the specified client if <code>null</code>, sets a default value for
     * everything else, and sets defaults as defined in the parent class.
     * 
     * @param options
     *            The input options to copy from when applying defaults
     * @param client
     *            A {@link CloudFileClient} object that represents the service client used to set the default timeout
     *            interval and retry policy, if they are <code>null</code>. Additionally, if the
     *            {@link #concurrentRequestCount} field's value is null, it will be set to the value specified by the
     *            cloud blob client's {@link CloudFileClient#getConcurrentRequestCount} method.
     * @param setStartTime
     *            whether to initialize the startTimeInMs field, or not
     */
    protected static final FileRequestOptions populateAndApplyDefaults(final FileRequestOptions options, 
            final CloudFileClient client, final boolean setStartTime) {
        FileRequestOptions modifiedOptions = new FileRequestOptions(options);
        FileRequestOptions.populateRequestOptions(modifiedOptions, client.getDefaultRequestOptions(), setStartTime);
        FileRequestOptions.applyDefaults(modifiedOptions);
        return modifiedOptions;
    }

    /**
     * Applies defaults to the options passed in.
     * 
     * @param modifiedOptions
     *          The options to apply defaults to.
     */
    protected static void applyDefaults(final FileRequestOptions modifiedOptions) {
        Utility.assertNotNull("modifiedOptions", modifiedOptions);
        RequestOptions.applyBaseDefaultsInternal(modifiedOptions);
        if (modifiedOptions.getConcurrentRequestCount() == null) {
            modifiedOptions.setConcurrentRequestCount(FileConstants.DEFAULT_CONCURRENT_REQUEST_COUNT);
        }

        if (modifiedOptions.getUseTransactionalContentMD5() == null) {
            modifiedOptions.setUseTransactionalContentMD5(false);
        }

        if (modifiedOptions.getStoreFileContentMD5() == null) {
            modifiedOptions.setStoreFileContentMD5(false);
        }

        if (modifiedOptions.getDisableContentMD5Validation() == null) {
            modifiedOptions.setDisableContentMD5Validation(false);
        }
    }

    /**
     * Populates any null fields in the first requestOptions object with values from the second requestOptions object.
     */
    private static void populateRequestOptions(FileRequestOptions modifiedOptions,
            final FileRequestOptions clientOptions, boolean setStartTime) {
        RequestOptions.populateRequestOptions(modifiedOptions, clientOptions, setStartTime);
        if (modifiedOptions.getConcurrentRequestCount() == null) {
            modifiedOptions.setConcurrentRequestCount(clientOptions.getConcurrentRequestCount());
        }
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
     * All partial file uploads or downloads will be restricted to 4 MB. For more information about transactional
     * content MD5 defaults, see {@link #setUseTransactionalContentMD5(Boolean)}.
     * 
     * @return the useTransactionalContentMD5
     */
    public Boolean getUseTransactionalContentMD5() {
        return this.useTransactionalContentMD5;
    }

    /**
     * Gets whether the file's ContentMD5 header should be set on uploads. For more information about storing file
     * content MD5 defaults, see {@link #setStoreFileContentMD5(Boolean)} .
     * 
     * @return the storeFileContentMD5
     */
    public Boolean getStoreFileContentMD5() {
        return this.storeFileContentMD5;
    }

    /**
     * Gets whether download and {@link FileInputStream} methods should ignore the file's ContentMD5 header. For more
     * information about disabling content MD5 validation defaults, see {@link #setDisableContentMD5Validation(Boolean)}
     * .
     * 
     * @return the disableContentMD5Validation
     */
    public Boolean getDisableContentMD5Validation() {
        return this.disableContentMD5Validation;
    }

    /**
     * Sets the concurrent number of simultaneous requests per operation.
     * <p>
     * The default concurrent request count is set in the client and is by default 1, indicating no concurrency. You can
     * change the concurrent request count on this request by setting this property. You can also change the value on
     * the {@link CloudFileClient#getDefaultRequestOptions()} object so that all subsequent requests made via the
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
     * <p>
     * The default useTransactionalContentMD5 value is set in the client and is by default <code>false</code>. You can
     * change the useTransactionalContentMD5 value on this request by setting this property. You can also change the
     * value on the {@link CloudFileClient#getDefaultRequestOptions()} object so that all subsequent requests made via
     * the service client will use that useTransactionalContentMD5 value.
     * 
     * @param useTransactionalContentMD5
     *            the useTransactionalContentMD5 to set
     */
    public void setUseTransactionalContentMD5(final Boolean useTransactionalContentMD5) {
        this.useTransactionalContentMD5 = useTransactionalContentMD5;
    }

    /**
     * Sets whether the file's ContentMD5 header should be set on uploads.
     * <p>
     * The default storeFileContentMD5 value is set in the client and is by default <code>true</code>. You can change
     * the storeFileContentMD5 value on this request by setting this property. You can also change the value on the
     * {@link CloudFileClient#getDefaultRequestOptions()} object so that all subsequent requests made via the service
     * client will use that storeFileContentMD5 value.
     * 
     * @param storeFileContentMD5
     *            the storeFileContentMD5 to set
     */
    public void setStoreFileContentMD5(final Boolean storeFileContentMD5) {
        this.storeFileContentMD5 = storeFileContentMD5;
    }

    /**
     * Sets whether download and {@link FileInputStream} methods should ignore the file's ContentMD5 header.
     * <p>
     * The default disableContentMD5Validation value is set in the client and is by default <code>false</code>. You can
     * change the disableContentMD5Validation value on this request by setting this property. You can also change the
     * value on the {@link CloudFileClient#getDefaultRequestOptions()} object so that all subsequent requests made via
     * the service client will use that disableContentMD5Validation value.
     * 
     * @param disableContentMD5Validation
     *            the disableContentMD5Validation to set
     */
    public void setDisableContentMD5Validation(final Boolean disableContentMD5Validation) {
        this.disableContentMD5Validation = disableContentMD5Validation;
    }

    /**
     * Sets the {@link LocationMode} for this request.
     * <p>
     * The default {@link LocationMode} is set in the client and is by default {@link LocationMode#PRIMARY_ONLY}. You
     * can change the {@link LocationMode} on this request by setting this property. You can also change the value on
     * the {@link ServiceClient#getDefaultRequestOptions()} object so that all subsequent requests made via the service
     * client will use that {@link LocationMode}.
     * 
     * @param locationMode
     *            the locationMode to set
     */
    @Override
    public final void setLocationMode(final LocationMode locationMode) {
        if (locationMode != null && !locationMode.equals(LocationMode.PRIMARY_ONLY)) {
            throw new UnsupportedOperationException(SR.PRIMARY_ONLY_COMMAND);
        }

        super.setLocationMode(locationMode);
    }
}
