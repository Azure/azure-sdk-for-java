// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryExecutionInfo {

    @JsonProperty(value = "reverseRidEnabled")
    private boolean reverseRidEnabled;

    @JsonProperty(value = "reverseIndexScan")
    public boolean reverseIndexScan;

    public QueryExecutionInfo() {}

    public boolean getReverseRidEnabled() {
        return this.reverseRidEnabled;
    }

    public boolean getReverseIndexScan() {
        return this.reverseIndexScan;
    }
}
