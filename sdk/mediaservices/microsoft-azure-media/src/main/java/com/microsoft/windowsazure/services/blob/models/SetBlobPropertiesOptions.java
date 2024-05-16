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
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#setBlobProperties(String, String, SetBlobPropertiesOptions)
 * setBlobProperties} request. These options include an optional server timeout
 * for the operation, the MIME content type and content encoding for the blob,
 * the content length, the content language, the MD5 hash, a cache control
 * value, a blob lease ID, a sequence number and sequence number action value,
 * and any access conditions for the operation.
 */
public class SetBlobPropertiesOptions extends BlobServiceOptions {
    private String leaseId;
    private String contentType;
    private Long contentLength;
    private String contentEncoding;
    private String contentLanguage;
    private String contentMD5;
    private String cacheControl;
    private String sequenceNumberAction;
    private Long sequenceNumber;
    private AccessConditionHeader accessCondition;

    /**
     * Sets the optional server request timeout value associated with this
     * {@link SetBlobPropertiesOptions} instance.
     * <p>
     * The <em>timeout</em> value only affects calls made on methods where this
     * {@link SetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param timeout
     *            The server request timeout value to set in milliseconds.
     * @return A reference to this {@link SetBlobPropertiesOptions} instance.
     */
    @Override
    public SetBlobPropertiesOptions setTimeout(Integer timeout) {
        super.setTimeout(timeout);
        return this;
    }

    /**
     * Gets the MIME content type value set in this
     * {@link SetBlobPropertiesOptions} instance.
     * 
     * @return A {@link String} containing the MIME content type value set, if
     *         any.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the optional MIME content type for the blob content. This value will
     * be returned to clients in the <code>Content-Type</code> header of the
     * response when the blob data or blob properties are requested. If no
     * content type is specified, the default content type is
     * <strong>application/octet-stream</strong>.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link SetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param contentType
     *            A {@link String} containing the MIME content type value to
     *            set.
     * @return A reference to this {@link SetBlobPropertiesOptions} instance.
     */
    public SetBlobPropertiesOptions setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets the new page blob size set in this {@link SetBlobPropertiesOptions}
     * instance.
     * 
     * @return The new size to set for a page blob.
     */
    public Long getContentLength() {
        return contentLength;
    }

    /**
     * Sets the size of a page blob to the specified size. If the specified
     * <em>contentLength</em> value is less than the current size of the blob,
     * then all pages above the specified value are cleared.
     * <p>
     * This property cannot be used to change the size of a block blob. Setting
     * this property for a block blob causes a {@link com.microsoft.windowsazure.exception.ServiceException} to be
     * thrown.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link SetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param contentLength
     *            The new size to set for a page blob.
     * @return A reference to this {@link SetBlobPropertiesOptions} instance.
     */
    public SetBlobPropertiesOptions setContentLength(Long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    /**
     * Gets the HTTP content encoding value set in this
     * {@link SetBlobPropertiesOptions} instance.
     * 
     * @return A {@link String} containing the HTTP content encoding value set,
     *         if any.
     */
    public String getContentEncoding() {
        return contentEncoding;
    }

    /**
     * Sets the optional HTML content encoding value for the blob content. Use
     * this value to specify any HTTP content encodings applied to the blob,
     * passed as a <code>x-ms-blob-content-encoding</code> header value to the
     * server. This value will be returned to clients in the headers of the
     * response when the blob data or blob properties are requested. Pass an
     * empty value to update a blob to the default value, which will cause no
     * content encoding header to be returned with the blob.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link SetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param contentEncoding
     *            A {@link String} containing the
     *            <code>x-ms-blob-content-encoding</code> header value to set.
     * @return A reference to this {@link SetBlobPropertiesOptions} instance.
     */
    public SetBlobPropertiesOptions setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    /**
     * Gets the HTTP content language header value set in this
     * {@link SetBlobPropertiesOptions} instance.
     * 
     * @return A {@link String} containing the HTTP content language header
     *         value set, if any.
     */
    public String getContentLanguage() {
        return contentLanguage;
    }

    /**
     * Sets the optional HTTP content language header value for the blob
     * content. Use this value to specify the content language of the blob. This
     * value will be returned to clients in the
     * <code>x-ms-blob-content-language</code> header of the response when the
     * blob data or blob properties are requested.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link SetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param contentLanguage
     *            A {@link String} containing the
     *            <code>x-ms-blob-content-language</code> header value to set.
     * @return A reference to this {@link SetBlobPropertiesOptions} instance.
     */
    public SetBlobPropertiesOptions setContentLanguage(String contentLanguage) {
        this.contentLanguage = contentLanguage;
        return this;
    }

    /**
     * Gets the MD5 hash value for the blob content set in this
     * {@link SetBlobPropertiesOptions} instance.
     * 
     * @return A {@link String} containing the MD5 hash value for the blob
     *         content set, if any.
     */
    public String getContentMD5() {
        return contentMD5;
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
     * {@link SetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param contentMD5
     *            A {@link String} containing the MD5 hash value for the blob
     *            content to set.
     * @return A reference to this {@link SetBlobPropertiesOptions} instance.
     */
    public SetBlobPropertiesOptions setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
        return this;
    }

    /**
     * Gets the HTTP cache control value set in this
     * {@link SetBlobPropertiesOptions} instance.
     * 
     * @return A {@link String} containing the HTTP cache control value set, if
     *         any.
     */
    public String getCacheControl() {
        return cacheControl;
    }

    /**
     * Sets the optional HTTP cache control value for the blob content. The Blob
     * service stores this value but does not use or modify it. This value will
     * be returned to clients in the headers of the response when the blob data
     * or blob properties are requested.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link SetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param cacheControl
     *            A {@link String} containing the HTTP cache control value to
     *            set.
     * @return A reference to this {@link SetBlobPropertiesOptions} instance.
     */
    public SetBlobPropertiesOptions setCacheControl(String cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    /**
     * Gets the sequence number value set in this
     * {@link SetBlobPropertiesOptions} instance.
     * 
     * @return The sequence number to set, if any.
     */
    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Sets the page blob sequence number. The sequence number is a
     * user-controlled property that you can use to track requests and manage
     * concurrency issues. This value is optional, but is required if the
     * sequence number action value is set to <code>max</code> or
     * <code>update</code>.
     * <p>
     * Use the <em>sequenceNumber</em> parameter together with the sequence
     * number action to update the blob's sequence number, either to the
     * specified value or to the higher of the values specified with the request
     * or currently stored with the blob. This header should not be specified if
     * the sequence number action is set to <code>increment</code>; in this case
     * the service automatically increments the sequence number by one.
     * <p>
     * To set the sequence number to a value of your choosing, this property
     * must be specified on the request together with a sequence number action
     * value of <code>update</code>.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link SetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param sequenceNumber
     *            The sequence number to set.
     * @return A reference to this {@link SetBlobPropertiesOptions} instance.
     */
    public SetBlobPropertiesOptions setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    /**
     * Gets the lease ID to match for the blob set in this
     * {@link SetBlobPropertiesOptions} instance.
     * 
     * @return A {@link String} containing the lease ID set, if any.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * Sets an optional lease ID value to match when setting properties of the
     * blob. If set, the lease must be active and the value must match the lease
     * ID set on the leased blob for the operation to succeed.
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link SetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param leaseId
     *            A {@link String} containing the lease ID to set.
     * @return A reference to this {@link SetBlobPropertiesOptions} instance.
     */
    public SetBlobPropertiesOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }

    /**
     * Gets the sequence number action set in this
     * {@link SetBlobPropertiesOptions} instance.
     * 
     * @return A {@link String} containing the sequence number action set, if
     *         any.
     */
    public String getSequenceNumberAction() {
        return sequenceNumberAction;
    }

    /**
     * Sets an optional sequence number action for a page blob. This value is
     * required if a sequence number is set for the request.
     * <p>
     * The <em>sequenceNumberAction</em> parameter indicates how the service
     * should modify the page blob's sequence number. Specify one of the
     * following strings for this parameter:
     * <ul>
     * <li><code>max</code> - Sets the sequence number to be the higher of the
     * value included with the request and the value currently stored for the
     * blob.</li>
     * <li><code>update</code> - Sets the sequence number to the value included
     * with the request.</li>
     * <li><code>increment</code> - Increments the value of the sequence number
     * by 1. If specifying this option, do not set the sequence number value;
     * doing so will cause a {@link com.microsoft.windowsazure.exception.ServiceException} to be thrown.</li>
     * </ul>
     * <p>
     * Note that this value only affects calls made on methods where this
     * {@link SetBlobPropertiesOptions} instance is passed as a parameter.
     * 
     * @param sequenceNumberAction
     *            A {@link String} containing the sequence number action to set
     *            on the page blob.
     * @return A reference to this {@link SetBlobPropertiesOptions} instance.
     */
    public SetBlobPropertiesOptions setSequenceNumberAction(
            String sequenceNumberAction) {
        this.sequenceNumberAction = sequenceNumberAction;
        return this;
    }

    /**
     * Gets the access conditions set in this {@link SetBlobPropertiesOptions}
     * instance.
     * 
     * @return An {@link AccessCondition} containing the access conditions set,
     *         if any.
     */
    public AccessConditionHeader getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets the access conditions for setting the properties of a blob. By
     * default, the set blob properties operation will set the properties
     * unconditionally. Use this method to specify conditions on the ETag or
     * last modified time value for performing the operation.
     * <p>
     * The <em>accessCondition</em> value only affects calls made on methods
     * where this {@link SetBlobPropertiesOptions} instance is passed as a
     * parameter.
     * 
     * @param accessCondition
     *            An {@link AccessCondition} containing the access conditions to
     *            set.
     * @return A reference to this {@link SetBlobPropertiesOptions} instance.
     */
    public SetBlobPropertiesOptions setAccessCondition(
            AccessConditionHeader accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }
}
