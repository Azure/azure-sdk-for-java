// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.query.hybridsearch.HybridSearchQueryInfo;
import com.azure.cosmos.implementation.routing.Range;

import java.util.List;

public class PartitionKeyRangesAndQueryInfos {
    QueryInfo queryInfo;
    HybridSearchQueryInfo hybridSearchQueryInfo;
    List<Range<String>> targetRanges;
    List<Range<String>> allRanges;

    public PartitionKeyRangesAndQueryInfos(QueryInfo queryInfo, HybridSearchQueryInfo hybridSearchQueryInfo, List<Range<String>> targetRanges, List<Range<String>> allRanges) {
        this.queryInfo = queryInfo;
        this.hybridSearchQueryInfo = hybridSearchQueryInfo;
        this.targetRanges = targetRanges;
        this.allRanges = allRanges;
    }

    public QueryInfo getQueryInfo() {
        return queryInfo;
    }

    public void setQueryInfo(QueryInfo queryInfo) {
        this.queryInfo = queryInfo;
    }

    public HybridSearchQueryInfo getHybridSearchQueryInfo() {
        return hybridSearchQueryInfo;
    }

    public void setHybridSearchQueryInfo(HybridSearchQueryInfo hybridSearchQueryInfo) {
        this.hybridSearchQueryInfo = hybridSearchQueryInfo;
    }

    public List<Range<String>> getTargetRanges() {
        return targetRanges;
    }

    public void setTargetRanges(List<Range<String>> targetRanges) {
        this.targetRanges = targetRanges;
    }

    public List<Range<String>> getAllRanges() {
        return allRanges;
    }

    public void setAllRanges(List<Range<String>> allRanges) {
        this.allRanges = allRanges;
    }
}
