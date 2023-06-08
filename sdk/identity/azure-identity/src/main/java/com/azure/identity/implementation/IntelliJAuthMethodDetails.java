// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Represents Azure Tools for IntelliJ IDE Plugin's authentication method details.
 */
public class IntelliJAuthMethodDetails implements JsonSerializable<IntelliJAuthMethodDetails> {

    private String accountEmail;
    private String credFilePath;
    private String authMethod;
    private String azureEnv;

    /**
     * Get the account email.
     *
     * @return the account email.
     */
    public String getAccountEmail() {
        return accountEmail;
    }

    /**
     * Get the Service Principal cred file path.
     *
     * @return the cred file path.
     */
    public String getCredFilePath() {
        return credFilePath;
    }

    /**
     * Get the auth method used by Azure Tools for IntelliJ plugin.
     *
     * @return the auth method used.
     */
    public String getAuthMethod() {
        return authMethod;
    }

    /**
     * Get the Azure env used.
     *
     * @return the Azure env used.
     */
    public String getAzureEnv() {
        return azureEnv;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("accountEmail", accountEmail)
            .writeStringField("credFilePath", credFilePath)
            .writeStringField("authMethod", authMethod)
            .writeStringField("azureEnv", azureEnv)
            .writeEndObject();
    }

    /**
     * Reads an instance of IntelliJAuthMethodDetails from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of IntelliJAuthMethodDetails if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the IntelliJAuthMethodDetails.
     */
    public static IntelliJAuthMethodDetails fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            IntelliJAuthMethodDetails intelliJAuthMethodDetails = new IntelliJAuthMethodDetails();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("accountEmail".equals(fieldName)) {
                    intelliJAuthMethodDetails.accountEmail = reader.getString();
                } else if ("credFilePath".equals(fieldName)) {
                    intelliJAuthMethodDetails.credFilePath = reader.getString();
                } else if ("authMethod".equals(fieldName)) {
                    intelliJAuthMethodDetails.authMethod = reader.getString();
                } else if ("azureEnv".equals(fieldName)) {
                    intelliJAuthMethodDetails.azureEnv = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return intelliJAuthMethodDetails;
        });
    }
}
