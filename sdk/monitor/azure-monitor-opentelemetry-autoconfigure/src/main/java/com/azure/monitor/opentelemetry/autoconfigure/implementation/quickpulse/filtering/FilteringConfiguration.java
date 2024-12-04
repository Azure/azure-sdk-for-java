// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationError;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationErrorType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentStreamInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentFilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.TelemetryType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.KeyValuePairString;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.PredicateType;

import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.util.Arrays.asList;

public class FilteringConfiguration {
    private final Set<String> seenMetricIds;

    // key is the telemetry type
    private ConcurrentMap<String, List<DerivedMetricInfo>> validDerivedMetricInfos;

    // first key is the telemetry type, second key is the id associated with the document filters
    private ConcurrentMap<String, ConcurrentMap<String, List<FilterConjunctionGroupInfo>>> validDocumentFilterConjunctionGroupInfos;

    private final ErrorTracker errorTracker;

    private volatile String etag;

    private static final ClientLogger logger = new ClientLogger(FilteringConfiguration.class);

    public FilteringConfiguration(ErrorTracker errorTracker) {
        validDerivedMetricInfos = new ConcurrentHashMap<>();
        validDocumentFilterConjunctionGroupInfos = new ConcurrentHashMap<>();
        seenMetricIds = new HashSet<>();
        etag = "";
        this.errorTracker = errorTracker;
    }

    public synchronized void updateConfiguration(CollectionConfigurationInfo configuration) {
        try {
            logger.verbose("passed in config: {}", configuration.toJsonString());
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        etag = configuration.getETag();
        logger.verbose("etag in update config after change: {}", etag);
        errorTracker.clearValidationTimeErrors();
        seenMetricIds.clear();
        parseDocumentFilterConfiguration(configuration);
        parseMetricFilterConfiguration(configuration);

        try {
            for (Map.Entry<String, ConcurrentMap<String, List<FilterConjunctionGroupInfo>>> entry : validDocumentFilterConjunctionGroupInfos
                .entrySet()) {
                logger.verbose(entry.getKey());
                ConcurrentMap<String, List<FilterConjunctionGroupInfo>> value = entry.getValue();
                for (Map.Entry<String, List<FilterConjunctionGroupInfo>> e2 : value.entrySet()) {
                    logger.verbose("  {}", e2.getKey());
                    for (FilterConjunctionGroupInfo filterGroup : e2.getValue()) {
                        logger.verbose("    {}", filterGroup.toJsonString());
                    }
                }
            }

            for (Map.Entry<String, List<DerivedMetricInfo>> entry : validDerivedMetricInfos.entrySet()) {
                logger.verbose(entry.getKey());
                for (DerivedMetricInfo dmi : entry.getValue()) {
                    logger.verbose("  {}", dmi.toJsonString());
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

    }

    public synchronized List<DerivedMetricInfo> fetchMetricConfigForTelemetryType(String telemetryType) {
        List<DerivedMetricInfo> result;
        if (validDerivedMetricInfos.containsKey(telemetryType)) {
            result = new ArrayList<>(validDerivedMetricInfos.get(telemetryType));
        } else {
            result = new ArrayList<>();
        }
        return result;
    }

    public synchronized ConcurrentMap<String, List<FilterConjunctionGroupInfo>>
        fetchDocumentsConfigForTelemetryType(String telemetryType) {
        ConcurrentMap<String, List<FilterConjunctionGroupInfo>> result;
        if (validDocumentFilterConjunctionGroupInfos.containsKey(telemetryType)) {
            result = new ConcurrentHashMap<>(validDocumentFilterConjunctionGroupInfos.get(telemetryType));
        } else {
            result = new ConcurrentHashMap<>();
        }
        return result;
    }

    public synchronized String getETag() {
        return etag;
    }

    private void parseDocumentFilterConfiguration(CollectionConfigurationInfo configuration) {
        ConcurrentMap<String, ConcurrentMap<String, List<FilterConjunctionGroupInfo>>> newValidDocumentsConfig
            = new ConcurrentHashMap<>();
        for (DocumentStreamInfo documentStreamInfo : configuration.getDocumentStreams()) {
            String documentStreamId = documentStreamInfo.getId();
            for (DocumentFilterConjunctionGroupInfo documentFilterGroupInfo : documentStreamInfo
                .getDocumentFilterGroups()) {
                String telemetryType = documentFilterGroupInfo.getTelemetryType().getValue();
                FilterConjunctionGroupInfo filterGroup = documentFilterGroupInfo.getFilters();
                try {
                    Validator.validateTelemetryType(documentFilterGroupInfo.getTelemetryType());
                    Validator.validateDocumentFilters(documentFilterGroupInfo);

                    if (!newValidDocumentsConfig.containsKey(telemetryType)) {
                        newValidDocumentsConfig.put(telemetryType, new ConcurrentHashMap<>());
                    }

                    ConcurrentMap<String, List<FilterConjunctionGroupInfo>> innerMap
                        = newValidDocumentsConfig.get(telemetryType);
                    if (innerMap.containsKey(documentStreamId)) {
                        innerMap.get(documentStreamId).add(filterGroup);
                    } else {
                        List<FilterConjunctionGroupInfo> filterGroups = new ArrayList<>();
                        filterGroups.add(filterGroup);
                        innerMap.put(documentStreamId, filterGroups);
                    }
                } catch (UnexpectedFilterCreateException | TelemetryTypeException e) {
                    CollectionConfigurationError error = new CollectionConfigurationError();
                    if (e instanceof TelemetryTypeException) {
                        error.setCollectionConfigurationErrorType(
                            CollectionConfigurationErrorType.fromString("DocumentTelemetryTypeUnsupported"));
                    } else {
                        error.setCollectionConfigurationErrorType(
                            CollectionConfigurationErrorType.FILTER_FAILURE_TO_CREATE_UNEXPECTED);
                    }

                    error.setMessage(e.getMessage());

                    KeyValuePairString documentStreamInfoId = new KeyValuePairString();
                    documentStreamInfoId.setKey("DocumentStreamInfoId");
                    documentStreamInfoId.setValue(documentStreamId);
                    KeyValuePairString etag = new KeyValuePairString();
                    etag.setKey("Etag");
                    etag.setValue(configuration.getETag());

                    List<KeyValuePairString> data = new ArrayList<>();
                    data.add(documentStreamInfoId);
                    data.add(etag);
                    error.setData(data);

                    errorTracker.addValidationError(error);
                }
            }
        }
        validDocumentFilterConjunctionGroupInfos = newValidDocumentsConfig;
    }

    private void parseMetricFilterConfiguration(CollectionConfigurationInfo configuration) {
        ConcurrentMap<String, List<DerivedMetricInfo>> newValidConfig = new ConcurrentHashMap<>();
        for (DerivedMetricInfo derivedMetricInfo : configuration.getMetrics()) {
            String telemetryType = derivedMetricInfo.getTelemetryType();
            String id = derivedMetricInfo.getId();
            try {
                if (!seenMetricIds.contains(id)) {
                    seenMetricIds.add(id);
                    Validator.validateTelemetryType(TelemetryType.fromString(telemetryType));
                    Validator.checkCustomMetricProjection(derivedMetricInfo);
                    Validator.validateMetricFilters(derivedMetricInfo);

                    if (newValidConfig.containsKey(telemetryType)) {
                        newValidConfig.get(telemetryType).add(derivedMetricInfo);
                    } else {
                        List<DerivedMetricInfo> infos = new ArrayList<>();
                        infos.add(derivedMetricInfo);
                        newValidConfig.put(telemetryType, infos);
                    }
                } else {
                    throw new DuplicateMetricIdException("Duplicate Metric Id: " + id);
                }
            } catch (UnexpectedFilterCreateException | TelemetryTypeException | DuplicateMetricIdException e) {
                CollectionConfigurationError error = new CollectionConfigurationError();
                if (e instanceof TelemetryTypeException) {
                    error.setCollectionConfigurationErrorType(
                        CollectionConfigurationErrorType.METRIC_TELEMETRY_TYPE_UNSUPPORTED);
                } else if (e instanceof UnexpectedFilterCreateException) {
                    error.setCollectionConfigurationErrorType(
                        CollectionConfigurationErrorType.FILTER_FAILURE_TO_CREATE_UNEXPECTED);
                } else {
                    error.setCollectionConfigurationErrorType(CollectionConfigurationErrorType.METRIC_DUPLICATE_IDS);
                }

                error.setMessage(e.getMessage());

                KeyValuePairString metricId = new KeyValuePairString();
                metricId.setKey("MetricId");
                metricId.setValue(id);
                KeyValuePairString etag = new KeyValuePairString();
                etag.setKey("Etag");
                etag.setValue(configuration.getETag());

                List<KeyValuePairString> data = new ArrayList<>();
                data.add(metricId);
                data.add(etag);
                error.setData(data);

                errorTracker.addValidationError(error);
            }
        }
        validDerivedMetricInfos = newValidConfig;
    }

    public static class Validator {
        private static final Set<String> knownStringColumns
            = new HashSet<String>(asList(KnownRequestColumns.URL, KnownRequestColumns.NAME, KnownDependencyColumns.DATA,
                KnownDependencyColumns.TARGET, KnownDependencyColumns.TYPE, KnownTraceColumns.MESSAGE,
                KnownExceptionColumns.MESSAGE, KnownExceptionColumns.STACK));

        private static final Set<String> knownNumericColumns = new HashSet<>(asList(KnownRequestColumns.RESPONSE_CODE,
            KnownRequestColumns.DURATION, KnownDependencyColumns.RESULT_CODE));

        private static final Set<PredicateType> validStringPredicates = new HashSet<>(asList(PredicateType.CONTAINS,
            PredicateType.DOES_NOT_CONTAIN, PredicateType.EQUAL, PredicateType.NOT_EQUAL));

        public static void validateTelemetryType(TelemetryType telemetryType) throws TelemetryTypeException {
            if (telemetryType.equals(TelemetryType.PERFORMANCE_COUNTER)) {
                throw new TelemetryTypeException(
                    "The telemetry type PerformanceCounter was specified, but the distro does not send performance counters other than the default CPU/Mem to quickpulse");
            } else if (telemetryType.equals(TelemetryType.EVENT)) {
                throw new TelemetryTypeException(
                    "The telemetry type Event was specified, but the distro does not send events to quickpulse");
            } else if (telemetryType.equals(TelemetryType.METRIC)) {
                throw new TelemetryTypeException(
                    "The telemetry type Metric was specified, but the distro does not support sending open telemetry metrics to quickpulse");
            } else if (!TelemetryType.values().contains(telemetryType)) {
                throw new TelemetryTypeException(telemetryType + " is not a valid telemetry type");
            }
        }

        public static void checkCustomMetricProjection(DerivedMetricInfo derivedMetricInfo)
            throws UnexpectedFilterCreateException {
            if (derivedMetricInfo.getProjection().startsWith("CustomMetrics.")) {
                throw new UnexpectedFilterCreateException(
                    "The projection of a customMetric property is not supported in this distro.");
            }
        }

        public static void validateMetricFilters(DerivedMetricInfo derivedMetricInfo)
            throws UnexpectedFilterCreateException {
            for (FilterConjunctionGroupInfo conjunctionGroupInfo : derivedMetricInfo.getFilterGroups()) {
                for (FilterInfo filter : conjunctionGroupInfo.getFilters()) {
                    TelemetryType telemetryType = TelemetryType.fromString(derivedMetricInfo.getTelemetryType());
                    validateFieldNames(filter.getFieldName(), telemetryType);
                    validatePredicateAndComparand(filter);
                }
            }
        }

        public static void
            validateDocumentFilters(DocumentFilterConjunctionGroupInfo documentFilterConjunctionGroupInfo)
                throws UnexpectedFilterCreateException {
            FilterConjunctionGroupInfo conjunctionGroupInfo = documentFilterConjunctionGroupInfo.getFilters();
            for (FilterInfo filter : conjunctionGroupInfo.getFilters()) {
                validateFieldNames(filter.getFieldName(), documentFilterConjunctionGroupInfo.getTelemetryType());
                validatePredicateAndComparand(filter);
            }
        }

        private static boolean isCustomDimOrAnyField(String fieldName) {
            return fieldName.startsWith(Filter.CUSTOM_DIM_FIELDNAME_PREFIX) || fieldName.equals(Filter.ANY_FIELD);
        }

        private static void validateFieldNames(String fieldName, TelemetryType telemetryType)
            throws UnexpectedFilterCreateException {
            if (fieldName.isEmpty()) {
                throw new UnexpectedFilterCreateException("A filter must have a field name.");
            }
            if (fieldName.startsWith("CustomMetrics.")) {
                throw new UnexpectedFilterCreateException(
                    "Filtering of a customMetric property is not supported in this distro.");
            }

            // Only writing the following if-else blocks in case the service side experiences some weird bug that sends us a weird
            // fieldName value. From a UI perspective, it currently isn't possible to select an invalid fieldName from the dropdown menu.
            // We don't want the Filter/*Columns classes to need to deal with fieldNames that don't exist - validation should weed out buggy
            // filters.
            if (telemetryType.equals(TelemetryType.REQUEST)) {
                if (!isCustomDimOrAnyField(fieldName) && !KnownRequestColumns.ALL_COLUMNS.contains(fieldName)) {
                    throw new UnexpectedFilterCreateException(
                        fieldName + " is not a valid field name for the telemetry type Request");
                }
            } else if (telemetryType.equals(TelemetryType.DEPENDENCY)) {
                if (!isCustomDimOrAnyField(fieldName) && !KnownDependencyColumns.ALL_COLUMNS.contains(fieldName)) {
                    throw new UnexpectedFilterCreateException(
                        fieldName + " is not a valid field name for the telemetry type Dependency");
                }
            } else if (telemetryType.equals(TelemetryType.EXCEPTION)) {
                if (!isCustomDimOrAnyField(fieldName)
                    && !fieldName.equals(KnownExceptionColumns.MESSAGE)
                    && !fieldName.equals(KnownExceptionColumns.STACK)) {
                    throw new UnexpectedFilterCreateException(
                        fieldName + " is not a valid field name for the telemetry type Exception");
                }
            } else if (telemetryType.equals(TelemetryType.TRACE)) {
                if (!isCustomDimOrAnyField(fieldName) && !fieldName.equals(KnownTraceColumns.MESSAGE)) {
                    throw new UnexpectedFilterCreateException(
                        fieldName + " is not a valid field name for the telemetry type Exception");
                }
            } else {
                throw new UnexpectedFilterCreateException(telemetryType.getValue() + " is not a valid telemetry type");
            }
        }

        private static void validatePredicateAndComparand(FilterInfo filter) throws UnexpectedFilterCreateException {
            if (filter.getComparand().isEmpty()) {
                // It is possible to not type in a comparand and the service side to send us empty string.
                throw new UnexpectedFilterCreateException("A filter must have a non-empty comparand. FilterName: "
                    + filter.getFieldName() + " Predicate: " + filter.getPredicate().getValue());
            } else if (Filter.ANY_FIELD.equals(filter.getFieldName())
                && !(filter.getPredicate().equals(PredicateType.CONTAINS)
                    || filter.getPredicate().equals(PredicateType.DOES_NOT_CONTAIN))) {
                // While the UI allows != and == for the ANY_FIELD fieldName, .net classic code only allows contains/not contains & the spec follows
                // .net classic behavior for this particular condition.
                throw new UnexpectedFilterCreateException(
                    "The predicate " + filter.getPredicate().getValue() + " is not supported for the field name *");
            } else if (knownNumericColumns.contains(filter.getFieldName())) {
                // Technically the following case isn't possible to hit with current UI; only checking in case service side has a weird bug and sends some weird value.
                if (filter.getPredicate().equals(PredicateType.CONTAINS)
                    || filter.getPredicate().equals(PredicateType.DOES_NOT_CONTAIN)) {
                    throw new UnexpectedFilterCreateException("The predicate " + filter.getPredicate().getValue()
                        + "is not supported for the field name " + filter.getFieldName());
                }

                // Just in case a strange timestamp value is passed from the service side. Not completely sure how robust the service side validation is for user entered durations.
                if (KnownDependencyColumns.DURATION.equals(filter.getFieldName())) {
                    if (Filter.getMicroSecondsFromFilterTimestampString(filter.getComparand()) == Long.MIN_VALUE) {
                        throw new UnexpectedFilterCreateException(
                            "The provided duration timestamp can't be converted to microseconds: " + filter.getComparand());
                    }
                } else { // The service side not does not validate entered codes for result code or response code.
                    try {
                        Long.parseLong(filter.getComparand());
                    } catch (NumberFormatException e) {
                        throw new UnexpectedFilterCreateException(
                            "Could not convert the provided result/response code to a numeric value: "
                                + filter.getComparand());
                    }
                }
            } else if (knownStringColumns.contains(filter.getFieldName())
                || filter.getFieldName().startsWith(Filter.CUSTOM_DIM_FIELDNAME_PREFIX)) {
                // While the UI allows a user to select any predicate for a custom dimension filter, .net classic treats all custom dimensions like
                // String values. therefore we validate for predicates applicable to String. This is called out in the spec as well.
                if (!validStringPredicates.contains(filter.getPredicate())) {
                    throw new UnexpectedFilterCreateException("The predicate " + filter.getPredicate().getValue()
                        + " is not supported for the field " + filter.getFieldName());
                }
            } else if (filter.getFieldName().equals(KnownRequestColumns.SUCCESS)) {
                // This case shouldn't happen in practice as the UI restricts which predicates can be selected in the dropdown for success. However,
                // checking this anyway in the service side has a future bug.
                if (!filter.getPredicate().equals(PredicateType.NOT_EQUAL)
                    && !filter.getPredicate().equals(PredicateType.EQUAL)) {
                    throw new UnexpectedFilterCreateException("The predicate " + filter.getPredicate().getValue()
                        + " is not supported for the Success field");
                }

                // This case also shouldn't happen in practice as the UI has dropdown for success values. However, checking this anyway in case the service side has a
                // future bug.
                String lowerCaseComparand = filter.getComparand().toLowerCase();
                if (!"true".equals(lowerCaseComparand) && !"false".equals(lowerCaseComparand)) {
                    throw new UnexpectedFilterCreateException("The provided value for the Success comparand "
                        + filter.getComparand() + "is not a valid boolean value");
                }
            }
        }
    }

}
