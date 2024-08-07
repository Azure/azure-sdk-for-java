// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.List;

/**
 * State details.
 */
@Fluent
public final class AvailableProvidersListState implements JsonSerializable<AvailableProvidersListState> {
    /*
     * The state name.
     */
    private String stateName;

    /*
     * A list of Internet service providers.
     */
    private List<String> providers;

    /*
     * List of available cities or towns in the state.
     */
    private List<AvailableProvidersListCity> cities;

    /**
     * Creates an instance of AvailableProvidersListState class.
     */
    public AvailableProvidersListState() {
    }

    /**
     * Get the stateName property: The state name.
     * 
     * @return the stateName value.
     */
    public String stateName() {
        return this.stateName;
    }

    /**
     * Set the stateName property: The state name.
     * 
     * @param stateName the stateName value to set.
     * @return the AvailableProvidersListState object itself.
     */
    public AvailableProvidersListState withStateName(String stateName) {
        this.stateName = stateName;
        return this;
    }

    /**
     * Get the providers property: A list of Internet service providers.
     * 
     * @return the providers value.
     */
    public List<String> providers() {
        return this.providers;
    }

    /**
     * Set the providers property: A list of Internet service providers.
     * 
     * @param providers the providers value to set.
     * @return the AvailableProvidersListState object itself.
     */
    public AvailableProvidersListState withProviders(List<String> providers) {
        this.providers = providers;
        return this;
    }

    /**
     * Get the cities property: List of available cities or towns in the state.
     * 
     * @return the cities value.
     */
    public List<AvailableProvidersListCity> cities() {
        return this.cities;
    }

    /**
     * Set the cities property: List of available cities or towns in the state.
     * 
     * @param cities the cities value to set.
     * @return the AvailableProvidersListState object itself.
     */
    public AvailableProvidersListState withCities(List<AvailableProvidersListCity> cities) {
        this.cities = cities;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (cities() != null) {
            cities().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("stateName", this.stateName);
        jsonWriter.writeArrayField("providers", this.providers, (writer, element) -> writer.writeString(element));
        jsonWriter.writeArrayField("cities", this.cities, (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AvailableProvidersListState from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of AvailableProvidersListState if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the AvailableProvidersListState.
     */
    public static AvailableProvidersListState fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AvailableProvidersListState deserializedAvailableProvidersListState = new AvailableProvidersListState();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("stateName".equals(fieldName)) {
                    deserializedAvailableProvidersListState.stateName = reader.getString();
                } else if ("providers".equals(fieldName)) {
                    List<String> providers = reader.readArray(reader1 -> reader1.getString());
                    deserializedAvailableProvidersListState.providers = providers;
                } else if ("cities".equals(fieldName)) {
                    List<AvailableProvidersListCity> cities
                        = reader.readArray(reader1 -> AvailableProvidersListCity.fromJson(reader1));
                    deserializedAvailableProvidersListState.cities = cities;
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedAvailableProvidersListState;
        });
    }
}
