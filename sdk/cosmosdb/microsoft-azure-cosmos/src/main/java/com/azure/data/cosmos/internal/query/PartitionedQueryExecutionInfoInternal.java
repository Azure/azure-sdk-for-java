// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.JsonSerializable;
import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.Utils;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternal;
import com.azure.data.cosmos.internal.routing.Range;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public final class PartitionedQueryExecutionInfoInternal extends JsonSerializable {
    static final String QUERY_INFO_PROPERTY = "queryInfo";
    static final String QUERY_RANGES_PROPERTY = "queryRanges";
    static final String PARTITIONED_QUERY_EXECUTION_INFO_VERSION_PROPERTY = "partitionedQueryExecutionInfoVersion";

    @SuppressWarnings("unchecked")
    private static final Class<Range<PartitionKeyInternal>> QUERY_RANGE_CLASS = (Class<Range<PartitionKeyInternal>>) Range
            .getEmptyRange((PartitionKeyInternal) null).getClass();

    private QueryInfo queryInfo;
    private List<Range<PartitionKeyInternal>> queryRanges;

    public PartitionedQueryExecutionInfoInternal() {
        BridgeInternal.setProperty(this, PARTITIONED_QUERY_EXECUTION_INFO_VERSION_PROPERTY, Constants.PartitionedQueryExecutionInfo.VERSION_1);
    }

    public PartitionedQueryExecutionInfoInternal(String jsonString) {
        super(jsonString);
    }

    public int getVersion() {
        return super.getInt(PARTITIONED_QUERY_EXECUTION_INFO_VERSION_PROPERTY);
    }

    public QueryInfo getQueryInfo() {
        return this.queryInfo != null ? this.queryInfo
                : (this.queryInfo = super.getObject(QUERY_INFO_PROPERTY, QueryInfo.class));
    }

    public void setQueryInfo(QueryInfo queryInfo) {
        this.queryInfo = queryInfo;
    }

    public List<Range<PartitionKeyInternal>> getQueryRanges() {
        return this.queryRanges != null ? this.queryRanges
                : (this.queryRanges = super.getList(QUERY_RANGES_PROPERTY, QUERY_RANGE_CLASS));
    }

    public void setQueryRanges(List<Range<PartitionKeyInternal>> queryRanges) {
        this.queryRanges = queryRanges;
    }
        
    public String toJson() {
        try {
            return Utils.getSimpleObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize partition query execution info internal.");
        }
    }
}
