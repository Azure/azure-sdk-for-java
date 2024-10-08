// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.automation.fluent.models.SoftwareUpdateConfigurationCollectionItemProperties;
import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Software update configuration collection item properties.
 */
@Fluent
public final class SoftwareUpdateConfigurationCollectionItem
    implements JsonSerializable<SoftwareUpdateConfigurationCollectionItem> {
    /*
     * Name of the software update configuration.
     */
    private String name;

    /*
     * Resource Id of the software update configuration
     */
    private String id;

    /*
     * Software update configuration properties.
     */
    private SoftwareUpdateConfigurationCollectionItemProperties innerProperties
        = new SoftwareUpdateConfigurationCollectionItemProperties();

    /**
     * Creates an instance of SoftwareUpdateConfigurationCollectionItem class.
     */
    public SoftwareUpdateConfigurationCollectionItem() {
    }

    /**
     * Get the name property: Name of the software update configuration.
     * 
     * @return the name value.
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the id property: Resource Id of the software update configuration.
     * 
     * @return the id value.
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the innerProperties property: Software update configuration properties.
     * 
     * @return the innerProperties value.
     */
    private SoftwareUpdateConfigurationCollectionItemProperties innerProperties() {
        return this.innerProperties;
    }

    /**
     * Get the updateConfiguration property: Update specific properties of the software update configuration.
     * 
     * @return the updateConfiguration value.
     */
    public UpdateConfiguration updateConfiguration() {
        return this.innerProperties() == null ? null : this.innerProperties().updateConfiguration();
    }

    /**
     * Set the updateConfiguration property: Update specific properties of the software update configuration.
     * 
     * @param updateConfiguration the updateConfiguration value to set.
     * @return the SoftwareUpdateConfigurationCollectionItem object itself.
     */
    public SoftwareUpdateConfigurationCollectionItem withUpdateConfiguration(UpdateConfiguration updateConfiguration) {
        if (this.innerProperties() == null) {
            this.innerProperties = new SoftwareUpdateConfigurationCollectionItemProperties();
        }
        this.innerProperties().withUpdateConfiguration(updateConfiguration);
        return this;
    }

    /**
     * Get the tasks property: Pre and Post Tasks defined.
     * 
     * @return the tasks value.
     */
    public SoftwareUpdateConfigurationTasks tasks() {
        return this.innerProperties() == null ? null : this.innerProperties().tasks();
    }

    /**
     * Set the tasks property: Pre and Post Tasks defined.
     * 
     * @param tasks the tasks value to set.
     * @return the SoftwareUpdateConfigurationCollectionItem object itself.
     */
    public SoftwareUpdateConfigurationCollectionItem withTasks(SoftwareUpdateConfigurationTasks tasks) {
        if (this.innerProperties() == null) {
            this.innerProperties = new SoftwareUpdateConfigurationCollectionItemProperties();
        }
        this.innerProperties().withTasks(tasks);
        return this;
    }

    /**
     * Get the frequency property: execution frequency of the schedule associated with the software update
     * configuration.
     * 
     * @return the frequency value.
     */
    public ScheduleFrequency frequency() {
        return this.innerProperties() == null ? null : this.innerProperties().frequency();
    }

    /**
     * Set the frequency property: execution frequency of the schedule associated with the software update
     * configuration.
     * 
     * @param frequency the frequency value to set.
     * @return the SoftwareUpdateConfigurationCollectionItem object itself.
     */
    public SoftwareUpdateConfigurationCollectionItem withFrequency(ScheduleFrequency frequency) {
        if (this.innerProperties() == null) {
            this.innerProperties = new SoftwareUpdateConfigurationCollectionItemProperties();
        }
        this.innerProperties().withFrequency(frequency);
        return this;
    }

    /**
     * Get the startTime property: the start time of the update.
     * 
     * @return the startTime value.
     */
    public OffsetDateTime startTime() {
        return this.innerProperties() == null ? null : this.innerProperties().startTime();
    }

    /**
     * Set the startTime property: the start time of the update.
     * 
     * @param startTime the startTime value to set.
     * @return the SoftwareUpdateConfigurationCollectionItem object itself.
     */
    public SoftwareUpdateConfigurationCollectionItem withStartTime(OffsetDateTime startTime) {
        if (this.innerProperties() == null) {
            this.innerProperties = new SoftwareUpdateConfigurationCollectionItemProperties();
        }
        this.innerProperties().withStartTime(startTime);
        return this;
    }

    /**
     * Get the creationTime property: Creation time of the software update configuration, which only appears in the
     * response.
     * 
     * @return the creationTime value.
     */
    public OffsetDateTime creationTime() {
        return this.innerProperties() == null ? null : this.innerProperties().creationTime();
    }

    /**
     * Get the lastModifiedTime property: Last time software update configuration was modified, which only appears in
     * the response.
     * 
     * @return the lastModifiedTime value.
     */
    public OffsetDateTime lastModifiedTime() {
        return this.innerProperties() == null ? null : this.innerProperties().lastModifiedTime();
    }

    /**
     * Get the provisioningState property: Provisioning state for the software update configuration, which only appears
     * in the response.
     * 
     * @return the provisioningState value.
     */
    public String provisioningState() {
        return this.innerProperties() == null ? null : this.innerProperties().provisioningState();
    }

    /**
     * Get the nextRun property: ext run time of the update.
     * 
     * @return the nextRun value.
     */
    public OffsetDateTime nextRun() {
        return this.innerProperties() == null ? null : this.innerProperties().nextRun();
    }

    /**
     * Set the nextRun property: ext run time of the update.
     * 
     * @param nextRun the nextRun value to set.
     * @return the SoftwareUpdateConfigurationCollectionItem object itself.
     */
    public SoftwareUpdateConfigurationCollectionItem withNextRun(OffsetDateTime nextRun) {
        if (this.innerProperties() == null) {
            this.innerProperties = new SoftwareUpdateConfigurationCollectionItemProperties();
        }
        this.innerProperties().withNextRun(nextRun);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (innerProperties() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property innerProperties in model SoftwareUpdateConfigurationCollectionItem"));
        } else {
            innerProperties().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(SoftwareUpdateConfigurationCollectionItem.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("properties", this.innerProperties);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SoftwareUpdateConfigurationCollectionItem from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SoftwareUpdateConfigurationCollectionItem if the JsonReader was pointing to an instance of
     * it, or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the SoftwareUpdateConfigurationCollectionItem.
     */
    public static SoftwareUpdateConfigurationCollectionItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SoftwareUpdateConfigurationCollectionItem deserializedSoftwareUpdateConfigurationCollectionItem
                = new SoftwareUpdateConfigurationCollectionItem();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("properties".equals(fieldName)) {
                    deserializedSoftwareUpdateConfigurationCollectionItem.innerProperties
                        = SoftwareUpdateConfigurationCollectionItemProperties.fromJson(reader);
                } else if ("name".equals(fieldName)) {
                    deserializedSoftwareUpdateConfigurationCollectionItem.name = reader.getString();
                } else if ("id".equals(fieldName)) {
                    deserializedSoftwareUpdateConfigurationCollectionItem.id = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSoftwareUpdateConfigurationCollectionItem;
        });
    }
}
