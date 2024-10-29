// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * {@link ModelsRepositoryMetadata} is designated to store
 * information about models repository.
 */
public class ModelsRepositoryMetadata implements JsonSerializable<ModelsRepositoryMetadata> {

    private final String commitId;
    private final String publishDateUtc;
    private final String sourceRepo;
    private final Integer totalModelCount;
    private final RepositoryFeatures features;

    public ModelsRepositoryMetadata(String commitId, String publishDateUtc, String sourceRepo, Integer totalModelCount,
        RepositoryFeatures features) {
        this.commitId = commitId;
        this.publishDateUtc = publishDateUtc;
        this.sourceRepo = sourceRepo;
        this.totalModelCount = totalModelCount;
        this.features = features;
    }

    public ModelsRepositoryMetadata() {
        this.commitId = null;
        this.publishDateUtc = null;
        this.sourceRepo = null;
        this.totalModelCount = null;
        this.features = null;
    }

    public String getCommitId() {
        return this.commitId;
    }

    public String getPublishDateUtc() {
        return this.publishDateUtc;
    }

    public String getSourceRepo() {
        return this.sourceRepo;
    }

    public Integer getTotalModelCount() {
        return this.totalModelCount;
    }

    public RepositoryFeatures getFeatures() {
        return this.features;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("commitId", commitId)
            .writeStringField("publishDateUtc", publishDateUtc)
            .writeStringField("sourceRepo", sourceRepo)
            .writeNumberField("totalModelCount", totalModelCount)
            .writeJsonField("features", features)
            .writeEndObject();
    }

    public static ModelsRepositoryMetadata fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String commitId = null;
            String publishDateUtc = null;
            String sourceRepo = null;
            Integer totalModelCount = null;
            RepositoryFeatures features = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("commitId".equals(fieldName)) {
                    commitId = reader.getString();
                } else if ("publishDateUtc".equals(fieldName)) {
                    publishDateUtc = reader.getString();
                } else if ("sourceRepo".equals(fieldName)) {
                    sourceRepo = reader.getString();
                } else if ("totalModelCount".equals(fieldName)) {
                    totalModelCount = reader.getNullable(JsonReader::getInt);
                } else if ("features".equals(fieldName)) {
                    features = RepositoryFeatures.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            return new ModelsRepositoryMetadata(commitId, publishDateUtc, sourceRepo, totalModelCount, features);
        });
    }
}
