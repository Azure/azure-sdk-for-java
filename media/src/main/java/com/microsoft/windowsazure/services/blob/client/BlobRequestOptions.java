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
/**
 * 
 */
package com.microsoft.windowsazure.services.blob.client;

import com.microsoft.windowsazure.services.core.storage.RequestOptions;

/**
 * Represents a set of options that may be specified on a request.
 */
public final class BlobRequestOptions extends RequestOptions
{

    /**
     * Represents the concurrent number of simultaneous requests per operation.
     * If it's null, it will be set to the value specified by the cloud blob
     * client's {@link CloudBlobClient#getConcurrentRequestCount} method during
     * upload operations.
     */
    private Integer concurrentRequestCount = null;

    /**
     * Specifies whether a range PUT or GET operation will use the Content-MD5
     * header to enforce transactional security. All partial blob uploads or
     * downloads will be restricted to 4 MB. The default value is
     * <code>false</code>.
     */
    private Boolean useTransactionalContentMD5 = false;

    /**
     * Specifies whether the blob's ContentMD5 header should be set on uploads.
     * This field is not supported for page blobs. The default value is
     * <code>false</code>.
     */
    private Boolean storeBlobContentMD5 = false;

    /**
     * Specifies whether download and {@link BlobInputStream} methods should
     * ignore the blob's ContentMD5 header. The default value is
     * <code>false</code>.
     */
    private Boolean disableContentMD5Validation = false;

    /**
     * Specifies whether to use sparse page blobs.
     * 
     * When <code>true</code>, "zero" pages are not explicitly uploaded for a
     * page blob; additionally, GET page ranges are used only to download
     * explicit information for the page blob. The default value is
     * <code>false</code>.
     */
    private Boolean useSparsePageBlob = false;

    /**
     * Creates an instance of the <code>BlobRequestOptions</code> class.
     */
    public BlobRequestOptions()
    {
        // Empty Default Ctor
    }

    /**
     * Creates an instance of the <code>BlobRequestOptions</code> class by
     * copying values from another <code>BlobRequestOptions</code> instance.
     * 
     * @param other
     *            A <code>BlobRequestOptions</code> object that represents the
     *            blob request options to copy.
     */
    public BlobRequestOptions(final BlobRequestOptions other)
    {
        super(other);
        this.setConcurrentRequestCount(other.getConcurrentRequestCount());
        this.setStoreBlobContentMD5(other.getStoreBlobContentMD5());
        this.setUseTransactionalContentMD5(other
                .getUseTransactionalContentMD5());
        this.setUseSparsePageBlob(other.getUseSparsePageBlob());
        this.disableContentMD5Validation = other.disableContentMD5Validation;
    }

    /**
     * Uses the default timeout interval and retry policy from the specified
     * client if the timeout interval and retry policy are <code>null</code>,
     * and also sets the concurrent request count (whether or not it has already
     * been assigned a value).
     * 
     * @param client
     *            A {@link CloudBlobClient} object that represents the service
     *            client used to set the default timeout interval and retry
     *            policy, if they are <code>null</code>. Additionally, if the
     *            {@link #concurrentRequestCount} field's value is null, it will
     *            be set to the value specified by the cloud blob client's
     *            {@link CloudBlobClient#getConcurrentRequestCount} method.
     */
    protected void applyDefaults(final CloudBlobClient client)
    {
        super.applyBaseDefaults(client);

        if (this.getConcurrentRequestCount() == null)
        {
            this.setConcurrentRequestCount(client.getConcurrentRequestCount());
        }
    }

    /**
     * @return the concurrentRequestCount
     */
    public Integer getConcurrentRequestCount()
    {
        return this.concurrentRequestCount;
    }

    /**
     * @return the disableContentMD5Validation
     */
    protected boolean getDisableContentMD5Validation()
    {
        return this.disableContentMD5Validation;
    }

    /**
     * @return the storeBlobContentMD5
     */
    public boolean getStoreBlobContentMD5()
    {
        return this.storeBlobContentMD5;
    }

    /**
     * @return the useSparsePageBlob
     */
    public boolean getUseSparsePageBlob()
    {
        return this.useSparsePageBlob;
    }

    /**
     * @return the useTransactionalContentMD5
     */
    public boolean getUseTransactionalContentMD5()
    {
        return this.useTransactionalContentMD5;
    }

    /**
     * @param concurrentRequestCount
     *            the concurrentRequestCount to set
     */
    public void setConcurrentRequestCount(final Integer concurrentRequestCount)
    {
        this.concurrentRequestCount = concurrentRequestCount;
    }

    /**
     * @param disableContentMD5Validation
     *            the disableContentMD5Validation to set
     */
    public void setDisableContentMD5Validation(
            final boolean disableContentMD5Validation)
    {
        this.disableContentMD5Validation = disableContentMD5Validation;
    }

    /**
     * @param storeBlobContentMD5
     *            the storeBlobContentMD5 to set
     */
    public void setStoreBlobContentMD5(final boolean storeBlobContentMD5)
    {
        this.storeBlobContentMD5 = storeBlobContentMD5;
    }

    /**
     * @param useSparsePageBlob
     *            the useSparsePageBlob to set
     */
    public void setUseSparsePageBlob(final boolean useSparsePageBlob)
    {
        this.useSparsePageBlob = useSparsePageBlob;
    }

    /**
     * @param useTransactionalContentMD5
     *            the useTransactionalContentMD5 to set
     */
    public void setUseTransactionalContentMD5(
            final boolean useTransactionalContentMD5)
    {
        this.useTransactionalContentMD5 = useTransactionalContentMD5;
    }
}
