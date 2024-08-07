// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Information about the current running state of the overall upgrade.
 */
@Immutable
public final class RollingUpgradeRunningStatus implements JsonSerializable<RollingUpgradeRunningStatus> {
    /*
     * Code indicating the current status of the upgrade.
     */
    private RollingUpgradeStatusCode code;

    /*
     * Start time of the upgrade.
     */
    private OffsetDateTime startTime;

    /*
     * The last action performed on the rolling upgrade.
     */
    private RollingUpgradeActionType lastAction;

    /*
     * Last action time of the upgrade.
     */
    private OffsetDateTime lastActionTime;

    /**
     * Creates an instance of RollingUpgradeRunningStatus class.
     */
    public RollingUpgradeRunningStatus() {
    }

    /**
     * Get the code property: Code indicating the current status of the upgrade.
     * 
     * @return the code value.
     */
    public RollingUpgradeStatusCode code() {
        return this.code;
    }

    /**
     * Get the startTime property: Start time of the upgrade.
     * 
     * @return the startTime value.
     */
    public OffsetDateTime startTime() {
        return this.startTime;
    }

    /**
     * Get the lastAction property: The last action performed on the rolling upgrade.
     * 
     * @return the lastAction value.
     */
    public RollingUpgradeActionType lastAction() {
        return this.lastAction;
    }

    /**
     * Get the lastActionTime property: Last action time of the upgrade.
     * 
     * @return the lastActionTime value.
     */
    public OffsetDateTime lastActionTime() {
        return this.lastActionTime;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of RollingUpgradeRunningStatus from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of RollingUpgradeRunningStatus if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RollingUpgradeRunningStatus.
     */
    public static RollingUpgradeRunningStatus fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            RollingUpgradeRunningStatus deserializedRollingUpgradeRunningStatus = new RollingUpgradeRunningStatus();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("code".equals(fieldName)) {
                    deserializedRollingUpgradeRunningStatus.code
                        = RollingUpgradeStatusCode.fromString(reader.getString());
                } else if ("startTime".equals(fieldName)) {
                    deserializedRollingUpgradeRunningStatus.startTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("lastAction".equals(fieldName)) {
                    deserializedRollingUpgradeRunningStatus.lastAction
                        = RollingUpgradeActionType.fromString(reader.getString());
                } else if ("lastActionTime".equals(fieldName)) {
                    deserializedRollingUpgradeRunningStatus.lastActionTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedRollingUpgradeRunningStatus;
        });
    }
}
