// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.serializer.jackson.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class Book {
    @JsonProperty(value = "ISBN")
    private String isbn;

    @JsonProperty(value = "Title")
    private String title;

    @JsonProperty(value = "Author")
    private Author author;

    @JsonProperty(value = "PublishDate")
    private OffsetDateTime publishDate;

    public String isbn() {
        return this.isbn;
    }

    public Book isbn(String isbn) {
        this.isbn = isbn;
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

    public String toString() {
        return String.format("{\"ISBN\":\"%s\",\"Title\":\"%s\",\"Author\":%s,\"PublishDate\":\"%s\"}",
            isbn, title, author, publishDate == null ? null : publishDate.format(DateTimeFormatter.ISO_INSTANT));
    }
}
