// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.compute.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The publishing profile of a gallery image Version.
 */
@Fluent
public final class GalleryImageVersionPublishingProfile extends GalleryArtifactPublishingProfileBase {
    /*
     * The timestamp for when the gallery image version is published.
     */
    private OffsetDateTime publishedDate;

    /**
     * Creates an instance of GalleryImageVersionPublishingProfile class.
     */
    public GalleryImageVersionPublishingProfile() {
    }

    /**
     * Get the publishedDate property: The timestamp for when the gallery image version is published.
     * 
     * @return the publishedDate value.
     */
    @Override
    public OffsetDateTime publishedDate() {
        return this.publishedDate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GalleryImageVersionPublishingProfile withTargetRegions(List<TargetRegion> targetRegions) {
        super.withTargetRegions(targetRegions);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GalleryImageVersionPublishingProfile withReplicaCount(Integer replicaCount) {
        super.withReplicaCount(replicaCount);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GalleryImageVersionPublishingProfile withExcludeFromLatest(Boolean excludeFromLatest) {
        super.withExcludeFromLatest(excludeFromLatest);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GalleryImageVersionPublishingProfile withEndOfLifeDate(OffsetDateTime endOfLifeDate) {
        super.withEndOfLifeDate(endOfLifeDate);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GalleryImageVersionPublishingProfile withStorageAccountType(StorageAccountType storageAccountType) {
        super.withStorageAccountType(storageAccountType);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GalleryImageVersionPublishingProfile withReplicationMode(ReplicationMode replicationMode) {
        super.withReplicationMode(replicationMode);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GalleryImageVersionPublishingProfile
        withTargetExtendedLocations(List<GalleryTargetExtendedLocation> targetExtendedLocations) {
        super.withTargetExtendedLocations(targetExtendedLocations);
        return this;
    }

    /**
     * Validates the instance.
     * 
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    @Override
    public void validate() {
        if (targetRegions() != null) {
            targetRegions().forEach(e -> e.validate());
        }
        if (targetExtendedLocations() != null) {
            targetExtendedLocations().forEach(e -> e.validate());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeArrayField("targetRegions", targetRegions(), (writer, element) -> writer.writeJson(element));
        jsonWriter.writeNumberField("replicaCount", replicaCount());
        jsonWriter.writeBooleanField("excludeFromLatest", excludeFromLatest());
        jsonWriter.writeStringField("endOfLifeDate",
            endOfLifeDate() == null ? null : DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(endOfLifeDate()));
        jsonWriter.writeStringField("storageAccountType",
            storageAccountType() == null ? null : storageAccountType().toString());
        jsonWriter.writeStringField("replicationMode", replicationMode() == null ? null : replicationMode().toString());
        jsonWriter.writeArrayField("targetExtendedLocations", targetExtendedLocations(),
            (writer, element) -> writer.writeJson(element));
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of GalleryImageVersionPublishingProfile from the JsonReader.
     * 
     * @param jsonReader The JsonReader being read.
     * @return An instance of GalleryImageVersionPublishingProfile if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the GalleryImageVersionPublishingProfile.
     */
    public static GalleryImageVersionPublishingProfile fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            GalleryImageVersionPublishingProfile deserializedGalleryImageVersionPublishingProfile
                = new GalleryImageVersionPublishingProfile();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("targetRegions".equals(fieldName)) {
                    List<TargetRegion> targetRegions = reader.readArray(reader1 -> TargetRegion.fromJson(reader1));
                    deserializedGalleryImageVersionPublishingProfile.withTargetRegions(targetRegions);
                } else if ("replicaCount".equals(fieldName)) {
                    deserializedGalleryImageVersionPublishingProfile
                        .withReplicaCount(reader.getNullable(JsonReader::getInt));
                } else if ("excludeFromLatest".equals(fieldName)) {
                    deserializedGalleryImageVersionPublishingProfile
                        .withExcludeFromLatest(reader.getNullable(JsonReader::getBoolean));
                } else if ("publishedDate".equals(fieldName)) {
                    deserializedGalleryImageVersionPublishingProfile.publishedDate = reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString()));
                } else if ("endOfLifeDate".equals(fieldName)) {
                    deserializedGalleryImageVersionPublishingProfile.withEndOfLifeDate(reader
                        .getNullable(nonNullReader -> CoreUtils.parseBestOffsetDateTime(nonNullReader.getString())));
                } else if ("storageAccountType".equals(fieldName)) {
                    deserializedGalleryImageVersionPublishingProfile
                        .withStorageAccountType(StorageAccountType.fromString(reader.getString()));
                } else if ("replicationMode".equals(fieldName)) {
                    deserializedGalleryImageVersionPublishingProfile
                        .withReplicationMode(ReplicationMode.fromString(reader.getString()));
                } else if ("targetExtendedLocations".equals(fieldName)) {
                    List<GalleryTargetExtendedLocation> targetExtendedLocations
                        = reader.readArray(reader1 -> GalleryTargetExtendedLocation.fromJson(reader1));
                    deserializedGalleryImageVersionPublishingProfile
                        .withTargetExtendedLocations(targetExtendedLocations);
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedGalleryImageVersionPublishingProfile;
        });
    }
}
