// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.time.OffsetDateTime;

/** Metadata pertaining to creation and last modification of the resource. */
public final class SystemData implements JsonSerializable<SystemData> {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String createdBy;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private ResourceAuthorIdentityType createdByType;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime createdAt;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String lastModifiedBy;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private ResourceAuthorIdentityType lastModifiedByType;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private OffsetDateTime lastModifiedAt;

    /**
     * Creates an instance of {@link SystemData}.
     */
    public SystemData() {
    }

    /**
     * Get the identity that created the resource.
     *
     * @return the identity that created the resource.
     */
    public String createdBy() {
        return this.createdBy;
    }

    /**
     * Get the type of identity that created the resource.
     *
     * @return the type of identity that created the resource.
     */
    public ResourceAuthorIdentityType createdByType() {
        return this.createdByType;
    }

    /**
     * Get the timestamp of resource creation (UTC).
     *
     * @return the timestamp of resource creation (UTC).
     */
    public OffsetDateTime createdAt() {
        return this.createdAt;
    }

    /**
     * Get the identity that last modified the resource.
     *
     * @return the identity that last modified the resource.
     */
    public String lastModifiedBy() {
        return this.lastModifiedBy;
    }

    /**
     * Get the type of identity that last modified the resource.
     *
     * @return the type of identity that last modified the resource.
     */
    public ResourceAuthorIdentityType lastModifiedByType() {
        return this.lastModifiedByType;
    }

    /**
     * Get the type of identity that last modified the resource.
     *
     * @return the timestamp of resource modification (UTC).
     */
    public OffsetDateTime lastModifiedAt() {
        return this.lastModifiedAt;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject().writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link SystemData}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link SystemData} that the JSON stream represented, may return null.
     * @throws IOException If a {@link SystemData} fails to be read from the {@code jsonReader}.
     */
    public static SystemData fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SystemData systemData = new SystemData();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("createdBy".equals(fieldName)) {
                    systemData.createdBy = reader.getString();
                } else if ("createdByType".equals(fieldName)) {
                    systemData.createdByType = ResourceAuthorIdentityType.fromString(reader.getString());
                } else if ("createdAt".equals(fieldName)) {
                    systemData.createdAt = CoreUtils.parseBestOffsetDateTime(reader.getString());
                } else if ("lastModifiedBy".equals(fieldName)) {
                    systemData.lastModifiedBy = reader.getString();
                } else if ("lastModifiedByType".equals(fieldName)) {
                    systemData.lastModifiedByType = ResourceAuthorIdentityType.fromString(reader.getString());
                } else if ("lastModifiedAt".equals(fieldName)) {
                    systemData.lastModifiedAt = CoreUtils.parseBestOffsetDateTime(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return systemData;
        });
    }
}
