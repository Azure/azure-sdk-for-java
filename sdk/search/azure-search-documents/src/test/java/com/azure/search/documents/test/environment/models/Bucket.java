// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

public class Bucket implements JsonSerializable<Bucket> {

    @JsonProperty(value = "BucketName")
    private String bucketName;

    @JsonProperty(value = "Count")
    private int count;

    public Bucket bucketName(String bucketName) {
        this.bucketName = bucketName;
        return this;
    }

    public String getBucketName() {
        return this.bucketName;
    }

    public Bucket count(int count) {
        this.count = count;
        return this;
    }

    public int getCount() {
        return count;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("BucketName", bucketName)
            .writeIntField("Count", count)
            .writeEndObject();
    }

    public static Bucket fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Bucket bucket = new Bucket();

            while (reader. nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("BucketName".equals(fieldName)) {
                    bucket.bucketName = reader.getString();
                } else if ("Count".equals(fieldName)) {
                    bucket.count = reader.getInt();
                } else {
                    reader.skipChildren();
                }
            }

            return bucket;
        });
    }
}
