// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * Describes the properties of an virtual machine instance view for available patch summary.
 */
@Immutable
public final class AvailablePatchSummary implements JsonSerializable<AvailablePatchSummary> {
    /*
     * The overall success or failure status of the operation. It remains "InProgress" until the operation completes. At
     * that point it will become "Unknown", "Failed", "Succeeded", or "CompletedWithWarnings."
     */
    private PatchOperationStatus status;

    /*
     * The activity ID of the operation that produced this result. It is used to correlate across CRP and extension
     * logs.
     */
    private String assessmentActivityId;

    /*
     * The overall reboot status of the VM. It will be true when partially installed patches require a reboot to
     * complete installation but the reboot has not yet occurred.
     */
    private Boolean rebootPending;

    /*
     * The number of critical or security patches that have been detected as available and not yet installed.
     */
    private Integer criticalAndSecurityPatchCount;

    /*
     * The number of all available patches excluding critical and security.
     */
    private Integer otherPatchCount;

    /*
     * The UTC timestamp when the operation began.
     */
    private OffsetDateTime startTime;

    /*
     * The UTC timestamp when the operation began.
     */
    private OffsetDateTime lastModifiedTime;

    /*
     * The errors that were encountered during execution of the operation. The details array contains the list of them.
     */
    private ApiError error;

    /**
     * Creates an instance of AvailablePatchSummary class.
     */
    public AvailablePatchSummary() {
    }

    /**
     * Get the status property: The overall success or failure status of the operation. It remains "InProgress" until
     * the operation completes. At that point it will become "Unknown", "Failed", "Succeeded", or
     * "CompletedWithWarnings.".
     * 
     * @return the status value.
     */
    public PatchOperationStatus status() {
        return this.status;
    }

    /**
     * Get the assessmentActivityId property: The activity ID of the operation that produced this result. It is used to
     * correlate across CRP and extension logs.
     * 
     * @return the assessmentActivityId value.
     */
    public String assessmentActivityId() {
        return this.assessmentActivityId;
    }

    /**
     * Get the rebootPending property: The overall reboot status of the VM. It will be true when partially installed
     * patches require a reboot to complete installation but the reboot has not yet occurred.
     * 
     * @return the rebootPending value.
     */
    public Boolean rebootPending() {
        return this.rebootPending;
    }

    /**
     * Get the criticalAndSecurityPatchCount property: The number of critical or security patches that have been
     * detected as available and not yet installed.
     * 
     * @return the criticalAndSecurityPatchCount value.
     */
    public Integer criticalAndSecurityPatchCount() {
        return this.criticalAndSecurityPatchCount;
    }

    /**
     * Get the otherPatchCount property: The number of all available patches excluding critical and security.
     * 
     * @return the otherPatchCount value.
     */
    public Integer otherPatchCount() {
        return this.otherPatchCount;
    }

    /**
     * Get the startTime property: The UTC timestamp when the operation began.
     * 
     * @return the startTime value.
     */
    public OffsetDateTime startTime() {
        return this.startTime;
    }

    /**
     * Get the lastModifiedTime property: The UTC timestamp when the operation began.
     * 
     * @return the lastModifiedTime value.
     */
    public OffsetDateTime lastModifiedTime() {
        return this.lastModifiedTime;
    }

    /**
     * Get the error property: The errors that were encountered during execution of the operation. The details array
     * contains the list of them.
     * 
     * @return the error value.
     */
    public ApiError error() {
        return this.error;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (error() != null) {
            error().validate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AvailablePatchSummary from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of AvailablePatchSummary if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IOException If an error occurs while reading the AvailablePatchSummary.
     */
    public static AvailablePatchSummary fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AvailablePatchSummary deserializedAvailablePatchSummary = new AvailablePatchSummary();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("status".equals(fieldName)) {
                    deserializedAvailablePatchSummary.status = PatchOperationStatus.fromString(reader.getString());
                } else if ("assessmentActivityId".equals(fieldName)) {
                    deserializedAvailablePatchSummary.assessmentActivityId = reader.getString();
                } else if ("rebootPending".equals(fieldName)) {
                    deserializedAvailablePatchSummary.rebootPending = reader.getNullable(JsonReader::getBoolean);
                } else if ("criticalAndSecurityPatchCount".equals(fieldName)) {
                    deserializedAvailablePatchSummary.criticalAndSecurityPatchCount
                        = reader.getNullable(JsonReader::getInt);
                } else if ("otherPatchCount".equals(fieldName)) {
                    deserializedAvailablePatchSummary.otherPatchCount = reader.getNullable(JsonReader::getInt);
                } else if ("startTime".equals(fieldName)) {
                    deserializedAvailablePatchSummary.startTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("lastModifiedTime".equals(fieldName)) {
                    deserializedAvailablePatchSummary.lastModifiedTime = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("error".equals(fieldName)) {
                    deserializedAvailablePatchSummary.error = ApiError.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedAvailablePatchSummary;
        });
    }
}
