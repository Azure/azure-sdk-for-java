// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.automation.fluent.models.SourceControlSyncJobInner;
import java.io.IOException;
import java.util.List;

/**
 * The response model for the list source control sync jobs operation.
 */
@Fluent
public final class SourceControlSyncJobListResult implements JsonSerializable<SourceControlSyncJobListResult> {
    /*
     * The list of source control sync jobs.
     */
    private List<SourceControlSyncJobInner> value;

    /*
     * The next link.
     */
    private String nextLink;

    /**
     * Creates an instance of SourceControlSyncJobListResult class.
     */
    public SourceControlSyncJobListResult() {
    }

    /**
     * Get the value property: The list of source control sync jobs.
     * 
     * @return the value value.
     */
    public List<SourceControlSyncJobInner> value() {
        return this.value;
    }

    /**
     * Set the value property: The list of source control sync jobs.
     * 
     * @param value the value value to set.
     * @return the SourceControlSyncJobListResult object itself.
     */
    public SourceControlSyncJobListResult withValue(List<SourceControlSyncJobInner> value) {
        this.value = value;
        return this;
    }

    /**
     * Get the nextLink property: The next link.
     * 
     * @return the nextLink value.
     */
    public String nextLink() {
        return this.nextLink;
    }

    /**
     * Set the nextLink property: The next link.
     * 
     * @param nextLink the nextLink value to set.
     * @return the SourceControlSyncJobListResult object itself.
     */
    public SourceControlSyncJobListResult withNextLink(String nextLink) {
        this.nextLink = nextLink;
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (value() != null) {
            value().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("value", this.value, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeStringField("nextLink", this.nextLink);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SourceControlSyncJobListResult from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SourceControlSyncJobListResult if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the SourceControlSyncJobListResult.
     */
    public static SourceControlSyncJobListResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SourceControlSyncJobListResult deserializedSourceControlSyncJobListResult
                = new SourceControlSyncJobListResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("value".equals(fieldName)) {
                    List<SourceControlSyncJobInner> value
                        = reader.readArray(reader1 -> SourceControlSyncJobInner.fromJson(reader1));
                    deserializedSourceControlSyncJobListResult.value = value;
                } else if ("nextLink".equals(fieldName)) {
                    deserializedSourceControlSyncJobListResult.nextLink = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSourceControlSyncJobListResult;
        });
    }
}
