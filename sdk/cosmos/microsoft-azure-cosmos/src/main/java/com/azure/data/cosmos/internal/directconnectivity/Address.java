// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.Resource;
import com.azure.data.cosmos.internal.Constants;

/**
 * Used internally to represent a physical address in the Azure Cosmos DB database service.
 */
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

    public boolean IsPrimary() {
        return super.getBoolean(Constants.Properties.IS_PRIMARY);
    }

    void setIsPrimary(boolean isPrimary) {
        BridgeInternal.setProperty(this, Constants.Properties.IS_PRIMARY, isPrimary);
    }

    public String getProtocolScheme() {
        return super.getString(Constants.Properties.PROTOCOL);
    }

    void setProtocol(String protocol) {
        BridgeInternal.setProperty(this, Constants.Properties.PROTOCOL, protocol);
    }

    public String getLogicalUri() {
        return super.getString(Constants.Properties.LOGICAL_URI);
    }

    void setLogicalUri(String logicalUri) {
        BridgeInternal.setProperty(this, Constants.Properties.LOGICAL_URI, logicalUri);
    }

    public String getPhyicalUri() {
        return super.getString(Constants.Properties.PHYISCAL_URI);
    }

    void setPhysicalUri(String phyicalUri) {
        BridgeInternal.setProperty(this, Constants.Properties.PHYISCAL_URI, phyicalUri);
    }

    public String getPartitionIndex() {
        return super.getString(Constants.Properties.PARTITION_INDEX);
    }

    void setPartitionIndex(String partitionIndex) {
        BridgeInternal.setProperty(this, Constants.Properties.PARTITION_INDEX, partitionIndex);
    }

    public String getParitionKeyRangeId() {
        return super.getString(Constants.Properties.PARTITION_KEY_RANGE_ID);
    }

    public void setPartitionKeyRangeId(String partitionKeyRangeId) {
        BridgeInternal.setProperty(this, Constants.Properties.PARTITION_KEY_RANGE_ID, partitionKeyRangeId);
    }
}
