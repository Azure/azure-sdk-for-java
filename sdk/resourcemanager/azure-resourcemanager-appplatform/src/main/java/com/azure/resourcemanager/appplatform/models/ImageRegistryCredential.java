// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Credential of the image registry.
 */
@Fluent
public final class ImageRegistryCredential implements JsonSerializable<ImageRegistryCredential> {
    /*
     * The username of the image registry credential
     */
    private String username;

    /*
     * The password of the image registry credential
     */
    private String password;

    /**
     * Creates an instance of ImageRegistryCredential class.
     */
    public ImageRegistryCredential() {
    }

    /**
     * Get the username property: The username of the image registry credential.
     * 
     * @return the username value.
     */
    public String username() {
        return this.username;
    }

    /**
     * Set the username property: The username of the image registry credential.
     * 
     * @param username the username value to set.
     * @return the ImageRegistryCredential object itself.
     */
    public ImageRegistryCredential withUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Get the password property: The password of the image registry credential.
     * 
     * @return the password value.
     */
    public String password() {
        return this.password;
    }

    /**
     * Set the password property: The password of the image registry credential.
     * 
     * @param password the password value to set.
     * @return the ImageRegistryCredential object itself.
     */
    public ImageRegistryCredential withPassword(String password) {
        this.password = password;
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
        jsonWriter.writeStringField("username", this.username);
        jsonWriter.writeStringField("password", this.password);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ImageRegistryCredential from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of ImageRegistryCredential if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ImageRegistryCredential.
     */
    public static ImageRegistryCredential fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ImageRegistryCredential deserializedImageRegistryCredential = new ImageRegistryCredential();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("username".equals(fieldName)) {
                    deserializedImageRegistryCredential.username = reader.getString();
                } else if ("password".equals(fieldName)) {
                    deserializedImageRegistryCredential.password = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedImageRegistryCredential;
        });
    }
}
