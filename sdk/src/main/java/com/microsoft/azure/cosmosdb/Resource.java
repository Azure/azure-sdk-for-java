/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

import java.util.Date;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Represents the base resource in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public class Resource extends JsonSerializable {

    /**
     * Constructor.
     */
    protected Resource() {
        super();
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the resource.
     * @param objectMapper the custom object mapper
     */
    protected Resource(String jsonString, ObjectMapper objectMapper) {
        super(jsonString, objectMapper);
    }
    
    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the resource.
     */
    protected Resource(String jsonString) {
        super(jsonString);
    }

    /**
     * Constructor.
     *
     * @param jsonObject the json object that represents the resource.
     */
    protected Resource(JSONObject jsonObject) {
        super(jsonObject);
    }

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return super.getString(Constants.Properties.ID);
    }

    /**
     * Sets the name of the resource.
     *
     * @param id the name of the resource.
     */
    public void setId(String id) {
        super.set(Constants.Properties.ID, id);
    }

    /**
     * Gets the ID associated with the resource.
     *
     * @return the ID associated with the resource.
     */
    public String getResourceId() {
        return super.getString(Constants.Properties.R_ID);
    }

    /**
     * Set the ID associated with the resource.
     *
     * @param resourceId the ID associated with the resource.
     */
    public void setResourceId(String resourceId) {
        super.set(Constants.Properties.R_ID, resourceId);
    }

    /**
     * Get the self-link associated with the resource.
     *
     * @return the self link.
     */
    public String getSelfLink() {
        return super.getString(Constants.Properties.SELF_LINK);
    }

    /**
     * Set the self-link associated with the resource.
     *
     * @param selfLink the self link.
     */
    void setSelfLink(String selfLink) {
        super.set(Constants.Properties.SELF_LINK, selfLink);
    }

    /**
     * Get the last modified timestamp associated with the resource.
     *
     * @return the timestamp.
     */
    public Date getTimestamp() {
        Double millisec = super.getDouble(Constants.Properties.LAST_MODIFIED);
        if (millisec == null) return null;
        // Multiply the existing 10 digit long value by 1000 to pass to Date. 
        return new Date(millisec.longValue() * 1000);
    }

    /**
     * Set the last modified timestamp associated with the resource.
     *
     * @param timestamp the timestamp.
     */
    void setTimestamp(Date timestamp) {
        double millisec = timestamp.getTime();
        super.set(Constants.Properties.LAST_MODIFIED, millisec);
    }

    /**
     * Get the entity tag associated with the resource.
     *
     * @return the e tag.
     */
    public String getETag() {
        return super.getString(Constants.Properties.E_TAG);
    }

    /**
     * Set the self-link associated with the resource.
     *
     * @param eTag the e tag.
     */
    void setETag(String eTag) {
        super.set(Constants.Properties.E_TAG, eTag);
    }
}
