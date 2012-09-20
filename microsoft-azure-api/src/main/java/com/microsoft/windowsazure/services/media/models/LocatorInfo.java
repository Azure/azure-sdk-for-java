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
 * The Class LocatorInfo.
 */
public class LocatorInfo {

    /** The id. */
    private String id;

    /** The expiration datetime. */
    private Date expirationDatetime;

    /** The path. */
    private String path;

    /** The access policy id. */
    private String accessPolicyId;

    /** The asset id. */
    private String assetId;

    /** The start time. */
    private Date startTime;

    /** The locator type. */
    private LocatorType locatorType;

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
     * @return the locator info
     */
    public LocatorInfo setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the expiration date time.
     * 
     * @param expirationDateTime
     *            the expiration date time
     * @return the locator info
     */
    public LocatorInfo setExpirationDateTime(Date expirationDateTime) {
        this.expirationDatetime = expirationDateTime;
        return this;
    }

    /**
     * Gets the expiration date time.
     * 
     * @return the expiration date time
     */
    public Date getExpirationDateTime() {
        return this.expirationDatetime;
    }

    /**
     * Sets the locator type.
     * 
     * @param locatorType
     *            the locator type
     * @return the locator info
     */
    public LocatorInfo setLocatorType(LocatorType locatorType) {
        this.locatorType = locatorType;
        return this;
    }

    /**
     * Gets the locator type.
     * 
     * @return the locator type
     */
    public LocatorType getLocatorType() {
        return this.locatorType;
    }

    /**
     * Sets the path.
     * 
     * @param path
     *            the path
     * @return the locator info
     */
    public LocatorInfo setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets the path.
     * 
     * @return the path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Sets the access policy id.
     * 
     * @param accessPolicyId
     *            the access policy id
     * @return the locator info
     */
    public LocatorInfo setAccessPolicyId(String accessPolicyId) {
        this.accessPolicyId = accessPolicyId;
        return this;
    }

    /**
     * Gets the access policy id.
     * 
     * @return the access policy id
     */
    public String getAccessPolicyId() {
        return this.accessPolicyId;
    }

    /**
     * Sets the asset id.
     * 
     * @param assetId
     *            the asset id
     * @return the locator info
     */
    public LocatorInfo setAssetId(String assetId) {
        this.assetId = assetId;
        return this;
    }

    /**
     * Gets the asset id.
     * 
     * @return the asset id
     */
    public String getAssetId() {
        return this.assetId;
    }

    /**
     * Sets the start time.
     * 
     * @param startTime
     *            the start time
     * @return the locator info
     */
    public LocatorInfo setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Gets the start time.
     * 
     * @return the start time
     */
    public Date getStartTime() {
        return this.startTime;
    }

}
