// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Specifies Redeploy related Scheduled Event related configurations.
 */
@Fluent
public final class UserInitiatedRedeploy implements JsonSerializable<UserInitiatedRedeploy> {
    /*
     * Specifies Redeploy Scheduled Event related configurations.
     */
    private Boolean automaticallyApprove;

    /**
     * Creates an instance of UserInitiatedRedeploy class.
     */
    public UserInitiatedRedeploy() {
    }

    /**
     * Get the automaticallyApprove property: Specifies Redeploy Scheduled Event related configurations.
     * 
     * @return the automaticallyApprove value.
     */
    public Boolean automaticallyApprove() {
        return this.automaticallyApprove;
    }

    /**
     * Set the automaticallyApprove property: Specifies Redeploy Scheduled Event related configurations.
     * 
     * @param automaticallyApprove the automaticallyApprove value to set.
     * @return the UserInitiatedRedeploy object itself.
     */
    public UserInitiatedRedeploy withAutomaticallyApprove(Boolean automaticallyApprove) {
        this.automaticallyApprove = automaticallyApprove;
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
        jsonWriter.writeBooleanField("automaticallyApprove", this.automaticallyApprove);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of UserInitiatedRedeploy from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of UserInitiatedRedeploy if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the UserInitiatedRedeploy.
     */
    public static UserInitiatedRedeploy fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            UserInitiatedRedeploy deserializedUserInitiatedRedeploy = new UserInitiatedRedeploy();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("automaticallyApprove".equals(fieldName)) {
                    deserializedUserInitiatedRedeploy.automaticallyApprove = reader.getNullable(JsonReader::getBoolean);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedUserInitiatedRedeploy;
        });
    }
}
