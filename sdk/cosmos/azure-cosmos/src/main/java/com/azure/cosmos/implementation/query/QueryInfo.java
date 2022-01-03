// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.RequestTimeline;
import com.azure.cosmos.implementation.DiagnosticsInstantSerializer;
import com.azure.cosmos.implementation.query.aggregation.AggregateOperator;
import com.azure.cosmos.implementation.JsonSerializable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.Strings;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Used internally to encapsulates a query's information in the Azure Cosmos DB database service.
 */
public final class QueryInfo extends JsonSerializable {
    public static final QueryInfo EMPTY = new QueryInfo();

    private static final String HAS_SELECT_VALUE = "hasSelectValue";
    private Integer top;
    private List<SortOrder> orderBy;
    private Collection<AggregateOperator> aggregates;
    private Collection<String> orderByExpressions;
    private String rewrittenQuery;
    private Integer offset;
    private Integer limit;
    private DistinctQueryType distinctQueryType;
    private QueryPlanDiagnosticsContext queryPlanDiagnosticsContext;
    private DCountInfo dCountInfo;

    public QueryInfo() {
    }

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represent the
     * {@link JsonSerializable}
     */
    public QueryInfo(ObjectNode objectNode) {
        super(objectNode);
    }

    public QueryInfo(String jsonString) {
        super(jsonString);
    }

    public Integer getTop() {
        return this.top != null ? this.top : (this.top = super.getInt("top"));
    }

    public List<SortOrder> getOrderBy() {
        return this.orderBy != null ? this.orderBy : (this.orderBy = super.getList("orderBy", SortOrder.class));
    }

    public String getRewrittenQuery() {
        return this.rewrittenQuery != null ? this.rewrittenQuery
                   : (this.rewrittenQuery = super.getString("rewrittenQuery"));
    }

    public boolean hasTop() {
        return this.getTop() != null;
    }

    public boolean hasOrderBy() {
        Collection<SortOrder> orderBy = this.getOrderBy();
        return orderBy != null && orderBy.size() > 0;
    }

    public boolean hasRewrittenQuery() {
        return !StringUtils.isEmpty(this.getRewrittenQuery());
    }

    public boolean hasAggregates() {
        Collection<AggregateOperator> aggregates = this.getAggregates();
        boolean hasAggregates = aggregates != null && aggregates.size() > 0;

        if (hasAggregates) {
            return hasAggregates;
        }
        boolean aggregateAliasMappingNonEmpty = (this.getGroupByAliasToAggregateType() != null)
                                                 && !this.getGroupByAliasToAggregateType()
                                                        .values()
                                                        .isEmpty();
        return aggregateAliasMappingNonEmpty;
    }

    public Collection<AggregateOperator> getAggregates() {
        return this.aggregates != null
                ? this.aggregates
                : (this.aggregates = super.getCollection("aggregates", AggregateOperator.class));
    }

    public Collection<String> getOrderByExpressions() {
        return this.orderByExpressions != null
                   ? this.orderByExpressions
                   : (this.orderByExpressions = super.getCollection("orderByExpressions", String.class));
    }

    public boolean hasSelectValue() {
        return super.has(HAS_SELECT_VALUE) && Boolean.TRUE.equals(super.getBoolean(HAS_SELECT_VALUE));
    }

    public boolean hasOffset() {
        return this.getOffset() != null;
    }

    public boolean hasLimit() {
        return this.getLimit() != null;
    }

    public Integer getLimit() {
        return this.limit != null ? this.limit : (this.limit = super.getInt("limit"));
    }

    public Integer getOffset() {
        return this.offset != null ? this.offset : (this.offset = super.getInt("offset"));
    }

    public boolean hasDistinct() {
        return this.getDistinctQueryType() != DistinctQueryType.NONE;
    }

    public DistinctQueryType getDistinctQueryType() {
        if (distinctQueryType != null) {
            return distinctQueryType;
        } else {
            final String distinctType = super.getString("distinctType");

            if (distinctType == null) {
                return DistinctQueryType.NONE;
            }

            switch (distinctType) {
                case "Ordered":
                    distinctQueryType = DistinctQueryType.ORDERED;
                    break;
                case "Unordered":
                    distinctQueryType = DistinctQueryType.UNORDERED;
                    break;
                default:
                    distinctQueryType = DistinctQueryType.NONE;
                    break;
            }
            return distinctQueryType;
        }
    }

    public boolean hasGroupBy() {
        final List<String> groupByExpressions = super.getList("groupByExpressions", String.class);
        return groupByExpressions != null && !groupByExpressions.isEmpty();
    }

    public Map<String, AggregateOperator> getGroupByAliasToAggregateType(){
            Map<String, AggregateOperator>  groupByAliasToAggregateMap;
            groupByAliasToAggregateMap = super.getMap("groupByAliasToAggregateType");
            return groupByAliasToAggregateMap;
    }

    public List<String> getGroupByAliases() {
        return super.getList("groupByAliases", String.class);
    }

    public boolean hasDCount() {
        return this.getDCountInfo() != null;
    }

    public DCountInfo getDCountInfo() {
        return this.dCountInfo != null ?
                   this.dCountInfo : (this.dCountInfo = super.getObject("dCountInfo", DCountInfo.class));
    }

    public String getDCountAlias() {
        return this.dCountInfo.getDCountAlias();
    }

    public boolean isValueAggregate() {
        return Strings.isNullOrEmpty(this.getDCountAlias());
    }

    public QueryPlanDiagnosticsContext getQueryPlanDiagnosticsContext() {
        return queryPlanDiagnosticsContext;
    }

    public void setQueryPlanDiagnosticsContext(QueryPlanDiagnosticsContext queryPlanDiagnosticsContext) {
        this.queryPlanDiagnosticsContext = queryPlanDiagnosticsContext;
    }

    public static final class QueryPlanDiagnosticsContext {
        @JsonSerialize(using = DiagnosticsInstantSerializer.class)
        private volatile Instant startTimeUTC;
        @JsonSerialize(using = DiagnosticsInstantSerializer.class)
        private volatile Instant endTimeUTC;
        private volatile RequestTimeline requestTimeline;
        public QueryPlanDiagnosticsContext(Instant startTimeUTC, Instant endTimeUTC) {
            this.startTimeUTC = startTimeUTC;
            this.endTimeUTC = endTimeUTC;
        }

        public QueryPlanDiagnosticsContext(Instant startTimeUTC, Instant endTimeUTC, RequestTimeline requestTimeline) {
            this.startTimeUTC = startTimeUTC;
            this.endTimeUTC = endTimeUTC;
            this.requestTimeline = requestTimeline;
        }

        public Instant getStartTimeUTC() {
            return startTimeUTC;
        }

        public Instant getEndTimeUTC() {
            return endTimeUTC;
        }

        public RequestTimeline getRequestTimeline() {
            return requestTimeline;
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

