/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.internal.query;

import java.util.List;

import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.cosmosdb.JsonSerializable;
import com.microsoft.azure.cosmosdb.internal.Constants;
import com.microsoft.azure.cosmosdb.internal.Utils;
import com.microsoft.azure.cosmosdb.internal.routing.PartitionKeyInternal;
import com.microsoft.azure.cosmosdb.internal.routing.Range;

@SuppressWarnings("serial")
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
        super.set(PARTITIONED_QUERY_EXECUTION_INFO_VERSION_PROPERTY, Constants.PartitionedQueryExecutionInfo.VERSION_1);
    }

    public PartitionedQueryExecutionInfoInternal(String jsonString) {
        super(jsonString);
    }

    public PartitionedQueryExecutionInfoInternal(JSONObject jsonObject) {
        super(jsonObject);
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
                : (this.queryRanges = (List<Range<PartitionKeyInternal>>) super.getCollection(QUERY_RANGES_PROPERTY, QUERY_RANGE_CLASS));
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
