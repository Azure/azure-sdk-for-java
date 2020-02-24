// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PermissionMode;
import com.azure.data.cosmos.Resource;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;

/**
 * Represents a per-User Permission to access a specific resource e.g. Document or Collection in the Azure Cosmos DB database service.
 */
public class Permission extends Resource {
    /**
     * Initialize a permission object.
     */
    public Permission() {
        super();
    }

    /**
     * Initialize a permission object from json string.
     *
     * @param jsonString the json string that represents the permission.
     */
    public Permission(String jsonString) {
        super(jsonString);
    }

    /**
     * Sets the id
     * @param id the name of the resource.
     * @return the current instance of permission
     */
    public Permission id(String id){
        super.id(id);
        return this;
    }

    /**
     * Gets the self-link of resource to which the permission applies.
     *
     * @return the resource link.
     */
    public String getResourceLink() {
        return super.getString(Constants.Properties.RESOURCE_LINK);
    }

    /**
     * Sets the self-link of resource to which the permission applies.
     *
     * @param resourceLink the resource link.
     */
    public void setResourceLink(String resourceLink) {
        BridgeInternal.setProperty(this, Constants.Properties.RESOURCE_LINK, resourceLink);
    }

    /**
     * Gets the permission mode.
     *
     * @return the permission mode.
     */
    public PermissionMode getPermissionMode() {
        String value = super.getString(Constants.Properties.PERMISSION_MODE);
        return PermissionMode.valueOf(StringUtils.upperCase(value));
    }

    /**
     * Sets the permission mode.
     *
     * @param permissionMode the permission mode.
     */
    public void setPermissionMode(PermissionMode permissionMode) {
        BridgeInternal.setProperty(this, Constants.Properties.PERMISSION_MODE,
                permissionMode.toString().toLowerCase());
    }

    /**
     * Gets the access token granting the defined permission.
     *
     * @return the access token.
     */
    public String getToken() {
        return super.getString(Constants.Properties.TOKEN);
    }

    /**
     * Gets the resource partition key associated with this permission object.
     *
     * @return the partition key.
     */
    public PartitionKey getResourcePartitionKey() {
        PartitionKey key = null;
        Object value = super.get(Constants.Properties.RESOURCE_PARTITION_KEY);
        if (value != null) {
            ArrayNode arrayValue = (ArrayNode) value;
            key = new PartitionKey(BridgeInternal.getValue(arrayValue.get(0)));
        }

        return key;
    }

    /**
     * Sets the resource partition key associated with this permission object.
     *
     * @param partitionkey the partition key.
     */
    public void setResourcePartitionKey(PartitionKey partitionkey) {
        BridgeInternal.setProperty(this, Constants.Properties.RESOURCE_PARTITION_KEY, partitionkey.getInternalPartitionKey().toJson());
    }
}
