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

import com.microsoft.azure.cosmosdb.JsonSerializable;
import com.microsoft.azure.cosmosdb.internal.Constants;
import com.microsoft.azure.cosmosdb.internal.routing.Range;

/**
 * Used internally to encapsulates execution information for a query in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public final class PartitionedQueryExecutionInfo extends JsonSerializable {
    @SuppressWarnings("unchecked")
    private static final Class<Range<String>> QUERY_RANGES_CLASS = (Class<Range<String>>) Range
            .getEmptyRange((String) null).getClass();

    private QueryInfo queryInfo;
    private List<Range<String>> queryRanges;

    PartitionedQueryExecutionInfo(QueryInfo queryInfo, List<Range<String>> queryRanges) {
        this.queryInfo = queryInfo;
        this.queryRanges = queryRanges;

        super.set(
                PartitionedQueryExecutionInfoInternal.PARTITIONED_QUERY_EXECUTION_INFO_VERSION_PROPERTY,
                Constants.PartitionedQueryExecutionInfo.VERSION_1);
    }

    public PartitionedQueryExecutionInfo(String jsonString) {
        super(jsonString);
    }

    public PartitionedQueryExecutionInfo(JSONObject jsonObject) {
        super(jsonObject);
    }

    public int getVersion() {
        return super.getInt(PartitionedQueryExecutionInfoInternal.PARTITIONED_QUERY_EXECUTION_INFO_VERSION_PROPERTY);
    }

    public QueryInfo getQueryInfo() {
        return this.queryInfo != null ? this.queryInfo
                : (this.queryInfo = super.getObject(
                        PartitionedQueryExecutionInfoInternal.QUERY_INFO_PROPERTY, QueryInfo.class));
    }

    public List<Range<String>> getQueryRanges() {
        return this.queryRanges != null ? this.queryRanges
                : (this.queryRanges = (List<Range<String>>) super.getCollection(
                        PartitionedQueryExecutionInfoInternal.QUERY_RANGES_PROPERTY, QUERY_RANGES_CLASS));
    }
}
