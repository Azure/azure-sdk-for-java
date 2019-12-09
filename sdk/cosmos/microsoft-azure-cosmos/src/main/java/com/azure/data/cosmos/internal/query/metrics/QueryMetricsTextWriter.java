// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query.metrics;

import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class QueryMetricsTextWriter extends QueryMetricsWriter {

    private final StringBuilder stringBuilder;

    // QueryMetrics
    private static final String ActivityIds = "Activity Ids";
    private static final String RetrievedDocumentCount = "Retrieved Document Count";
    private static final String RetrievedDocumentSize = "Retrieved Document Size";
    private static final String OutputDocumentCount = "Output Document Count";
    private static final String OutputDocumentSize = "Output Document Size";
    private static final String IndexUtilizationText = "Index Utilization";
    private static final String TotalQueryExecutionTime = "Total Query Execution Time";

    // QueryPreparationTimes
    private static final String QueryPreparationTimes = "Query Preparation Times";
    private static final String QueryCompileTime = "Query Compilation Time";
    private static final String LogicalPlanBuildTime = "Logical Plan Build Time";
    private static final String PhysicalPlanBuildTime = "Physical Plan Build Time";
    private static final String QueryOptimizationTime = "Query Optimization Time";

    // QueryTimes
    private static final String QueryEngineTimes = "Query Engine Times";
    private static final String IndexLookupTime = "Index Lookup Time";
    private static final String DocumentLoadTime = "Document Load Time";
    private static final String DocumentWriteTime = "Document Write Time";

    // RuntimeExecutionTimes
    private static final String RuntimeExecutionTimes = "Runtime Execution Times";
    private static final String TotalExecutionTime = "Query Engine Execution Time";
    private static final String SystemFunctionExecuteTime = "System Function Execution Time";
    private static final String UserDefinedFunctionExecutionTime = "User-defined Function Execution Time";

    // ClientSideQueryMetrics
    private static final String ClientSideQueryMetrics = "Client Side Metrics";
    private static final String Retries = "Retry Count";
    private static final String RequestCharge = "Request Charge";
    private static final String FetchExecutionRanges = "Partition Execution Timeline";
    private static final String SchedulingMetrics = "Scheduling Metrics";

    // Constants for Partition Execution Timeline Table
    private static final String StartTimeHeader = "Start Time (UTC)";
    private static final String EndTimeHeader = "End Time (UTC)";
    private static final String DurationHeader = "Duration (ms)";
    private static final String PartitionKeyRangeIdHeader = "Partition Id";
    private static final String NumberOfDocumentsHeader = "NUMBER of Documents";
    private static final String RetryCountHeader = "Retry Count";
    private static final String ActivityIdHeader = "Activity Id";

    // Constants for Scheduling Metrics Table
    private static final String PartitionIdHeader = "Partition Id";
    private static final String ResponseTimeHeader = "Response Time (ms)";
    private static final String RunTimeHeader = "Run Time (ms)";
    private static final String WaitTimeHeader = "Wait Time (ms)";
    private static final String TurnaroundTimeHeader = "Turnaround Time (ms)";
    private static final String NumberOfPreemptionHeader = "NUMBER of Preemptions";

    // Static  for Partition Execution Timeline Table
    // private static  int MaxDateTimeStringLength = LocalDateTime.MAX.toString().length();
    private static final int MaxDateTimeStringLength = 16;
    private static final int StartTimeHeaderLength = Math.max(MaxDateTimeStringLength, StartTimeHeader.length());
    private static final int EndTimeHeaderLength = Math.max(MaxDateTimeStringLength, EndTimeHeader.length());
    private static final int DurationHeaderLength = DurationHeader.length();
    private static final int PartitionKeyRangeIdHeaderLength = PartitionKeyRangeIdHeader.length();
    private static final int NumberOfDocumentsHeaderLength = NumberOfDocumentsHeader.length();
    private static final int RetryCountHeaderLength = RetryCountHeader.length();
    private static final int ActivityIdHeaderLength = UUID.randomUUID().toString().length();

    private static TextTable.Column[] PartitionExecutionTimelineColumns = new TextTable.Column[]
            {
                    new TextTable.Column(PartitionKeyRangeIdHeader, PartitionKeyRangeIdHeaderLength),
                    new TextTable.Column(ActivityIdHeader, ActivityIdHeaderLength),
                    new TextTable.Column(StartTimeHeader, StartTimeHeaderLength),
                    new TextTable.Column(EndTimeHeader, EndTimeHeaderLength),
                    new TextTable.Column(DurationHeader, DurationHeaderLength),
                    new TextTable.Column(NumberOfDocumentsHeader, NumberOfDocumentsHeaderLength),
                    new TextTable.Column(RetryCountHeader, RetryCountHeaderLength),
            };

    private static TextTable PartitionExecutionTimelineTable = new TextTable(Arrays.asList(PartitionExecutionTimelineColumns));

    // Static  for Scheduling Metrics Table
    //private static readonly int MaxTimeSpanStringLength = Math.Max(TimeSpan.MaxValue.TotalMilliseconds.ToString
    // ("G17").Length, TurnaroundTimeHeader.Length);
    private static final int PartitionIdHeaderLength = PartitionIdHeader.length();
    private static final int ResponseTimeHeaderLength = ResponseTimeHeader.length();
    private static final int RunTimeHeaderLength = RunTimeHeader.length();
    private static final int WaitTimeHeaderLength = WaitTimeHeader.length();
    private static final int TurnaroundTimeHeaderLength = TurnaroundTimeHeader.length();
    private static final int NumberOfPreemptionHeaderLength = NumberOfPreemptionHeader.length();

    private static TextTable.Column[] SchedulingMetricsColumns = new TextTable.Column[]
            {
                    new TextTable.Column(PartitionIdHeader, PartitionIdHeaderLength),
                    new TextTable.Column(ResponseTimeHeader, ResponseTimeHeaderLength),
                    new TextTable.Column(RunTimeHeader, RunTimeHeaderLength),
                    new TextTable.Column(WaitTimeHeader, WaitTimeHeaderLength),
                    new TextTable.Column(TurnaroundTimeHeader, TurnaroundTimeHeaderLength),
                    new TextTable.Column(NumberOfPreemptionHeader, NumberOfPreemptionHeaderLength),
            };

    private static TextTable SchedulingMetricsTable = new TextTable(Arrays.asList(SchedulingMetricsColumns));

    // FetchExecutionRange state
    private String lastFetchPartitionId;
    private String lastActivityId;
    private Instant lastStartTime;
    private Instant lastEndTime;
    private long lastFetchDocumentCount;
    private long lastFetchRetryCount;

    // PartitionSchedulingTimeSpan state
    private String lastSchedulingPartitionId;
    private long lastResponseTime;
    private long lastRunTime;
    private long lastWaitTime;
    private long lastTurnaroundTime;
    private long lastNumberOfPreemptions;

    static DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("HH:mm:ss:SSSS").withZone(ZoneOffset.UTC);

    public QueryMetricsTextWriter(StringBuilder stringBuilder) {
        assert stringBuilder != null;
        this.stringBuilder = stringBuilder;
    }

    @Override
    protected void writeBeforeQueryMetrics() {
        // Do Nothing
    }

    @Override
    protected void writeRetrievedDocumentCount(long retrievedDocumentCount) {
        QueryMetricsTextWriter.appendCountToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.RetrievedDocumentCount, retrievedDocumentCount, 0);
    }

    @Override
    protected void writeRetrievedDocumentSize(long retrievedDocumentSize) {
        QueryMetricsTextWriter.appendBytesToStringBuilder(stringBuilder, QueryMetricsTextWriter.RetrievedDocumentSize
                , retrievedDocumentSize, 0);
    }

    @Override
    protected void writeOutputDocumentCount(long outputDocumentCount) {
        QueryMetricsTextWriter.appendCountToStringBuilder(stringBuilder, QueryMetricsTextWriter.OutputDocumentCount,
                outputDocumentCount, 0);
    }

    @Override
    protected void writeOutputDocumentSize(long outputDocumentSize) {
        QueryMetricsTextWriter.appendBytesToStringBuilder(stringBuilder, QueryMetricsTextWriter.OutputDocumentSize,
                outputDocumentSize, 0);
    }

    @Override
    protected void writeIndexHitRatio(double indexHitRatio) {
        QueryMetricsTextWriter.appendPercentageToStringBuilder(stringBuilder, QueryMetricsTextWriter.IndexUtilizationText
                , indexHitRatio, 0);
    }

    @Override
    protected void writeTotalQueryExecutionTime(Duration totalQueryExecutionTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.TotalQueryExecutionTime, durationToMilliseconds(totalQueryExecutionTime), 0);
    }

    @Override
    protected void writeBeforeQueryPreparationTimes() {
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.QueryPreparationTimes, 1);
    }

    @Override
    protected void writeQueryCompilationTime(Duration queryCompilationTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.QueryCompileTime, durationToMilliseconds(queryCompilationTime), 2);
    }

    @Override
    protected void writeLogicalPlanBuildTime(Duration logicalPlanBuildTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.LogicalPlanBuildTime, durationToMilliseconds(logicalPlanBuildTime), 2);
    }

    @Override
    protected void writePhysicalPlanBuildTime(Duration physicalPlanBuildTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.PhysicalPlanBuildTime, durationToMilliseconds(physicalPlanBuildTime), 2);
    }

    @Override
    protected void writeQueryOptimizationTime(Duration queryOptimizationTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.QueryOptimizationTime, durationToMilliseconds(queryOptimizationTime), 2);
    }

    @Override
    protected void writeAfterQueryPreparationTimes() {
        // Do Nothing
    }

    @Override
    protected void writeIndexLookupTime(Duration indexLookupTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.IndexLookupTime, durationToMilliseconds(indexLookupTime), 1);
    }

    @Override
    protected void writeDocumentLoadTime(Duration documentLoadTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.DocumentLoadTime, durationToMilliseconds(documentLoadTime), 1);
    }

    @Override
    protected void writeVMExecutionTime(Duration vMExecutionTime) {
        // Do Nothing
    }

    @Override
    protected void writeBeforeRuntimeExecutionTimes() {
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.RuntimeExecutionTimes, 1);
    }

    @Override
    protected void writeQueryEngineExecutionTime(Duration queryEngineExecutionTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.QueryEngineTimes, durationToMilliseconds(queryEngineExecutionTime), 2);
    }

    @Override
    protected void writeSystemFunctionExecutionTime(Duration systemFunctionExecutionTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.SystemFunctionExecuteTime, durationToMilliseconds(systemFunctionExecutionTime)
                , 2);
    }

    @Override
    protected void writeUserDefinedFunctionExecutionTime(Duration userDefinedFunctionExecutionTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.UserDefinedFunctionExecutionTime,
                durationToMilliseconds(userDefinedFunctionExecutionTime), 2);
    }

    @Override
    protected void writeAfterRuntimeExecutionTimes() {
        // Do Nothing
    }

    @Override
    protected void writeDocumentWriteTime(Duration documentWriteTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.DocumentWriteTime, durationToMilliseconds(documentWriteTime), 1);
    }

    @Override
    protected void writeBeforeClientSideMetrics() {
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.ClientSideQueryMetrics, 0);
    }

    @Override
    protected void writeRetries(long retries) {
        QueryMetricsTextWriter.appendCountToStringBuilder(stringBuilder, QueryMetricsTextWriter.Retries, retries, 1);
    }

    @Override
    protected void writeRequestCharge(double requestCharge) {
        QueryMetricsTextWriter.appendRUToStringBuilder(stringBuilder, QueryMetricsTextWriter.RequestCharge,
                requestCharge, 1);
    }

    @Override
    protected void writeBeforePartitionExecutionTimeline() {
        QueryMetricsTextWriter.appendNewlineToStringBuilder(stringBuilder);

        // Building the table for fetch execution ranges
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, QueryMetricsTextWriter.FetchExecutionRanges
                , 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, PartitionExecutionTimelineTable.getTopLine(), 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, PartitionExecutionTimelineTable.getHeader(), 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, PartitionExecutionTimelineTable.getMiddleLine(), 1);
    }

    @Override
    protected void writeBeforeFetchExecutionRange() {
        // Do Nothing
    }

    @Override
    protected void writeFetchPartitionKeyRangeId(String partitionId) {
        this.lastFetchPartitionId = partitionId;
    }

    @Override
    protected void writeActivityId(String activityId) {
        this.lastActivityId = activityId;
    }

    @Override
    protected void writeStartTime(Instant startTime) {
        this.lastStartTime = startTime;
    }

    @Override
    protected void writeEndTime(Instant endTime) {
        this.lastEndTime = endTime;
    }

    @Override
    protected void writeFetchDocumentCount(long numberOfDocuments) {
        this.lastFetchDocumentCount = numberOfDocuments;
    }

    @Override
    protected void writeFetchRetryCount(long retryCount) {
        this.lastFetchRetryCount = retryCount;
    }

    @Override
    protected void writeAfterFetchExecutionRange() {
        QueryMetricsTextWriter.appendHeaderToStringBuilder(
                stringBuilder,
                PartitionExecutionTimelineTable.getRow(Arrays.asList(
                        this.lastFetchPartitionId,
                        this.lastActivityId,
                        formatter.format(this.lastStartTime),
                        formatter.format(this.lastEndTime),
                        nanosToMilliSeconds(this.lastEndTime.minusNanos(lastStartTime.getNano()).getNano()),
                        this.lastFetchDocumentCount,
                        this.lastFetchRetryCount)),
                1);
    }

    @Override
    protected void writeAfterPartitionExecutionTimeline() {
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, PartitionExecutionTimelineTable.getBottomLine(),
                1);
    }

    @Override
    protected void writeBeforeSchedulingMetrics() {
        QueryMetricsTextWriter.appendNewlineToStringBuilder(stringBuilder);

        // Building the table for scheduling metrics
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, QueryMetricsTextWriter.SchedulingMetrics, 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, SchedulingMetricsTable.getTopLine(), 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, SchedulingMetricsTable.getHeader(), 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, SchedulingMetricsTable.getMiddleLine(), 1);
    }

    @Override
    protected void writeBeforePartitionSchedulingDuration() {
        // Do Nothing
    }

    @Override
    protected void writePartitionSchedulingDurationId(String partitionId) {
        this.lastSchedulingPartitionId = partitionId;
    }

    @Override
    protected void writeResponseTime(long responseTime) {
        this.lastResponseTime = responseTime;
    }

    @Override
    protected void writeRunTime(long runTime) {
        this.lastRunTime = runTime;
    }

    @Override
    protected void writeWaitTime(long waitTime) {
        this.lastWaitTime = waitTime;
    }

    @Override
    protected void writeTurnaroundTime(long turnaroundTime) {
        this.lastTurnaroundTime = turnaroundTime;
    }

    @Override
    protected void writeNumberOfPreemptions(long numPreemptions) {
        this.lastNumberOfPreemptions = numPreemptions;
    }

    @Override
    protected void writeAfterPartitionSchedulingDuration() {
        QueryMetricsTextWriter.appendHeaderToStringBuilder(
                stringBuilder,
                SchedulingMetricsTable.getRow(Arrays.asList(
                        this.lastSchedulingPartitionId,
                        this.lastResponseTime,
                        this.lastRunTime,
                        this.lastWaitTime,
                        this.lastTurnaroundTime,
                        this.lastNumberOfPreemptions)),
                1);
    }

    @Override
    protected void writeAfterSchedulingMetrics() {
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, SchedulingMetricsTable.getBottomLine(), 1);
    }

    @Override
    protected void writeAfterClientSideMetrics() {
        // Do Nothing
    }

    @Override
    protected void writeAfterQueryMetrics() {
        // Do Nothing
    }

    // Util functions
    private static final int NANOS_TO_MILLIS = 1000000;

    static HashMap<String, Double> parseDelimitedString(String delimitedString) {
        if (delimitedString == null) {
            throw new NullPointerException("delimitedString");
        }

        HashMap<String, Double> metrics = new HashMap<>();

        final int key = 0;
        final int value = 1;
        String[] headerAttributes = StringUtils.split(delimitedString, ";");

        for (String attribute : headerAttributes) {
            String[] attributeKeyValue = StringUtils.split(attribute, "=");

            if (attributeKeyValue.length != 2) {
                throw new NullPointerException("recieved a malformed delimited STRING");
            }

            String attributeKey = attributeKeyValue[key];
            double attributeValue = Double.parseDouble(attributeKeyValue[value]);
            metrics.put(attributeKey, attributeValue);
        }

        return metrics;
    }

    static Duration durationFromMetrics(HashMap<String, Double> metrics, String key) {
        // Just attempt to get the metrics
        Double durationInMilliseconds = metrics.get(key);
        if (durationInMilliseconds == null) {
            return Duration.ZERO;
        }

        long seconds = (long) (durationInMilliseconds / 1e3);
        long nanoseconds = (long) ((durationInMilliseconds - (seconds * 1e3)) * 1e6);

        return Duration.ofSeconds(seconds, nanoseconds);
    }

    static double durationToMilliseconds(Duration duration) {
        double seconds = duration.getSeconds();
        double nano = duration.getNano();

        return (seconds * 1e3) + (nano / 1e6);
    }

    static Duration getDurationFromMetrics(HashMap<String, Double> metrics, String key) {
        double timeSpanInMilliseconds;
        Duration timeSpanFromMetrics;
        timeSpanInMilliseconds = metrics.get(key);
        timeSpanFromMetrics = doubleMillisecondsToDuration(timeSpanInMilliseconds);
        return timeSpanFromMetrics;
    }

    private static Duration doubleMillisecondsToDuration(double timeSpanInMilliseconds) {
        long timeInNanoSeconds = (long) (timeSpanInMilliseconds * NANOS_TO_MILLIS);
        return Duration.ofNanos(timeInNanoSeconds);
    }

    private static void appendToStringBuilder(StringBuilder stringBuilder, String property, String value,
                                              String units, int indentLevel) {
        final String Indent = "  ";
        final String FormatString = "%-40s : %15s %-12s %s";

        stringBuilder.append(String.format(
                Locale.ROOT,
                FormatString,
                StringUtils.repeat(Indent, indentLevel) + property,
                value,
                units,
                System.lineSeparator()));
    }

    static void appendBytesToStringBuilder(StringBuilder stringBuilder, String property, long bytes, int indentLevel) {
        final String BytesFormatString = "%d";
        final String BytesUnitString = "bytes";

        appendToStringBuilder(
                stringBuilder,
                property,
                String.format(BytesFormatString, bytes),
                BytesUnitString,
                indentLevel);
    }

    static void appendMillisecondsToStringBuilder(StringBuilder stringBuilder, String property, double milliseconds,
                                                  int indentLevel) {
        final String MillisecondsFormatString = "%f";
        final String MillisecondsUnitString = "milliseconds";

        appendToStringBuilder(stringBuilder, property, String.format(MillisecondsFormatString,
                milliseconds), MillisecondsUnitString, indentLevel);
    }

    static void appendNanosecondsToStringBuilder(StringBuilder stringBuilder, String property, double nanoSeconds,
                                                 int indentLevel) {
        final String MillisecondsFormatString = "%.2f";
        final String MillisecondsUnitString = "milliseconds";
        appendToStringBuilder(stringBuilder, property, String.format(MillisecondsFormatString,
                nanosToMilliSeconds(nanoSeconds)), MillisecondsUnitString, indentLevel);
    }

    static double nanosToMilliSeconds(double nanos) {
        return nanos / NANOS_TO_MILLIS;
    }

    static void appendHeaderToStringBuilder(StringBuilder stringBuilder, String headerTitle, int indentLevel) {
        final String Indent = "  ";
        final String FormatString = "%s %s";
        stringBuilder.append(String.format(
                Locale.ROOT,
                FormatString,
                String.join(StringUtils.repeat(Indent, indentLevel)) + headerTitle,
                System.lineSeparator()));
    }

    static void appendRUToStringBuilder(StringBuilder stringBuilder, String property, double requestCharge,
                                        int indentLevel) {
        final String RequestChargeFormatString = "%s";
        final String RequestChargeUnitString = "RUs";

        appendToStringBuilder(
                stringBuilder,
                property,
                String.format(Locale.ROOT, RequestChargeFormatString, requestCharge),
                RequestChargeUnitString,
                indentLevel);
    }

    static void appendActivityIdsToStringBuilder(StringBuilder stringBuilder, String activityIdsLabel,
                                                 List<String> activityIds, int indentLevel) {
        final String Indent = "  ";
        stringBuilder.append(activityIdsLabel);
        stringBuilder.append(System.lineSeparator());
        for (String activityId : activityIds) {
            stringBuilder.append(Indent);
            stringBuilder.append(activityId);
            stringBuilder.append(System.lineSeparator());
        }
    }

    static void appendPercentageToStringBuilder(StringBuilder stringBuilder, String property, double percentage,
                                                int indentLevel) {
        final String PercentageFormatString = "%.2f";
        final String PercentageUnitString = "%";

        appendToStringBuilder(stringBuilder, property, String.format(PercentageFormatString,
                percentage * 100), PercentageUnitString, indentLevel);
    }

    static void appendCountToStringBuilder(StringBuilder stringBuilder, String property, long count, int indentLevel) {
        final String CountFormatString = "%s";
        final String CountUnitString = "";

        appendToStringBuilder(
                stringBuilder,
                property,
                String.format(CountFormatString, count),
                CountUnitString,
                indentLevel);
    }

    static void appendNewlineToStringBuilder(StringBuilder stringBuilder) {
        appendHeaderToStringBuilder(stringBuilder, StringUtils.EMPTY, 0);
    }

}
