// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Cosmos permission properties.
 */
public final class CosmosPermissionProperties {

    private Permission permission;
    private String databaseName;
    private String resourceToken;

    private String permissionName;
    private String containerName;
    private String resourceName;
    private ContainerChildResourceType resourceKind;
    private PermissionMode permissionMode;
    private PartitionKey resourcePartitionKey;

    /**
     * Initialize a permission object.
     */
    public CosmosPermissionProperties() {
    }

    /**
     * Sets the name of the permission resource.
     *
     * @param id the name of the resource.
     * @return the current {@link CosmosPermissionProperties} object
     */
    public CosmosPermissionProperties setId(String id) {
        this.permissionName = id;

        // Following is required by permission replace (PUT) scenario.
        if (this.permission != null) {
            this.permission.setId(id);
        }

        return this;
    }

    /**
     * Initialize a permission object from json string.
     *
     * @param jsonString the json string that represents the getPermission.
     */
    CosmosPermissionProperties(String jsonString) {
        this.permission = new Permission(jsonString);
        this.permissionName = permission.getId();
        this.permissionMode = permission.getPermissionMode();
        this.resourcePartitionKey = permission.getResourcePartitionKey();
        this.resourceToken = permission.getToken();

        String[] parts = StringUtils.split(Utils.trimBeginningAndEndingSlashes(permission.getResourceLink()), "/");

        if (parts.length < 4) {
            throw new IllegalArgumentException("jsonString");
        }

        this.databaseName = parts[1];
        this.containerName = parts[3];

        if (parts.length >= 6) {
            this.resourceName = parts[5];

            if (Paths.DOCUMENTS_PATH_SEGMENT.equalsIgnoreCase(parts[4])) {
                this.resourceKind = ContainerChildResourceType.ITEM;
            } else if (Paths.STORED_PROCEDURES_PATH_SEGMENT.equalsIgnoreCase(parts[4])) {
                this.resourceKind = ContainerChildResourceType.STORED_PROCEDURE;
            } else if (Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT.equalsIgnoreCase(parts[4])) {
                this.resourceKind = ContainerChildResourceType.USER_DEFINED_FUNCTION;
            } else if (Paths.TRIGGERS_PATH_SEGMENT.equalsIgnoreCase(parts[4])) {
                this.resourceKind = ContainerChildResourceType.TRIGGER;
            } else {
                throw new IllegalArgumentException("jsonString");
            }
        }
    }

    /**
     * Sets the name of the Cosmos container as the parent resource which is associated with this permission object.
     *
     * @param containerName the name of the Cosmos container representing the parent resource.
     * @return the current {@link CosmosPermissionProperties} object.
     */
    public CosmosPermissionProperties setContainerName(String containerName) {
        this.containerName = containerName;

        // Following is required by permission replace (PUT) scenario.
        if (this.permission != null) {
            this.permission.setResourceLink(this.databaseName);
            this.resourceToken = null;
        }

        return this;
    }

    /**
     * Gets the name of the Cosmos container as the parent resource which is associated with this permission object.
     *
     * @return the name of the Cosmos container representing the parent resource.
     */
    public String getContainerName() {
        return this.containerName;
    }

    /**
     * Gets the kind of resource that has a Cosmos container as the parent resource which is associated with this
     *   permission object.
     *
     * @return the kind of resource that has a Cosmos container as parent resource.
     */
    public ContainerChildResourceType getResourceKind() {
        return this.resourceKind;
    }

    /**
     * Gets the name of resource that has a Cosmos container as the parent resource which is associated with this
     *   permission object.
     *
     * @return the name of resource that has a Cosmos container as the parent resource.
     */
    public String getResourceName() {
        return this.resourceName;
    }

    /**
     * Sets the resource path represented by the name and kind of a resource that has a Cosmos container as the parent
     * resource which is associated with this permission object.
     *
     * @param resourceKind the kind of resource that has a Cosmos container as parent resource.
     * @param resourceName the name of resource that has a Cosmos container as the parent resource.
     * @return the current {@link CosmosPermissionProperties} object.
     */
    public CosmosPermissionProperties setResourcePath(ContainerChildResourceType resourceKind, String resourceName) {
        this.resourceKind = resourceKind;
        this.resourceName = resourceName;

        // Following is required by permission replace (PUT) scenario.
        if (this.permission != null) {
            this.permission.setResourceLink(this.databaseName);
            this.resourceToken = null;
        }

        return this;
    }

    /**
     * Gets the permission mode.
     *
     * @return the permission mode.
     */
    public PermissionMode getPermissionMode() {
        return this.permissionMode;
    }

    /**
     * Sets the permission mode.
     *
     * @param permissionMode the permission mode.
     * @return the current {@link CosmosPermissionProperties} object
     */
    public CosmosPermissionProperties setPermissionMode(PermissionMode permissionMode) {
        this.permissionMode = permissionMode;

        // Following is required by permission replace (PUT) scenario.
        if (this.permission != null) {
            this.permission.setPermissionMode(permissionMode);
            this.resourceToken = null;
        }

        return this;
    }

    /**
     * Gets the resource partition key associated with this permission object.
     *
     * @return the partition key.
     */
    public PartitionKey getResourcePartitionKey() {
        return this.resourcePartitionKey;
    }

    /**
     * Sets the resource partition key associated with this permission object.
     *
     * @param partitionKey the partition key.
     * @return the current {@link CosmosPermissionProperties} object.
     */
    public CosmosPermissionProperties setResourcePartitionKey(PartitionKey partitionKey) {
        this.resourcePartitionKey = partitionKey;

        // Following is required by permission replace (PUT) scenario.
        if (this.permission != null) {
            this.permission.setResourcePartitionKey(partitionKey);
            this.resourceToken = null;
        }

        return this;
    }

    Resource getResource() {
        return this.permission;
    }

    String getResourcePath(String databaseName) {
        StringBuilder resourcePrefixPath = new StringBuilder();

        if (databaseName == null || databaseName.isEmpty()) {
            throw new IllegalArgumentException("databaseName");
        }
        resourcePrefixPath.append(Paths.DATABASES_PATH_SEGMENT)
            .append("/").append(databaseName);

        if (this.containerName == null || this.containerName.isEmpty()) {
            throw new IllegalArgumentException("containerName");
        }
        resourcePrefixPath.append("/").append(Paths.COLLECTIONS_PATH_SEGMENT)
            .append("/").append(this.containerName);

        if (this.resourceName != null && !this.resourceName.isEmpty()) {
            switch (this.resourceKind) {
                case ITEM:
                    resourcePrefixPath.append("/").append(Paths.DOCUMENTS_PATH_SEGMENT);
                    break;
                case STORED_PROCEDURE:
                    resourcePrefixPath.append("/").append(Paths.STORED_PROCEDURES_PATH_SEGMENT);
                    break;
                case USER_DEFINED_FUNCTION:
                    resourcePrefixPath.append("/").append(Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT);
                    break;
                case TRIGGER:
                    resourcePrefixPath.append("/").append(Paths.TRIGGERS_PATH_SEGMENT);
                    break;
                default:
                    throw new IllegalArgumentException("resourceKind");
            }

            resourcePrefixPath.append("/").append(resourceName);
        }

        return resourcePrefixPath.toString();
    }

    Permission getResource(String databaseName) {
        if (this.permission != null) {
            return this.permission;
        }

        Permission permission = new Permission();
        permission.setId(this.permissionName);
        permission.setPermissionMode(this.permissionMode);
        permission.setResourceLink(getResourcePath(databaseName));

        if (this.resourcePartitionKey != null) {
            permission.setResourcePartitionKey(this.resourcePartitionKey);
        }

        return permission;
    }

    /**
     * Gets the name of the resource.
     *
     * @return the name of the resource.
     */
    public String getId() {
        return this.permissionName;
    }

    /**
     * Get the last modified timestamp associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the timestamp or null if the permission object was not yet registered with the Cosmos service (when
     *  executing a create, upsert or replace operation) or if it was changed through one of the setter methods.
     */
    public Instant getTimestamp() {
        if (this.permission != null) {
            return this.permission.getTimestamp();
        } else {
            return null;
        }
    }

    /**
     * Get the entity tag associated with the resource.
     * This is only relevant when getting response from the server.
     *
     * @return the Cosmos ETAG property or null if the permission object was not yet registered with the Cosmos service
     * (when executing a create, upsert or replace operation) or if it was changed through one of the setter methods.
     */
    public String getETag() {
        if (this.permission != null) {
            return this.permission.getETag();
        } else {
            return null;
        }
    }

    /**
     * Gets the access token granting the defined permission.
     *
     * @return the access token or null if the permission object was not yet registered with the Cosmos service (when
     *  executing a create, upsert or replace operation) or if it was changed through one of the setter methods.
     */
    public String getToken() {
        return this.resourceToken;
    }

    Permission getPermission(String databaseName) {
        return getResource(databaseName);
    }

    Permission getPermission() {
        return this.permission;
    }

    static List<CosmosPermissionProperties> getPermissions(List<Permission> results) {
        return results.stream().map(permission -> new CosmosPermissionProperties(permission.toJson()))
            .collect(Collectors.toList());
    }
}
