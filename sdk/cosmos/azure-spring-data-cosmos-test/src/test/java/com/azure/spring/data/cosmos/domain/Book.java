// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.spring.data.cosmos.common.TestConstants;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

@Container(containerName = TestConstants.BOOK_COLLECTION_NAME,
    autoCreateContainer = false)
public class Book {
    @Id
    String id;

    @PartitionKey
    String authorId;

    String title;

    public Book() {

    }

    public Book(String id, String authorId, String title) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
    }

    /**
     * Getter for property 'id'.
     *
     * @return Value for property 'id'.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for property 'id'.
     *
     * @param id Value to set for property 'id'.
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Getter for property 'authorId'.
     *
     * @return Value for property 'authorId'.
     */
    public String getAuthorId() {
        return authorId;
    }

    /**
     * Setter for property 'authorId'.
     *
     * @param authorId Value to set for property 'authorId'.
     */
    public void setAuthorId(final String authorId) {
        this.authorId = authorId;
    }

    /**
     * Getter for property 'title'.
     *
     * @return Value for property 'title'.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter for property 'title'.
     *
     * @param title Value to set for property 'title'.
     */
    public void setTitle(final String title) {
        this.title = title;
    }
}
