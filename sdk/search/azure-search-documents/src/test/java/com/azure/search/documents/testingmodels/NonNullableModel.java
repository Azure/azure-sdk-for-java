// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.testingmodels;

import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UseOfObsoleteDateTimeApi")
public class NonNullableModel implements JsonSerializable<NonNullableModel> {

    @JsonProperty(value = "Key")
    private String key;

    @JsonProperty(value = "Rating")
    private int rating;

    @JsonProperty(value = "Count")
    private long count;

    @JsonProperty(value = "IsEnabled")
    private boolean isEnabled;

    @JsonProperty(value = "Ratio")
    private double ratio;

    @JsonProperty(value = "StartDate")
    private OffsetDateTime startDate;

    @JsonProperty(value = "EndDate")
    private OffsetDateTime endDate;

    @JsonProperty(value = "TopLevelBucket")
    private Bucket topLevelBucket;

    @JsonProperty(value = "Buckets")
    private Bucket[] buckets;

    public String key() {
        return key;
    }

    public NonNullableModel key(String key) {
        this.key = key;
        return this;
    }

    public NonNullableModel rating(int rating) {
        this.rating = rating;
        return this;
    }

    public NonNullableModel count(long count) {
        this.count = count;
        return this;
    }

    public NonNullableModel isEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }

    public NonNullableModel ratio(double ratio) {
        this.ratio = ratio;
        return this;
    }

    public NonNullableModel startDate(OffsetDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public NonNullableModel endDate(OffsetDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public NonNullableModel topLevelBucket(Bucket topLevelBucket) {
        this.topLevelBucket = topLevelBucket;
        return this;
    }

    public NonNullableModel buckets(Bucket[] buckets) {
        this.buckets = CoreUtils.clone(buckets);
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("Key", key)
            .writeIntField("Rating", rating)
            .writeLongField("Count", count)
            .writeBooleanField("IsEnabled", isEnabled)
            .writeDoubleField("Ratio", ratio)
            .writeStringField("StartDate", Objects.toString(startDate, null))
            .writeStringField("EndDate", Objects.toString(endDate, null))
            .writeJsonField("TopLevelBucket", topLevelBucket)
            .writeArrayField("Buckets", buckets, JsonWriter::writeJson)
            .writeEndObject();
    }

    public static NonNullableModel fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            NonNullableModel model = new NonNullableModel();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("Key".equals(fieldName)) {
                    model.key = reader.getString();
                } else if ("Rating".equals(fieldName)) {
                    model.rating = reader.getInt();
                } else if ("Count".equals(fieldName)) {
                    model.count = reader.getLong();
                } else if ("IsEnabled".equals(fieldName)) {
                    model.isEnabled = reader.getBoolean();
                } else if ("Ratio".equals(fieldName)) {
                    model.ratio = reader.getDouble();
                } else if ("StartDate".equals(fieldName)) {
                    model.startDate = reader.getNullable(nonNull -> OffsetDateTime.parse(nonNull.getString()));
                } else if ("EndDate".equals(fieldName)) {
                    model.endDate = reader.getNullable(nonNull -> OffsetDateTime.parse(nonNull.getString()));
                } else if ("TopLevelBucket".equals(fieldName)) {
                    model.topLevelBucket = Bucket.fromJson(reader);
                } else if ("Buckets".equals(fieldName)) {
                    List<Bucket> buckets = reader.readArray(Bucket::fromJson);
                    if (buckets != null) {
                        model.buckets = buckets.toArray(new Bucket[0]);
                    }
                } else {
                    reader.skipChildren();
                }
            }
            return model;
        });
    }
}
