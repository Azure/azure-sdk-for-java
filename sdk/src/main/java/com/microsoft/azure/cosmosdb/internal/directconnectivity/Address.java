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

package com.microsoft.azure.cosmosdb.internal.directconnectivity;

import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Used internally to represent a physical address in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public class Address extends Resource {
    /**
     * Initialize an offer object.
     */
    public Address() {
        super();
    }

    /**
     * Initialize an address object from json string.
     *
     * @param jsonString the json string that represents the address.
     */
    public Address(String jsonString) {
        super(jsonString);
    }

    /**
     * Initialize an address object from json object.
     *
     * @param jsonObject the json object that represents the address.
     */
    public Address(JSONObject jsonObject) {
        super(jsonObject);
    }

    public boolean IsPrimary() {
        return super.getBoolean(Constants.Properties.IS_PRIMARY);
    }

    void setIsPrimary(boolean isPrimary) {
        super.set(Constants.Properties.IS_PRIMARY, isPrimary);
    }

    public String getProtocol() {
        return super.getString(Constants.Properties.PROTOCOL);
    }

    void setProtocol(String protocol) {
        super.set(Constants.Properties.PROTOCOL, protocol);
    }

    public String getLogicalUri() {
        return super.getString(Constants.Properties.LOGICAL_URI);
    }

    void setLogicalUri(String logicalUri) {
        super.set(Constants.Properties.LOGICAL_URI, logicalUri);
    }

    public String getPhyicalUri() {
        return super.getString(Constants.Properties.PHYISCAL_URI);
    }

    void setPhysicalUri(String phyicalUri) {
        super.set(Constants.Properties.PHYISCAL_URI, phyicalUri);
    }

    public String getPartitionIndex() {
        return super.getString(Constants.Properties.PARTITION_INDEX);
    }

    void setPartitionIndex(String partitionIndex) {
        super.set(Constants.Properties.PARTITION_INDEX, partitionIndex);
    }

    public String getParitionKeyRangeId() {
        return super.getString(Constants.Properties.PARTITION_KEY_RANGE_ID);
    }

    public void setPartitionKeyRangeId(String partitionKeyRangeId) {
        super.set(Constants.Properties.PARTITION_KEY_RANGE_ID, partitionKeyRangeId);
    }
}
