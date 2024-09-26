// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.monitor.opentelemetry.exporter.implementation.models.ContextTagKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.StackFrame;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryExceptionData;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryExceptionDetails;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseDependencyDocument;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseDocument;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseExceptionDocument;
import com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model.QuickPulseRequestDocument;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.CpuPerformanceCounterCalculator;
import reactor.util.annotation.Nullable;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final boolean useNormalizedValueForNonNormalizedCpuPercentage;

    private volatile QuickPulseStatus quickPulseStatus = QuickPulseStatus.QP_IS_OFF;

    private volatile Supplier<String> instrumentationKeySupplier;

    QuickPulseDataCollector(boolean useNormalizedValueForNonNormalizedCpuPercentage) {
        this.useNormalizedValueForNonNormalizedCpuPercentage = useNormalizedValueForNonNormalizedCpuPercentage;
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
        QuickPulseDependencyDocument quickPulseDependencyDocument = new QuickPulseDependencyDocument();
        quickPulseDependencyDocument.setDocumentType("RemoteDependency");
        quickPulseDependencyDocument.setType("DependencyTelemetryDocument");
        quickPulseDependencyDocument.setOperationId(telemetry.getId());
        quickPulseDependencyDocument.setVersion("1.0");
        quickPulseDependencyDocument.setName(telemetry.getName());
        quickPulseDependencyDocument.setCommandName(telemetry.getData());
        quickPulseDependencyDocument.setTarget(telemetry.getTarget());
        quickPulseDependencyDocument.setSuccess(telemetry.isSuccess());
        quickPulseDependencyDocument.setDuration(Duration.ofMillis(durationMillis).toString());
        quickPulseDependencyDocument.setResultCode(telemetry.getResultCode());
        quickPulseDependencyDocument.setOperationName(telemetry.getId());
        quickPulseDependencyDocument.setDependencyTypeName(telemetry.getType());
        quickPulseDependencyDocument
            .setProperties(aggregateProperties(telemetry.getProperties(), telemetry.getMeasurements()));
        synchronized (counters.documentList) {
            if (counters.documentList.size() < Counters.MAX_DOCUMENTS_SIZE) {
                counters.documentList.add(quickPulseDependencyDocument);
            }
        }
    }

    private void addException(TelemetryExceptionData exceptionData, int itemCount) {
        Counters counters = this.counters.get();
        if (counters == null) {
            return;
        }

        counters.exceptions.addAndGet(itemCount);
        QuickPulseExceptionDocument quickPulseExceptionDocument = new QuickPulseExceptionDocument();
        quickPulseExceptionDocument.setDocumentType("Exception");
        quickPulseExceptionDocument.setType("ExceptionTelemetryDocument");
        quickPulseExceptionDocument.setOperationId(exceptionData.getProblemId());
        quickPulseExceptionDocument.setVersion("1.0");
        List<TelemetryExceptionDetails> exceptionList = exceptionData.getExceptions();
        StringBuilder exceptions = new StringBuilder();
        if (exceptionList != null && exceptionList.size() > 0) {
            List<StackFrame> parsedStack = exceptionList.get(0).getParsedStack();
            String stack = exceptionList.get(0).getStack();
            if (parsedStack != null && parsedStack.size() > 0) {
                for (StackFrame stackFrame : parsedStack) {
                    if (stackFrame != null && stackFrame.getAssembly() != null) {
                        exceptions.append(stackFrame.getAssembly()).append("\n");
                    }
                }
            } else if (stack != null && stack.length() > 0) {
                exceptions.append(stack);
            }
            quickPulseExceptionDocument.setException(exceptions.toString());
            quickPulseExceptionDocument.setExceptionMessage(exceptionList.get(0).getMessage());
            quickPulseExceptionDocument.setExceptionType(exceptionList.get(0).getTypeName());
        }
        synchronized (counters.documentList) {
            if (counters.documentList.size() < Counters.MAX_DOCUMENTS_SIZE) {
                counters.documentList.add(quickPulseExceptionDocument);
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
        QuickPulseRequestDocument quickPulseRequestDocument = new QuickPulseRequestDocument();
        quickPulseRequestDocument.setDocumentType("Request");
        quickPulseRequestDocument.setType("RequestTelemetryDocument");
        quickPulseRequestDocument.setOperationId(requestTelemetry.getId());
        quickPulseRequestDocument.setVersion("1.0");
        quickPulseRequestDocument.setSuccess(requestTelemetry.isSuccess());
        quickPulseRequestDocument.setDuration(Duration.ofMillis(durationMillis).toString());
        quickPulseRequestDocument.setResponseCode(requestTelemetry.getResponseCode());
        quickPulseRequestDocument.setOperationName(operationName);
        quickPulseRequestDocument.setName(requestTelemetry.getName());
        quickPulseRequestDocument.setUrl(requestTelemetry.getUrl());
        quickPulseRequestDocument
            .setProperties(aggregateProperties(requestTelemetry.getProperties(), requestTelemetry.getMeasurements()));
        synchronized (counters.documentList) {
            if (counters.documentList.size() < Counters.MAX_DOCUMENTS_SIZE) {
                counters.documentList.add(quickPulseRequestDocument);
            }
        }
    }

    private static Map<String, String> aggregateProperties(@Nullable Map<String, String> properties,
        @Nullable Map<String, Double> measurements) {
        Map<String, String> aggregatedProperties = new HashMap<>();
        if (measurements != null) {
            measurements.forEach((k, v) -> aggregatedProperties.put(k, String.valueOf(v)));
        }
        if (properties != null) {
            aggregatedProperties.putAll(properties);
        }
        return aggregatedProperties;
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
        final long memoryCommitted;
        final double cpuUsage;
        final List<QuickPulseDocument> documentList = new ArrayList<>();

        private FinalCounters(Counters currentCounters) {

            memoryCommitted = getMemoryCommitted(memory);
            cpuUsage = getNonNormalizedCpuPercentage(cpuPerformanceCounterCalculator);
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

        private long getMemoryCommitted(@Nullable MemoryMXBean memory) {
            if (memory == null) {
                return -1;
            }
            MemoryUsage heapMemoryUsage = memory.getHeapMemoryUsage();
            if (heapMemoryUsage == null) {
                return -1;
            }
            return heapMemoryUsage.getCommitted();
        }

        private double
            getNonNormalizedCpuPercentage(@Nullable CpuPerformanceCounterCalculator cpuPerformanceCounterCalculator) {
            if (cpuPerformanceCounterCalculator == null) {
                return -1;
            }
            Double cpuDatum = cpuPerformanceCounterCalculator.getCpuPercentage();
            if (cpuDatum == null) {
                return -1;
            }

            if (useNormalizedValueForNonNormalizedCpuPercentage) {
                // normalize for backwards compatibility even though this is supposed to be non-normalized
                cpuDatum /= operatingSystemMxBean.getAvailableProcessors();
            }

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
        final List<QuickPulseDocument> documentList = new ArrayList<>();

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
