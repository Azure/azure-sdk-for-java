// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.testingmodels;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.BasicField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

public class Author implements JsonSerializable<Author> {
    @BasicField(name = "FirstName")
    @JsonProperty(value = "FirstName")
    private String firstName;

    @BasicField(name = "LastName")
    @JsonProperty(value = "LastName")
    private String lastName;

    public String firstName() {
        return this.firstName;
    }

    public Author firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String lastName() {
        return this.lastName;
    }

    public Author lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("FirstName", firstName)
            .writeStringField("LastName", lastName)
            .writeEndObject();
    }

    public static Author fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Author author = new Author();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("FirstName".equals(fieldName)) {
                    author.firstName = reader.getString();
                } else if ("LastName".equals(fieldName)) {
                    author.lastName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return author;
        });
    }
}
