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
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CosmosPermissionSettings extends Resource {


    public static List<CosmosPermissionSettings> getFromV2Results(List<Permission> results) {
        return results.stream().map(permission -> new CosmosPermissionSettings(permission.toJson())).collect(Collectors.toList());
    }

    /**
     * Initialize a permission object.
     */
    public CosmosPermissionSettings() {
        super();
    }

    /**
     * Initialize a permission object from json string.
     *
     * @param jsonString the json string that represents the permission.
     */
    public CosmosPermissionSettings(String jsonString) {
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
     */
    public void setResourceLink(String resourceLink) {
        super.set(Constants.Properties.RESOURCE_LINK, resourceLink);
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
        this.set(Constants.Properties.PERMISSION_MODE,
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
     * @param partitionkey the partition key.
     */
    public void setResourcePartitionKey(PartitionKey partitionkey) {
        super.set(Constants.Properties.RESOURCE_PARTITION_KEY, partitionkey.getInternalPartitionKey().toJson());
    }

    public Permission getV2Permissions() {
        return null;
    }
}
