// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.util.Beta;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;

/**
 * Represents the change feed policy configuration for the container in the Azure Cosmos DB service.
 *
 * <p>
 * The example below creates a new container with a change feed policy for full fidelity change feed with a
 * retention window of 8 minutes - so intermediary snapshots of changes as well as deleted documents would be
 * available for processing for 8 minutes before they vanish. Processing the change feed with full fidelity mode will
 * only be able within this retention window - if you attempt to process a change feed after more
 * than the retention window (8 minutes in this sample) an error (Status Code 400) will be returned. It would
 * still be possible to process changes using Incremental mode even when configuring a full fidelity change
 * feed policy with retention window on the container and when using Incremental mode it doesn't matter whether
 * your are out of the retention window or not.
 *
 * <pre>{@code
 *
 * CosmosContainerProperties containerProperties =
 *      new CosmosContainerProperties("ContainerName", "/somePartitionKeyProperty");
 * containerProperties.setChangeFeedPolicy(ChangeFeedPolicy.createFullFidelityPolicy(8));
 *
 * CosmosAsyncDatabase database = client.createDatabase(databaseProperties).block().getDatabase();
 * CosmosAsyncContainer container = database.createContainer(containerProperties).block().getContainer();
 *
 * }
 * </pre>
 * <p>
 * The example below creates a new container with a change feed policy for incremental change feed. Processing
 * the change feed with full fidelity mode will not be possible for this container. It would still be possible to
 * process changes using Incremental mode. The Incremental change feed policy is also the default that
 * is used when not explicitly specifying a change feed policy.
 *
 * <pre>{@code
 *
 * CosmosContainerProperties containerProperties =
 *      new CosmosContainerProperties("ContainerName", "/somePartitionKeyProperty");
 * containerProperties.setChangeFeedPolicy(ChangeFeedPolicy.createIncrementalPolicy());
 *
 * CosmosAsyncDatabase database = client.createDatabase(databaseProperties).block().getDatabase();
 * CosmosAsyncContainer container = database.createContainer(containerProperties).block().getContainer();
 *
 * }
 * </pre>
 */
@Beta(value = Beta.SinceVersion.V4_12_0,
    warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public final class ChangeFeedPolicy {

    private final JsonSerializable jsonSerializable;

    /**
     * Creates a ChangeFeedPolicy with retention duration for full fidelity processing
     * <p>
     * @param retentionDuration  - the retention duration (max granularity in minutes) in which it
     *                             will be possible to process change feed events with full fidelity
     *                             mode (meaning intermediary changes and deletes
     *                             will be exposed in change feed).
     *
     * @return ChangeFeedPolicy for full fidelity change feed.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0,
        warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static ChangeFeedPolicy createFullFidelityPolicy(Duration retentionDuration) {

        if (retentionDuration.isNegative() ||
            retentionDuration.isZero() ||
            retentionDuration.getNano() != 0 ||
            retentionDuration.getSeconds() % 60 != 0) {
            throw new IllegalArgumentException(
                "Argument retentionDuration must be a duration of a positive number of minutes."
            );
        }

        ChangeFeedPolicy policy = new ChangeFeedPolicy();
        policy.setFullFidelityRetentionDurationInMinutes((int)(retentionDuration.getSeconds() / 60));
        return policy;
    }

    /**
     * Creates a default ChangeFeedPolicy without retention duration specified. With the default/incremental
     * change feed it will not be possible to process intermediary changes or deletes.
     * <p>
     * This is the default policy being used when not specifying any ChangeFeedPolicy for the Container.
     * </p>
     *
     * @return ChangeFeedPolicy for default/incremental change feed without full fidelity.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0,
        warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public static ChangeFeedPolicy createIncrementalPolicy() {

        ChangeFeedPolicy policy = new ChangeFeedPolicy();
        policy.setFullFidelityRetentionDurationInMinutes(null);
        return policy;
    }

    /**
     * Initializes a new instance of the {@link ChangeFeedPolicy} class for the Azure Cosmos DB service.
     */
    ChangeFeedPolicy() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Instantiates a new change feed policy.
     *
     * @param jsonString the json string
     */
    ChangeFeedPolicy(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }

    /**
     * Instantiates a new change feed policy.
     *
     * @param objectNode the object node.
     */
    ChangeFeedPolicy(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Gets the retention duration in which it will be possible to
     * process change feed events with full fidelity mode (meaning intermediary changes and deletes
     * will be exposed in change feed).
     * By default full fidelity change feed is not enabled - so the retention duration would be Duration.ZERO.
     *
     * @return full fidelity retention duration.
     */
    @Beta(value = Beta.SinceVersion.V4_12_0,
        warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    public Duration getFullFidelityRetentionDuration() {
        return Duration.ofMinutes(this.getFullFidelityRetentionDurationInMinutes());
    }

    /**
     * Gets the retention duration in minutes in which it will be possible to
     * process change feed events with full fidelity mode (meaning intermediary changes and deletes
     * will be exposed in change feed).
     * By default full fidelity change feed is not enabled - so the retention duration would be 0.
     *
     * @return full fidelity retention duration in minutes.
     */
    int getFullFidelityRetentionDurationInMinutes() {

        Integer intValue = this.jsonSerializable.getInt(Constants.Properties.LOG_RETENTION_DURATION);

        if (intValue == null || intValue < 0) {
            return 0;
        }

        return intValue;
    }

    /**
     * Sets the retention duration in minutes in which it will be possible to
     * process change feed events with full fidelity mode (meaning intermediary changes
     * and deletes will be exposed in change feed).
     * If the value of the {@param retentionDurationInMinutes} argument is null, 0 or negative
     * no full fidelity change feed is available for the container and change feed events can only
     * be processed with the default mode "Incremental".
     *
     * @param retentionDurationInMinutes - Full fidelity retention duration in minutes.
     */
    ChangeFeedPolicy setFullFidelityRetentionDurationInMinutes(Integer retentionDurationInMinutes) {
        if (retentionDurationInMinutes == null || retentionDurationInMinutes <= 0) {
            this.jsonSerializable.set(
                Constants.Properties.LOG_RETENTION_DURATION,
                0);
        }
        else {
            this.jsonSerializable.set(
                Constants.Properties.LOG_RETENTION_DURATION,
                retentionDurationInMinutes);
        }

        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }
}
