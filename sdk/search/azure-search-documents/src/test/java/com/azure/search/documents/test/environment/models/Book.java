// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.test.environment.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class Book {
    @JsonProperty(value = "ISBN")
    private String ISBN;

    @JsonProperty(value = "Title")
    private String title;

    @JsonProperty(value = "Author")
    private Author author;

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
}
