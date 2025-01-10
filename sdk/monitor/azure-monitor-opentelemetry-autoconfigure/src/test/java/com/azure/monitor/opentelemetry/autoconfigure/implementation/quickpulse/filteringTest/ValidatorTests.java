// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filteringTest;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.DerivedMetricProjections;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.Filter;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.KnownRequestColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.Validator;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorTests {

    @ParameterizedTest
    @MethodSource("invalidTelemetryTypes")
    void rejectInvalidTelemetryTypesForDmi(TelemetryType telemetryType) {
        Validator validator = new Validator();
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndNoFilters();
        DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", telemetryType.getValue(), AggregationType.SUM,
            AggregationType.SUM, DerivedMetricProjections.COUNT, filterGroups);
        assertTrue(validator.validateDerivedMetricInfo(dmi).isPresent());
    }

    @ParameterizedTest
    @MethodSource("invalidTelemetryTypes")
    void rejectInvalidTelemetryTypesForDocs(TelemetryType telemetryType) {
        Validator validator = new Validator();
        DocumentFilterConjunctionGroupInfo docGroup = createDocGroupWithNoFilters(telemetryType);
        assertTrue(validator.validateDocConjunctionGroupInfo(docGroup).isPresent());
    }

    @ParameterizedTest
    @MethodSource("validTelemetryTypes")
    void acceptValidTelemetryTypeForDmi(TelemetryType telemetryType) {
        Validator validator = new Validator();
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndNoFilters();
        DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", telemetryType.getValue(), AggregationType.SUM,
            AggregationType.SUM, DerivedMetricProjections.COUNT, filterGroups);
        assertFalse(validator.validateDerivedMetricInfo(dmi).isPresent());
    }

    @ParameterizedTest
    @MethodSource("validTelemetryTypes")
    void acceptValidTelemetryTypeForDocs(TelemetryType telemetryType) {
        Validator validator = new Validator();
        DocumentFilterConjunctionGroupInfo docGroup = createDocGroupWithNoFilters(telemetryType);
        assertFalse(validator.validateDocConjunctionGroupInfo(docGroup).isPresent());
    }

    @Test
    void rejectCustomMetricProjection() {
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndNoFilters();
        Validator validator = new Validator();
        DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", TelemetryType.TRACE.getValue(),
            AggregationType.SUM, AggregationType.SUM, "CustomMetrics.property", filterGroups);
        assertTrue(validator.validateDerivedMetricInfo(dmi).isPresent());
    }

    @Test
    void rejectCustomMetricFieldName() {
        FilterInfo filter = createFilterInfoWithParams("CustomMetrics.property", PredicateType.EQUAL, "5");
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndOneFilter(filter);
        Validator validator = new Validator();
        DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", TelemetryType.TRACE.getValue(),
            AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT, filterGroups);
        assertTrue(validator.validateDerivedMetricInfo(dmi).isPresent());
    }

    @ParameterizedTest
    @MethodSource("invalidFilters")
    void rejectInvalidFiltersForDmi(FilterInfo filter) {
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndOneFilter(filter);
        DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", TelemetryType.REQUEST.getValue(),
            AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT, filterGroups);
        Validator validator = new Validator();
        assertTrue(validator.validateDerivedMetricInfo(dmi).isPresent());
    }

    @ParameterizedTest
    @MethodSource("invalidFilters")
    void rejectInvalidFiltersForDocs(FilterInfo filter) {
        Validator validator = new Validator();
        DocumentFilterConjunctionGroupInfo docGroup = createDocGroupWithOneFilter(TelemetryType.REQUEST, filter);
        assertTrue(validator.validateDocConjunctionGroupInfo(docGroup).isPresent());
    }

    @Test
    void rejectInvalidGroupWithMultipleFilters() {
        List<FilterConjunctionGroupInfo> filterGroups = new ArrayList<>();
        FilterConjunctionGroupInfo filterGroup = new FilterConjunctionGroupInfo();
        List<FilterInfo> filters = Arrays.asList(createFilterInfoWithParams("", PredicateType.EQUAL, "blah"), // invalid
            createFilterInfoWithParams(KnownRequestColumns.SUCCESS, PredicateType.EQUAL, "true") // valid
        );
        filterGroup.setFilters(filters);
        filterGroups.add(filterGroup);

        Validator validator = new Validator();

        DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", TelemetryType.REQUEST.getValue(),
            AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT, filterGroups);
        assertTrue(validator.validateDerivedMetricInfo(dmi).isPresent());

        DocumentFilterConjunctionGroupInfo docGroup = new DocumentFilterConjunctionGroupInfo();
        docGroup.setFilters(filterGroup);
        docGroup.setTelemetryType(TelemetryType.REQUEST);
        assertTrue(validator.validateDocConjunctionGroupInfo(docGroup).isPresent());
    }

    @ParameterizedTest
    @MethodSource("validFilters")
    void acceptValidFiltersForDmi(FilterInfo filter) {
        List<FilterConjunctionGroupInfo> filterGroups = createListWithOneFilterConjunctionGroupAndOneFilter(filter);
        DerivedMetricInfo dmi = createDerivedMetricInfo("random-id", TelemetryType.REQUEST.getValue(),
            AggregationType.SUM, AggregationType.SUM, DerivedMetricProjections.COUNT, filterGroups);
        Validator validator = new Validator();
        assertFalse(validator.validateDerivedMetricInfo(dmi).isPresent());
    }

    @ParameterizedTest
    @MethodSource("validFilters")
    void acceptValidFiltersForDocs(FilterInfo filter) {
        Validator validator = new Validator();
        DocumentFilterConjunctionGroupInfo docGroup = createDocGroupWithOneFilter(TelemetryType.REQUEST, filter);
        assertFalse(validator.validateDocConjunctionGroupInfo(docGroup).isPresent());
    }

    private static List<TelemetryType> invalidTelemetryTypes() {
        return Arrays.asList(TelemetryType.METRIC, TelemetryType.EVENT, TelemetryType.PERFORMANCE_COUNTER);
    }

    private static List<TelemetryType> validTelemetryTypes() {
        return Arrays.asList(TelemetryType.DEPENDENCY, TelemetryType.EXCEPTION, TelemetryType.REQUEST,
            TelemetryType.TRACE);
    }

    private static List<FilterInfo> invalidFilters() {
        return Arrays.asList(createFilterInfoWithParams("", PredicateType.EQUAL, "blah"),
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
            createFilterInfoWithParams(KnownRequestColumns.DURATION, PredicateType.LESS_THAN, "0.0:0:"));
    }

    private static List<FilterInfo> validFilters() {
        return Arrays.asList(createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.CONTAINS, "hi"),
            createFilterInfoWithParams(Filter.ANY_FIELD, PredicateType.DOES_NOT_CONTAIN, "hi"),
            createFilterInfoWithParams(KnownRequestColumns.URL, PredicateType.NOT_EQUAL, "hi"),
            createFilterInfoWithParams(KnownRequestColumns.URL, PredicateType.EQUAL, "hi"),
            createFilterInfoWithParams(KnownRequestColumns.URL, PredicateType.CONTAINS, "hi"),
            createFilterInfoWithParams(KnownRequestColumns.URL, PredicateType.DOES_NOT_CONTAIN, "hi"),
            createFilterInfoWithParams("CustomDimensions.property", PredicateType.NOT_EQUAL, "hi"),
            createFilterInfoWithParams("CustomDimensions.property", PredicateType.EQUAL, "hi"),
            createFilterInfoWithParams("CustomDimensions.property", PredicateType.CONTAINS, "hi"),
            createFilterInfoWithParams("CustomDimensions.property", PredicateType.DOES_NOT_CONTAIN, "hi"),
            createFilterInfoWithParams(KnownRequestColumns.RESPONSE_CODE, PredicateType.LESS_THAN, "5"),
            createFilterInfoWithParams(KnownRequestColumns.RESPONSE_CODE, PredicateType.LESS_THAN_OR_EQUAL, "5"),
            createFilterInfoWithParams(KnownRequestColumns.RESPONSE_CODE, PredicateType.GREATER_THAN, "5"),
            createFilterInfoWithParams(KnownRequestColumns.RESPONSE_CODE, PredicateType.GREATER_THAN_OR_EQUAL, "5"),
            createFilterInfoWithParams(KnownRequestColumns.RESPONSE_CODE, PredicateType.EQUAL, "5"),
            createFilterInfoWithParams(KnownRequestColumns.RESPONSE_CODE, PredicateType.NOT_EQUAL, "5"),
            createFilterInfoWithParams(KnownRequestColumns.DURATION, PredicateType.EQUAL, "0.0:0:0.2"),
            createFilterInfoWithParams(KnownRequestColumns.SUCCESS, PredicateType.EQUAL, "true"),
            createFilterInfoWithParams(KnownRequestColumns.SUCCESS, PredicateType.NOT_EQUAL, "false"));
    }

    private static FilterInfo createFilterInfoWithParams(String fieldName, PredicateType predicate, String comparand) {
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

    private DocumentFilterConjunctionGroupInfo createDocGroupWithOneFilter(TelemetryType telemetryType,
        FilterInfo filter) {
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
