// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.sql.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.ProxyResource;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.sql.models.SyncAgentState;
import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * An Azure SQL Database sync agent.
 */
@Fluent
public final class SyncAgentInner extends ProxyResource {
    /*
     * Resource properties.
     */
    private SyncAgentProperties innerProperties;

    /*
     * The type of the resource.
     */
    private String type;

    /*
     * The name of the resource.
     */
    private String name;

    /*
     * Fully qualified resource Id for the resource.
     */
    private String id;

    /**
     * Creates an instance of SyncAgentInner class.
     */
    public SyncAgentInner() {
    }

    /**
     * Get the innerProperties property: Resource properties.
     * 
     * @return the innerProperties value.
     */
    private SyncAgentProperties innerProperties() {
        return this.innerProperties;
    }

    /**
     * Get the type property: The type of the resource.
     * 
     * @return the type value.
     */
    @Override
    public String type() {
        return this.type;
    }

    /**
     * Get the name property: The name of the resource.
     * 
     * @return the name value.
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * Get the id property: Fully qualified resource Id for the resource.
     * 
     * @return the id value.
     */
    @Override
    public String id() {
        return this.id;
    }

    /**
     * Get the name property: Name of the sync agent.
     * 
     * @return the name value.
     */
    public String namePropertiesName() {
        return this.innerProperties() == null ? null : this.innerProperties().name();
    }

    /**
     * Get the syncDatabaseId property: ARM resource id of the sync database in the sync agent.
     * 
     * @return the syncDatabaseId value.
     */
    public String syncDatabaseId() {
        return this.innerProperties() == null ? null : this.innerProperties().syncDatabaseId();
    }

    /**
     * Set the syncDatabaseId property: ARM resource id of the sync database in the sync agent.
     * 
     * @param syncDatabaseId the syncDatabaseId value to set.
     * @return the SyncAgentInner object itself.
     */
    public SyncAgentInner withSyncDatabaseId(String syncDatabaseId) {
        if (this.innerProperties() == null) {
            this.innerProperties = new SyncAgentProperties();
        }
        this.innerProperties().withSyncDatabaseId(syncDatabaseId);
        return this;
    }

    /**
     * Get the lastAliveTime property: Last alive time of the sync agent.
     * 
     * @return the lastAliveTime value.
     */
    public OffsetDateTime lastAliveTime() {
        return this.innerProperties() == null ? null : this.innerProperties().lastAliveTime();
    }

    /**
     * Get the state property: State of the sync agent.
     * 
     * @return the state value.
     */
    public SyncAgentState state() {
        return this.innerProperties() == null ? null : this.innerProperties().state();
    }

    /**
     * Get the isUpToDate property: If the sync agent version is up to date.
     * 
     * @return the isUpToDate value.
     */
    public Boolean isUpToDate() {
        return this.innerProperties() == null ? null : this.innerProperties().isUpToDate();
    }

    /**
     * Get the expiryTime property: Expiration time of the sync agent version.
     * 
     * @return the expiryTime value.
     */
    public OffsetDateTime expiryTime() {
        return this.innerProperties() == null ? null : this.innerProperties().expiryTime();
    }

    /**
     * Get the version property: Version of the sync agent.
     * 
     * @return the version value.
     */
    public String version() {
        return this.innerProperties() == null ? null : this.innerProperties().version();
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (innerProperties() != null) {
            innerProperties().validate();
        }
    }

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
     * Reads an instance of SyncAgentInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SyncAgentInner if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the SyncAgentInner.
     */
    public static SyncAgentInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SyncAgentInner deserializedSyncAgentInner = new SyncAgentInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedSyncAgentInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedSyncAgentInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedSyncAgentInner.type = reader.getString();
                } else if ("properties".equals(fieldName)) {
                    deserializedSyncAgentInner.innerProperties = SyncAgentProperties.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSyncAgentInner;
        });
    }
}
