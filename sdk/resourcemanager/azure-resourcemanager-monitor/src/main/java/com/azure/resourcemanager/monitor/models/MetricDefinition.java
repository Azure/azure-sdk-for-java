// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.models;

import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.fluent.models.MetricDefinitionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import reactor.core.publisher.Mono;

/** The Azure metric definition entries are of type MetricDefinition. */
public interface MetricDefinition extends HasManager<MonitorManager>, HasInnerModel<MetricDefinitionInner> {

    /**
     * Get the resourceId value.
     *
     * @return the resourceId value
     */
    String resourceId();

    /**
     * Get the name value.
     *
     * @return the name value
     */
    LocalizableString name();

    /**
     * Get the namespace value.
     *
     * @return the namespace value
     */
    String namespace();

    /**
     * Get the isDimensionRequired value.
     *
     * @return the isDimensionRequired value
     */
    boolean isDimensionRequired();

    /**
     * the name and the display name of the dimension, i.e. it is a localizable string.
     *
     * @return the list of dimension values.
     */
    List<LocalizableString> dimensions();

    /**
     * the collection of what aggregation types are supported.
     *
     * @return the list of supported aggregation type values.
     */
    List<AggregationType> supportedAggregationTypes();

    /**
     * Get the unit value.
     *
     * @return the unit value
     */
    Unit unit();

    /**
     * Get the primaryAggregationType value.
     *
     * @return the primaryAggregationType value
     */
    AggregationType primaryAggregationType();

    /**
     * Get the metricAvailabilities value.
     *
     * @return the metricAvailabilities value
     */
    List<MetricAvailability> metricAvailabilities();

    /**
     * Get the id value.
     *
     * @return the id value
     */
    String id();

    /**
     * Begins a definition for a new resource Metric query.
     *
     * @return the stage of start time filter definition.
     */
    MetricsQueryDefinitionStages.WithMetricStartTimeFilter defineQuery();

    /** The entirety of a Metrics query definition. */
    interface MetricsQueryDefinition
        extends MetricsQueryDefinitionStages.WithMetricStartTimeFilter,
            MetricsQueryDefinitionStages.WithMetricEndFilter,
            MetricsQueryDefinitionStages.WithMetricsQueryExecute {
    }

    /** Grouping of Metric query stages. */
    interface MetricsQueryDefinitionStages {

        /** The stage of a Metric query allowing to specify start time filter. */
        interface WithMetricStartTimeFilter {
            /**
             * Sets the start time for Metric query filter.
             *
             * @param startTime specifies start time of cut off filter.
             * @return the stage of end time filter definition.
             */
            WithMetricEndFilter startingFrom(OffsetDateTime startTime);
        }

        /** The stage of a Metric query allowing to specify end time filter. */
        interface WithMetricEndFilter {
            /**
             * Sets the end time for Metric query filter.
             *
             * @param endTime specifies end time of cut off filter.
             * @return the stage of optional query parameter definition and query execution.
             */
            WithMetricsQueryExecute endsBefore(OffsetDateTime endTime);
        }

        /** The stage of a Metric query allowing to specify optional filters and execute the query. */
        interface WithMetricsQueryExecute {
            /**
             * Sets the list of aggregation types to retrieve.
             *
             * @param aggregation The list of aggregation types (comma separated) to retrieve.
             * @return the stage of optional query parameter definition and query execution.
             */
            WithMetricsQueryExecute withAggregation(String aggregation);

            /**
             * Sets the interval of the query.
             *
             * @param interval The interval of the query.
             * @return the stage of optional query parameter definition and query execution.
             */
            WithMetricsQueryExecute withInterval(Duration interval);

            /**
             * Sets the **$filter** that is used to reduce the set of metric data returned. &lt;br&gt;Example:&lt;br&gt;
             * Metric contains metadata A, B and C.&lt;br&gt;
             *
             * <p>- Return all time series of C where A = a1 and B = b1 or b2&lt;br&gt; **$filter=A eq ‘a1’ and B eq
             * ‘b1’ or B eq ‘b2’ and C eq ‘*’**&lt;br&gt;
             *
             * <p>- Invalid variant:&lt;br&gt; **$filter=A eq ‘a1’ and B eq ‘b1’ and C eq ‘*’ or B = ‘b2’**&lt;br&gt;
             * This is invalid because the logical or operator cannot separate two different metadata names.&lt;br&gt;
             *
             * <p>- Return all time series where A = a1, B = b1 and C = c1:&lt;br&gt; **$filter=A eq ‘a1’ and B eq ‘b1’
             * and C eq ‘c1’**&lt;br&gt;
             *
             * <p>- Return all time series where A = a1&lt;br&gt; **$filter=A eq ‘a1’ and B eq ‘*’ and C eq ‘*’**.
             *
             * @param odataFilter the **$filter** to reduce the set of the returned metric data.
             * @return the stage of optional query parameter definition and query execution.
             */
            WithMetricsQueryExecute withOdataFilter(String odataFilter);

            /**
             * Reduces the set of data collected. The syntax allowed depends on the operation. See the operation's
             * description for details. Possible values include: 'Data', 'Metadata'
             *
             * @param resultType the type of metric to retrieve.
             * @return the stage of optional query parameter definition and query execution.
             */
            WithMetricsQueryExecute withResultType(ResultType resultType);

            /**
             * Sets the maximum number of records to retrieve. Valid only if $filter is specified. Defaults to 10.
             *
             * @param top the maximum number of records to retrieve.
             * @return the stage of optional query parameter definition and query execution.
             */
            WithMetricsQueryExecute selectTop(int top);

            /**
             * Sets the aggregation to use for sorting results and the direction of the sort. Only one order can be
             * specified. Examples: sum asc.
             *
             * @param orderBy the aggregation to use for sorting results and the direction of the sort.
             * @return the stage of optional query parameter definition and query execution.
             */
            WithMetricsQueryExecute orderBy(String orderBy);

            /**
             * Filters Metrics for a given namespace.
             *
             * @param namespaceName Metric namespace to query metric definitions for.
             * @return the stage of optional query parameter definition and query execution.
             */
            WithMetricsQueryExecute filterByNamespace(String namespaceName);

            /**
             * Executes the query.
             *
             * @return Metric collection received after query execution.
             */
            MetricCollection execute();

            /**
             * Executes the query.
             *
             * @return a representation of the deferred computation of Metric collection query call
             */
            Mono<MetricCollection> executeAsync();
        }
    }
}
