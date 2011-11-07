package com.microsoft.azure.services.blob;

import java.util.Date;

import com.microsoft.azure.utils.RFC1123DateConverter;

/**
 * TODO: Unify this with client layer
 *
 * Represents a set of access conditions to be used for operations against the storage services.
 *
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class AccessCondition {

    /**
     * Specifies that no access condition is set.
     */
    public static final AccessCondition NONE = new AccessCondition(AccessConditionHeaderType.NONE, null);

    /**
     * Returns an access condition such that an operation will be performed only if the resource's ETag value matches
     * the specified ETag value.
     * <p>
     * Setting this access condition modifies the request to include the HTTP <i>If-Match</i> conditional header. If
     * this access condition is set, the operation is performed only if the ETag of the resource matches the specified
     * ETag.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     *
     * @param etag
     *            A <code>String</code> that represents the ETag value to check.
     *
     * @return An <code>AccessCondition</code> object that represents the <i>If-Match</i> condition.
     */
    public static AccessCondition ifMatch(String etag) {
        return new AccessCondition(AccessConditionHeaderType.IF_MATCH, etag);
    }

    /**
     * Returns an access condition such that an operation will be performed only if the resource has been modified since
     * the specified time.
     * <p>
     * Setting this access condition modifies the request to include the HTTP <i>If-Modified-Since</i> conditional
     * header. If this access condition is set, the operation is performed only if the resource has been modified since
     * the specified time.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     *
     * @param lastMotified
     *            A <code>java.util.Date</code> object that represents the last-modified time to check for the resource.
     *
     * @return An <code>AccessCondition</code> object that represents the <i>If-Modified-Since</i> condition.
     */
    public static AccessCondition ifModifiedSince(Date lastMotified) {
        return new AccessCondition(AccessConditionHeaderType.IF_MODIFIED_SINCE, new RFC1123DateConverter().format(lastMotified));
    }

    /**
     * Returns an access condition such that an operation will be performed only if the resource's ETag value does not
     * match the specified ETag value.
     * <p>
     * Setting this access condition modifies the request to include the HTTP <i>If-None-Match</i> conditional header.
     * If this access condition is set, the operation is performed only if the ETag of the resource does not match the
     * specified ETag.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     *
     * @param etag
     *            A <code>String</code> that represents the ETag value to check.
     *
     * @return An <code>AccessCondition</code> object that represents the <i>If-None-Match</i> condition.
     */
    public static AccessCondition ifNoneMatch(String etag) {
        return new AccessCondition(AccessConditionHeaderType.IF_NONE_MATCH, etag);
    }

    /**
     * Returns an access condition such that an operation will be performed only if the resource has not been modified
     * since the specified time.
     * <p>
     * Setting this access condition modifies the request to include the HTTP <i>If-Unmodified-Since</i> conditional
     * header. If this access condition is set, the operation is performed only if the resource has not been modified
     * since the specified time.
     * <p>
     * For more information, see <a href= 'http://go.microsoft.com/fwlink/?LinkID=224642&clcid=0x409'>Specifying
     * Conditional Headers for Blob Service Operations</a>.
     *
     * @param lastMotified
     *            A <code>java.util.Date</code> object that represents the last-modified time to check for the resource.
     *
     * @return An <code>AccessCondition</code> object that represents the <i>If-Unmodified-Since</i> condition.
     */
    public static AccessCondition ifNotModifiedSince(Date lastMotified) {
        return new AccessCondition(AccessConditionHeaderType.IF_UNMODIFIED_SINCE, new RFC1123DateConverter().format(lastMotified));
    }

    /**
     * Represents the header type.
     */
    private AccessConditionHeaderType header = AccessConditionHeaderType.NONE;

    /**
     * Represents the header value.
     */
    private String value;

    /**
     * Creates an instance of the <code>AccessCondition</code> class.
     */
    protected AccessCondition() {
        // Empty Default Ctor
    }

    /**
     * Creates an instance of the <code>AccessCondition</code> class using the specified header type and value.
     *
     * @param headerType
     *            An {@link AccessConditionHeaderType} value that represents the header type.
     * @param value
     *            A <code>String</code> that represents the value of the header.
     */
    protected AccessCondition(AccessConditionHeaderType headerType, String value) {
        this.setHeader(headerType);
        this.setValue(value);
    }

    public AccessConditionHeaderType getHeader() {
        return header;
    }

    public void setHeader(AccessConditionHeaderType header) {
        this.header = header;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
