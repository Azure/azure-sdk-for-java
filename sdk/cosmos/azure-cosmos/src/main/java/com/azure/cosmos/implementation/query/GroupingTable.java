// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.query.aggregation.AggregateOperator;
import com.azure.cosmos.implementation.routing.UInt128;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupingTable {
    private static final List<AggregateOperator> EMPTY_AGGREGATE_OPERATORS = new ArrayList<>();

    private final Map<UInt128, SingleGroupAggregator> table;
    private final Map<String, AggregateOperator> groupByAliasToAggregateType;
    private final List<String> orderedAliases;
    private final boolean hasSelectValue;

    GroupingTable(Map<String, AggregateOperator> groupByAliasToAggregateType, List<String> orderedAliases,
        boolean hasSelectValue) {
        if (groupByAliasToAggregateType == null) {
            throw new IllegalArgumentException("groupByAliasToAggregateType cannot be null");
        }
        this.table = new HashMap<>();
        this.groupByAliasToAggregateType = groupByAliasToAggregateType;
        this.orderedAliases = orderedAliases;
        this.hasSelectValue = hasSelectValue;
    }

    public void addPayLoad(GroupByDocumentQueryExecutionContext<?>.RewrittenGroupByProjection rewrittenGroupByProjection) {
        try {
            final UInt128 groupByKeysHash = DistinctHash.getHash(rewrittenGroupByProjection.getGroupByItems());
            SingleGroupAggregator singleGroupAggregator;
            if (!this.table.containsKey(groupByKeysHash)) {
                singleGroupAggregator = SingleGroupAggregator.create(EMPTY_AGGREGATE_OPERATORS,
                                                                     this.groupByAliasToAggregateType,
                                                                     this.orderedAliases,
                                                                     this.hasSelectValue,
                                                                      /*continuationtoken*/ null);
                this.table.put(groupByKeysHash, singleGroupAggregator);
            } else {
                singleGroupAggregator = table.get(groupByKeysHash);
            }

            singleGroupAggregator.addValues(rewrittenGroupByProjection.getPayload());

        } catch (IOException e) {
            throw new IllegalStateException("Failed to add payload to groupby projection", e);
        }
    }

    public List<Document> drain(int maxItemCount) {
        Collection<UInt128> keys = this.table.keySet().stream().limit(maxItemCount).collect(Collectors.toList());
        List<SingleGroupAggregator> singleGroupAggregators = new ArrayList<>(keys.size());
        for (UInt128 key : keys) {
            singleGroupAggregators.add(this.table.get(key));
            this.table.remove(key);
        }
        List<Document> results = new ArrayList<>();
        for (SingleGroupAggregator singleGroupAggregator : singleGroupAggregators) {
            results.add(singleGroupAggregator.getResult());
        }

        return results;
    }
}
