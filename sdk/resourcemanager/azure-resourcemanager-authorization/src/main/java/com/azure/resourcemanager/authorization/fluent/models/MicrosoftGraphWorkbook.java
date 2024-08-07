// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.authorization.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * workbook.
 */
@Fluent
public final class MicrosoftGraphWorkbook extends MicrosoftGraphEntity {
    /*
     * workbookApplication
     */
    private MicrosoftGraphWorkbookApplication application;

    /*
     * The comments property.
     */
    private List<MicrosoftGraphWorkbookComment> comments;

    /*
     * workbookFunctions
     */
    private MicrosoftGraphWorkbookFunctions functions;

    /*
     * Represents a collection of workbook scoped named items (named ranges and constants). Read-only.
     */
    private List<MicrosoftGraphWorkbookNamedItem> names;

    /*
     * The status of workbook operations. Getting an operation collection is not supported, but you can get the status
     * of a long-running operation if the Location header is returned in the response. Read-only.
     */
    private List<MicrosoftGraphWorkbookOperation> operations;

    /*
     * Represents a collection of tables associated with the workbook. Read-only.
     */
    private List<MicrosoftGraphWorkbookTable> tables;

    /*
     * Represents a collection of worksheets associated with the workbook. Read-only.
     */
    private List<MicrosoftGraphWorkbookWorksheet> worksheets;

    /*
     * workbook
     */
    private Map<String, Object> additionalProperties;

    /**
     * Creates an instance of MicrosoftGraphWorkbook class.
     */
    public MicrosoftGraphWorkbook() {
    }

    /**
     * Get the application property: workbookApplication.
     * 
     * @return the application value.
     */
    public MicrosoftGraphWorkbookApplication application() {
        return this.application;
    }

    /**
     * Set the application property: workbookApplication.
     * 
     * @param application the application value to set.
     * @return the MicrosoftGraphWorkbook object itself.
     */
    public MicrosoftGraphWorkbook withApplication(MicrosoftGraphWorkbookApplication application) {
        this.application = application;
        return this;
    }

    /**
     * Get the comments property: The comments property.
     * 
     * @return the comments value.
     */
    public List<MicrosoftGraphWorkbookComment> comments() {
        return this.comments;
    }

    /**
     * Set the comments property: The comments property.
     * 
     * @param comments the comments value to set.
     * @return the MicrosoftGraphWorkbook object itself.
     */
    public MicrosoftGraphWorkbook withComments(List<MicrosoftGraphWorkbookComment> comments) {
        this.comments = comments;
        return this;
    }

    /**
     * Get the functions property: workbookFunctions.
     * 
     * @return the functions value.
     */
    public MicrosoftGraphWorkbookFunctions functions() {
        return this.functions;
    }

    /**
     * Set the functions property: workbookFunctions.
     * 
     * @param functions the functions value to set.
     * @return the MicrosoftGraphWorkbook object itself.
     */
    public MicrosoftGraphWorkbook withFunctions(MicrosoftGraphWorkbookFunctions functions) {
        this.functions = functions;
        return this;
    }

    /**
     * Get the names property: Represents a collection of workbook scoped named items (named ranges and constants).
     * Read-only.
     * 
     * @return the names value.
     */
    public List<MicrosoftGraphWorkbookNamedItem> names() {
        return this.names;
    }

    /**
     * Set the names property: Represents a collection of workbook scoped named items (named ranges and constants).
     * Read-only.
     * 
     * @param names the names value to set.
     * @return the MicrosoftGraphWorkbook object itself.
     */
    public MicrosoftGraphWorkbook withNames(List<MicrosoftGraphWorkbookNamedItem> names) {
        this.names = names;
        return this;
    }

    /**
     * Get the operations property: The status of workbook operations. Getting an operation collection is not supported,
     * but you can get the status of a long-running operation if the Location header is returned in the response.
     * Read-only.
     * 
     * @return the operations value.
     */
    public List<MicrosoftGraphWorkbookOperation> operations() {
        return this.operations;
    }

    /**
     * Set the operations property: The status of workbook operations. Getting an operation collection is not supported,
     * but you can get the status of a long-running operation if the Location header is returned in the response.
     * Read-only.
     * 
     * @param operations the operations value to set.
     * @return the MicrosoftGraphWorkbook object itself.
     */
    public MicrosoftGraphWorkbook withOperations(List<MicrosoftGraphWorkbookOperation> operations) {
        this.operations = operations;
        return this;
    }

    /**
     * Get the tables property: Represents a collection of tables associated with the workbook. Read-only.
     * 
     * @return the tables value.
     */
    public List<MicrosoftGraphWorkbookTable> tables() {
        return this.tables;
    }

    /**
     * Set the tables property: Represents a collection of tables associated with the workbook. Read-only.
     * 
     * @param tables the tables value to set.
     * @return the MicrosoftGraphWorkbook object itself.
     */
    public MicrosoftGraphWorkbook withTables(List<MicrosoftGraphWorkbookTable> tables) {
        this.tables = tables;
        return this;
    }

    /**
     * Get the worksheets property: Represents a collection of worksheets associated with the workbook. Read-only.
     * 
     * @return the worksheets value.
     */
    public List<MicrosoftGraphWorkbookWorksheet> worksheets() {
        return this.worksheets;
    }

    /**
     * Set the worksheets property: Represents a collection of worksheets associated with the workbook. Read-only.
     * 
     * @param worksheets the worksheets value to set.
     * @return the MicrosoftGraphWorkbook object itself.
     */
    public MicrosoftGraphWorkbook withWorksheets(List<MicrosoftGraphWorkbookWorksheet> worksheets) {
        this.worksheets = worksheets;
        return this;
    }

    /**
     * Get the additionalProperties property: workbook.
     * 
     * @return the additionalProperties value.
     */
    public Map<String, Object> additionalProperties() {
        return this.additionalProperties;
    }

    /**
     * Set the additionalProperties property: workbook.
     * 
     * @param additionalProperties the additionalProperties value to set.
     * @return the MicrosoftGraphWorkbook object itself.
     */
    public MicrosoftGraphWorkbook withAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MicrosoftGraphWorkbook withId(String id) {
        super.withId(id);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        super.validate();
        if (application() != null) {
            application().validate();
        }
        if (comments() != null) {
            comments().forEach(e -> e.validate());
        }
        if (functions() != null) {
            functions().validate();
        }
        if (names() != null) {
            names().forEach(e -> e.validate());
        }
        if (operations() != null) {
            operations().forEach(e -> e.validate());
        }
        if (tables() != null) {
            tables().forEach(e -> e.validate());
        }
        if (worksheets() != null) {
            worksheets().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("id", id());
        jsonWriter.writeJsonField("application", this.application);
        jsonWriter.writeArrayField("comments", this.comments, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeJsonField("functions", this.functions);
        jsonWriter.writeArrayField("names", this.names, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("operations", this.operations, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("tables", this.tables, (writer, element) -> writer.writeJson(element));
        jsonWriter.writeArrayField("worksheets", this.worksheets, (writer, element) -> writer.writeJson(element));
        if (additionalProperties != null) {
            for (Map.Entry<String, Object> additionalProperty : additionalProperties.entrySet()) {
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MicrosoftGraphWorkbook from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of MicrosoftGraphWorkbook if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the MicrosoftGraphWorkbook.
     */
    public static MicrosoftGraphWorkbook fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            MicrosoftGraphWorkbook deserializedMicrosoftGraphWorkbook = new MicrosoftGraphWorkbook();
            Map<String, Object> additionalProperties = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("id".equals(fieldName)) {
                    deserializedMicrosoftGraphWorkbook.withId(reader.getString());
                } else if ("application".equals(fieldName)) {
                    deserializedMicrosoftGraphWorkbook.application = MicrosoftGraphWorkbookApplication.fromJson(reader);
                } else if ("comments".equals(fieldName)) {
                    List<MicrosoftGraphWorkbookComment> comments
                        = reader.readArray(reader1 -> MicrosoftGraphWorkbookComment.fromJson(reader1));
                    deserializedMicrosoftGraphWorkbook.comments = comments;
                } else if ("functions".equals(fieldName)) {
                    deserializedMicrosoftGraphWorkbook.functions = MicrosoftGraphWorkbookFunctions.fromJson(reader);
                } else if ("names".equals(fieldName)) {
                    List<MicrosoftGraphWorkbookNamedItem> names
                        = reader.readArray(reader1 -> MicrosoftGraphWorkbookNamedItem.fromJson(reader1));
                    deserializedMicrosoftGraphWorkbook.names = names;
                } else if ("operations".equals(fieldName)) {
                    List<MicrosoftGraphWorkbookOperation> operations
                        = reader.readArray(reader1 -> MicrosoftGraphWorkbookOperation.fromJson(reader1));
                    deserializedMicrosoftGraphWorkbook.operations = operations;
                } else if ("tables".equals(fieldName)) {
                    List<MicrosoftGraphWorkbookTable> tables
                        = reader.readArray(reader1 -> MicrosoftGraphWorkbookTable.fromJson(reader1));
                    deserializedMicrosoftGraphWorkbook.tables = tables;
                } else if ("worksheets".equals(fieldName)) {
                    List<MicrosoftGraphWorkbookWorksheet> worksheets
                        = reader.readArray(reader1 -> MicrosoftGraphWorkbookWorksheet.fromJson(reader1));
                    deserializedMicrosoftGraphWorkbook.worksheets = worksheets;
                } else {
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    additionalProperties.put(fieldName, reader.readUntyped());
                }
            }
            deserializedMicrosoftGraphWorkbook.additionalProperties = additionalProperties;

            return deserializedMicrosoftGraphWorkbook;
        });
    }
}
