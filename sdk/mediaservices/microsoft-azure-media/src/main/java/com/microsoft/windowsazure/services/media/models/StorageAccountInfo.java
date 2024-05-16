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

package com.microsoft.windowsazure.services.media.models;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.StorageAccountType;

/**
 * Data about a Media Services Asset entity.
 * 
 */
public class StorageAccountInfo extends ODataEntity<StorageAccountType> {

    /**
     * Instantiates a new asset info.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public StorageAccountInfo(EntryType entry, StorageAccountType content) {
        super(entry, content);
    }

    /**
     * Get the asset name.
     * 
     * @return the name
     */
    public String getName() {
        return this.getContent().getName();
    }

    /**
     * Get the bytes used
     * 
     * @return the bytes used
     */
    public long getBytesUsed() {
        return getContent().getBytesUsed();
    }

    /**
     * Gets true if this storage account is the default one.
     * 
     * @return true if this storage account is the default one, instead false.
     */
    public boolean isDefault() {
        return getContent().isDefault();
    }
}
