// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query.metrics;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

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
    private static final String RETRIEVED_DOCUMENT_COUNT = "Retrieved Document Count";
    private static final String RETRIEVED_DOCUMENT_SIZE = "Retrieved Document Size";
    private static final String OUTPUT_DOCUMENT_COUNT = "Output Document Count";
    private static final String OUTPUT_DOCUMENT_SIZE = "Output Document Size";
    private static final String INDEX_UTILIZATION_TEXT = "Index Utilization";
    private static final String TOTAL_QUERY_EXECUTION_TIME = "Total Query Execution Time";

    // QueryPreparationTimes
    private static final String QUERY_PREPARATION_TIMES = "Query Preparation Times";
    private static final String QUERY_COMPILATION_TIME = "Query Compilation Time";
    private static final String LOGICAL_PLAN_BUILD_TIME = "Logical Plan Build Time";
    private static final String PHYSICAL_PLAN_BUILD_TIME = "Physical Plan Build Time";
    private static final String QUERY_OPTIMIZATION_TIME = "Query Optimization Time";

    // QueryTimes
    private static final String QUERY_ENGINE_TIMES = "Query Engine Times";
    private static final String INDEX_LOOKUP_TIME = "Index Lookup Time";
    private static final String DOCUMENT_LOAD_TIME = "Document Load Time";
    private static final String DOCUMENT_WRITE_TIME = "Document Write Time";

    // RuntimeExecutionTimes
    private static final String RUNTIME_EXECUTION_TIMES = "Runtime Execution Times";
    private static final String TOTAL_EXECUTION_TIME = "Query Engine Execution Time";
    private static final String SYSTEM_FUNCTION_EXECUTION_TIME = "System Function Execution Time";
    private static final String USER_DEFINED_FUNCTION_EXECUTION_TIME = "User-defined Function Execution Time";

    // ClientSideQueryMetrics
    private static final String CLIENT_SIDE_METRICS = "Client Side Metrics";
    private static final String RETRIES = "Retry Count";
    private static final String REQUEST_CHARGE = "Request Charge";
    private static final String FETCH_EXECUTION_RANGES = "Partition Execution Timeline";
    private static final String SCHEDULING_METRICS = "Scheduling Metrics";

    // Constants for Partition Execution Timeline Table
    public static final String START_TIME_HEADER = "Start Time (UTC)";
    public static final String END_TIME_HEADER = "End Time (UTC)";
    public static final String DURATION_HEADER = "Duration (ms)";
    private static final String PARTITION_KEY_RANGE_HEADER = "Partition Key Range";
    private static final String NUMBER_OF_DOCUMENTS_HEADER = "NUMBER of Documents";
    private static final String RETRY_COUNT_HEADER = "Retry Count";
    private static final String ACTIVITY_ID_HEADER = "Activity Id";

    // Constants for Scheduling Metrics Table
    private static final String PARTITION_RANGE_HEADER = "Partition Range";
    private static final String RESPONSE_TIME_HEADER = "Response Time (ms)";
    private static final String RUN_TIME_HEADER = "Run Time (ms)";
    private static final String WAIT_TIME_HEADER = "Wait Time (ms)";
    private static final String TURNAROUND_TIME_HEADER = "Turnaround Time (ms)";
    private static final String NUMBER_OF_PREEMPTION_HEADER = "NUMBER of Preemptions";

    // Static  for Partition Execution Timeline Table
    // private static  int MaxDateTimeStringLength = LocalDateTime.MAX.toString().length();
    private static final int MAX_DATE_TIME_STRING_LENGTH = 16;
    private static final int START_TIME_HEADER_LENGTH
        = Math.max(MAX_DATE_TIME_STRING_LENGTH, START_TIME_HEADER.length());
    private static final int END_TIME_HEADER_LENGTH = Math.max(MAX_DATE_TIME_STRING_LENGTH, END_TIME_HEADER.length());
    private static final int DURATION_HEADER_LENGTH = DURATION_HEADER.length();
    private static final int PARTITION_KEY_RANGE_ID_HEADER_LENGTH = PARTITION_KEY_RANGE_HEADER.length();
    private static final int NUMBER_OF_DOCUMENTS_HEADER_LENGTH = NUMBER_OF_DOCUMENTS_HEADER.length();
    private static final int RETRY_COUNT_HEADER_LENGTH = RETRY_COUNT_HEADER.length();
    private static final int ACTIVITY_ID_HEADER_LENGTH = UUID.randomUUID().toString().length();

    private static final TextTable.Column[] PARTITION_EXECUTION_TIMELINE_COLUMNS = new TextTable.Column[]
            {
                    new TextTable.Column(PARTITION_KEY_RANGE_HEADER, PARTITION_KEY_RANGE_ID_HEADER_LENGTH),
                    new TextTable.Column(ACTIVITY_ID_HEADER, ACTIVITY_ID_HEADER_LENGTH),
                    new TextTable.Column(START_TIME_HEADER, START_TIME_HEADER_LENGTH),
                    new TextTable.Column(END_TIME_HEADER, END_TIME_HEADER_LENGTH),
                    new TextTable.Column(DURATION_HEADER, DURATION_HEADER_LENGTH),
                    new TextTable.Column(NUMBER_OF_DOCUMENTS_HEADER, NUMBER_OF_DOCUMENTS_HEADER_LENGTH),
                    new TextTable.Column(RETRY_COUNT_HEADER, RETRY_COUNT_HEADER_LENGTH),
            };

    private static final TextTable PARTITION_EXECUTION_TIMELINE_TABLE
        = new TextTable(Arrays.asList(PARTITION_EXECUTION_TIMELINE_COLUMNS));

    // Static  for Scheduling Metrics Table
    //private static readonly int MaxTimeSpanStringLength = Math.Max(TimeSpan.MaxValue.TotalMilliseconds.ToString
    // ("G17").Length, TurnaroundTimeHeader.Length);
    private static final int PARTITION_ID_HEADER_LENGTH = PARTITION_RANGE_HEADER.length();
    private static final int RESPONSE_TIME_HEADER_LENGTH = RESPONSE_TIME_HEADER.length();
    private static final int RUN_TIME_HEADER_LENGTH = RUN_TIME_HEADER.length();
    private static final int WAIT_TIME_HEADER_LENGTH = WAIT_TIME_HEADER.length();
    private static final int TURNAROUND_TIME_HEADER_LENGTH = TURNAROUND_TIME_HEADER.length();
    private static final int NUMBER_OF_PREEMPTION_HEADER_LENGTH = NUMBER_OF_PREEMPTION_HEADER.length();

    private static final TextTable.Column[] SCHEDULING_METRICS_COLUMNS = new TextTable.Column[]
            {
                    new TextTable.Column(PARTITION_RANGE_HEADER, PARTITION_ID_HEADER_LENGTH),
                    new TextTable.Column(RESPONSE_TIME_HEADER, RESPONSE_TIME_HEADER_LENGTH),
                    new TextTable.Column(RUN_TIME_HEADER, RUN_TIME_HEADER_LENGTH),
                    new TextTable.Column(WAIT_TIME_HEADER, WAIT_TIME_HEADER_LENGTH),
                    new TextTable.Column(TURNAROUND_TIME_HEADER, TURNAROUND_TIME_HEADER_LENGTH),
                    new TextTable.Column(NUMBER_OF_PREEMPTION_HEADER, NUMBER_OF_PREEMPTION_HEADER_LENGTH),
            };

    private static final TextTable SCHEDULING_METRICS_TABLE = new TextTable(Arrays.asList(SCHEDULING_METRICS_COLUMNS));

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

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
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
                QueryMetricsTextWriter.RETRIEVED_DOCUMENT_COUNT, retrievedDocumentCount, 0);
    }

    @Override
    protected void writeRetrievedDocumentSize(long retrievedDocumentSize) {
        QueryMetricsTextWriter.appendBytesToStringBuilder(stringBuilder, QueryMetricsTextWriter.RETRIEVED_DOCUMENT_SIZE
                , retrievedDocumentSize, 0);
    }

    @Override
    protected void writeOutputDocumentCount(long outputDocumentCount) {
        QueryMetricsTextWriter.appendCountToStringBuilder(stringBuilder, QueryMetricsTextWriter.OUTPUT_DOCUMENT_COUNT,
                outputDocumentCount, 0);
    }

    @Override
    protected void writeOutputDocumentSize(long outputDocumentSize) {
        QueryMetricsTextWriter.appendBytesToStringBuilder(stringBuilder, QueryMetricsTextWriter.OUTPUT_DOCUMENT_SIZE,
                outputDocumentSize, 0);
    }

    @Override
    protected void writeIndexHitRatio(double indexHitRatio) {
        QueryMetricsTextWriter.appendPercentageToStringBuilder(stringBuilder, QueryMetricsTextWriter.INDEX_UTILIZATION_TEXT
                , indexHitRatio, 0);
    }

    @Override
    protected void writeTotalQueryExecutionTime(Duration totalQueryExecutionTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.TOTAL_QUERY_EXECUTION_TIME, durationToMilliseconds(totalQueryExecutionTime), 0);
    }

    @Override
    protected void writeBeforeQueryPreparationTimes() {
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.QUERY_PREPARATION_TIMES, 1);
    }

    @Override
    protected void writeQueryCompilationTime(Duration queryCompilationTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.QUERY_COMPILATION_TIME, durationToMilliseconds(queryCompilationTime), 2);
    }

    @Override
    protected void writeLogicalPlanBuildTime(Duration logicalPlanBuildTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.LOGICAL_PLAN_BUILD_TIME, durationToMilliseconds(logicalPlanBuildTime), 2);
    }

    @Override
    protected void writePhysicalPlanBuildTime(Duration physicalPlanBuildTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.PHYSICAL_PLAN_BUILD_TIME, durationToMilliseconds(physicalPlanBuildTime), 2);
    }

    @Override
    protected void writeQueryOptimizationTime(Duration queryOptimizationTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.QUERY_OPTIMIZATION_TIME, durationToMilliseconds(queryOptimizationTime), 2);
    }

    @Override
    protected void writeAfterQueryPreparationTimes() {
        // Do Nothing
    }

    @Override
    protected void writeIndexLookupTime(Duration indexLookupTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.INDEX_LOOKUP_TIME, durationToMilliseconds(indexLookupTime), 1);
    }

    @Override
    protected void writeDocumentLoadTime(Duration documentLoadTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.DOCUMENT_LOAD_TIME, durationToMilliseconds(documentLoadTime), 1);
    }

    @Override
    protected void writeVMExecutionTime(Duration vMExecutionTime) {
        // Do Nothing
    }

    @Override
    protected void writeBeforeRuntimeExecutionTimes() {
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.RUNTIME_EXECUTION_TIMES, 1);
    }

    @Override
    protected void writeQueryEngineExecutionTime(Duration queryEngineExecutionTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.QUERY_ENGINE_TIMES, durationToMilliseconds(queryEngineExecutionTime), 2);
    }

    @Override
    protected void writeSystemFunctionExecutionTime(Duration systemFunctionExecutionTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.SYSTEM_FUNCTION_EXECUTION_TIME, durationToMilliseconds(systemFunctionExecutionTime)
                , 2);
    }

    @Override
    protected void writeUserDefinedFunctionExecutionTime(Duration userDefinedFunctionExecutionTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.USER_DEFINED_FUNCTION_EXECUTION_TIME,
                durationToMilliseconds(userDefinedFunctionExecutionTime), 2);
    }

    @Override
    protected void writeAfterRuntimeExecutionTimes() {
        // Do Nothing
    }

    @Override
    protected void writeDocumentWriteTime(Duration documentWriteTime) {
        QueryMetricsTextWriter.appendMillisecondsToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.DOCUMENT_WRITE_TIME, durationToMilliseconds(documentWriteTime), 1);
    }

    @Override
    protected void writeBeforeClientSideMetrics() {
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder,
                QueryMetricsTextWriter.CLIENT_SIDE_METRICS, 0);
    }

    @Override
    protected void writeRetries(long retries) {
        QueryMetricsTextWriter.appendCountToStringBuilder(stringBuilder, QueryMetricsTextWriter.RETRIES, retries, 1);
    }

    @Override
    protected void writeRequestCharge(double requestCharge) {
        QueryMetricsTextWriter.appendRUToStringBuilder(stringBuilder, QueryMetricsTextWriter.REQUEST_CHARGE,
                requestCharge, 1);
    }

    @Override
    protected void writeBeforePartitionExecutionTimeline() {
        QueryMetricsTextWriter.appendNewlineToStringBuilder(stringBuilder);

        // Building the table for fetch execution ranges
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, QueryMetricsTextWriter.FETCH_EXECUTION_RANGES
                , 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, PARTITION_EXECUTION_TIMELINE_TABLE.getTopLine(), 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, PARTITION_EXECUTION_TIMELINE_TABLE.getHeader(), 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, PARTITION_EXECUTION_TIMELINE_TABLE.getMiddleLine(), 1);
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
                PARTITION_EXECUTION_TIMELINE_TABLE.getRow(Arrays.asList(
                        this.lastFetchPartitionId,
                        this.lastActivityId,
                        DATE_TIME_FORMATTER.format(this.lastStartTime),
                        DATE_TIME_FORMATTER.format(this.lastEndTime),
                        nanosToMilliSeconds(this.lastEndTime.minusNanos(lastStartTime.getNano()).getNano()),
                        this.lastFetchDocumentCount,
                        this.lastFetchRetryCount)),
                1);
    }

    @Override
    protected void writeAfterPartitionExecutionTimeline() {
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, PARTITION_EXECUTION_TIMELINE_TABLE.getBottomLine(),
                1);
    }

    @Override
    protected void writeBeforeSchedulingMetrics() {
        QueryMetricsTextWriter.appendNewlineToStringBuilder(stringBuilder);

        // Building the table for scheduling metrics
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, QueryMetricsTextWriter.SCHEDULING_METRICS, 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, SCHEDULING_METRICS_TABLE.getTopLine(), 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, SCHEDULING_METRICS_TABLE.getHeader(), 1);
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, SCHEDULING_METRICS_TABLE.getMiddleLine(), 1);
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
                SCHEDULING_METRICS_TABLE.getRow(Arrays.asList(
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
        QueryMetricsTextWriter.appendHeaderToStringBuilder(stringBuilder, SCHEDULING_METRICS_TABLE.getBottomLine(), 1);
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
