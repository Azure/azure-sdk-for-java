// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents the global secondary index definition for a container in the Azure Cosmos DB service.
 * A global secondary index is derived from a source container and is defined by a SQL-like query.
 * Once created, the source container id and the query definition are immutable for the lifetime
 * of the global secondary index.
 * <p>
 * Example:
 * <pre>{@code
 * CosmosGlobalSecondaryIndexDefinition definition =
 *     new CosmosGlobalSecondaryIndexDefinition("gsi-src", "SELECT c.customerId, c.emailAddress FROM c");
 * }</pre>
 */
@Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class CosmosGlobalSecondaryIndexDefinition {

    private final JsonSerializable jsonSerializable;

    /**
     * Creates a new global secondary index definition.
     * The source container id and the query definition are immutable once the definition is created.
     *
     * @param sourceContainerId the id of the source container from which this global secondary index is derived.
     *                          The SDK will automatically resolve this container id to its resource id (RID)
     *                          during container creation.
     * @param definition the SQL-like query definition (e.g. {@code "SELECT c.customerId, c.emailAddress FROM c"}).
     * @throws IllegalArgumentException if {@code sourceContainerId} or {@code definition} is {@code null} or empty.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosGlobalSecondaryIndexDefinition(String sourceContainerId, String definition) {
        if (sourceContainerId == null || sourceContainerId.trim().isEmpty()) {
            throw new IllegalArgumentException("sourceContainerId cannot be null, empty, or blank");
        }
        if (definition == null || definition.trim().isEmpty()) {
            throw new IllegalArgumentException("definition cannot be null, empty, or blank");
        }
        this.jsonSerializable = new JsonSerializable();
        this.jsonSerializable.set(Constants.Properties.GLOBAL_SECONDARY_INDEX_SOURCE_COLLECTION_ID, sourceContainerId);
        this.jsonSerializable.set(Constants.Properties.GLOBAL_SECONDARY_INDEX_QUERY_DEFINITION, definition);
    }

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represents the GlobalSecondaryIndex definition.
     */
    CosmosGlobalSecondaryIndexDefinition(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Gets the source container id for the global secondary index.
     *
     * @return the source container id.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getSourceContainerId() {
        return this.jsonSerializable.getString(Constants.Properties.GLOBAL_SECONDARY_INDEX_SOURCE_COLLECTION_ID);
    }

    void setSourceContainerRidInternal(String sourceCollectionRid) {
        this.jsonSerializable.set(Constants.Properties.GLOBAL_SECONDARY_INDEX_SOURCE_COLLECTION_RID, sourceCollectionRid);
    }

    /**
     * Gets the source container resource id (RID) for the GlobalSecondaryIndex as returned by the server.
     * This is a read-only field populated from server responses.
     *
     * @return the source container resource id.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getSourceContainerRid() {
        return this.jsonSerializable.getString(Constants.Properties.GLOBAL_SECONDARY_INDEX_SOURCE_COLLECTION_RID);
    }

    /**
     * Gets the build status of the GlobalSecondaryIndex as returned by the server.
     * This is a read-only field populated from server responses. Returns {@code null} when the status field
     * is absent from the server response. When the server returns a value this SDK version does not declare
     * as a known constant, the returned instance still preserves the original wire value via
     * {@link CosmosGlobalSecondaryIndexBuildStatus#toString()}.
     *
     * @return the GlobalSecondaryIndex build status, or {@code null} if the server did not return one.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public CosmosGlobalSecondaryIndexBuildStatus getStatus() {
        return CosmosGlobalSecondaryIndexBuildStatus.fromString(
            this.jsonSerializable.getString(Constants.Properties.GLOBAL_SECONDARY_INDEX_STATUS));
    }

    /**
     * Gets the query definition for the GlobalSecondaryIndex.
     *
     * @return the query definition.
     */
    @Beta(value = Beta.SinceVersion.V4_81_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public String getDefinition() {
        return this.jsonSerializable.getString(Constants.Properties.GLOBAL_SECONDARY_INDEX_QUERY_DEFINITION);
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() {
        return this.jsonSerializable;
    }

    @Override
    public String toString() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to convert object to string", e);
        }
    }

    static void initialize() {
        ImplementationBridgeHelpers.CosmosGlobalSecondaryIndexDefinitionHelper
            .setCosmosGlobalSecondaryIndexDefinitionAccessor(
                (definition, sourceCollectionRid) -> definition.setSourceContainerRidInternal(sourceCollectionRid)
            );
    }
}
