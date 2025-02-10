// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.exception;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.List;

/**
 * An instance of this class provides additional information about an http error response.
 */
@Immutable
public class ManagementError implements JsonSerializable<ManagementError> {
    /**
     * Constructs a new {@link ManagementError} object.
     */
    public ManagementError() {
    }

    /**
     * Constructs a new {@link ManagementError} object.
     *
     * @param code the error code.
     * @param message the error message.
     */
    public ManagementError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * The error code parsed from the body of the http error response.
     */
    @JsonProperty(value = "code", access = JsonProperty.Access.WRITE_ONLY)
    private String code;

    /**
     * The error message parsed from the body of the http error response.
     */
    @JsonProperty(value = "message", access = JsonProperty.Access.WRITE_ONLY)
    private String message;

    /**
     * The target of the error.
     */
    @JsonProperty(value = "target", access = JsonProperty.Access.WRITE_ONLY)
    private String target;

    /**
     * Details for the error.
     */
    @JsonProperty(value = "details", access = JsonProperty.Access.WRITE_ONLY)
    private List<ManagementError> details;

    /**
     * Additional info for the error.
     */
    @JsonProperty(value = "additionalInfo", access = JsonProperty.Access.WRITE_ONLY)
    private List<AdditionalInfo> additionalInfo;

    /**
     * Gets the error code parsed from the body of the http error response.
     *
     * @return the error code parsed from the body of the http error response.
     */
    public String getCode() {
        return code;
    }

    void setCode(String code) {
        this.code = code;
    }

    /**
     * Gets the error message.
     *
     * @return the error message.
     */
    public String getMessage() {
        return message;
    }

    void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the target of the error.
     *
     * @return the target of the error.
     */
    public String getTarget() {
        return target;
    }

    void setTarget(String target) {
        this.target = target;
    }

    /**
     * Gets the details for the error.
     *
     * @return the details for the error.
     */
    public List<? extends ManagementError> getDetails() {
        return details;
    }

    void setDetails(List<ManagementError> details) {
        this.details = details;
    }

    /**
     * Gets the additional info for the error.
     *
     * @return the additional info for the error.
     */
    public List<AdditionalInfo> getAdditionalInfo() {
        return additionalInfo;
    }

    void setAdditionalInfo(List<AdditionalInfo> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return message == null ? super.toString() : message;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("code", code)
            .writeStringField("message", message)
            .writeStringField("target", target)
            .writeArrayField("details", details, JsonWriter::writeJson)
            .writeArrayField("additionalInfo", additionalInfo, JsonWriter::writeJson)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link ManagementError}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link ManagementError} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IOException If a {@link ManagementError} fails to be read from the {@code jsonReader}.
     */
    public static ManagementError fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            // Buffer the next JSON object as ResponseError can take two forms:
            //
            // - A ManagementError object
            // - A ManagementError object wrapped in an "error" node.
            JsonReader bufferedReader = reader.bufferObject();
            bufferedReader.nextToken(); // Get to the START_OBJECT token.
            while (bufferedReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = bufferedReader.getFieldName();
                bufferedReader.nextToken();

                if ("error".equals(fieldName)) {
                    // If the ManagementError was wrapped in the "error" node begin reading it now.
                    return readManagementError(bufferedReader);
                } else {
                    bufferedReader.skipChildren();
                }
            }

            // Otherwise reset the JsonReader and read the whole JSON object.
            return readManagementError(bufferedReader.reset());
        });
    }

    private static ManagementError readManagementError(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ManagementError managementError = new ManagementError();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("code".equalsIgnoreCase(fieldName)) {
                    managementError.code = reader.getString();
                } else if ("message".equalsIgnoreCase(fieldName)) {
                    managementError.message = reader.getString();
                } else if ("target".equalsIgnoreCase(fieldName)) {
                    managementError.target = reader.getString();
                } else if ("details".equalsIgnoreCase(fieldName)) {
                    managementError.details = reader.readArray(ManagementError::fromJson);
                } else if ("additionalInfo".equalsIgnoreCase(fieldName)) {
                    managementError.additionalInfo = reader.readArray(AdditionalInfo::fromJson);
                } else {
                    reader.skipChildren();
                }
            }

            return managementError;
        });
    }
}
