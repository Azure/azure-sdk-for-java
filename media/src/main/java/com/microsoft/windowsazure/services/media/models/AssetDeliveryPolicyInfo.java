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

import java.util.Date;
import java.util.EnumSet;
import java.util.Map;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.AssetDeliveryPolicyRestType;

/**
 * Type containing data about asset delivery policy.
 * 
 */
public class AssetDeliveryPolicyInfo extends ODataEntity<AssetDeliveryPolicyRestType> {

    /**
     * Creates a new {@link AssetDeliveryPolicyInfo} wrapping the given ATOM
     * entry and content objects.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public AssetDeliveryPolicyInfo(EntryType entry, AssetDeliveryPolicyRestType content) {
        super(entry, content);
    }

    /**
     * Get the asset delivery policy id.
     * 
     * @return the id.
     */
    public String getId() {
        return getContent().getId();
    }

    /**
     * Get the asset delivery policy name.
     * 
     * @return the name.
     */
    public String getName() {
        return getContent().getName();
    }

    /**
     * Get the asset delivery policy type.
     * 
     * @return the type.
     */
    public AssetDeliveryPolicyType getAssetDeliveryPolicyType() {
        return AssetDeliveryPolicyType.fromCode(getContent().getAssetDeliveryPolicyType());
    }

    /**
     * Get the asset delivery policy protocol.
     * 
     * @return the protocol.
     */
    public EnumSet<AssetDeliveryProtocol> getAssetDeliveryProtocol() {
        return AssetDeliveryProtocol.protocolsFromBits(getContent().getAssetDeliveryProtocol());
    }

    /**
     * Get the asset delivery policy configuration.
     * 
     * @return the configuration.
     */
    public Map<AssetDeliveryPolicyConfigurationKey, String> getAssetDeliveryConfiguration() {
        return getContent().getAssetDeliveryConfiguration();
    }

    /**
     * Get the asset delivery policy creation date.
     * 
     * @return the creation date.
     */
    public Date getCreated() {
        return getContent().getCreated();
    }

    /**
     * Get last date where any asset delivery policy's property was changed.
     * 
     * @return the last date where any property was changed.
     */
    public Date getLastModified() {
        return getContent().getLastModified();
    }

}
