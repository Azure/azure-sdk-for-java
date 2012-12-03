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
     * Instantiates a new locator info.
     */
    public LocatorInfo() {
        super(new LocatorRestType());
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
     * @return the locator info
     */
    public LocatorInfo setId(String id) {
        getContent().setId(id);
        return this;
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
     * Sets the expiration date time.
     * 
     * @param expirationDateTime
     *            the expiration date time
     * @return the locator info
     */
    public LocatorInfo setExpirationDateTime(Date expirationDateTime) {
        getContent().setExpirationDateTime(expirationDateTime);
        return this;
    }

    /**
     * Sets the locator type.
     * 
     * @param locatorType
     *            the locator type
     * @return the locator info
     */
    public LocatorInfo setLocatorType(LocatorType locatorType) {
        getContent().setType(locatorType.getCode());
        return this;
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
     * Sets the path.
     * 
     * @param path
     *            the path
     * @return the locator info
     */
    public LocatorInfo setPath(String path) {
        getContent().setPath(path);
        return this;
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
     * Sets the access policy id.
     * 
     * @param accessPolicyId
     *            the access policy id
     * @return the locator info
     */
    public LocatorInfo setAccessPolicyId(String accessPolicyId) {
        getContent().setAccessPolicyId(accessPolicyId);
        return this;
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
     * Sets the asset id.
     * 
     * @param assetId
     *            the asset id
     * @return the locator info
     */
    public LocatorInfo setAssetId(String assetId) {
        getContent().setAssetId(assetId);
        return this;
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
     * Sets the start time.
     * 
     * @param startTime
     *            the start time
     * @return the locator info
     */
    public LocatorInfo setStartTime(Date startTime) {
        getContent().setStartTime(startTime);
        return this;
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
     * Sets the base uri.
     * 
     * @param baseUri
     *            the base uri
     * @return the locator info
     */
    public LocatorInfo setBaseUri(String baseUri) {
        this.getContent().setBaseUri(baseUri);
        return this;
    }

    /**
     * Sets the content access component.
     * 
     * @param contentAccessComponent
     *            the content access component
     * @return the locator info
     */
    public LocatorInfo setContentAccessComponent(String contentAccessComponent) {
        this.getContent().setContentAccessComponent(contentAccessComponent);
        return this;
    }

    /**
     * Gets the content access token.
     * 
     * @return the content access token
     */
    public String getContentAccessToken() {
        return this.getContent().getContentAccessComponent();
    }

}
