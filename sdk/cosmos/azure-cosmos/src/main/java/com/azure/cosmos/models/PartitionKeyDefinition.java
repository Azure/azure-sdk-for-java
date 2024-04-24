// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a partition key definition in the Azure Cosmos DB database service. A partition key definition
 * specifies which
 * item property is used as the partition key in a container that has multiple partitions.
 */
public final class PartitionKeyDefinition {
    private List<String> paths;
    private PartitionKind kind;
    private Optional<PartitionKeyDefinitionVersion> versionOptional;
    private Boolean systemKey;

    private JsonSerializable jsonSerializable;

    /**
     * Constructor. Creates a new instance of the PartitionKeyDefinition object.
     */
    public PartitionKeyDefinition() {
        this.jsonSerializable = new JsonSerializable();
        this.setKind(PartitionKind.HASH);
    }

    /**
     * Constructor. Creates a new instance of the PartitionKeyDefinition object from a
     * JSON string.
     *
     * @param jsonString the JSON string that represents the partition key definition.
     */
    PartitionKeyDefinition(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }

    /**
     * Constructor. Creates a new instance of the PartitionKeyDefinition object from a
     * JSON string.
     *
     * @param objectNode the object node that represents the partition key definition.
     */
    PartitionKeyDefinition(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Sets the partition algorithm used to calculate the partition id given a partition key.
     *
     * @return the partition algorithm.
     */
    public PartitionKind getKind() {
        if (this.kind == null) {
            this.kind = this.jsonSerializable.getObject(Constants.Properties.PARTITION_KIND, PartitionKind.class, true);
        }

        return this.kind;
    }

    /**
     * Sets the partition algorithm used to calculate the partition id given a partition key.
     *
     * @param kind the partition algorithm.
     * @return this PartitionKeyDefinition.
     */
    public PartitionKeyDefinition setKind(PartitionKind kind) {
        this.kind = kind;
        return this;
    }

    /**
     * Gets version.
     *
     * @return the {@link PartitionKeyDefinitionVersion}
     */
    public PartitionKeyDefinitionVersion getVersion() {
        if (this.versionOptional == null) {
            Object versionObject = this.jsonSerializable.getObject(Constants.Properties.PARTITION_KEY_DEFINITION_VERSION, Object.class);
            if (versionObject == null) {
                this.versionOptional = Optional.empty();
            } else {
                String versionStr = String.valueOf(versionObject);
                if (StringUtils.isNumeric(versionStr)) {
                    this.versionOptional = Optional.of(PartitionKeyDefinitionVersion.valueOf(String.format("V%d",
                        Integer.parseInt(versionStr))));
                } else {
                    this.versionOptional = !Strings.isNullOrEmpty(versionStr)
                               ? Optional.of(PartitionKeyDefinitionVersion.valueOf(StringUtils.upperCase(versionStr)))
                               : Optional.empty();
                }
            }

            assert versionOptional != null;
        }

        return this.versionOptional.isPresent() ? this.versionOptional.get() : null;
    }

    /**
     * Sets version.
     *
     * @param version the version
     * @return the version
     */
    public PartitionKeyDefinition setVersion(PartitionKeyDefinitionVersion version) {
        this.versionOptional = Optional.of(version);
        return this;
    }

    /**
     * Gets the item property paths for the partition key.
     *
     * @return the paths to the item properties that form the partition key.
     */
    public List<String> getPaths() {
        if (this.paths == null) {
            if (this.jsonSerializable.has(Constants.Properties.PARTITION_KEY_PATHS)) {
                paths = this.jsonSerializable.getList(Constants.Properties.PARTITION_KEY_PATHS, String.class);
            } else {
                paths = new ArrayList<>();
            }
        }

        return paths;
    }

    /**
     * Sets the item property paths for the partition key.
     *
     * @param paths the paths to item properties that form the partition key.
     * @return this PartitionKeyDefinition.
     * @throws IllegalArgumentException thrown if an error occurs
     */
    public PartitionKeyDefinition setPaths(List<String> paths) {
        if (paths == null || paths.size() == 0) {
            throw new IllegalArgumentException("getPaths must not be null or empty.");
        }

        this.paths = paths;
        return this;
    }

    /**
     * Indicates if the partition key is generated by the system.
     *
     * @return the boolean indicating is it is a system key.
     */
    Boolean isSystemKey() {
        if (this.systemKey == null) {
            if (this.jsonSerializable.has(Constants.Properties.SYSTEM_KEY)) {
                this.systemKey = this.jsonSerializable.getBoolean(Constants.Properties.SYSTEM_KEY);
            } else {
                this.systemKey = false;
            }
        }

        return this.systemKey;
    }

    PartitionKeyInternal getNonePartitionKeyValue() {
        if (this.getPaths().size() == 0 || this.isSystemKey()) {
            return PartitionKeyInternal.Empty;
        } else {
            return PartitionKeyInternal.UndefinedPartitionKey;
        }
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
        if (this.kind != null) {
            this.jsonSerializable.set(
                Constants.Properties.PARTITION_KIND,
                kind.toString(),
                CosmosItemSerializer.DEFAULT_SERIALIZER);
        }
        if (this.paths != null) {
            this.jsonSerializable.set(
                Constants.Properties.PARTITION_KEY_PATHS,
                paths,
                CosmosItemSerializer.DEFAULT_SERIALIZER);
        }

        if (this.versionOptional != null && versionOptional.isPresent()) {
            this.jsonSerializable.set(
                Constants.Properties.PARTITION_KEY_DEFINITION_VERSION,
                versionOptional.get().toString(),
                CosmosItemSerializer.DEFAULT_SERIALIZER);
        }
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }
}
