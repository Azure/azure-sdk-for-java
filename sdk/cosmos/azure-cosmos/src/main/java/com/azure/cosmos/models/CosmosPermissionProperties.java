// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * The type Cosmos permission properties.
 */
public final class CosmosPermissionProperties extends Resource {

    static List<CosmosPermissionProperties> getFromV2Results(List<Permission> results) {
        return results.stream().map(permission -> new CosmosPermissionProperties(permission.toJson()))
                   .collect(Collectors.toList());
    }

    /**
     * Initialize a permission object.
     */
    public CosmosPermissionProperties() {
        super();
    }

    /**
     * Sets the id
     *
     * @param id the name of the resource.
     * @return the current {@link CosmosPermissionProperties} object
     */
    public CosmosPermissionProperties setId(String id) {
        super.setId(id);
        return this;
    }

    /**
     * Initialize a permission object from json string.
     *
     * @param jsonString the json string that represents the getPermission.
     */
    CosmosPermissionProperties(String jsonString) {
        super(jsonString);
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
     * @return the current {@link CosmosPermissionProperties} object
     */
    public CosmosPermissionProperties setResourceLink(String resourceLink) {
        super.set(Constants.Properties.RESOURCE_LINK, resourceLink);
        return this;
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
     * @return the current {@link CosmosPermissionProperties} object
     */
    public CosmosPermissionProperties setPermissionMode(PermissionMode permissionMode) {
        this.set(Constants.Properties.PERMISSION_MODE,
            permissionMode.toString().toLowerCase(Locale.ROOT));
        return this;
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
     * @param partitionKey the partition key.
     * @return the current {@link CosmosPermissionProperties} object
     */
    public CosmosPermissionProperties setResourcePartitionKey(PartitionKey partitionKey) {
        super.set(Constants.Properties.RESOURCE_PARTITION_KEY, BridgeInternal.getPartitionKeyInternal(partitionKey).toJson());
        return this;
    }

    Permission getV2Permissions() {
        return new Permission(this.toJson());
    }
}
