// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.testingmodels;

import com.azure.core.util.CoreUtils;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.search.documents.indexes.BasicField;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Objects;

public class Book implements JsonSerializable<Book> {
    @BasicField(name = "ISBN")
    @JsonProperty(value = "ISBN")
    private String ISBN;

    @BasicField(name = "Title")
    @JsonProperty(value = "Title")
    private String title;

    @BasicField(name = "Author")
    @JsonProperty(value = "Author")
    private Author author;

    @BasicField(name = "PublishDate")
    @JsonProperty(value = "PublishDate")
    private OffsetDateTime publishDate;

    public String ISBN() {
        return this.ISBN;
    }

    public Book ISBN(String ISBN) {
        this.ISBN = ISBN;
        return this;
    }

    public String title() {
        return this.title;
    }

    public Book title(String title) {
        this.title = title;
        return this;
    }

    public Author author() {
        return this.author;
    }

    public Book author(Author author) {
        this.author = author;
        return this;
    }

    public OffsetDateTime publishDate() {
        return this.publishDate;
    }

    public Book publishDate(OffsetDateTime publishDate) {
        this.publishDate = publishDate;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("ISBN", ISBN)
            .writeStringField("Title", title)
            .writeJsonField("Author", author)
            .writeStringField("PublishDate", Objects.toString(publishDate, null))
            .writeEndObject();
    }

    public static Book fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            Book book = new Book();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("ISBN".equals(fieldName)) {
                    book.ISBN = reader.getString();
                } else if ("Title".equals(fieldName)) {
                    book.title = reader.getString();
                } else if ("Author".equals(fieldName)) {
                    book.author = Author.fromJson(reader);
                } else if ("PublishDate".equals(fieldName)) {
                    book.publishDate
                        = reader.getNullable(nonNull -> CoreUtils.parseBestOffsetDateTime(nonNull.getString()));
                } else {
                    reader.skipChildren();
                }
            }

            return book;
        });
    }
}
