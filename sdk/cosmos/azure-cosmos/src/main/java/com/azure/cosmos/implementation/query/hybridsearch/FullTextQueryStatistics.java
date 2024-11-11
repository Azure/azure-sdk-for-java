// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.hybridsearch;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FullTextQueryStatistics {
    @JsonProperty(Constants.Properties.HIT_COUNTS)
    private List<Long> hitCounts;
    @JsonProperty(Constants.Properties.TOTAL_WORD_COUNT)
    private Long totalWordCount;
    private JsonSerializable jsonSerializable;

    public FullTextQueryStatistics(List<Long> hitCounts, Long totalWordCount) {
        this.hitCounts = hitCounts;
        this.totalWordCount = totalWordCount;
    }

    public FullTextQueryStatistics() {
        this.jsonSerializable = new JsonSerializable();
    }

    public FullTextQueryStatistics(JsonSerializable jsonSerializable) {
        this.jsonSerializable = new JsonSerializable();
        if (jsonSerializable.get(Constants.Properties.HIT_COUNTS) == null) {
            throw new IllegalArgumentException("Missing required property hitCounts in " + Constants.Properties.HIT_COUNTS);
        }
        if (jsonSerializable.get(Constants.Properties.TOTAL_WORD_COUNT) == null) {
            throw new IllegalArgumentException("Missing required property totalWordCount in " + Constants.Properties.TOTAL_WORD_COUNT);
        }

        this.hitCounts = jsonSerializable.getList(Constants.Properties.HIT_COUNTS, Long.class);
        this.totalWordCount = jsonSerializable.getLong(Constants.Properties.TOTAL_WORD_COUNT);
    }

    public List<Long> getHitCounts() {
        return hitCounts;
    }

    public void setHitCounts(List<Long> hitCounts) {
        this.hitCounts = hitCounts;
    }

    public Long getTotalWordCount() {
        return totalWordCount;
    }

    public void setTotalWordCount(Long totalWordCount) {
        this.totalWordCount = totalWordCount;
    }
}
