// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.graphservices.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Resource;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.graphservices.models.AccountResourceProperties;
import com.azure.resourcemanager.graphservices.models.AccountResourceSystemData;
import java.io.IOException;
import java.util.Map;

/**
 * Account details.
 */
@Fluent
public final class AccountResourceInner extends Resource {
    /*
     * Metadata pertaining to creation and last modification of the resource.
     */
    private AccountResourceSystemData systemData;

    /*
     * Property bag from billing account
     */
    private AccountResourceProperties properties;

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
     * Creates an instance of AccountResourceInner class.
     */
    public AccountResourceInner() {
    }

    /**
     * Get the systemData property: Metadata pertaining to creation and last modification of the resource.
     * 
     * @return the systemData value.
     */
    public AccountResourceSystemData systemData() {
        return this.systemData;
    }

    /**
     * Get the properties property: Property bag from billing account.
     * 
     * @return the properties value.
     */
    public AccountResourceProperties properties() {
        return this.properties;
    }

    /**
     * Set the properties property: Property bag from billing account.
     * 
     * @param properties the properties value to set.
     * @return the AccountResourceInner object itself.
     */
    public AccountResourceInner withProperties(AccountResourceProperties properties) {
        this.properties = properties;
        return this;
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
     * {@inheritDoc}
     */
    @Override
    public AccountResourceInner withLocation(String location) {
        super.withLocation(location);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountResourceInner withTags(Map<String, String> tags) {
        super.withTags(tags);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (systemData() != null) {
            systemData().validate();
        }
        if (properties() == null) {
            throw LOGGER.atError()
                .log(
                    new IllegalArgumentException("Missing required property properties in model AccountResourceInner"));
        } else {
            properties().validate();
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(AccountResourceInner.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("location", location());
        jsonWriter.writeMapField("tags", tags(), (writer, element) -> writer.writeString(element));
        jsonWriter.writeJsonField("properties", this.properties);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AccountResourceInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of AccountResourceInner if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the AccountResourceInner.
     */
    public static AccountResourceInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AccountResourceInner deserializedAccountResourceInner = new AccountResourceInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedAccountResourceInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedAccountResourceInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedAccountResourceInner.type = reader.getString();
                } else if ("location".equals(fieldName)) {
                    deserializedAccountResourceInner.withLocation(reader.getString());
                } else if ("tags".equals(fieldName)) {
                    Map<String, String> tags = reader.readMap(reader1 -> reader1.getString());
                    deserializedAccountResourceInner.withTags(tags);
                } else if ("properties".equals(fieldName)) {
                    deserializedAccountResourceInner.properties = AccountResourceProperties.fromJson(reader);
                } else if ("systemData".equals(fieldName)) {
                    deserializedAccountResourceInner.systemData = AccountResourceSystemData.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedAccountResourceInner;
        });
    }
}
