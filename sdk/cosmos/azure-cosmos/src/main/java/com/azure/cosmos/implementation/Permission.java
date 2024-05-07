// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PermissionMode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Locale;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Represents a per-User Permission to access a specific resource e.g. item or container in the Azure Cosmos DB
 * database service.
 */
public final class Permission extends Resource {
    /**
     * Initialize a permission object.
     */
    public Permission() {
        super();
    }

    /**
     * Initialize a permission object from json string.
     *
     * @param jsonNode the json string that represents the permission.
     */
    public Permission(ObjectNode jsonNode) {
        super(jsonNode);
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the current instance of permission
     */
    public Permission setId(String id) {
        super.setId(id);
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
        this.set(Constants.Properties.RESOURCE_LINK, resourceLink, CosmosItemSerializer.DEFAULT_SERIALIZER);
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
        this.set(
            Constants.Properties.PERMISSION_MODE,
            permissionMode.toString().toLowerCase(Locale.ROOT),
            CosmosItemSerializer.DEFAULT_SERIALIZER);
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
            key = new PartitionKey(JsonSerializable.getValue(arrayValue.get(0)));
        }

        return key;
    }

    /**
     * Sets the resource partition key associated with this permission object.
     *
     * @param partitionkey the partition key.
     */
    public void setResourcePartitionKey(PartitionKey partitionkey) {
        checkNotNull(partitionkey, "Partition key can not be null");

        this.set(
            Constants.Properties.RESOURCE_PARTITION_KEY,
            ModelBridgeInternal.getPartitionKeyInternal(partitionkey).toObjectArray(),
            CosmosItemSerializer.DEFAULT_SERIALIZER);
    }
}
