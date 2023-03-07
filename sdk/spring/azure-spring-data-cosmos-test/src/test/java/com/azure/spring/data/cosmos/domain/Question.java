// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.domain;

import com.azure.cosmos.models.IndexingMode;
import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.CosmosIndexingPolicy;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import org.springframework.data.annotation.Id;

import java.util.Objects;
import java.util.UUID;

@Container
@CosmosIndexingPolicy(mode = IndexingMode.CONSISTENT)
public class Question {

    @Id
    @PartitionKey
    private String id = UUID.randomUUID().toString();

    private String url;

    public Question(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public Question() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Question question = (Question) o;
        return Objects.equals(id, question.id)
            && Objects.equals(url, question.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url);
    }

    @Override
    public String toString() {
        return "Question{"
            + "id='"
            + id
            + '\''
            + ", url='"
            + url
            + '\''
            + '}';
    }
}
