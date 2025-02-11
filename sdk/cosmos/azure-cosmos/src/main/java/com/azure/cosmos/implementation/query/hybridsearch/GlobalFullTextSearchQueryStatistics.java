// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.hybridsearch;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GlobalFullTextSearchQueryStatistics {
    @JsonProperty(Constants.Properties.DOCUMENT_COUNT)
    private Long documentCount;
    @JsonProperty(Constants.Properties.FULL_TEXT_QUERY_STATISTICS)
    private List<FullTextQueryStatistics> fullTextQueryStatistics;

    public GlobalFullTextSearchQueryStatistics(Document document) {
        this.documentCount = document.getLong(Constants.Properties.DOCUMENT_COUNT);
        this.fullTextQueryStatistics = document.getList(Constants.Properties.FULL_TEXT_QUERY_STATISTICS, FullTextQueryStatistics.class);
    }

    public GlobalFullTextSearchQueryStatistics() {}

    public Long getDocumentCount() {
        return documentCount;
    }

    public void setDocumentCount(Long documentCount) {
        this.documentCount = documentCount;
    }

    public List<FullTextQueryStatistics> getFullTextQueryStatistics() {
        return fullTextQueryStatistics;
    }

    public void setFullTextQueryStatistics(List<FullTextQueryStatistics> fullTextQueryStatistics) {
        this.fullTextQueryStatistics = fullTextQueryStatistics;
    }
}
