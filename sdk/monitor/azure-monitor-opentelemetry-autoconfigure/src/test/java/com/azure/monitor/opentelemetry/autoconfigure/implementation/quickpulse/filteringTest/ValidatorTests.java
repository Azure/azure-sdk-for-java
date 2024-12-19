package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filteringTest;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.DerivedMetricProjections;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.Filter;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.KnownRequestColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.Validator;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidatorTests {

    @Test
    void rejectInvalidTelemetryTypes() {
        List<TelemetryType> telemetryTypes = asList(TelemetryType.EVENT, TelemetryType.PERFORMANCE_COUNTER, TelemetryType.METRIC);
        Validator validator = new Validator();
        for (TelemetryType telemetryType : telemetryTypes) {
            List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndNoFilters();
            DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", telemetryType.getValue(), AggregationType.SUM,
                AggregationType.SUM, DerivedMetricProjections.COUNT, filterGroups);
            assertFalse(validator.isValidDerivedMetricInfo(dmi));
            DocumentFilterConjunctionGroupInfo docGroup = createDocGroupWithNoFilters(telemetryType);
            assertFalse(validator.isValidDocConjunctionGroupInfo(docGroup));
        }
    }

    @Test
    void acceptValidTelemetryType() {
        List<TelemetryType> telemetryTypes = asList(TelemetryType.TRACE, TelemetryType.REQUEST, TelemetryType.DEPENDENCY, TelemetryType.EXCEPTION);
        Validator validator = new Validator();
        for (TelemetryType telemetryType : telemetryTypes) {
            List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndNoFilters();
            DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", telemetryType.getValue(), AggregationType.SUM,
                AggregationType.SUM, DerivedMetricProjections.COUNT, filterGroups);
            assertTrue(validator.isValidDerivedMetricInfo(dmi));
            DocumentFilterConjunctionGroupInfo docGroup = createDocGroupWithNoFilters(telemetryType);
            assertTrue(validator.isValidDocConjunctionGroupInfo(docGroup));
        }
    }

    @Test
    void rejectCustomMetricProjection() {
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndNoFilters();
        Validator validator = new Validator();
        DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", TelemetryType.TRACE.getValue(), AggregationType.SUM,
            AggregationType.SUM, "CustomMetrics.property", filterGroups);
        assertFalse(validator.isValidDerivedMetricInfo(dmi));

    }

    @Test
    void rejectCustomMetricFieldName() {
        FilterInfo filter = createFilterInfoWithParams("CustomMetrics.property", PredicateType.EQUAL, "5");
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndOneFilter(filter);
        Validator validator = new Validator();
        DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", TelemetryType.TRACE.getValue(), AggregationType.SUM,
            AggregationType.SUM, DerivedMetricProjections.COUNT, filterGroups);
        assertFalse(validator.isValidDerivedMetricInfo(dmi));
    }

    @Test
    void rejectInvalidFilters() {
        List<FilterInfo> filtersToTest = Arrays.asList(
            createFilterInfoWithParams("", PredicateType.EQUAL, "blah"),
            createFilterInfoWithParams(KnownRequestColumns.URL, PredicateType.EQUAL, ""),
            createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.EQUAL, "5"),
            createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.NOT_EQUAL, "5"),
            createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.LESS_THAN, "5"),
            createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.LESS_THAN_OR_EQUAL, "5"),
            createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.GREATER_THAN, "5"),
            createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.GREATER_THAN_OR_EQUAL, "5"),
            createFilterInfoWithParams("CustomDimensions.property", PredicateType.LESS_THAN, "blah"),
            createFilterInfoWithParams("CustomDimensions.property", PredicateType.LESS_THAN_OR_EQUAL, "blah"),
            createFilterInfoWithParams("CustomDimensions.property", PredicateType.GREATER_THAN, "blah"),
            createFilterInfoWithParams("CustomDimensions.property", PredicateType.GREATER_THAN_OR_EQUAL, "blah"),
            createFilterInfoWithParams(KnownRequestColumns.DURATION, PredicateType.LESS_THAN, "invalid timestamp"),
            createFilterInfoWithParams(KnownRequestColumns.DURATION, PredicateType.LESS_THAN, "150"),
            createFilterInfoWithParams(KnownRequestColumns.DURATION, PredicateType.LESS_THAN, "0.0:0:0.")
        );

        for (FilterInfo filter: filtersToTest) {
            List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndOneFilter(filter);
            DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", TelemetryType.REQUEST.getValue(), AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT, filterGroups);
            Validator validator = new Validator();
            assertFalse(validator.isValidDerivedMetricInfo(dmi));

            DocumentFilterConjunctionGroupInfo docGroup = createDocGroupWithOneFilter(TelemetryType.REQUEST, filter);
            assertFalse(validator.isValidDocConjunctionGroupInfo(docGroup));
        }
    }

    @Test
    void rejectInvalidGroupWithMultipleFilters() {
        List<FilterConjunctionGroupInfo> filterGroups = new ArrayList<>();
        FilterConjunctionGroupInfo filterGroup = new FilterConjunctionGroupInfo();
        List<FilterInfo> filters = Arrays.asList(
            createFilterInfoWithParams("", PredicateType.EQUAL, "blah"), // invalid
            createFilterInfoWithParams(KnownRequestColumns.SUCCESS, PredicateType.EQUAL, "true") // valid
        );
        filterGroup.setFilters(filters);
        filterGroups.add(filterGroup);

        Validator validator = new Validator();

        DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", TelemetryType.REQUEST.getValue(), AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT, filterGroups);
        assertFalse(validator.isValidDerivedMetricInfo(dmi));

        DocumentFilterConjunctionGroupInfo docGroup = new DocumentFilterConjunctionGroupInfo();
        docGroup.setFilters(filterGroup);
        docGroup.setTelemetryType(TelemetryType.REQUEST);
        assertFalse(validator.isValidDocConjunctionGroupInfo(docGroup));
    }
    private FilterInfo createFilterInfoWithParams(String fieldName, PredicateType predicate, String comparand) {
        FilterInfo result = new FilterInfo();
        result.setFieldName(fieldName);
        result.setPredicate(predicate);
        result.setComparand(comparand);
        return result;
    }


    private DerivedMetricInfo createDerivedMetricInfo(String id, String telemetryType, AggregationType agg,
                                                      AggregationType backendAgg, String projection, List<FilterConjunctionGroupInfo> filterGroups) {
        DerivedMetricInfo result = new DerivedMetricInfo();
        result.setId(id);
        result.setTelemetryType(telemetryType);
        result.setAggregation(agg);
        result.setBackEndAggregation(backendAgg);
        result.setProjection(projection);
        result.setFilterGroups(filterGroups);
        return result;
    }

    private List<FilterConjunctionGroupInfo> createListWithOneFilterConjunctionGroupAndOneFilter(FilterInfo filter) {
        List<FilterConjunctionGroupInfo> result = new ArrayList<>();
        FilterConjunctionGroupInfo group = new FilterConjunctionGroupInfo();
        List<FilterInfo> filters = new ArrayList<>();
        filters.add(filter);
        group.setFilters(filters);
        result.add(group);
        return result;
    }

    private List<FilterConjunctionGroupInfo> createListWithOneFilterConjunctionGroupAndNoFilters() {
        List<FilterConjunctionGroupInfo> result = new ArrayList<>();
        FilterConjunctionGroupInfo group = new FilterConjunctionGroupInfo();
        List<FilterInfo> filters = new ArrayList<>();
        group.setFilters(filters);
        result.add(group);
        return result;
    }

    private DocumentFilterConjunctionGroupInfo createDocGroupWithNoFilters(TelemetryType telemetryType) {
        DocumentFilterConjunctionGroupInfo docGroup = new DocumentFilterConjunctionGroupInfo();
        FilterConjunctionGroupInfo group = new FilterConjunctionGroupInfo();
        List<FilterInfo> filters = new ArrayList<>();
        group.setFilters(filters);
        docGroup.setFilters(group);
        docGroup.setTelemetryType(telemetryType);
        return docGroup;
    }

    private DocumentFilterConjunctionGroupInfo createDocGroupWithOneFilter(TelemetryType telemetryType, FilterInfo filter) {
        DocumentFilterConjunctionGroupInfo docGroup = new DocumentFilterConjunctionGroupInfo();
        FilterConjunctionGroupInfo group = new FilterConjunctionGroupInfo();
        List<FilterInfo> filters = new ArrayList<>();
        filters.add(filter);
        group.setFilters(filters);
        docGroup.setFilters(group);
        docGroup.setTelemetryType(telemetryType);
        return docGroup;
    }

}
