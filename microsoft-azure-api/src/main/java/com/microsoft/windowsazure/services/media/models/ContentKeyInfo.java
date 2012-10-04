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
 * The Class ContentKeyInfo.
 */
public class ContentKeyInfo {

    /** The id. */
    private String id;

    /** The created. */
    private Date created;

    /** The last modified. */
    private Date lastModified;

    /** The content key type. */
    private ContentKeyType contentKeyType;

    /** The encrypted content key. */
    private String encryptedContentKey;

    /** The name. */
    private String name;

    /** The protection key id. */
    private String protectionKeyId;

    /** The check sum. */
    private String checkSum;

    /** The protection key type. */
    private ProtectionKeyType protectionKeyType;

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
     * @return the content key info
     */
    public ContentKeyInfo setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the create.
     * 
     * @param created
     *            the created
     * @return the content key info
     */
    public ContentKeyInfo setCreate(Date created) {
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
     * @return the content key info
     */
    public ContentKeyInfo setLastModified(Date lastModified) {
        this.lastModified = lastModified;
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
     * @return the content key info
     */
    public ContentKeyInfo setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the check sum.
     * 
     * @param checkSum
     *            the check sum
     * @return the content key info
     */
    public ContentKeyInfo setCheckSum(String checkSum) {
        this.checkSum = checkSum;
        return this;
    }

    /**
     * Gets the check sum.
     * 
     * @return the check sum
     */
    public String getCheckSum() {
        return this.checkSum;
    }

    /**
     * Sets the protection key type.
     * 
     * @param protectionKeyType
     *            the protection key type
     * @return the content key info
     */
    public ContentKeyInfo setProtectionKeyType(ProtectionKeyType protectionKeyType) {
        this.protectionKeyType = protectionKeyType;
        return this;
    }

    /**
     * Gets the protection key type.
     * 
     * @return the protection key type
     */
    public ProtectionKeyType getProtectionKeyType() {
        return this.protectionKeyType;
    }

    /**
     * Sets the protection key id.
     * 
     * @param protectionKeyId
     *            the protection key id
     * @return the content key info
     */
    public ContentKeyInfo setProtectionKeyId(String protectionKeyId) {
        this.protectionKeyId = protectionKeyId;
        return this;
    }

    /**
     * Gets the protection key id.
     * 
     * @return the protection key id
     */
    public String getProtectionKeyId() {
        return this.protectionKeyId;
    }

    /**
     * Sets the encrypted content key.
     * 
     * @param encryptedContentKey
     *            the encrypted content key
     * @return the content key info
     */
    public ContentKeyInfo setEncryptedContentKey(String encryptedContentKey) {
        this.encryptedContentKey = encryptedContentKey;
        return this;
    }

    /**
     * Gets the encrypted content key.
     * 
     * @return the encrypted content key
     */
    public String getEncryptedContentKey() {
        return this.encryptedContentKey;
    }

    /**
     * Sets the content key type.
     * 
     * @param contentKeyType
     *            the content key type
     * @return the content key info
     */
    public ContentKeyInfo setContentKeyType(ContentKeyType contentKeyType) {
        this.contentKeyType = contentKeyType;
        return this;
    }

    /**
     * Gets the content key type.
     * 
     * @return the content key type
     */
    public ContentKeyType getContentKeyType() {
        return this.contentKeyType;
    }

    /**
     * Sets the created.
     * 
     * @param created
     *            the created
     * @return the content key info
     */
    public ContentKeyInfo setCreated(Date created) {
        this.created = created;
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

}
