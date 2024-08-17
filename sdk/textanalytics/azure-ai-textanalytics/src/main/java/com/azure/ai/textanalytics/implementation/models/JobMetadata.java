// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/** The JobMetadata model. */
@Fluent
public class JobMetadata implements JsonSerializable<JobMetadata> {
    /*
     * The displayName property.
     */
    private String displayName;

    /*
     * The createdDateTime property.
     */
    private OffsetDateTime createdDateTime;

    /*
     * The expirationDateTime property.
     */
    private OffsetDateTime expirationDateTime;

    /*
     * The jobId property.
     */
    private UUID jobId;

    /*
     * The lastUpdateDateTime property.
     */
    private OffsetDateTime lastUpdateDateTime;

    /*
     * The status property.
     */
    private State status;

    /**
     * Get the displayName property: The displayName property.
     *
     * @return the displayName value.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    /**
     * Set the displayName property: The displayName property.
     *
     * @param displayName the displayName value to set.
     * @return the JobMetadata object itself.
     */
    public JobMetadata setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the createdDateTime property: The createdDateTime property.
     *
     * @return the createdDateTime value.
     */
    public OffsetDateTime getCreatedDateTime() {
        return this.createdDateTime;
    }

    /**
     * Set the createdDateTime property: The createdDateTime property.
     *
     * @param createdDateTime the createdDateTime value to set.
     * @return the JobMetadata object itself.
     */
    public JobMetadata setCreatedDateTime(OffsetDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
        return this;
    }

    /**
     * Get the expirationDateTime property: The expirationDateTime property.
     *
     * @return the expirationDateTime value.
     */
    public OffsetDateTime getExpirationDateTime() {
        return this.expirationDateTime;
    }

    /**
     * Set the expirationDateTime property: The expirationDateTime property.
     *
     * @param expirationDateTime the expirationDateTime value to set.
     * @return the JobMetadata object itself.
     */
    public JobMetadata setExpirationDateTime(OffsetDateTime expirationDateTime) {
        this.expirationDateTime = expirationDateTime;
        return this;
    }

    /**
     * Get the jobId property: The jobId property.
     *
     * @return the jobId value.
     */
    public UUID getJobId() {
        return this.jobId;
    }

    /**
     * Set the jobId property: The jobId property.
     *
     * @param jobId the jobId value to set.
     * @return the JobMetadata object itself.
     */
    public JobMetadata setJobId(UUID jobId) {
        this.jobId = jobId;
        return this;
    }

    /**
     * Get the lastUpdateDateTime property: The lastUpdateDateTime property.
     *
     * @return the lastUpdateDateTime value.
     */
    public OffsetDateTime getLastUpdateDateTime() {
        return this.lastUpdateDateTime;
    }

    /**
     * Set the lastUpdateDateTime property: The lastUpdateDateTime property.
     *
     * @param lastUpdateDateTime the lastUpdateDateTime value to set.
     * @return the JobMetadata object itself.
     */
    public JobMetadata setLastUpdateDateTime(OffsetDateTime lastUpdateDateTime) {
        this.lastUpdateDateTime = lastUpdateDateTime;
        return this;
    }

    /**
     * Get the status property: The status property.
     *
     * @return the status value.
     */
    public State getStatus() {
        return this.status;
    }

    /**
     * Set the status property: The status property.
     *
     * @param status the status value to set.
     * @return the JobMetadata object itself.
     */
    public JobMetadata setStatus(State status) {
        this.status = status;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("displayName", this.displayName);
        jsonWriter.writeStringField("createdDateTime",
                this.createdDateTime == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.createdDateTime));
        jsonWriter.writeStringField("expirationDateTime",
                this.expirationDateTime == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.expirationDateTime));
        jsonWriter.writeStringField("jobId", this.jobId == null ? null : this.jobId.toString());
        jsonWriter.writeStringField("lastUpdateDateTime",
                this.lastUpdateDateTime == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(this.lastUpdateDateTime));
        jsonWriter.writeStringField("status", this.status == null ? null : this.status.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of JobMetadata from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of JobMetadata if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the JobMetadata.
     */
    public static JobMetadata fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            JobMetadata jobMetadata = new JobMetadata();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("displayName".equals(fieldName)) {
                    jobMetadata.displayName = reader.getString();
                } else if ("createdDateTime".equals(fieldName)) {
                    jobMetadata.createdDateTime = reader.getNullable(nonNullReader -> OffsetDateTime.parse(nonNullReader.getString()));
                } else if ("expirationDateTime".equals(fieldName)) {
                    jobMetadata.expirationDateTime = reader.getNullable(nonNullReader -> OffsetDateTime.parse(nonNullReader.getString()));
                } else if ("jobId".equals(fieldName)) {
                    jobMetadata.jobId = UUID.fromString(reader.getString());
                } else if ("lastUpdateDateTime".equals(fieldName)) {
                    jobMetadata.lastUpdateDateTime = reader.getNullable(nonNullReader -> OffsetDateTime.parse(nonNullReader.getString()));
                } else if ("status".equals(fieldName)) {
                    jobMetadata.status = State.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return jobMetadata;
        });
    }
}
