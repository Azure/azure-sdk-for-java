/*
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

package com.microsoft.windowsazure.services.media.models;

import java.util.Date;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.ContentKeyRestType;

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
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return getContent().getId();
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
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return getContent().getName();
    }

    /**
     * Gets the check sum.
     * 
     * @return the check sum
     */
    public String getChecksum() {
        return getContent().getChecksum();
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
     * Gets the protection key id.
     * 
     * @return the protection key id
     */
    public String getProtectionKeyId() {
        return getContent().getProtectionKeyId();
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
     * Gets the authorization policy id.
     * 
     * @return the authorization policy id
     */
    public String getAuthorizationPolicyId() {
        return getContent().getAuthorizationPolicyId();
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
