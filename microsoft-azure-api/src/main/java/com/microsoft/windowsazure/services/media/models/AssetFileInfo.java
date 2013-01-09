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
import com.microsoft.windowsazure.services.media.implementation.content.AssetFileType;

/**
 * The Class AssetFileInfo.
 */
public class AssetFileInfo extends ODataEntity<AssetFileType> {

    public AssetFileInfo(EntryType entry, AssetFileType content) {
        super(entry, content);
    }

    public AssetFileInfo() {
        super(new AssetFileType());
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return this.getContent().getId();
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return this.getContent().getName();
    }

    /**
     * Gets the content file size.
     * 
     * @return the content file size
     */
    public long getContentFileSize() {
        return this.getContent().getContentFileSize();
    }

    /**
     * Gets the parent asset id.
     * 
     * @return the parent asset id
     */
    public String getParentAssetId() {
        return this.getContent().getParentAssetId();
    }

    /**
     * Gets the encryption version.
     * 
     * @return the encryption version
     */
    public String getEncryptionVersion() {
        return this.getContent().getEncryptionVersion();
    }

    /**
     * Gets the encryption scheme.
     * 
     * @return the encryption scheme
     */
    public String getEncryptionScheme() {
        return this.getContent().getEncryptionScheme();
    }

    /**
     * Gets the checks if is encrypted.
     * 
     * @return the checks if is encrypted
     */
    public boolean getIsEncrypted() {
        return this.getContent().getIsEncrypted();
    }

    /**
     * Gets the encryption key id.
     * 
     * @return the encryption key id
     */
    public String getEncryptionKeyId() {
        return this.getContent().getEncryptionKeyId();
    }

    /**
     * Gets the initialization vector.
     * 
     * @return the initialization vector
     */
    public String getInitializationVector() {
        return this.getContent().getInitializationVector();
    }

    /**
     * Gets the checks if is primary.
     * 
     * @return the checks if is primary
     */
    public boolean getIsPrimary() {
        return this.getContent().getIsPrimary();
    }

    /**
     * Gets the last modified.
     * 
     * @return the last modified
     */
    public Date getLastModified() {
        return this.getContent().getLastModified();
    }

    /**
     * Gets the created.
     * 
     * @return the created
     */
    public Date getCreated() {
        return this.getContent().getCreated();
    }

    /**
     * Gets the mime type.
     * 
     * @return the mime type
     */
    public String getMimeType() {
        return this.getContent().getMimeType();
    }

    /**
     * Gets the content checksum.
     * 
     * @return the content checksum
     */
    public String getContentChecksum() {
        return this.getContent().getContentChecksum();
    }
}
