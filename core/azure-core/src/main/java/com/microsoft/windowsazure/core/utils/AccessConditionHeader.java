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
package com.microsoft.windowsazure.core.utils;

import com.microsoft.windowsazure.core.RFC1123DateConverter;

import java.util.Date;

/**
 * Represents a set of access conditions for operations that use storage
 * services.
 */
public final class AccessConditionHeader {

    /**
     * Specifies an access condition with no conditions set.
     */
    public static final AccessConditionHeader NONE = new AccessConditionHeader(
            AccessConditionHeaderType.NONE, null);

    /**
     * Creates an access condition that only allows an operation if the
     * resource's ETag value matches the specified ETag value.
     * <p>
     * Setting this access condition modifies the request to include the HTTP
     * <i>If-Match</i> conditional header. If this access condition is set, the
     * operation is performed only if the ETag of the resource matches the
     * specified ETag.
     * <p>
     * For more information, see <a href=
     * 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     * 
     * @param etag
     *            A <code>String</code> that represents the ETag value to check.
     * 
     * @return An <code>AccessCondition</code> object that represents the
     *         <i>If-Match</i> condition.
     */
    public static AccessConditionHeader ifMatch(String etag) {
        return new AccessConditionHeader(AccessConditionHeaderType.IF_MATCH,
                etag);
    }

    /**
     * Creates an access condition that only allows an operation if the resource
     * has been modified since the specified time.
     * <p>
     * Setting this access condition modifies the request to include the HTTP
     * <i>If-Modified-Since</i> conditional header. If this access condition is
     * set, the operation is performed only if the resource has been modified
     * since the specified time.
     * <p>
     * For more information, see <a href=
     * 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     * 
     * @param lastMotified
     *            A <code>java.util.Date</code> object that represents the
     *            last-modified time to check for the resource.
     * 
     * @return An <code>AccessCondition</code> object that represents the
     *         <i>If-Modified-Since</i> condition.
     */
    public static AccessConditionHeader ifModifiedSince(Date lastMotified) {
        return new AccessConditionHeader(
                AccessConditionHeaderType.IF_MODIFIED_SINCE,
                new RFC1123DateConverter().format(lastMotified));
    }

    /**
     * Creates an access condition that only allows an operation if the
     * resource's ETag value does not match the specified ETag value.
     * <p>
     * Setting this access condition modifies the request to include the HTTP
     * <i>If-None-Match</i> conditional header. If this access condition is set,
     * the operation is performed only if the ETag of the resource does not
     * match the specified ETag.
     * <p>
     * For more information, see <a href=
     * 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     * 
     * @param etag
     *            A <code>String</code> that represents the ETag value to check.
     * 
     * @return An <code>AccessCondition</code> object that represents the
     *         <i>If-None-Match</i> condition.
     */
    public static AccessConditionHeader ifNoneMatch(String etag) {
        return new AccessConditionHeader(
                AccessConditionHeaderType.IF_NONE_MATCH, etag);
    }

    /**
     * Creates an access condition that only allows an operation if the resource
     * has not been modified since the specified time.
     * <p>
     * Setting this access condition modifies the request to include the HTTP
     * <i>If-Unmodified-Since</i> conditional header. If this access condition
     * is set, the operation is performed only if the resource has not been
     * modified since the specified time.
     * <p>
     * For more information, see <a href=
     * 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     * 
     * @param lastMotified
     *            A <code>java.util.Date</code> object that represents the
     *            last-modified time to check for the resource.
     * 
     * @return An <code>AccessCondition</code> object that represents the
     *         <i>If-Unmodified-Since</i> condition.
     */
    public static AccessConditionHeader ifNotModifiedSince(Date lastMotified) {
        return new AccessConditionHeader(
                AccessConditionHeaderType.IF_UNMODIFIED_SINCE,
                new RFC1123DateConverter().format(lastMotified));
    }

    /**
     * Represents the access condition header type.
     */
    private AccessConditionHeaderType header = AccessConditionHeaderType.NONE;

    /**
     * Represents the access condition header value.
     */
    private String value;

    /**
     * Creates an instance of the <code>AccessCondition</code> class.
     */
    protected AccessConditionHeader() {
        // Empty Default Ctor
    }

    /**
     * Creates an instance of the <code>AccessCondition</code> class using the
     * specified header type and value.
     * 
     * @param headerType
     *            An {@link AccessConditionHeaderType} value that represents the
     *            header type.
     * @param value
     *            A <code>String</code> that represents the value of the header.
     */
    protected AccessConditionHeader(AccessConditionHeaderType headerType,
            String value) {
        this.setHeader(headerType);
        this.setValue(value);
    }

    /**
     * Gets the access condition header type set in this
     * <code>AccessCondition</code> instance.
     * 
     * @return The {@link AccessConditionHeaderType} set in this
     *         <code>AccessCondition</code> instance.
     */
    public AccessConditionHeaderType getHeader() {
        return header;
    }

    /**
     * Sets the access condition header type in this
     * <code>AccessCondition</code> instance.
     * 
     * @param header
     *            The {@link AccessConditionHeaderType} to set in this
     *            <code>AccessCondition</code> instance.
     */
    public void setHeader(AccessConditionHeaderType header) {
        this.header = header;
    }

    /**
     * Gets the access condition value set in this <code>AccessCondition</code>
     * instance.
     * 
     * @return A {@link String} containing the access condition value set in
     *         this <code>AccessCondition</code> instance.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the access condition value in this <code>AccessCondition</code>
     * instance.
     * 
     * @param value
     *            A {@link String} containing the access condition value to set
     *            in this <code>AccessCondition</code> instance.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
