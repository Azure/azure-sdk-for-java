// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.Permission;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CosmosPermissionProperties extends Resource {

    public static List<CosmosPermissionProperties> getFromV2Results(List<Permission> results) {
        return results.stream().map(permission -> new CosmosPermissionProperties(permission.toJson())).collect(Collectors.toList());
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
    public CosmosPermissionProperties id(String id) {
        super.id(id);
        return this;
    }

    /**
     * Initialize a permission object from json string.
     *
     * @param jsonString the json string that represents the permission.
     */
    CosmosPermissionProperties(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets the self-link of resource to which the permission applies.
     *
     * @return the resource link.
     */
    public String resourceLink() {
        return super.getString(Constants.Properties.RESOURCE_LINK);
    }

    /**
     * Sets the self-link of resource to which the permission applies.
     *
     * @param resourceLink the resource link.
     * @return the current {@link CosmosPermissionProperties} object
     */
    public CosmosPermissionProperties resourceLink(String resourceLink) {
        super.set(Constants.Properties.RESOURCE_LINK, resourceLink);
        return this;
    }

    /**
     * Gets the permission mode.
     *
     * @return the permission mode.
     */
    public PermissionMode permissionMode() {
        String value = super.getString(Constants.Properties.PERMISSION_MODE);
        return PermissionMode.valueOf(StringUtils.upperCase(value));
    }

    /**
     * Sets the permission mode.
     *
     * @param permissionMode the permission mode.
     * @return the current {@link CosmosPermissionProperties} object
     */
    public CosmosPermissionProperties permissionMode(PermissionMode permissionMode) {
        this.set(Constants.Properties.PERMISSION_MODE,
                permissionMode.toString().toLowerCase());
        return this;
    }

    //TODO: need value from JsonSerializable
//    /**
//     * Gets the resource partition key associated with this permission object.
//     *
//     * @return the partition key.
//     */
//    public PartitionKey getResourcePartitionKey() {
//        PartitionKey key = null;
//        Object value = super.get(Constants.Properties.RESOURCE_PARTITION_KEY);
//        if (value != null) {
//            ArrayNode arrayValue = (ArrayNode) value;
//            key = new PartitionKey(value(arrayValue.get(0)));
//        }
//
//        return key;
//    }

    /**
     * Sets the resource partition key associated with this permission object.
     *
     * @param partitionKey the partition key.
     * @return the current {@link CosmosPermissionProperties} object
     */
    public CosmosPermissionProperties resourcePartitionKey(PartitionKey partitionKey) {
        super.set(Constants.Properties.RESOURCE_PARTITION_KEY, partitionKey.getInternalPartitionKey().toJson());
        return this;
    }

    Permission getV2Permissions() {
        return new Permission(this.toJson());
    }
}
