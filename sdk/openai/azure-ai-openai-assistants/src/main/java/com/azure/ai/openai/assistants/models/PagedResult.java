package com.azure.ai.openai.assistants.models;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@Immutable
public class PagedResult<T> {

    @JsonProperty(value = "object")
    private String object = "list";

    @JsonProperty(value = "data")
    private List<T> data;

    @JsonProperty(value = "first_id")
    private String firstId;

    @JsonProperty(value = "last_id")
    private String lastId;

    @JsonProperty(value = "has_more")
    private boolean hasMore;

    @JsonCreator
    private PagedResult(@JsonProperty(value = "data") List<T> data,
                   @JsonProperty(value = "first_id") String firstId,
                   @JsonProperty(value = "last_id") String lastId,
                   @JsonProperty(value = "has_more") boolean hasMore) {
        this.data = data;
        this.firstId = firstId;
        this.lastId = lastId;
        this.hasMore = hasMore;
    }

    public String getObject() {
        return this.object;
    }

    public List<T> getData() {
        return this.data;
    }

    public String getFirstId() {
        return this.firstId;
    }

    public String getLastId() {
        return this.lastId;
    }

    public boolean isHasMore() {
        return this.hasMore;
    }
}
