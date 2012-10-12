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

package com.microsoft.windowsazure.services.media.implementation.content;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Wrapper DTO for Media Services Locator.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class LocatorRestType implements MediaServiceDTO {

    /** The id. */
    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    protected String id;

    /** The expiration date time. */
    @XmlElement(name = "ExpirationDateTime", namespace = Constants.ODATA_DATA_NS)
    protected Date expirationDateTime;

    /** The type. */
    @XmlElement(name = "Type", namespace = Constants.ODATA_DATA_NS)
    protected int type;

    /** The path. */
    @XmlElement(name = "Path", namespace = Constants.ODATA_DATA_NS)
    protected String path;

    /** The access policy id. */
    @XmlElement(name = "AccessPolicyId", namespace = Constants.ODATA_DATA_NS)
    protected String accessPolicyId;

    /** The asset id. */
    @XmlElement(name = "AssetId", namespace = Constants.ODATA_DATA_NS)
    protected String assetId;

    /** The start time. */
    @XmlElement(name = "StartTime", namespace = Constants.ODATA_DATA_NS)
    protected Date startTime;

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id to set
     * @return the locator rest type
     */
    public LocatorRestType setId(String id) {
        this.id = id;
        return this;
    }

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
     * @return the locator rest type
     */
    public LocatorRestType setExpirationDateTime(Date expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
        return this;
    }

    /**
     * Gets the type.
     * 
     * @return the type
     */
    public int getType() {
        return this.type;
    }

    /**
     * Sets the type.
     * 
     * @param type
     *            the type
     * @return the locator rest type
     */
    public LocatorRestType setType(int type) {
        this.type = type;
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
     * Gets the access policy id.
     * 
     * @return the access policy id
     */
    public String getAccessPolicyId() {
        return this.accessPolicyId;
    }

    /**
     * Sets the access policy id.
     * 
     * @param accessPolicyId
     *            the access policy id
     * @return the locator rest type
     */
    public LocatorRestType setAccessPolicyId(String accessPolicyId) {
        this.accessPolicyId = accessPolicyId;
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
     * Sets the asset id.
     * 
     * @param assetId
     *            the asset id
     * @return the locator rest type
     */
    public LocatorRestType setAssetId(String assetId) {
        this.assetId = assetId;
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

    /**
     * Sets the start time.
     * 
     * @param startTime
     *            the start time
     * @return the locator rest type
     */
    public LocatorRestType setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Sets the path.
     * 
     * @param path
     *            the path
     * @return the locator rest type
     */
    public LocatorRestType setPath(String path) {
        this.path = path;
        return this;
    }
}
