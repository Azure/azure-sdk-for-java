// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.computefleet.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Fleet Update Model.
 */
@Fluent
public final class FleetUpdate implements JsonSerializable<FleetUpdate> {
    /*
     * Resource tags.
     */
    private Map<String, String> tags;

    /*
     * Updatable managed service identity
     */
    private ManagedServiceIdentityUpdate identity;

    /*
     * Updatable resource plan
     */
    private ResourcePlanUpdate plan;

    /*
     * RP-specific updatable properties
     */
    private FleetProperties properties;

    /**
     * Creates an instance of FleetUpdate class.
     */
    public FleetUpdate() {
    }

    /**
     * Get the tags property: Resource tags.
     * 
     * @return the tags value.
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags property: Resource tags.
     * 
     * @param tags the tags value to set.
     * @return the FleetUpdate object itself.
     */
    public FleetUpdate withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the identity property: Updatable managed service identity.
     * 
     * @return the identity value.
     */
    public ManagedServiceIdentityUpdate identity() {
        return this.identity;
    }

    /**
     * Set the identity property: Updatable managed service identity.
     * 
     * @param identity the identity value to set.
     * @return the FleetUpdate object itself.
     */
    public FleetUpdate withIdentity(ManagedServiceIdentityUpdate identity) {
        this.identity = identity;
        return this;
    }

    /**
     * Get the plan property: Updatable resource plan.
     * 
     * @return the plan value.
     */
    public ResourcePlanUpdate plan() {
        return this.plan;
    }

    /**
     * Set the plan property: Updatable resource plan.
     * 
     * @param plan the plan value to set.
     * @return the FleetUpdate object itself.
     */
    public FleetUpdate withPlan(ResourcePlanUpdate plan) {
        this.plan = plan;
        return this;
    }

    /**
     * Get the properties property: RP-specific updatable properties.
     * 
     * @return the properties value.
     */
    public FleetProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties property: RP-specific updatable properties.
     * 
     * @param properties the properties value to set.
     * @return the FleetUpdate object itself.
     */
    public FleetUpdate withProperties(FleetProperties properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (identity() != null) {
            identity().validate();
        }
        if (plan() != null) {
            plan().validate();
        }
        if (properties() != null) {
            properties().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeMapField("tags", this.tags, (writer, element) -> writer.writeString(element));
        jsonWriter.writeJsonField("identity", this.identity);
        jsonWriter.writeJsonField("plan", this.plan);
        jsonWriter.writeJsonField("properties", this.properties);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of FleetUpdate from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of FleetUpdate if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the FleetUpdate.
     */
    public static FleetUpdate fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            FleetUpdate deserializedFleetUpdate = new FleetUpdate();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("tags".equals(fieldName)) {
                    Map<String, String> tags = reader.readMap(reader1 -> reader1.getString());
                    deserializedFleetUpdate.tags = tags;
                } else if ("identity".equals(fieldName)) {
                    deserializedFleetUpdate.identity = ManagedServiceIdentityUpdate.fromJson(reader);
                } else if ("plan".equals(fieldName)) {
                    deserializedFleetUpdate.plan = ResourcePlanUpdate.fromJson(reader);
                } else if ("properties".equals(fieldName)) {
                    deserializedFleetUpdate.properties = FleetProperties.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedFleetUpdate;
        });
    }
}
