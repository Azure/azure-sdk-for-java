// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Constants;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Used internally to represent a physical address in the Azure Cosmos DB database service.
 */
public class Address extends Resource {

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represent the
     * {@link JsonSerializable}
     */
    public Address(ObjectNode objectNode) {
        super(objectNode);
    }

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

    public boolean isPrimary() {
        return Boolean.TRUE.equals(super.getBoolean(Constants.Properties.IS_PRIMARY));
    }

    void setIsPrimary(boolean isPrimary) {
        this.set(Constants.Properties.IS_PRIMARY, isPrimary);
    }

    public String getProtocolScheme() {
        return super.getString(Constants.Properties.PROTOCOL);
    }

    void setProtocol(String protocol) {
        this.set(Constants.Properties.PROTOCOL, protocol);
    }

    public String getLogicalUri() {
        return super.getString(Constants.Properties.LOGICAL_URI);
    }

    void setLogicalUri(String logicalUri) {
        this.set(Constants.Properties.LOGICAL_URI, logicalUri);
    }

    public String getPhyicalUri() {
        return super.getString(Constants.Properties.PHYISCAL_URI);
    }

    void setPhysicalUri(String phyicalUri) {
        this.set(Constants.Properties.PHYISCAL_URI, phyicalUri);
    }

    public String getPartitionIndex() {
        return super.getString(Constants.Properties.PARTITION_INDEX);
    }

    void setPartitionIndex(String partitionIndex) {
        this.set(Constants.Properties.PARTITION_INDEX, partitionIndex);
    }

    public String getParitionKeyRangeId() {
        return super.getString(Constants.Properties.PARTITION_KEY_RANGE_ID);
    }

    public void setPartitionKeyRangeId(String partitionKeyRangeId) {
        this.set(Constants.Properties.PARTITION_KEY_RANGE_ID, partitionKeyRangeId);
    }

    @Override
    public Object get(String propertyName) {
        return super.get(propertyName);
    }
}
