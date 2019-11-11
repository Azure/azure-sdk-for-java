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
import com.microsoft.windowsazure.services.media.implementation.content.LocatorRestType;

/**
 * The Class LocatorInfo.
 */
public class LocatorInfo extends ODataEntity<LocatorRestType> {

    /**
     * Instantiates a new locator info.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public LocatorInfo(EntryType entry, LocatorRestType content) {
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
     * Gets the expiration date time.
     * 
     * @return the expiration date time
     */
    public Date getExpirationDateTime() {
        return getContent().getExpirationDateTime();
    }

    /**
     * Gets the locator type.
     * 
     * @return the locator type
     */
    public LocatorType getLocatorType() {
        return LocatorType.fromCode(getContent().getType());
    }

    /**
     * Gets the path.
     * 
     * @return the path
     */
    public String getPath() {
        return getContent().getPath();
    }

    /**
     * Gets the access policy id.
     * 
     * @return the access policy id
     */
    public String getAccessPolicyId() {
        return getContent().getAccessPolicyId();
    }

    /**
     * Gets the asset id.
     * 
     * @return the asset id
     */
    public String getAssetId() {
        return getContent().getAssetId();
    }

    /**
     * Gets the start time.
     * 
     * @return the start time
     */
    public Date getStartTime() {
        return getContent().getStartTime();
    }

    /**
     * Gets the base uri.
     * 
     * @return the base uri
     */
    public String getBaseUri() {
        return getContent().getBaseUri();
    }

    /**
     * Gets the content access token.
     * 
     * @return the content access token
     */
    public String getContentAccessToken() {
        return this.getContent().getContentAccessComponent();
    }

    /**
     * Return a link that gets this locator's access policy
     * 
     * @return the link
     */
    public LinkInfo<AccessPolicyInfo> getAccessPolicyLink() {
        return this.<AccessPolicyInfo> getRelationLink("AccessPolicy");
    }

    /**
     * Return a link that gets this locator's asset
     * 
     * @return the link
     */
    public LinkInfo<AssetInfo> getAssetLink() {
        return this.<AssetInfo> getRelationLink("Asset");
    }
}
