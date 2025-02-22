// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.synapse.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.resourcemanager.synapse.models.SensitivityLabelRank;
import java.io.IOException;

/**
 * Properties of a sensitivity label.
 */
@Fluent
public final class SensitivityLabelProperties implements JsonSerializable<SensitivityLabelProperties> {
    /*
     * The schema name.
     */
    private String schemaName;

    /*
     * The table name.
     */
    private String tableName;

    /*
     * The column name.
     */
    private String columnName;

    /*
     * The label name.
     */
    private String labelName;

    /*
     * The label ID.
     */
    private String labelId;

    /*
     * The information type.
     */
    private String informationType;

    /*
     * The information type ID.
     */
    private String informationTypeId;

    /*
     * Is sensitivity recommendation disabled. Applicable for recommended sensitivity label only. Specifies whether the
     * sensitivity recommendation on this column is disabled (dismissed) or not.
     */
    private Boolean isDisabled;

    /*
     * The rank property.
     */
    private SensitivityLabelRank rank;

    /**
     * Creates an instance of SensitivityLabelProperties class.
     */
    public SensitivityLabelProperties() {
    }

    /**
     * Get the schemaName property: The schema name.
     * 
     * @return the schemaName value.
     */
    public String schemaName() {
        return this.schemaName;
    }

    /**
     * Get the tableName property: The table name.
     * 
     * @return the tableName value.
     */
    public String tableName() {
        return this.tableName;
    }

    /**
     * Get the columnName property: The column name.
     * 
     * @return the columnName value.
     */
    public String columnName() {
        return this.columnName;
    }

    /**
     * Get the labelName property: The label name.
     * 
     * @return the labelName value.
     */
    public String labelName() {
        return this.labelName;
    }

    /**
     * Set the labelName property: The label name.
     * 
     * @param labelName the labelName value to set.
     * @return the SensitivityLabelProperties object itself.
     */
    public SensitivityLabelProperties withLabelName(String labelName) {
        this.labelName = labelName;
        return this;
    }

    /**
     * Get the labelId property: The label ID.
     * 
     * @return the labelId value.
     */
    public String labelId() {
        return this.labelId;
    }

    /**
     * Set the labelId property: The label ID.
     * 
     * @param labelId the labelId value to set.
     * @return the SensitivityLabelProperties object itself.
     */
    public SensitivityLabelProperties withLabelId(String labelId) {
        this.labelId = labelId;
        return this;
    }

    /**
     * Get the informationType property: The information type.
     * 
     * @return the informationType value.
     */
    public String informationType() {
        return this.informationType;
    }

    /**
     * Set the informationType property: The information type.
     * 
     * @param informationType the informationType value to set.
     * @return the SensitivityLabelProperties object itself.
     */
    public SensitivityLabelProperties withInformationType(String informationType) {
        this.informationType = informationType;
        return this;
    }

    /**
     * Get the informationTypeId property: The information type ID.
     * 
     * @return the informationTypeId value.
     */
    public String informationTypeId() {
        return this.informationTypeId;
    }

    /**
     * Set the informationTypeId property: The information type ID.
     * 
     * @param informationTypeId the informationTypeId value to set.
     * @return the SensitivityLabelProperties object itself.
     */
    public SensitivityLabelProperties withInformationTypeId(String informationTypeId) {
        this.informationTypeId = informationTypeId;
        return this;
    }

    /**
     * Get the isDisabled property: Is sensitivity recommendation disabled. Applicable for recommended sensitivity label
     * only. Specifies whether the sensitivity recommendation on this column is disabled (dismissed) or not.
     * 
     * @return the isDisabled value.
     */
    public Boolean isDisabled() {
        return this.isDisabled;
    }

    /**
     * Get the rank property: The rank property.
     * 
     * @return the rank value.
     */
    public SensitivityLabelRank rank() {
        return this.rank;
    }

    /**
     * Set the rank property: The rank property.
     * 
     * @param rank the rank value to set.
     * @return the SensitivityLabelProperties object itself.
     */
    public SensitivityLabelProperties withRank(SensitivityLabelRank rank) {
        this.rank = rank;
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
        jsonWriter.writeStringField("labelName", this.labelName);
        jsonWriter.writeStringField("labelId", this.labelId);
        jsonWriter.writeStringField("informationType", this.informationType);
        jsonWriter.writeStringField("informationTypeId", this.informationTypeId);
        jsonWriter.writeStringField("rank", this.rank == null ? null : this.rank.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of SensitivityLabelProperties from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of SensitivityLabelProperties if the JsonReader was pointing to an instance of it, or null if
     * it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the SensitivityLabelProperties.
     */
    public static SensitivityLabelProperties fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SensitivityLabelProperties deserializedSensitivityLabelProperties = new SensitivityLabelProperties();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("schemaName".equals(fieldName)) {
                    deserializedSensitivityLabelProperties.schemaName = reader.getString();
                } else if ("tableName".equals(fieldName)) {
                    deserializedSensitivityLabelProperties.tableName = reader.getString();
                } else if ("columnName".equals(fieldName)) {
                    deserializedSensitivityLabelProperties.columnName = reader.getString();
                } else if ("labelName".equals(fieldName)) {
                    deserializedSensitivityLabelProperties.labelName = reader.getString();
                } else if ("labelId".equals(fieldName)) {
                    deserializedSensitivityLabelProperties.labelId = reader.getString();
                } else if ("informationType".equals(fieldName)) {
                    deserializedSensitivityLabelProperties.informationType = reader.getString();
                } else if ("informationTypeId".equals(fieldName)) {
                    deserializedSensitivityLabelProperties.informationTypeId = reader.getString();
                } else if ("isDisabled".equals(fieldName)) {
                    deserializedSensitivityLabelProperties.isDisabled = reader.getNullable(JsonReader::getBoolean);
                } else if ("rank".equals(fieldName)) {
                    deserializedSensitivityLabelProperties.rank = SensitivityLabelRank.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSensitivityLabelProperties;
        });
    }
}
