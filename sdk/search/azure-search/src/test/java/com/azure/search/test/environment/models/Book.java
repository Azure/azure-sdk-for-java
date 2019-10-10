// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.test.environment.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Book)) {
            return false;
        }
        Book book = (Book) o;
        return Objects.equals(ISBN, book.ISBN)
            && Objects.equals(title, book.title)
            && Objects.equals(author, book.author)
            && Objects.equals(publishDate, book.publishDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ISBN, title, author, publishDate);
    }
}
