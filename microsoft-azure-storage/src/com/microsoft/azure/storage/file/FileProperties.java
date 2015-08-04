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
 * Represents the system properties for a file.
 */
public final class FileProperties {
    /**
     * Represents the cache-control value stored for the file.
     */
    private String cacheControl;

    /**
     * Represents the content-disposition value stored for the file. If this field has not been set for the file, the
     * field returns <code>null</code>.
     */
    private String contentDisposition;

    /**
     * Represents the content-encoding value stored for the file. If this field has not been set for the file, the field
     * returns <code>null</code>.
     */
    private String contentEncoding;

    /**
     * Represents the content-language value stored for the file. If this field has not been set for the file, the field
     * returns <code>null</code>.
     */
    private String contentLanguage;

    /**
     * Represents the content MD5 value stored for the file.
     */
    private String contentMD5;

    /**
     * Represents the content type value stored for the file. If this field has not been set for the file, the field
     * returns <code>null</code>.
     */
    private String contentType;
    
    /**
     * Represents the state of the most recent or pending copy operation.
     */
    private CopyState copyState;

    /**
     * Represents the size, in bytes, of the file.
     */
    private long length;

    /**
     * Represents the file's ETag value.
     */
    private String etag;

    /**
     * Represents the last-modified time for the file.
     */
    private Date lastModified;

    /**
     * Creates an instance of the <code>FileProperties</code> class.
     */
    public FileProperties() {
        // No op
    }

    /**
     * Creates an instance of the <code>FileProperties</code> class by copying values from another instance of the
     * <code>FileProperties</code> class.
     * 
     * @param other
     *            A <code>FileProperties</code> object which represents the file properties to copy.
     */
    public FileProperties(final FileProperties other) {
        this.cacheControl = other.cacheControl;
        this.contentDisposition = other.contentDisposition;
        this.contentEncoding = other.contentEncoding;
        this.contentLanguage = other.contentLanguage;
        this.contentMD5 = other.contentMD5;
        this.contentType = other.contentType;
        this.etag = other.etag;
        this.length = other.length;
        this.lastModified = other.lastModified;
    }

    /**
     * Gets the cache control value for the file.
     * 
     * @return A <code>String</code> which represents the content cache control value for the file.
     */
    public String getCacheControl() {
        return this.cacheControl;
    }

    /**
     * Gets the content disposition value for the file.
     * 
     * @return A <code>String</code> which represents the content disposition, or <code>null</code> if content
     *         disposition has not been set on the file.
     */
    public String getContentDisposition() {
        return this.contentDisposition;
    }

    /**
     * Gets the content encoding value for the file.
     * 
     * @return A <code>String</code> which represents the content encoding, or <code>null</code> if content encoding has
     *         not been set on the file.
     */
    public String getContentEncoding() {
        return this.contentEncoding;
    }

    /**
     * Gets the content language value for the file.
     * 
     * @return A <code>String</code> which represents the content language, or <code>null</code> if content language
     *         has not been set on the file.
     */
    public String getContentLanguage() {
        return this.contentLanguage;
    }

    /**
     * Gets the content MD5 value for the file.
     * 
     * @return A <code>String</code> which represents the content MD5 value.
     */
    public String getContentMD5() {
        return this.contentMD5;
    }

    /**
     * Gets the content type value for the file.
     * 
     * @return A <code>String</code> which represents the content type, or <code>null</code> if the content type has not
     *         be set for the file.
     */
    public String getContentType() {
        return this.contentType;
    }
    
    /**
     * Gets the file's copy state.
     * 
     * @return A {@link CopyState} object which represents the copy state of the file.
     */
    public CopyState getCopyState() {
        return this.copyState;
    }
    
    /**
     * Gets the ETag value for the file.
     * <p>
     * The ETag value is a unique identifier that is updated when a write operation is performed against the file. It
     * may be used to perform operations conditionally, providing concurrency control and improved efficiency.
     * <p>
     * The {@link AccessCondition#generateIfMatchCondition(String)} and
     * {@link AccessCondition#generateIfNoneMatchCondition(String)} methods take an ETag value and return an
     * {@link AccessCondition} object that may be specified on the request.
     * 
     * @return A <code>String</code> which represents the ETag value.
     */
    public String getEtag() {
        return this.etag;
    }

    /**
     * Gets the last modified time for the file.
     * 
     * @return A {@link java.util.Date} object which represents the last modified time.
     */
    public Date getLastModified() {
        return this.lastModified;
    }

    /**
     * Gets the size, in bytes, of the file.
     * 
     * @return A <code>long</code> which represents the length of the file.
     */
    public long getLength() {
        return this.length;
    }

    /**
     * Sets the cache control value for the file.
     * 
     * @param cacheControl
     *            A <code>String</code> which specifies the cache control value to set.
     */
    public void setCacheControl(final String cacheControl) {
        this.cacheControl = cacheControl;
    }

    /**
     * Sets the content disposition value for the file.
     * 
     * @param contentDisposition
     *            A <code>String</code> which specifies the content disposition value to set.
     */
    public void setContentDisposition(final String contentDisposition) {
        this.contentDisposition = contentDisposition;
    }

    /**
     * Sets the content encoding value for the file.
     * 
     * @param contentEncoding
     *            A <code>String</code> which specifies the content encoding value to set.
     */
    public void setContentEncoding(final String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    /**
     * Sets the content language for the file.
     * 
     * @param contentLanguage
     *            A <code>String</code> which specifies the content language value to set.
     */
    public void setContentLanguage(final String contentLanguage) {
        this.contentLanguage = contentLanguage;
    }

    /**
     * Sets the content MD5 value for the file.
     * 
     * @param contentMD5
     *            A <code>String</code> which specifies the content MD5 value to set.
     */
    public void setContentMD5(final String contentMD5) {
        this.contentMD5 = contentMD5;
    }

    /**
     * Sets the content type value for the file.
     * 
     * @param contentType
     *            A <code>String</code> which specifies the content type value to set.
     */
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }
    
    /**
     * Sets the copy state value for the file.
     * 
     * @param copyState
     *        A {@link CopyState} object which specifies the copy state value to set.
     */
    protected void setCopyState(final CopyState copyState) {
        this.copyState = copyState;
    }

    /**
     * Sets the ETag value for the file.
     * 
     * @param etag
     *            A <code>String</code> which specifies the ETag value to set.
     */
    protected void setEtag(final String etag) {
        this.etag = etag;
    }

    /**
     * Sets the last modified time for the file.
     * 
     * @param lastModified
     *            A {@link java.util.Date} object which specifies the last modified time to set.
     */
    protected void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Sets the content length, in bytes, for the file.
     * 
     * @param length
     *            A <code>long</code> which specifies the length to set.
     */
    protected void setLength(final long length) {
        this.length = length;
    }

}
