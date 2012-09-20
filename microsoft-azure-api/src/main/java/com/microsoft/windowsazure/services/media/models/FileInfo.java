/*
 * Copyright 2012 Microsoft Corporation
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

package com.microsoft.windowsazure.services.media.models;

import java.util.Date;

/**
 * The Class FileInfo.
 */
public class FileInfo {

    /** The id. */
    private String id;

    /** The name. */
    private String name;

    /** The content file size. */
    private int contentFileSize;

    /** The parent asset id. */
    private String parentAssetId;

    /** The encryption version. */
    private String encryptionVersion;

    /** The encryption scheme. */
    private String encryptionScheme;

    /** The is encrypted. */
    private Boolean isEncrypted;

    /** The encryption key id. */
    private String encryptionKeyId;

    /** The initialization vector. */
    private String initializationVector;

    /** The is primary. */
    private Boolean isPrimary;

    /** The last modified. */
    private Date lastModified;

    /** The created. */
    private Date created;

    /** The mime type. */
    private String mimeType;

    /** The content checksum. */
    private String contentChecksum;

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id
     * @return the file info
     */
    public FileInfo setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name
     * @return the file info
     */
    public FileInfo setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the content file size.
     * 
     * @return the content file size
     */
    public int getContentFileSize() {
        return this.contentFileSize;
    }

    /**
     * Sets the content file size.
     * 
     * @param contentFileSize
     *            the content file size
     * @return the file info
     */
    public FileInfo setContentFileSize(int contentFileSize) {
        this.contentFileSize = contentFileSize;
        return this;
    }

    /**
     * Gets the parent asset id.
     * 
     * @return the parent asset id
     */
    public String getParentAssetId() {
        return this.parentAssetId;
    }

    /**
     * Sets the parent asset id.
     * 
     * @param parentAssetId
     *            the parent asset id
     * @return the file info
     */
    public FileInfo setParentAssetId(String parentAssetId) {
        this.parentAssetId = parentAssetId;
        return this;
    }

    /**
     * Gets the created.
     * 
     * @return the created
     */
    public Date getCreated() {
        return this.created;
    }

    /**
     * Sets the create.
     * 
     * @param created
     *            the created
     * @return the file info
     */
    public FileInfo setCreate(Date created) {
        this.created = created;
        return this;
    }

    /**
     * Gets the last modified.
     * 
     * @return the last modified
     */
    public Date getLastModified() {
        return this.lastModified;
    }

    /**
     * Sets the last modified.
     * 
     * @param lastModified
     *            the last modified
     * @return the file info
     */
    public FileInfo setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    /**
     * Sets the encryption version.
     * 
     * @param encryptionVersion
     *            the encryption version
     * @return the file info
     */
    public FileInfo setEncryptionVersion(String encryptionVersion) {
        this.encryptionVersion = encryptionVersion;
        return this;
    }

    /**
     * Gets the encryption version.
     * 
     * @return the encryption version
     */
    public String getEncryptionVersion() {
        return this.encryptionVersion;
    }

    /**
     * Sets the encryption scheme.
     * 
     * @param encryptionScheme
     *            the encryption scheme
     * @return the file info
     */
    public FileInfo setEncryptionScheme(String encryptionScheme) {
        this.encryptionScheme = encryptionScheme;
        return this;
    }

    /**
     * Gets the encryption scheme.
     * 
     * @return the encryption scheme
     */
    public String getEncryptionScheme() {
        return this.encryptionScheme;
    }

    /**
     * Sets the is encrypted.
     * 
     * @param isEncrypted
     *            the is encrypted
     * @return the file info
     */
    public FileInfo setIsEncrypted(Boolean isEncrypted) {
        this.isEncrypted = isEncrypted;
        return this;
    }

    /**
     * Gets the checks if is encrypted.
     * 
     * @return the checks if is encrypted
     */
    public Boolean getIsEncrypted() {
        return this.isEncrypted;
    }

    /**
     * Sets the encryption key id.
     * 
     * @param encryptionKeyId
     *            the encryption key id
     * @return the file info
     */
    public FileInfo setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
        return this;
    }

    /**
     * Gets the encryption key id.
     * 
     * @return the encryption key id
     */
    public String getEncryptionKeyId() {
        return this.encryptionKeyId;
    }

    /**
     * Sets the initialization vector.
     * 
     * @param expectedInitializationVector
     *            the expected initialization vector
     * @return the file info
     */
    public FileInfo setInitializationVector(String expectedInitializationVector) {
        this.initializationVector = expectedInitializationVector;
        return this;
    }

    /**
     * Gets the initialization vector.
     * 
     * @return the initialization vector
     */
    public String getInitializationVector() {
        return this.initializationVector;
    }

    /**
     * Sets the is primary.
     * 
     * @param isPrimary
     *            the is primary
     * @return the file info
     */
    public FileInfo setIsPrimary(Boolean isPrimary) {
        this.isPrimary = isPrimary;
        return this;
    }

    /**
     * Gets the checks if is primary.
     * 
     * @return the checks if is primary
     */
    public Boolean getIsPrimary() {
        return this.isPrimary;
    }

    /**
     * Sets the created.
     * 
     * @param created
     *            the created
     * @return the file info
     */
    public FileInfo setCreated(Date created) {
        this.created = created;
        return this;
    }

    /**
     * Sets the mime type.
     * 
     * @param mimeType
     *            the mime type
     * @return the file info
     */
    public FileInfo setMimeType(String mimeType) {
        this.mimeType = mimeType;
        return this;
    }

    /**
     * Gets the mime type.
     * 
     * @return the mime type
     */
    public String getMimeType() {
        return this.mimeType;
    }

    /**
     * Sets the content checksum.
     * 
     * @param contentChecksum
     *            the content checksum
     * @return the file info
     */
    public FileInfo setContentChecksum(String contentChecksum) {
        this.contentChecksum = contentChecksum;
        return this;
    }

    /**
     * Gets the content checksum.
     * 
     * @return the content checksum
     */
    public String getContentChecksum() {
        return this.contentChecksum;
    }

}
