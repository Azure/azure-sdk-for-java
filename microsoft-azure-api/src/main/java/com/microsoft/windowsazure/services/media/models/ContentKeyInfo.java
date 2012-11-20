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

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyRestType;

// TODO: Auto-generated Javadoc
/**
 * The Class ContentKeyInfo.
 */
public class ContentKeyInfo extends ODataEntity<ContentKeyRestType> {

    /**
     * Instantiates a new content key info.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public ContentKeyInfo(EntryType entry, ContentKeyRestType content) {
        super(entry, content);
    }

    /**
     * Instantiates a new content key info.
     */
    public ContentKeyInfo() {
        super(new ContentKeyRestType());
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return getContent().getId();
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id
     * @return the content key info
     */
    public ContentKeyInfo setId(String id) {
        getContent().setId(id);
        return this;
    }

    /**
     * Sets the create.
     * 
     * @param created
     *            the created
     * @return the content key info
     */
    public ContentKeyInfo setCreated(Date created) {
        getContent().setCreated(created);
        return this;
    }

    /**
     * Gets the created.
     * 
     * @return the created
     */
    public Date getCreated() {
        return getContent().getCreated();
    }

    /**
     * Gets the last modified.
     * 
     * @return the last modified
     */
    public Date getLastModified() {
        return getContent().getLastModified();
    }

    /**
     * Sets the last modified.
     * 
     * @param lastModified
     *            the last modified
     * @return the content key info
     */
    public ContentKeyInfo setLastModified(Date lastModified) {
        getContent().setLastModified(lastModified);
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return getContent().getName();
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name
     * @return the content key info
     */
    public ContentKeyInfo setName(String name) {
        getContent().setName(name);
        return this;
    }

    /**
     * Sets the check sum.
     * 
     * @param checksum
     *            the check sum
     * @return the content key info
     */
    public ContentKeyInfo setChecksum(String checksum) {
        getContent().setChecksum(checksum);
        return this;
    }

    /**
     * Gets the check sum.
     * 
     * @return the check sum
     */
    public String getCheckSum() {
        return getContent().getChecksum();
    }

    /**
     * Sets the protection key type.
     * 
     * @param protectionKeyType
     *            the protection key type
     * @return the content key info
     */
    public ContentKeyInfo setProtectionKeyType(ProtectionKeyType protectionKeyType) {
        getContent().setProtectionKeyType(protectionKeyType.getCode());
        return this;
    }

    /**
     * Gets the protection key type.
     * 
     * @return the protection key type
     */
    public ProtectionKeyType getProtectionKeyType() {
        return ProtectionKeyType.fromCode(getContent().getProtectionKeyType());
    }

    /**
     * Sets the protection key id.
     * 
     * @param protectionKeyId
     *            the protection key id
     * @return the content key info
     */
    public ContentKeyInfo setProtectionKeyId(String protectionKeyId) {
        getContent().setProtectionKeyId(protectionKeyId);
        return this;
    }

    /**
     * Gets the protection key id.
     * 
     * @return the protection key id
     */
    public String getProtectionKeyId() {
        return getContent().getProtectionKeyId();
    }

    /**
     * Sets the encrypted content key.
     * 
     * @param encryptedContentKey
     *            the encrypted content key
     * @return the content key info
     */
    public ContentKeyInfo setEncryptedContentKey(String encryptedContentKey) {
        getContent().setEncryptedContentKey(encryptedContentKey);
        return this;
    }

    /**
     * Gets the encrypted content key.
     * 
     * @return the encrypted content key
     */
    public String getEncryptedContentKey() {
        return getContent().getEncryptedContentKey();
    }

    /**
     * Sets the content key type.
     * 
     * @param contentKeyType
     *            the content key type
     * @return the content key info
     */
    public ContentKeyInfo setContentKeyType(ContentKeyType contentKeyType) {
        if (contentKeyType == null) {
            getContent().setContentKeyType(null);
        }
        else {
            getContent().setContentKeyType(contentKeyType.getCode());
        }
        return this;
    }

    /**
     * Gets the content key type.
     * 
     * @return the content key type
     */
    public ContentKeyType getContentKeyType() {
        Integer contentKeyTypeInteger = getContent().getContentKeyType();
        ContentKeyType contentKeyType = null;
        if (contentKeyTypeInteger != null) {
            contentKeyType = ContentKeyType.fromCode(contentKeyTypeInteger);
        }
        return contentKeyType;
    }
}
