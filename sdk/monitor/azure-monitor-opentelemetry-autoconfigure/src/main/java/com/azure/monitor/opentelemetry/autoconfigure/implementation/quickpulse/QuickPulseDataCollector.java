// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionDetails;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MessageData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.DependencyDataColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.FilteringConfiguration;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.TelemetryColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.ExceptionDataColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.RequestDataColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.Filter;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.TraceDataColumns;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering.DerivedMetricProjections;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.RemoteDependency;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.Request;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.KeyValuePairString;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentIngress;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.Exception;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.FilterConjunctionGroupInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DerivedMetricInfo;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.Trace;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.TelemetryType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.AggregationType;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.CollectionConfigurationError;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.CpuPerformanceCounterCalculator;
import reactor.util.annotation.Nullable;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

final class QuickPulseDataCollector {

    private static final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

    private static final OperatingSystemMXBean operatingSystemMxBean = ManagementFactory.getOperatingSystemMXBean();

    private final AtomicReference<Counters> counters = new AtomicReference<>(null);
    private final CpuPerformanceCounterCalculator cpuPerformanceCounterCalculator
        = getCpuPerformanceCounterCalculator();

    // used to prevent race condition between processing a telemetry item and reporting it to the Quick Pulse service
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile QuickPulseStatus quickPulseStatus = QuickPulseStatus.QP_IS_OFF;

    private volatile Supplier<String> instrumentationKeySupplier;

    private final AtomicReference<FilteringConfiguration> configuration;

    QuickPulseDataCollector(AtomicReference<FilteringConfiguration> configuration) {
        this.configuration = configuration;
    }

    private static CpuPerformanceCounterCalculator getCpuPerformanceCounterCalculator() {
        return new CpuPerformanceCounterCalculator();
    }

    synchronized void disable() {
        counters.set(null);
        quickPulseStatus = QuickPulseStatus.QP_IS_OFF;
    }

    synchronized void enable(Supplier<String> instrumentationKeySupplier) {
        this.instrumentationKeySupplier = instrumentationKeySupplier;
        FilteringConfiguration config = configuration.get();
        counters.set(new Counters(config.getValidProjectionInitInfo(), config.getErrors()));
    }

    synchronized void setQuickPulseStatus(QuickPulseStatus quickPulseStatus) {
        this.quickPulseStatus = quickPulseStatus;
    }

    // Used only in tests
    synchronized QuickPulseStatus getQuickPulseStatus() {
        return this.quickPulseStatus;
    }

    @Nullable
    FinalCounters getAndRestart() {
        lock.writeLock().lock();
        try {
            FilteringConfiguration config = configuration.get();
            Counters currentCounters
                = counters.getAndSet(new Counters(config.getValidProjectionInitInfo(), config.getErrors()));
            if (currentCounters != null) {
                return new FinalCounters(currentCounters);
            }

            return null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // only used by tests
    @Nullable
    FinalCounters peek() {
        lock.readLock().lock();
        try {
            Counters currentCounters = this.counters.get(); // this should be the only differece
            if (currentCounters != null) {
                return new FinalCounters(currentCounters);
            }
            return null;
        } finally {
            lock.readLock().unlock();
        }
    }

    void add(TelemetryItem telemetryItem) {
        if (!isEnabled()) {
            // quick pulse is not enabled or quick pulse data sender is not enabled
            return;
        }

        if (!telemetryItem.getInstrumentationKey().equals(instrumentationKeySupplier.get())) {
            return;
        }

        Float sampleRate = telemetryItem.getSampleRate();
        if (sampleRate != null && sampleRate == 0) {
            // sampleRate should never be zero (how could it be captured if sampling set to zero percent?)
            return;
        }
        int itemCount = sampleRate == null ? 1 : Math.round(100 / sampleRate);
        FilteringConfiguration currentConfig = configuration.get();
        MonitorDomain data = telemetryItem.getData().getBaseData();

        if (!(data instanceof RequestData)
            && !(data instanceof RemoteDependencyData)
            && !(data instanceof TelemetryExceptionData)
            && !(data instanceof MessageData)) {
            // optimization before acquiring lock
            return;
        }

        Counters counters = this.counters.get();
        if (counters == null) {
            // optimization before acquiring lock
            return;
        }

        lock.readLock().lock();
        try {
            counters = this.counters.get();
            if (counters == null) {
                return;
            }
            if (data instanceof RequestData) {
                RequestData requestTelemetry = (RequestData) data;
                addRequest(requestTelemetry, itemCount, getOperationName(telemetryItem), currentConfig, counters);
            } else if (data instanceof RemoteDependencyData) {
                addDependency((RemoteDependencyData) data, itemCount, currentConfig, counters);
            } else if (data instanceof TelemetryExceptionData) {
                addException((TelemetryExceptionData) data, itemCount, currentConfig, counters);
            } else if (data instanceof MessageData) {
                addTrace((MessageData) data, currentConfig, counters);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    boolean isEnabled() {
        return quickPulseStatus == QuickPulseStatus.QP_IS_ON;
    }

    @Nullable
    private static String getOperationName(TelemetryItem telemetryItem) {
        Map<String, String> tags = telemetryItem.getTags();
        return tags == null ? null : tags.get(ContextTagKeys.AI_OPERATION_NAME.toString());
    }

    private boolean matchesDocumentFilters(TelemetryColumns columns, TelemetryType telemetryType,
        FilteringConfiguration currentConfig, List<String> matchingDocumentStreamIds) {
        Map<String, List<FilterConjunctionGroupInfo>> documentsConfig
            = currentConfig.fetchDocumentsConfigForTelemetryType(telemetryType);
        if (documentsConfig.isEmpty()) {
            return true;
        }

        // This will iterate through all the filtering configuration for a particular telemetry type to
        // determine which filters from that config match the incoming telemetry item. When emitting documents,
        // the document requires list of all applicable document stream ids. This is because it is possible for
        // multiple sessions of live metrics to be open, and the list of document stream ids is used by live
        // metrics UI to determine which document shows up in which session. Thus, tracking the matching
        // document stream ids here.
        for (Map.Entry<String, List<FilterConjunctionGroupInfo>> entry : documentsConfig.entrySet()) {
            String documentStreamId = entry.getKey();
            for (FilterConjunctionGroupInfo filterGroup : entry.getValue()) {
                if (Filter.checkFilterConjunctionGroup(filterGroup, columns)
                    && !matchingDocumentStreamIds.contains(documentStreamId)) {
                    matchingDocumentStreamIds.add(documentStreamId);
                }
            }
        }
        return !matchingDocumentStreamIds.isEmpty();
    }

    private void applyMetricFilters(TelemetryColumns columns, TelemetryType telemetryType,
        FilteringConfiguration currentConfig, Counters currentCounters) {
        List<DerivedMetricInfo> metricsConfig = currentConfig.fetchMetricConfigForTelemetryType(telemetryType);
        for (DerivedMetricInfo derivedMetricInfo : metricsConfig) {
            if (Filter.checkMetricFilters(derivedMetricInfo, columns)) {
                currentCounters.derivedMetrics.calculateProjection(derivedMetricInfo, columns);
            }
        }
    }

    private void addDependency(RemoteDependencyData telemetry, int itemCount, FilteringConfiguration currentConfig,
        Counters counters) {

        long durationMillis = parseDurationToMillis(telemetry.getDuration());
        counters.rddsAndDuations.addAndGet(Counters.encodeCountAndDuration(itemCount, durationMillis));
        Boolean success = telemetry.isSuccess();
        if (success != null && !success) { // success should not be null
            counters.unsuccessfulRdds.incrementAndGet();
        }

        DependencyDataColumns columns = new DependencyDataColumns(telemetry);
        applyMetricFilters(columns, TelemetryType.DEPENDENCY, currentConfig, counters);

        List<String> documentStreamIds = new ArrayList<>();
        if (matchesDocumentFilters(columns, TelemetryType.DEPENDENCY, currentConfig, documentStreamIds)) {
            RemoteDependency dependencyDoc = new RemoteDependency();
            dependencyDoc.setName(telemetry.getName());
            dependencyDoc.setCommandName(telemetry.getData());
            dependencyDoc.setDuration(Duration.ofMillis(durationMillis).toString());
            dependencyDoc.setResultCode(telemetry.getResultCode());
            dependencyDoc.setProperties(setCustomDimensions(telemetry.getProperties(), telemetry.getMeasurements()));
            dependencyDoc.setDocumentStreamIds(documentStreamIds);

            synchronized (counters.documentList) {
                if (counters.documentList.size() < Counters.MAX_DOCUMENTS_SIZE) {
                    counters.documentList.add(dependencyDoc);
                }
            }
        }
    }

    private void addException(TelemetryExceptionData exceptionData, int itemCount, FilteringConfiguration currentConfig,
        Counters counters) {

        counters.exceptions.addAndGet(itemCount);

        ExceptionDataColumns columns = new ExceptionDataColumns(exceptionData);

        applyMetricFilters(columns, TelemetryType.EXCEPTION, currentConfig, counters);

        List<String> documentStreamIds = new ArrayList<>();
        if (matchesDocumentFilters(columns, TelemetryType.EXCEPTION, currentConfig, documentStreamIds)) {
            List<TelemetryExceptionDetails> exceptionList = exceptionData.getExceptions();
            // Exception is a class from live metrics swagger that represents a document for an exception
            Exception exceptionDoc = new Exception();
            if (exceptionList != null && !exceptionList.isEmpty()) {
                exceptionDoc.setExceptionMessage(exceptionList.get(0).getMessage());
                exceptionDoc.setExceptionType(exceptionList.get(0).getTypeName());
            }
            exceptionDoc
                .setProperties(setCustomDimensions(exceptionData.getProperties(), exceptionData.getMeasurements()));
            exceptionDoc.setDocumentStreamIds(documentStreamIds);

            synchronized (counters.documentList) {
                if (counters.documentList.size() < Counters.MAX_DOCUMENTS_SIZE) {
                    counters.documentList.add(exceptionDoc);
                }
            }
        }
    }

    private void addRequest(RequestData requestTelemetry, int itemCount, String operationName,
        FilteringConfiguration currentConfig, Counters counters) {

        long durationMillis = parseDurationToMillis(requestTelemetry.getDuration());
        counters.requestsAndDurations.addAndGet(Counters.encodeCountAndDuration(itemCount, durationMillis));
        if (!requestTelemetry.isSuccess()) {
            counters.unsuccessfulRequests.incrementAndGet();
        }

        RequestDataColumns columns = new RequestDataColumns(requestTelemetry);

        applyMetricFilters(columns, TelemetryType.REQUEST, currentConfig, counters);

        List<String> documentStreamIds = new ArrayList<>();
        if (matchesDocumentFilters(columns, TelemetryType.REQUEST, currentConfig, documentStreamIds)) {
            Request requestDoc = new Request();
            requestDoc.setDuration(Duration.ofMillis(durationMillis).toString());
            requestDoc.setResponseCode(requestTelemetry.getResponseCode());
            requestDoc.setName(requestTelemetry.getName());
            requestDoc.setUrl(requestTelemetry.getUrl());
            requestDoc.setProperties(
                setCustomDimensions(requestTelemetry.getProperties(), requestTelemetry.getMeasurements()));
            requestDoc.setDocumentStreamIds(documentStreamIds);
            synchronized (counters.documentList) {
                if (counters.documentList.size() < Counters.MAX_DOCUMENTS_SIZE) {
                    counters.documentList.add(requestDoc);
                }
            }
        }
    }

    private void addTrace(MessageData traceTelemetry, FilteringConfiguration currentConfig, Counters counters) {

        TraceDataColumns columns = new TraceDataColumns(traceTelemetry);
        applyMetricFilters(columns, TelemetryType.TRACE, currentConfig, counters);
        List<String> documentStreamIds = new ArrayList<>();
        if (matchesDocumentFilters(columns, TelemetryType.TRACE, currentConfig, documentStreamIds)) {
            Trace traceDoc = new Trace();
            traceDoc.setMessage(traceTelemetry.getMessage());
            traceDoc
                .setProperties(setCustomDimensions(traceTelemetry.getProperties(), traceTelemetry.getMeasurements()));
            traceDoc.setDocumentStreamIds(documentStreamIds);
            synchronized (counters.documentList) {
                if (counters.documentList.size() < Counters.MAX_DOCUMENTS_SIZE) {
                    counters.documentList.add(traceDoc);
                }
            }
        }
    }

    private static List<KeyValuePairString> setCustomDimensions(@Nullable Map<String, String> properties,
        @Nullable Map<String, Double> measurements) {
        List<KeyValuePairString> customDims = new ArrayList<>();

        if (properties != null) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                KeyValuePairString kvPair = new KeyValuePairString();
                kvPair.setKey(entry.getKey());
                kvPair.setValue(entry.getValue());
                customDims.add(kvPair);
            }
        }

        if (measurements != null) {
            for (Map.Entry<String, Double> entry : measurements.entrySet()) {
                KeyValuePairString kvPair = new KeyValuePairString();
                kvPair.setKey(entry.getKey());
                kvPair.setValue(entry.getValue().toString());
                customDims.add(kvPair);
            }
        }
        return customDims;
    }

    // TODO (trask) optimization: move live metrics request capture to OpenTelemetry layer so don't
    // have to parse String duration
    // visible for testing
    static long parseDurationToMillis(String duration) {
        // format is DD.HH:MM:SS.MMMMMM
        return startingAtDaysOrHours(duration);
    }

    private static long startingAtDaysOrHours(String duration) {
        int i = 0;
        char c = duration.charAt(i++);
        long daysOrHours = charToInt(c);

        c = duration.charAt(i++);
        while (c != ':' && c != '.') {
            daysOrHours = 10 * daysOrHours + charToInt(c);
            c = duration.charAt(i++);
        }
        if (c == ':') {
            // was really hours
            return startingAtMinutes(duration, i, daysOrHours);
        } else {
            return startingAtHours(duration, i, daysOrHours);
        }
    }

    private static long startingAtHours(String duration, int i, long runningTotalInDays) {
        char c1 = duration.charAt(i++);
        char c2 = duration.charAt(i++);
        int hours = 10 * charToInt(c1) + charToInt(c2);
        return startingAtMinutes(duration, i + 1, 24 * runningTotalInDays + hours);
    }

    private static long startingAtMinutes(String duration, int i, long runningTotalInHours) {
        char c1 = duration.charAt(i++);
        char c2 = duration.charAt(i++);
        int minutes = 10 * charToInt(c1) + charToInt(c2);
        // next char must be ':'
        return startingAtSeconds(duration, i + 1, 60 * runningTotalInHours + minutes);
    }

    private static long startingAtSeconds(String duration, int i, long runningTotalInMinutes) {
        char c1 = duration.charAt(i++);
        char c2 = duration.charAt(i++);
        int seconds = 10 * charToInt(c1) + charToInt(c2);
        return startingAtMicros(duration, i + 1, 60 * runningTotalInMinutes + seconds);
    }

    private static long startingAtMicros(String duration, int i, long runningTotalInSeconds) {
        int millis = 0;
        // only care about milliseconds
        for (int j = i; j < i + 3; j++) {
            char c = duration.charAt(j);
            millis = 10 * millis + charToInt(c);
        }
        return 1000 * runningTotalInSeconds + millis;
    }

    private static int charToInt(char c) {
        int x = c - '0';
        if (x < 0 || x > 9) {
            throw new AssertionError("Unexpected char '" + c + "'");
        }
        return x;
    }

    class FinalCounters {

        final int exceptions;
        final int requests;
        final double requestsDuration;
        final int unsuccessfulRequests;
        final long rdds;
        final double rddsDuration;
        final int unsuccessfulRdds;
        final long processPhysicalMemory;
        final double processNormalizedCpuUsage;
        final List<DocumentIngress> documentList = new ArrayList<>();

        final Map<String, Double> projections;

        final List<CollectionConfigurationError> configErrors;

        private FinalCounters(Counters currentCounters) {

            processPhysicalMemory = getPhysicalMemory(memory);
            processNormalizedCpuUsage = getNormalizedCpuPercentage(cpuPerformanceCounterCalculator);
            exceptions = currentCounters.exceptions.get();

            CountAndDuration countAndDuration
                = Counters.decodeCountAndDuration(currentCounters.requestsAndDurations.get());
            requests = (int) countAndDuration.count;
            this.requestsDuration = countAndDuration.duration;
            this.unsuccessfulRequests = currentCounters.unsuccessfulRequests.get();

            countAndDuration = Counters.decodeCountAndDuration(currentCounters.rddsAndDuations.get());
            this.rdds = countAndDuration.count;
            this.rddsDuration = countAndDuration.duration;
            this.unsuccessfulRdds = currentCounters.unsuccessfulRdds.get();
            synchronized (currentCounters.documentList) {
                this.documentList.addAll(currentCounters.documentList);
            }
            this.projections = currentCounters.derivedMetrics.fetchFinalDerivedMetricValues();
            this.configErrors = currentCounters.configErrors;

        }

        private long getPhysicalMemory(@Nullable MemoryMXBean memory) {
            if (memory == null) {
                return -1;
            }
            MemoryUsage heapMemoryUsage = memory.getHeapMemoryUsage();
            MemoryUsage nonHeapMemoryUsage = memory.getNonHeapMemoryUsage();
            if (heapMemoryUsage == null || nonHeapMemoryUsage == null) {
                return -1;
            }
            return heapMemoryUsage.getUsed() + nonHeapMemoryUsage.getUsed();
        }

        private double
            getNormalizedCpuPercentage(@Nullable CpuPerformanceCounterCalculator cpuPerformanceCounterCalculator) {
            if (cpuPerformanceCounterCalculator == null) {
                return -1;
            }
            Double cpuDatum = cpuPerformanceCounterCalculator.getCpuPercentage();
            if (cpuDatum == null) {
                return -1;
            }
            cpuDatum /= operatingSystemMxBean.getAvailableProcessors();
            return cpuDatum;
        }
    }

    static class CountAndDuration {
        final long count;
        final long duration;

        private CountAndDuration(long count, long duration) {
            this.count = count;
            this.duration = duration;
        }
    }

    static class Counters {
        private static final long MAX_COUNT = 524287L;
        private static final long MAX_DURATION = 17592186044415L;
        private static final int MAX_DOCUMENTS_SIZE = 1000;

        final AtomicInteger exceptions = new AtomicInteger(0);

        final AtomicLong requestsAndDurations = new AtomicLong(0);
        final AtomicInteger unsuccessfulRequests = new AtomicInteger(0);

        final AtomicLong rddsAndDuations = new AtomicLong(0);
        final AtomicInteger unsuccessfulRdds = new AtomicInteger(0);
        final List<DocumentIngress> documentList = new ArrayList<>();

        final DerivedMetricProjections derivedMetrics;

        final List<CollectionConfigurationError> configErrors;

        Counters(Map<String, AggregationType> projectionInfo, List<CollectionConfigurationError> errors) {
            derivedMetrics = new DerivedMetricProjections(projectionInfo);
            configErrors = errors;
        }

        static long encodeCountAndDuration(long count, long duration) {
            if (count > MAX_COUNT || duration > MAX_DURATION) {
                return 0;
            }

            return (count << 44) + duration;
        }

        static CountAndDuration decodeCountAndDuration(long countAndDuration) {
            return new CountAndDuration(countAndDuration >> 44, countAndDuration & MAX_DURATION);
        }
    }
}
