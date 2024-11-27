// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryExceptionDetails;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.DocumentIngress;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.Request;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.RemoteDependency;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.KeyValuePairString;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.swagger.models.Exception;
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
import java.util.function.Supplier;

final class QuickPulseDataCollector {

    private static final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();

    private static final OperatingSystemMXBean operatingSystemMxBean = ManagementFactory.getOperatingSystemMXBean();

    private final AtomicReference<Counters> counters = new AtomicReference<>(null);
    private final CpuPerformanceCounterCalculator cpuPerformanceCounterCalculator
        = getCpuPerformanceCounterCalculator();

    private volatile QuickPulseStatus quickPulseStatus = QuickPulseStatus.QP_IS_OFF;

    private volatile Supplier<String> instrumentationKeySupplier;

    QuickPulseDataCollector() {
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
        counters.set(new Counters());
    }

    synchronized void setQuickPulseStatus(QuickPulseStatus quickPulseStatus) {
        this.quickPulseStatus = quickPulseStatus;
    }

    // Used only in tests
    synchronized QuickPulseStatus getQuickPulseStatus() {
        return this.quickPulseStatus;
    }

    @Nullable
    synchronized FinalCounters getAndRestart() {
        Counters currentCounters = counters.getAndSet(new Counters());
        if (currentCounters != null) {
            return new FinalCounters(currentCounters);
        }

        return null;
    }

    // only used by tests
    @Nullable
    synchronized FinalCounters peek() {
        Counters currentCounters = this.counters.get(); // this should be the only differece
        if (currentCounters != null) {
            return new FinalCounters(currentCounters);
        }
        return null;
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

        MonitorDomain data = telemetryItem.getData().getBaseData();
        if (data instanceof RequestData) {
            RequestData requestTelemetry = (RequestData) data;
            addRequest(requestTelemetry, itemCount, getOperationName(telemetryItem));
        } else if (data instanceof RemoteDependencyData) {
            addDependency((RemoteDependencyData) data, itemCount);
        } else if (data instanceof TelemetryExceptionData) {
            addException((TelemetryExceptionData) data, itemCount);
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

    private void addDependency(RemoteDependencyData telemetry, int itemCount) {
        Counters counters = this.counters.get();
        if (counters == null) {
            return;
        }
        long durationMillis = parseDurationToMillis(telemetry.getDuration());
        counters.rddsAndDuations.addAndGet(Counters.encodeCountAndDuration(itemCount, durationMillis));
        Boolean success = telemetry.isSuccess();
        if (success != null && !success) { // success should not be null
            counters.unsuccessfulRdds.incrementAndGet();
        }
        RemoteDependency dependencyDoc = new RemoteDependency();
        dependencyDoc.setName(telemetry.getName());
        dependencyDoc.setCommandName(telemetry.getData());
        dependencyDoc.setDuration(Duration.ofMillis(durationMillis).toString());
        dependencyDoc.setResultCode(telemetry.getResultCode());
        dependencyDoc.setProperties(setCustomDimensions(telemetry.getProperties(), telemetry.getMeasurements()));

        synchronized (counters.documentList) {
            if (counters.documentList.size() < Counters.MAX_DOCUMENTS_SIZE) {
                counters.documentList.add(dependencyDoc);
            }
        }
    }

    private void addException(TelemetryExceptionData exceptionData, int itemCount) {
        Counters counters = this.counters.get();
        if (counters == null) {
            return;
        }

        counters.exceptions.addAndGet(itemCount);

        List<TelemetryExceptionDetails> exceptionList = exceptionData.getExceptions();
        // Exception is a class from live metrics swagger that represents a document for an exception
        Exception exceptionDoc = new Exception();
        if (exceptionList != null && !exceptionList.isEmpty()) {
            exceptionDoc.setExceptionMessage(exceptionList.get(0).getMessage());
            exceptionDoc.setExceptionType(exceptionList.get(0).getTypeName());
        }
        exceptionDoc.setProperties(setCustomDimensions(exceptionData.getProperties(), exceptionData.getMeasurements()));

        synchronized (counters.documentList) {
            if (counters.documentList.size() < Counters.MAX_DOCUMENTS_SIZE) {
                counters.documentList.add(exceptionDoc);
            }
        }
    }

    private void addRequest(RequestData requestTelemetry, int itemCount, String operationName) {
        Counters counters = this.counters.get();
        if (counters == null) {
            return;
        }
        long durationMillis = parseDurationToMillis(requestTelemetry.getDuration());
        counters.requestsAndDurations.addAndGet(Counters.encodeCountAndDuration(itemCount, durationMillis));
        if (!requestTelemetry.isSuccess()) {
            counters.unsuccessfulRequests.incrementAndGet();
        }

        Request requestDoc = new Request();
        requestDoc.setDuration(Duration.ofMillis(durationMillis).toString());
        requestDoc.setResponseCode(requestTelemetry.getResponseCode());
        requestDoc.setName(requestTelemetry.getName());
        requestDoc.setUrl(requestTelemetry.getUrl());
        requestDoc
            .setProperties(setCustomDimensions(requestTelemetry.getProperties(), requestTelemetry.getMeasurements()));
        synchronized (counters.documentList) {
            if (counters.documentList.size() < Counters.MAX_DOCUMENTS_SIZE) {
                counters.documentList.add(requestDoc);
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
