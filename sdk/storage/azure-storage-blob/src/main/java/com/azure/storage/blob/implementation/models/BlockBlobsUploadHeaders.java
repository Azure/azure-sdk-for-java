// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.storage.blob.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import java.time.OffsetDateTime;
import java.util.Base64;

/**
 * The BlockBlobsUploadHeaders model.
 */
@Fluent
public final class BlockBlobsUploadHeaders {
    /*
     * The x-ms-version property.
     */
    private String xMsVersion;

    /*
     * The ETag property.
     */
    private String eTag;

    /*
     * The Last-Modified property.
     */
    private DateTimeRfc1123 lastModified;

    /*
     * The x-ms-version-id property.
     */
    private String xMsVersionId;

    /*
     * The x-ms-encryption-key-sha256 property.
     */
    private String xMsEncryptionKeySha256;

    /*
     * The x-ms-structured-body property.
     */
    private String xMsStructuredBody;

    /*
     * The x-ms-request-id property.
     */
    private String xMsRequestId;

    /*
     * The x-ms-request-server-encrypted property.
     */
    private Boolean xMsRequestServerEncrypted;

    /*
     * The x-ms-client-request-id property.
     */
    private String xMsClientRequestId;

    /*
     * The Date property.
     */
    private DateTimeRfc1123 date;

    /*
     * The Content-MD5 property.
     */
    private byte[] contentMD5;

    /*
     * The x-ms-encryption-scope property.
     */
    private String xMsEncryptionScope;

    private static final HttpHeaderName X_MS_VERSION = HttpHeaderName.fromString("x-ms-version");

    private static final HttpHeaderName X_MS_VERSION_ID = HttpHeaderName.fromString("x-ms-version-id");

    private static final HttpHeaderName X_MS_ENCRYPTION_KEY_SHA256
        = HttpHeaderName.fromString("x-ms-encryption-key-sha256");

    private static final HttpHeaderName X_MS_STRUCTURED_BODY = HttpHeaderName.fromString("x-ms-structured-body");

    private static final HttpHeaderName X_MS_REQUEST_SERVER_ENCRYPTED
        = HttpHeaderName.fromString("x-ms-request-server-encrypted");

    private static final HttpHeaderName X_MS_ENCRYPTION_SCOPE = HttpHeaderName.fromString("x-ms-encryption-scope");

    // HttpHeaders containing the raw property values.
    /**
     * Creates an instance of BlockBlobsUploadHeaders class.
     * 
     * @param rawHeaders The raw HttpHeaders that will be used to create the property values.
     */
    public BlockBlobsUploadHeaders(HttpHeaders rawHeaders) {
        this.xMsVersion = rawHeaders.getValue(X_MS_VERSION);
        this.eTag = rawHeaders.getValue(HttpHeaderName.ETAG);
        String lastModified = rawHeaders.getValue(HttpHeaderName.LAST_MODIFIED);
        if (lastModified != null) {
            this.lastModified = new DateTimeRfc1123(lastModified);
        }
        this.xMsVersionId = rawHeaders.getValue(X_MS_VERSION_ID);
        this.xMsEncryptionKeySha256 = rawHeaders.getValue(X_MS_ENCRYPTION_KEY_SHA256);
        this.xMsStructuredBody = rawHeaders.getValue(X_MS_STRUCTURED_BODY);
        this.xMsRequestId = rawHeaders.getValue(HttpHeaderName.X_MS_REQUEST_ID);
        String xMsRequestServerEncrypted = rawHeaders.getValue(X_MS_REQUEST_SERVER_ENCRYPTED);
        if (xMsRequestServerEncrypted != null) {
            this.xMsRequestServerEncrypted = Boolean.parseBoolean(xMsRequestServerEncrypted);
        }
        this.xMsClientRequestId = rawHeaders.getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID);
        String date = rawHeaders.getValue(HttpHeaderName.DATE);
        if (date != null) {
            this.date = new DateTimeRfc1123(date);
        }
        String contentMD5 = rawHeaders.getValue(HttpHeaderName.CONTENT_MD5);
        if (contentMD5 != null) {
            this.contentMD5 = Base64.getDecoder().decode(contentMD5);
        }
        this.xMsEncryptionScope = rawHeaders.getValue(X_MS_ENCRYPTION_SCOPE);
    }

    /**
     * Get the xMsVersion property: The x-ms-version property.
     * 
     * @return the xMsVersion value.
     */
    public String getXMsVersion() {
        return this.xMsVersion;
    }

    /**
     * Set the xMsVersion property: The x-ms-version property.
     * 
     * @param xMsVersion the xMsVersion value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setXMsVersion(String xMsVersion) {
        this.xMsVersion = xMsVersion;
        return this;
    }

    /**
     * Get the eTag property: The ETag property.
     * 
     * @return the eTag value.
     */
    public String getETag() {
        return this.eTag;
    }

    /**
     * Set the eTag property: The ETag property.
     * 
     * @param eTag the eTag value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setETag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    /**
     * Get the lastModified property: The Last-Modified property.
     * 
     * @return the lastModified value.
     */
    public OffsetDateTime getLastModified() {
        if (this.lastModified == null) {
            return null;
        }
        return this.lastModified.getDateTime();
    }

    /**
     * Set the lastModified property: The Last-Modified property.
     * 
     * @param lastModified the lastModified value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setLastModified(OffsetDateTime lastModified) {
        if (lastModified == null) {
            this.lastModified = null;
        } else {
            this.lastModified = new DateTimeRfc1123(lastModified);
        }
        return this;
    }

    /**
     * Get the xMsVersionId property: The x-ms-version-id property.
     * 
     * @return the xMsVersionId value.
     */
    public String getXMsVersionId() {
        return this.xMsVersionId;
    }

    /**
     * Set the xMsVersionId property: The x-ms-version-id property.
     * 
     * @param xMsVersionId the xMsVersionId value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setXMsVersionId(String xMsVersionId) {
        this.xMsVersionId = xMsVersionId;
        return this;
    }

    /**
     * Get the xMsEncryptionKeySha256 property: The x-ms-encryption-key-sha256 property.
     * 
     * @return the xMsEncryptionKeySha256 value.
     */
    public String getXMsEncryptionKeySha256() {
        return this.xMsEncryptionKeySha256;
    }

    /**
     * Set the xMsEncryptionKeySha256 property: The x-ms-encryption-key-sha256 property.
     * 
     * @param xMsEncryptionKeySha256 the xMsEncryptionKeySha256 value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setXMsEncryptionKeySha256(String xMsEncryptionKeySha256) {
        this.xMsEncryptionKeySha256 = xMsEncryptionKeySha256;
        return this;
    }

    /**
     * Get the xMsStructuredBody property: The x-ms-structured-body property.
     * 
     * @return the xMsStructuredBody value.
     */
    public String getXMsStructuredBody() {
        return this.xMsStructuredBody;
    }

    /**
     * Set the xMsStructuredBody property: The x-ms-structured-body property.
     * 
     * @param xMsStructuredBody the xMsStructuredBody value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setXMsStructuredBody(String xMsStructuredBody) {
        this.xMsStructuredBody = xMsStructuredBody;
        return this;
    }

    /**
     * Get the xMsRequestId property: The x-ms-request-id property.
     * 
     * @return the xMsRequestId value.
     */
    public String getXMsRequestId() {
        return this.xMsRequestId;
    }

    /**
     * Set the xMsRequestId property: The x-ms-request-id property.
     * 
     * @param xMsRequestId the xMsRequestId value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setXMsRequestId(String xMsRequestId) {
        this.xMsRequestId = xMsRequestId;
        return this;
    }

    /**
     * Get the xMsRequestServerEncrypted property: The x-ms-request-server-encrypted property.
     * 
     * @return the xMsRequestServerEncrypted value.
     */
    public Boolean isXMsRequestServerEncrypted() {
        return this.xMsRequestServerEncrypted;
    }

    /**
     * Set the xMsRequestServerEncrypted property: The x-ms-request-server-encrypted property.
     * 
     * @param xMsRequestServerEncrypted the xMsRequestServerEncrypted value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setXMsRequestServerEncrypted(Boolean xMsRequestServerEncrypted) {
        this.xMsRequestServerEncrypted = xMsRequestServerEncrypted;
        return this;
    }

    /**
     * Get the xMsClientRequestId property: The x-ms-client-request-id property.
     * 
     * @return the xMsClientRequestId value.
     */
    public String getXMsClientRequestId() {
        return this.xMsClientRequestId;
    }

    /**
     * Set the xMsClientRequestId property: The x-ms-client-request-id property.
     * 
     * @param xMsClientRequestId the xMsClientRequestId value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setXMsClientRequestId(String xMsClientRequestId) {
        this.xMsClientRequestId = xMsClientRequestId;
        return this;
    }

    /**
     * Get the date property: The Date property.
     * 
     * @return the date value.
     */
    public OffsetDateTime getDate() {
        if (this.date == null) {
            return null;
        }
        return this.date.getDateTime();
    }

    /**
     * Set the date property: The Date property.
     * 
     * @param date the date value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setDate(OffsetDateTime date) {
        if (date == null) {
            this.date = null;
        } else {
            this.date = new DateTimeRfc1123(date);
        }
        return this;
    }

    /**
     * Get the contentMD5 property: The Content-MD5 property.
     * 
     * @return the contentMD5 value.
     */
    public byte[] getContentMD5() {
        return CoreUtils.clone(this.contentMD5);
    }

    /**
     * Set the contentMD5 property: The Content-MD5 property.
     * 
     * @param contentMD5 the contentMD5 value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setContentMD5(byte[] contentMD5) {
        this.contentMD5 = CoreUtils.clone(contentMD5);
        return this;
    }

    /**
     * Get the xMsEncryptionScope property: The x-ms-encryption-scope property.
     * 
     * @return the xMsEncryptionScope value.
     */
    public String getXMsEncryptionScope() {
        return this.xMsEncryptionScope;
    }

    /**
     * Set the xMsEncryptionScope property: The x-ms-encryption-scope property.
     * 
     * @param xMsEncryptionScope the xMsEncryptionScope value to set.
     * @return the BlockBlobsUploadHeaders object itself.
     */
    public BlockBlobsUploadHeaders setXMsEncryptionScope(String xMsEncryptionScope) {
        this.xMsEncryptionScope = xMsEncryptionScope;
        return this;
    }
}
