// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.cosmos.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The SqlStoredProcedureGetPropertiesResource model.
 */
@Fluent
public final class SqlStoredProcedureGetPropertiesResource extends SqlStoredProcedureResource {
    /*
     * A system generated property. A unique identifier.
     */
    private String rid;

    /*
     * A system generated property that denotes the last updated timestamp of the resource.
     */
    private Float ts;

    /*
     * A system generated property representing the resource etag required for optimistic concurrency control.
     */
    private String etag;

    /**
     * Creates an instance of SqlStoredProcedureGetPropertiesResource class.
     */
    public SqlStoredProcedureGetPropertiesResource() {
    }

    /**
     * Get the rid property: A system generated property. A unique identifier.
     * 
     * @return the rid value.
     */
    public String rid() {
        return this.rid;
    }

    /**
     * Get the ts property: A system generated property that denotes the last updated timestamp of the resource.
     * 
     * @return the ts value.
     */
    public Float ts() {
        return this.ts;
    }

    /**
     * Get the etag property: A system generated property representing the resource etag required for optimistic
     * concurrency control.
     * 
     * @return the etag value.
     */
    public String etag() {
        return this.etag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SqlStoredProcedureGetPropertiesResource withId(String id) {
        super.withId(id);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SqlStoredProcedureGetPropertiesResource withBody(String body) {
        super.withBody(body);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        if (id() == null) {
            throw LOGGER.atError()
                .log(new IllegalArgumentException(
                    "Missing required property id in model SqlStoredProcedureGetPropertiesResource"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(SqlStoredProcedureGetPropertiesResource.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", id());
        jsonWriter.writeStringField("body", body());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SqlStoredProcedureGetPropertiesResource from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SqlStoredProcedureGetPropertiesResource if the JsonReader was pointing to an instance of
     * it, or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the SqlStoredProcedureGetPropertiesResource.
     */
    public static SqlStoredProcedureGetPropertiesResource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SqlStoredProcedureGetPropertiesResource deserializedSqlStoredProcedureGetPropertiesResource
                = new SqlStoredProcedureGetPropertiesResource();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedSqlStoredProcedureGetPropertiesResource.withId(reader.getString());
                } else if ("body".equals(fieldName)) {
                    deserializedSqlStoredProcedureGetPropertiesResource.withBody(reader.getString());
                } else if ("_rid".equals(fieldName)) {
                    deserializedSqlStoredProcedureGetPropertiesResource.rid = reader.getString();
                } else if ("_ts".equals(fieldName)) {
                    deserializedSqlStoredProcedureGetPropertiesResource.ts = reader.getNullable(JsonReader::getFloat);
                } else if ("_etag".equals(fieldName)) {
                    deserializedSqlStoredProcedureGetPropertiesResource.etag = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSqlStoredProcedureGetPropertiesResource;
        });
    }
}
