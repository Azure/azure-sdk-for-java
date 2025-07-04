// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.analytics.synapse.artifacts.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Microsoft Fabric Warehouse dataset.
 */
@Fluent
public class WarehouseTableDataset extends Dataset {
    /*
     * Type of dataset.
     */
    @Generated
    private String type = "WarehouseTable";

    /*
     * The schema name of the Microsoft Fabric Warehouse. Type: string (or Expression with resultType string).
     */
    @Generated
    private Object schemaTypePropertiesSchema;

    /*
     * The table name of the Microsoft Fabric Warehouse. Type: string (or Expression with resultType string).
     */
    @Generated
    private Object table;

    /**
     * Creates an instance of WarehouseTableDataset class.
     */
    @Generated
    public WarehouseTableDataset() {
    }

    /**
     * Get the type property: Type of dataset.
     * 
     * @return the type value.
     */
    @Generated
    @Override
    public String getType() {
        return this.type;
    }

    /**
     * Get the schemaTypePropertiesSchema property: The schema name of the Microsoft Fabric Warehouse. Type: string (or
     * Expression with resultType string).
     * 
     * @return the schemaTypePropertiesSchema value.
     */
    @Generated
    public Object getSchemaTypePropertiesSchema() {
        return this.schemaTypePropertiesSchema;
    }

    /**
     * Set the schemaTypePropertiesSchema property: The schema name of the Microsoft Fabric Warehouse. Type: string (or
     * Expression with resultType string).
     * 
     * @param schemaTypePropertiesSchema the schemaTypePropertiesSchema value to set.
     * @return the WarehouseTableDataset object itself.
     */
    @Generated
    public WarehouseTableDataset setSchemaTypePropertiesSchema(Object schemaTypePropertiesSchema) {
        this.schemaTypePropertiesSchema = schemaTypePropertiesSchema;
        return this;
    }

    /**
     * Get the table property: The table name of the Microsoft Fabric Warehouse. Type: string (or Expression with
     * resultType string).
     * 
     * @return the table value.
     */
    @Generated
    public Object getTable() {
        return this.table;
    }

    /**
     * Set the table property: The table name of the Microsoft Fabric Warehouse. Type: string (or Expression with
     * resultType string).
     * 
     * @param table the table value to set.
     * @return the WarehouseTableDataset object itself.
     */
    @Generated
    public WarehouseTableDataset setTable(Object table) {
        this.table = table;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public WarehouseTableDataset setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public WarehouseTableDataset setStructure(Object structure) {
        super.setStructure(structure);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public WarehouseTableDataset setSchema(Object schema) {
        super.setSchema(schema);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public WarehouseTableDataset setLinkedServiceName(LinkedServiceReference linkedServiceName) {
        super.setLinkedServiceName(linkedServiceName);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public WarehouseTableDataset setParameters(Map<String, ParameterSpecification> parameters) {
        super.setParameters(parameters);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public WarehouseTableDataset setAnnotations(List<Object> annotations) {
        super.setAnnotations(annotations);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public WarehouseTableDataset setFolder(DatasetFolder folder) {
        super.setFolder(folder);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("linkedServiceName", getLinkedServiceName());
        jsonWriter.writeStringField("description", getDescription());
        if (getStructure() != null) {
            jsonWriter.writeUntypedField("structure", getStructure());
        }
        if (getSchema() != null) {
            jsonWriter.writeUntypedField("schema", getSchema());
        }
        jsonWriter.writeMapField("parameters", getParameters(), (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("annotations", getAnnotations(), (writer, element) -> writer.writeUntyped(element));
        jsonWriter.writeJsonField("folder", getFolder());
        jsonWriter.writeStringField("type", this.type);
        if (schemaTypePropertiesSchema != null || table != null) {
            jsonWriter.writeStartObject("typeProperties");
            if (this.schemaTypePropertiesSchema != null) {
                jsonWriter.writeUntypedField("schema", this.schemaTypePropertiesSchema);
            }
            if (this.table != null) {
                jsonWriter.writeUntypedField("table", this.table);
            }
            jsonWriter.writeEndObject();
        }
        if (getAdditionalProperties() != null) {
            for (Map.Entry<String, Object> additionalProperty : getAdditionalProperties().entrySet()) {
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of WarehouseTableDataset from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of WarehouseTableDataset if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the WarehouseTableDataset.
     */
    @Generated
    public static WarehouseTableDataset fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            WarehouseTableDataset deserializedWarehouseTableDataset = new WarehouseTableDataset();
            Map<String, Object> additionalProperties = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("linkedServiceName".equals(fieldName)) {
                    deserializedWarehouseTableDataset.setLinkedServiceName(LinkedServiceReference.fromJson(reader));
                } else if ("description".equals(fieldName)) {
                    deserializedWarehouseTableDataset.setDescription(reader.getString());
                } else if ("structure".equals(fieldName)) {
                    deserializedWarehouseTableDataset.setStructure(reader.readUntyped());
                } else if ("schema".equals(fieldName)) {
                    deserializedWarehouseTableDataset.setSchema(reader.readUntyped());
                } else if ("parameters".equals(fieldName)) {
                    Map<String, ParameterSpecification> parameters
                        = reader.readMap(reader1 -> ParameterSpecification.fromJson(reader1));
                    deserializedWarehouseTableDataset.setParameters(parameters);
                } else if ("annotations".equals(fieldName)) {
                    List<Object> annotations = reader.readArray(reader1 -> reader1.readUntyped());
                    deserializedWarehouseTableDataset.setAnnotations(annotations);
                } else if ("folder".equals(fieldName)) {
                    deserializedWarehouseTableDataset.setFolder(DatasetFolder.fromJson(reader));
                } else if ("type".equals(fieldName)) {
                    deserializedWarehouseTableDataset.type = reader.getString();
                } else if ("typeProperties".equals(fieldName) && reader.currentToken() == JsonToken.START_OBJECT) {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("schema".equals(fieldName)) {
                            deserializedWarehouseTableDataset.schemaTypePropertiesSchema = reader.readUntyped();
                        } else if ("table".equals(fieldName)) {
                            deserializedWarehouseTableDataset.table = reader.readUntyped();
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    additionalProperties.put(fieldName, reader.readUntyped());
                }
            }
            deserializedWarehouseTableDataset.setAdditionalProperties(additionalProperties);

            return deserializedWarehouseTableDataset;
        });
    }
}
