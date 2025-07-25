// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.playwright.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The updatable properties of the PlaywrightWorkspace.
 */
@Fluent
public final class PlaywrightWorkspaceUpdateProperties
    implements JsonSerializable<PlaywrightWorkspaceUpdateProperties> {
    /*
     * This property sets the connection region for client workers to cloud-hosted browsers. If enabled, workers connect
     * to browsers in the closest Azure region, ensuring lower latency. If disabled, workers connect to browsers in the
     * Azure region in which the workspace was initially created.
     */
    private EnablementStatus regionalAffinity;

    /*
     * When enabled, this feature allows the workspace to use local auth (through service access token) for executing
     * operations.
     */
    private EnablementStatus localAuth;

    /**
     * Creates an instance of PlaywrightWorkspaceUpdateProperties class.
     */
    public PlaywrightWorkspaceUpdateProperties() {
    }

    /**
     * Get the regionalAffinity property: This property sets the connection region for client workers to cloud-hosted
     * browsers. If enabled, workers connect to browsers in the closest Azure region, ensuring lower latency. If
     * disabled, workers connect to browsers in the Azure region in which the workspace was initially created.
     * 
     * @return the regionalAffinity value.
     */
    public EnablementStatus regionalAffinity() {
        return this.regionalAffinity;
    }

    /**
     * Set the regionalAffinity property: This property sets the connection region for client workers to cloud-hosted
     * browsers. If enabled, workers connect to browsers in the closest Azure region, ensuring lower latency. If
     * disabled, workers connect to browsers in the Azure region in which the workspace was initially created.
     * 
     * @param regionalAffinity the regionalAffinity value to set.
     * @return the PlaywrightWorkspaceUpdateProperties object itself.
     */
    public PlaywrightWorkspaceUpdateProperties withRegionalAffinity(EnablementStatus regionalAffinity) {
        this.regionalAffinity = regionalAffinity;
        return this;
    }

    /**
     * Get the localAuth property: When enabled, this feature allows the workspace to use local auth (through service
     * access token) for executing operations.
     * 
     * @return the localAuth value.
     */
    public EnablementStatus localAuth() {
        return this.localAuth;
    }

    /**
     * Set the localAuth property: When enabled, this feature allows the workspace to use local auth (through service
     * access token) for executing operations.
     * 
     * @param localAuth the localAuth value to set.
     * @return the PlaywrightWorkspaceUpdateProperties object itself.
     */
    public PlaywrightWorkspaceUpdateProperties withLocalAuth(EnablementStatus localAuth) {
        this.localAuth = localAuth;
        return this;
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
        jsonWriter.writeStringField("regionalAffinity",
            this.regionalAffinity == null ? null : this.regionalAffinity.toString());
        jsonWriter.writeStringField("localAuth", this.localAuth == null ? null : this.localAuth.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of PlaywrightWorkspaceUpdateProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of PlaywrightWorkspaceUpdateProperties if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the PlaywrightWorkspaceUpdateProperties.
     */
    public static PlaywrightWorkspaceUpdateProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            PlaywrightWorkspaceUpdateProperties deserializedPlaywrightWorkspaceUpdateProperties
                = new PlaywrightWorkspaceUpdateProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("regionalAffinity".equals(fieldName)) {
                    deserializedPlaywrightWorkspaceUpdateProperties.regionalAffinity
                        = EnablementStatus.fromString(reader.getString());
                } else if ("localAuth".equals(fieldName)) {
                    deserializedPlaywrightWorkspaceUpdateProperties.localAuth
                        = EnablementStatus.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedPlaywrightWorkspaceUpdateProperties;
        });
    }
}
