// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.query.aggregation.AggregateOperator;
import com.azure.cosmos.implementation.query.aggregation.Aggregator;
import com.azure.cosmos.implementation.query.aggregation.AverageAggregator;
import com.azure.cosmos.implementation.query.aggregation.CountAggregator;
import com.azure.cosmos.implementation.query.aggregation.MaxAggregator;
import com.azure.cosmos.implementation.query.aggregation.MinAggregator;
import com.azure.cosmos.implementation.query.aggregation.SumAggregator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aggregates all the projections for a single grouping.
 */
public abstract class SingleGroupAggregator {

    public static SingleGroupAggregator create(List<AggregateOperator> aggregates,
                                               Map<String, AggregateOperator> aggregateAliasToAggregateType,
                                               List<String> orderedAliases,
                                               boolean hasSelectValue,
                                               String continuationToken) {

        if (aggregates == null) {
            throw new IllegalArgumentException("aggregates");
        }

        if (aggregateAliasToAggregateType == null) {
            throw new IllegalArgumentException("aggregates");
        }

        SingleGroupAggregator singleGroupAggregator;

        if (hasSelectValue) {
            if (!aggregates.isEmpty()) {
                // SELECT VALUE <AGGREGATE>
                singleGroupAggregator = SelectValueAggregateValues.create(
                    aggregates.get(0),
                    continuationToken);
            } else {
                // SELECT VALUE <NON AGGREGATE>
                singleGroupAggregator = SelectValueAggregateValues.create(
                    null, continuationToken);
            }
        } else {
            singleGroupAggregator = SelectListAggregateValues.create(
                aggregateAliasToAggregateType,
                orderedAliases,
                continuationToken);
        }

        return singleGroupAggregator;
    }

    /**
     * Adds the payload for group by values
     * @param values
     */
    public abstract void addValues(Document values);

    /**
     * Forms the final result of the grouping.
     * @return Document
     */
    public abstract Document getResult();

    public abstract Resource getDocumentContinuationToken();

    /**
     * For SELECT VALUE queries there is only one value for each grouping.
     * This class just helps maintain that and captures the first value across all continuations.
     */
    public static final class SelectValueAggregateValues extends SingleGroupAggregator {
        private final AggregateValue aggregateValue;

        public SelectValueAggregateValues(AggregateValue aggregateValue) {
            //TODO: null check
            this.aggregateValue = aggregateValue;
        }

        public static SingleGroupAggregator create(AggregateOperator aggregateOperator, String continuationToken) {
            AggregateValue aggregateValue = AggregateValue.create(aggregateOperator, continuationToken);
            return new SelectValueAggregateValues(aggregateValue);
        }

        @Override
        public void addValues(Document values) {
            this.aggregateValue.addValue(values);
        }

        @Override
        public Document getResult() {
            Document document;
            Object result = aggregateValue.getResult();
            if (result instanceof Document) {
                document = (Document) aggregateValue.getResult();
            } else {
                document = new Document();
                if (result instanceof Undefined) {
                    result = null;
                }
                // This is for value queries.Need to append value property for scalar values (non key value pairs)
                // to make them a key value pair document as our impl is based on Document.
                document.set(Constants.Properties.VALUE, result);
            }
            return document;
        }

        @Override
        public Resource getDocumentContinuationToken() {
            return null;
        }
    }

    /**
     * For select list queries we need to create a dictionary of alias to group by value.
     * For each grouping drained from the backend we merge it with the results here.
     * At the end this class will form a JSON object with the correct aliases and grouping result.
     */
    public static final class SelectListAggregateValues extends SingleGroupAggregator {
        Map<String, AggregateValue> aliasToValue;
        List<String> orderedAliases;

        private SelectListAggregateValues(
            Map<String, AggregateValue> aliasToValue,
            List<String> orderedAliases) {
            this.aliasToValue = aliasToValue;
            this.orderedAliases = orderedAliases;
        }

        public static SingleGroupAggregator create(
            Map<String, AggregateOperator> aggregateAliasToAggregateType, List<String> orderedAliases,
            String continuationToken) {
            if (aggregateAliasToAggregateType == null) {
                throw new IllegalArgumentException("aggregateAliasToAggregateType cannot be null");
            }

            if (orderedAliases == null) {
                throw new IllegalArgumentException("orderedAliases cannot be null");
            }

            Map<String, AggregateValue> groupingTable = new HashMap<>();

            for (Map.Entry<String, AggregateOperator> aliasToAggregate : aggregateAliasToAggregateType.entrySet()) {
                String alias = aliasToAggregate.getKey();
                AggregateOperator aggregateOperator = null;
                if (aliasToAggregate.getValue() != null) {
                    aggregateOperator = AggregateOperator.valueOf(String.valueOf(aliasToAggregate.getValue()));
                }
                AggregateValue aggregateValue = AggregateValue.create(aggregateOperator, continuationToken);
                groupingTable.put(alias, aggregateValue);

            }
            return new SelectListAggregateValues(groupingTable, orderedAliases);
        }

        @Override
        public void addValues(Document values) {
            for (Map.Entry<String, AggregateValue> aliasAndValue : this.aliasToValue.entrySet()) {
                String alias = aliasAndValue.getKey();
                AggregateValue aggregateValue = aliasAndValue.getValue();
                aggregateValue.addValue(values.get(alias));
            }
        }

        @Override
        public Document getResult() {
            Document aggregateDocument = new Document();
            for (String alias : this.orderedAliases) {
                AggregateValue aggregateValue = this.aliasToValue.get(alias);
                if (aggregateValue.getResult() != null) {
                    aggregateDocument.set(alias, aggregateValue.getResult());
                }
            }
            return aggregateDocument;
        }

        @Override
        public Resource getDocumentContinuationToken() {
            return null;
        }
    }

    /**
     * With a group by value we need to encapsulate the fact that we have:
     * 1) aggregate group by values
     * 2) scalar group by values.
     */
    private abstract static class AggregateValue {
        public static AggregateValue create(AggregateOperator aggregateOperator, String continuationToken) {
            AggregateValue value;
            if (aggregateOperator != null) {
                value = AggregateAggregateValue.createAggregateValue(aggregateOperator,
                                                                     continuationToken);
            } else {
                value = ScalarAggregateValue.create(continuationToken);
            }
            return value;
        }

        abstract void addValue(Object aggregateValue);

        abstract Object getResult();

        abstract Resource getDocumentContinuationToken();
    }

    static class AggregateAggregateValue extends AggregateValue {

        private final Aggregator aggregator;

        AggregateAggregateValue(Aggregator aggregator) {
            this.aggregator = aggregator;
        }

        public static AggregateAggregateValue createAggregateValue(AggregateOperator aggregateOperator,
                                                                   String continuationToken) {
            Aggregator aggregator = null;
            switch (aggregateOperator) {
                case Average:
                    aggregator = new AverageAggregator();
                    break;
                case Count:
                    aggregator = new CountAggregator();
                    break;
                case Max:
                    aggregator = new MaxAggregator();
                    break;
                case Min:
                    aggregator = new MinAggregator();
                    break;
                case Sum:
                    aggregator = new SumAggregator();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown aggregator type: " + aggregateOperator);
            }

            return new AggregateAggregateValue(aggregator);
        }

        @Override
        void addValue(Object aggregateValue) {
            AggregateItem aggregateItem = new AggregateItem(aggregateValue);
            this.aggregator.aggregate(aggregateItem.getItem());
        }

        @Override
        Object getResult() {
            return this.aggregator.getResult();
        }

        @Override
        Resource getDocumentContinuationToken() {
            return null;
        }
    }

    static class ScalarAggregateValue extends AggregateValue {

        private Object value;
        private boolean initialized;

        ScalarAggregateValue(final Object value, final boolean initialized) {
            this.value = value;
            this.initialized = initialized;
        }

        public static ScalarAggregateValue create(String continuationToken) {
            /*
            Intialize these values using continuationToken when its support added.
            Document value = null;
            boolean initialized = false;
            */
            return new ScalarAggregateValue(null, false);
        }

        @Override
        void addValue(Object aggregateValue) {
            if (!this.initialized) {
                this.value = aggregateValue;
                this.initialized = true;
            }
        }

        @Override
        Object getResult() {
            if (!this.initialized) {
                throw new IllegalStateException("ScalarAggregateValue is not yet initialized.");
            }

            return this.value;
        }

        @Override
        Resource getDocumentContinuationToken() {
            return null;
        }
    }

}
