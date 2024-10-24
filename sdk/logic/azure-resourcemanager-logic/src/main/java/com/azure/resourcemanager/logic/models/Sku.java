// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.logic.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The sku type.
 */
@Fluent
public final class Sku implements JsonSerializable<Sku> {
    /*
     * The name.
     */
    private SkuName name;

    /*
     * The reference to plan.
     */
    private ResourceReference plan;

    /**
     * Creates an instance of Sku class.
     */
    public Sku() {
    }

    /**
     * Get the name property: The name.
     * 
     * @return the name value.
     */
    public SkuName name() {
        return this.name;
    }

    /**
     * Set the name property: The name.
     * 
     * @param name the name value to set.
     * @return the Sku object itself.
     */
    public Sku withName(SkuName name) {
        this.name = name;
        return this;
    }

    /**
     * Get the plan property: The reference to plan.
     * 
     * @return the plan value.
     */
    public ResourceReference plan() {
        return this.plan;
    }

    /**
     * Set the plan property: The reference to plan.
     * 
     * @param plan the plan value to set.
     * @return the Sku object itself.
     */
    public Sku withPlan(ResourceReference plan) {
        this.plan = plan;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (name() == null) {
            throw LOGGER.atError().log(new IllegalArgumentException("Missing required property name in model Sku"));
        }
        if (plan() != null) {
            plan().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(Sku.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("name", this.name == null ? null : this.name.toString());
        jsonWriter.writeJsonField("plan", this.plan);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of Sku from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of Sku if the JsonReader was pointing to an instance of it, or null if it was pointing to
     * JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the Sku.
     */
    public static Sku fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Sku deserializedSku = new Sku();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("name".equals(fieldName)) {
                    deserializedSku.name = SkuName.fromString(reader.getString());
                } else if ("plan".equals(fieldName)) {
                    deserializedSku.plan = ResourceReference.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSku;
        });
    }
}
