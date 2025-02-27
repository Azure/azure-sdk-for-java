// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.nginx.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The NginxConfigurationProtectedFileRequest model.
 */
@Fluent
public final class NginxConfigurationProtectedFileRequest
    implements JsonSerializable<NginxConfigurationProtectedFileRequest> {
    /*
     * The content of the protected file. This value is a PUT only value. If you perform a GET request on this value, it
     * will be empty because it is a protected file.
     */
    private String content;

    /*
     * The virtual path of the protected file.
     */
    private String virtualPath;

    /*
     * The hash of the content of the file. This value is used to determine if the file has changed.
     */
    private String contentHash;

    /**
     * Creates an instance of NginxConfigurationProtectedFileRequest class.
     */
    public NginxConfigurationProtectedFileRequest() {
    }

    /**
     * Get the content property: The content of the protected file. This value is a PUT only value. If you perform a GET
     * request on this value, it will be empty because it is a protected file.
     * 
     * @return the content value.
     */
    public String content() {
        return this.content;
    }

    /**
     * Set the content property: The content of the protected file. This value is a PUT only value. If you perform a GET
     * request on this value, it will be empty because it is a protected file.
     * 
     * @param content the content value to set.
     * @return the NginxConfigurationProtectedFileRequest object itself.
     */
    public NginxConfigurationProtectedFileRequest withContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Get the virtualPath property: The virtual path of the protected file.
     * 
     * @return the virtualPath value.
     */
    public String virtualPath() {
        return this.virtualPath;
    }

    /**
     * Set the virtualPath property: The virtual path of the protected file.
     * 
     * @param virtualPath the virtualPath value to set.
     * @return the NginxConfigurationProtectedFileRequest object itself.
     */
    public NginxConfigurationProtectedFileRequest withVirtualPath(String virtualPath) {
        this.virtualPath = virtualPath;
        return this;
    }

    /**
     * Get the contentHash property: The hash of the content of the file. This value is used to determine if the file
     * has changed.
     * 
     * @return the contentHash value.
     */
    public String contentHash() {
        return this.contentHash;
    }

    /**
     * Set the contentHash property: The hash of the content of the file. This value is used to determine if the file
     * has changed.
     * 
     * @param contentHash the contentHash value to set.
     * @return the NginxConfigurationProtectedFileRequest object itself.
     */
    public NginxConfigurationProtectedFileRequest withContentHash(String contentHash) {
        this.contentHash = contentHash;
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
        jsonWriter.writeStringField("content", this.content);
        jsonWriter.writeStringField("virtualPath", this.virtualPath);
        jsonWriter.writeStringField("contentHash", this.contentHash);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of NginxConfigurationProtectedFileRequest from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of NginxConfigurationProtectedFileRequest if the JsonReader was pointing to an instance of
     * it, or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the NginxConfigurationProtectedFileRequest.
     */
    public static NginxConfigurationProtectedFileRequest fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            NginxConfigurationProtectedFileRequest deserializedNginxConfigurationProtectedFileRequest
                = new NginxConfigurationProtectedFileRequest();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("content".equals(fieldName)) {
                    deserializedNginxConfigurationProtectedFileRequest.content = reader.getString();
                } else if ("virtualPath".equals(fieldName)) {
                    deserializedNginxConfigurationProtectedFileRequest.virtualPath = reader.getString();
                } else if ("contentHash".equals(fieldName)) {
                    deserializedNginxConfigurationProtectedFileRequest.contentHash = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedNginxConfigurationProtectedFileRequest;
        });
    }
}
