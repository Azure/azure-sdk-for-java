/**
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
 * The Class UpdateLocatorOptions.
 */
public class UpdateLocatorOptions {

    /** The expiration date time. */
    private Date expirationDateTime;

    /** The type. */
    private LocatorType type;

    /** The path. */
    private String path;

    /** The access policy id. */
    private String accessPolicyId;

    /** The asset id. */
    private String assetId;

    /** The start time. */
    private Date startTime;

    /**
     * Gets the expiration date time.
     * 
     * @return the expiration date time
     */
    public Date getExpirationDateTime() {
        return expirationDateTime;
    }

    /**
     * Sets the expiration date time.
     * 
     * @param expirationDateTime
     *            the expiration date time
     * @return the creates the locator options
     */
    public UpdateLocatorOptions setExpirationDateTime(Date expirationDateTime) {
        expirationDateTime = expirationDateTime;
        return this;
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public LocatorType getType() {
        return type;
    }

    /**
     * Sets the type.
     * 
     * @param type
     *            the type
     * @return the creates the locator options
     */
    public UpdateLocatorOptions setType(LocatorType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets the path.
     * 
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the path.
     * 
     * @param path
     *            the path
     * @return the creates the locator options
     */
    public UpdateLocatorOptions setPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets the access policy id.
     * 
     * @return the access policy id
     */
    public String getAccessPolicyId() {
        return accessPolicyId;
    }

    /**
     * Sets the access policy id.
     * 
     * @param accessPolicyId
     *            the access policy id
     * @return the creates the locator options
     */
    public UpdateLocatorOptions setAccessPolicyId(String accessPolicyId) {
        this.accessPolicyId = accessPolicyId;
        return this;
    }

    /**
     * Gets the asset id.
     * 
     * @return the asset id
     */
    public String getAssetId() {
        return assetId;
    }

    /**
     * Sets the asset id.
     * 
     * @param assetId
     *            the asset id
     * @return the creates the locator options
     */
    public UpdateLocatorOptions setAssetId(String assetId) {
        this.assetId = assetId;
        return this;
    }

    /**
     * Gets the start time.
     * 
     * @return the start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     * 
     * @param startTime
     *            the start time
     * @return the creates the locator options
     */
    public UpdateLocatorOptions setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

}