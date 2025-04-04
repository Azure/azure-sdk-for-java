// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.delegatednetwork.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.delegatednetwork.models.ControllerDetails;
import com.azure.resourcemanager.delegatednetwork.models.DelegatedSubnetResource;
import com.azure.resourcemanager.delegatednetwork.models.DelegatedSubnetState;
import com.azure.resourcemanager.delegatednetwork.models.SubnetDetails;
import java.io.IOException;
import java.util.Map;

/**
 * Represents an instance of a orchestrator.
 */
@Fluent
public final class DelegatedSubnetInner extends DelegatedSubnetResource {
    /*
     * Properties of the provision operation request.
     */
    private DelegatedSubnetProperties innerProperties;

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
     * Creates an instance of DelegatedSubnetInner class.
     */
    public DelegatedSubnetInner() {
    }

    /**
     * Get the innerProperties property: Properties of the provision operation request.
     * 
     * @return the innerProperties value.
     */
    private DelegatedSubnetProperties innerProperties() {
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
     * {@inheritDoc}
     */
    @Override
    public DelegatedSubnetInner withLocation(String location) {
        super.withLocation(location);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DelegatedSubnetInner withTags(Map<String, String> tags) {
        super.withTags(tags);
        return this;
    }

    /**
     * Get the resourceGuid property: Resource guid.
     * 
     * @return the resourceGuid value.
     */
    public String resourceGuid() {
        return this.innerProperties() == null ? null : this.innerProperties().resourceGuid();
    }

    /**
     * Get the provisioningState property: The current state of dnc delegated subnet resource.
     * 
     * @return the provisioningState value.
     */
    public DelegatedSubnetState provisioningState() {
        return this.innerProperties() == null ? null : this.innerProperties().provisioningState();
    }

    /**
     * Get the subnetDetails property: subnet details.
     * 
     * @return the subnetDetails value.
     */
    public SubnetDetails subnetDetails() {
        return this.innerProperties() == null ? null : this.innerProperties().subnetDetails();
    }

    /**
     * Set the subnetDetails property: subnet details.
     * 
     * @param subnetDetails the subnetDetails value to set.
     * @return the DelegatedSubnetInner object itself.
     */
    public DelegatedSubnetInner withSubnetDetails(SubnetDetails subnetDetails) {
        if (this.innerProperties() == null) {
            this.innerProperties = new DelegatedSubnetProperties();
        }
        this.innerProperties().withSubnetDetails(subnetDetails);
        return this;
    }

    /**
     * Get the controllerDetails property: Properties of the controller.
     * 
     * @return the controllerDetails value.
     */
    public ControllerDetails controllerDetails() {
        return this.innerProperties() == null ? null : this.innerProperties().controllerDetails();
    }

    /**
     * Set the controllerDetails property: Properties of the controller.
     * 
     * @param controllerDetails the controllerDetails value to set.
     * @return the DelegatedSubnetInner object itself.
     */
    public DelegatedSubnetInner withControllerDetails(ControllerDetails controllerDetails) {
        if (this.innerProperties() == null) {
            this.innerProperties = new DelegatedSubnetProperties();
        }
        this.innerProperties().withControllerDetails(controllerDetails);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
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
        jsonWriter.writeStringField("location", location());
        jsonWriter.writeMapField("tags", tags(), (writer, element) -> writer.writeString(element));
        jsonWriter.writeJsonField("properties", this.innerProperties);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DelegatedSubnetInner from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of DelegatedSubnetInner if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the DelegatedSubnetInner.
     */
    public static DelegatedSubnetInner fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            DelegatedSubnetInner deserializedDelegatedSubnetInner = new DelegatedSubnetInner();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedDelegatedSubnetInner.id = reader.getString();
                } else if ("name".equals(fieldName)) {
                    deserializedDelegatedSubnetInner.name = reader.getString();
                } else if ("type".equals(fieldName)) {
                    deserializedDelegatedSubnetInner.type = reader.getString();
                } else if ("location".equals(fieldName)) {
                    deserializedDelegatedSubnetInner.withLocation(reader.getString());
                } else if ("tags".equals(fieldName)) {
                    Map<String, String> tags = reader.readMap(reader1 -> reader1.getString());
                    deserializedDelegatedSubnetInner.withTags(tags);
                } else if ("properties".equals(fieldName)) {
                    deserializedDelegatedSubnetInner.innerProperties = DelegatedSubnetProperties.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedDelegatedSubnetInner;
        });
    }
}
