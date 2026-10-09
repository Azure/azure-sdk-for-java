// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.IdStrategy;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.ErrantRecordReporter;
import org.apache.kafka.connect.sink.SinkRecord;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SinkRecordTransformerTest {
    private static final int TIMEOUT = 60000;

    /**
     * Creates a SinkRecord with a Map value containing the given fields.
     */
    private SinkRecord createMapRecord(String topic, int partition, long offset, Map<String, Object> value) {
        return new SinkRecord(topic, partition, null, "key-" + offset, null, value, offset);
    }

    /**
     * Creates an IdStrategy that fails (throws ConnectException) when generating an ID
     * for records whose value map contains a field "fail" set to true,
     * and returns a valid ID otherwise.
     */
    private IdStrategy createSelectivelyFailingIdStrategy() {
        IdStrategy idStrategy = Mockito.mock(IdStrategy.class);
        when(idStrategy.generateId(any(SinkRecord.class))).thenAnswer(invocation -> {
            SinkRecord record = invocation.getArgument(0);
            Object value = record.value();
            if (value instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) value;
                if (Boolean.TRUE.equals(map.get("fail"))) {
                    throw new ConnectException("Cannot generate ID: missing required field");
                }
            }
            return "generated-id-" + record.kafkaOffset();
        });
        return idStrategy;
    }

    // ============================================================
    // T1: Mixed batch with reporter + tolerance ALL — bad record goes to DLQ, valid records in output
    // ============================================================
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void mixedBatchWithReporterToleranceAll_badRecordReportedValidRecordsInOutput() throws Exception {
        // Arrange
        IdStrategy idStrategy = createSelectivelyFailingIdStrategy();
        ErrantRecordReporter reporter = Mockito.mock(ErrantRecordReporter.class);
        Future<Void> mockFuture = Mockito.mock(Future.class);
        when(reporter.report(any(SinkRecord.class), any(Throwable.class))).thenReturn(mockFuture);

        SinkRecordTransformer transformer = new SinkRecordTransformer(idStrategy, reporter, ToleranceOnErrorLevel.ALL);

        Map<String, Object> goodValue1 = new HashMap<>();
        goodValue1.put("data", "hello");

        Map<String, Object> badValue = new HashMap<>();
        badValue.put("fail", true);

        Map<String, Object> goodValue2 = new HashMap<>();
        goodValue2.put("data", "world");

        List<SinkRecord> batch = Arrays.asList(
            createMapRecord("topic1", 0, 0L, goodValue1),
            createMapRecord("topic1", 0, 1L, badValue),
            createMapRecord("topic1", 0, 2L, goodValue2)
        );

        // Act
        List<SinkRecord> result = transformer.transform("container1", batch);

        // Assert — only 2 valid records in output
        assertThat(result.size()).isEqualTo(2);
        assertThat(((Map<String, Object>) result.get(0).value()).get("id")).isEqualTo("generated-id-0");
        assertThat(((Map<String, Object>) result.get(1).value()).get("id")).isEqualTo("generated-id-2");

        // Assert — reporter called exactly once with the bad record
        ArgumentCaptor<SinkRecord> recordCaptor = ArgumentCaptor.forClass(SinkRecord.class);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(reporter, times(1)).report(recordCaptor.capture(), errorCaptor.capture());
        assertThat(recordCaptor.getValue().kafkaOffset()).isEqualTo(1L);
        assertThat(errorCaptor.getValue()).isInstanceOf(ConnectException.class);
    }

    // ============================================================
    // T2: Mixed batch with tolerance ALL, no reporter — bad record skipped
    // ============================================================
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void mixedBatchToleranceAll_noReporter_badRecordSkipped() throws Exception {
        // Arrange
        IdStrategy idStrategy = createSelectivelyFailingIdStrategy();
        SinkRecordTransformer transformer = new SinkRecordTransformer(idStrategy, null, ToleranceOnErrorLevel.ALL);

        Map<String, Object> goodValue = new HashMap<>();
        goodValue.put("data", "hello");

        Map<String, Object> badValue = new HashMap<>();
        badValue.put("fail", true);

        List<SinkRecord> batch = Arrays.asList(
            createMapRecord("topicA", 1, 10L, goodValue),
            createMapRecord("topicA", 1, 11L, badValue)
        );

        // Act — should NOT throw
        List<SinkRecord> result = transformer.transform("container2", batch);

        // Assert — only 1 valid record
        assertThat(result.size()).isEqualTo(1);
        assertThat(((Map<String, Object>) result.get(0).value()).get("id")).isEqualTo("generated-id-10");
    }

    // ============================================================
    // T3: Mixed batch with tolerance NONE, no reporter — exception thrown (fail-fast)
    // ============================================================
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void mixedBatchToleranceNone_noReporter_exceptionThrown() throws Exception {
        // Arrange
        IdStrategy idStrategy = createSelectivelyFailingIdStrategy();
        SinkRecordTransformer transformer = new SinkRecordTransformer(idStrategy, null, ToleranceOnErrorLevel.NONE);

        Map<String, Object> goodValue = new HashMap<>();
        goodValue.put("data", "hello");

        Map<String, Object> badValue = new HashMap<>();
        badValue.put("fail", true);

        List<SinkRecord> batch = Arrays.asList(
            createMapRecord("topicB", 2, 20L, goodValue),
            createMapRecord("topicB", 2, 21L, badValue)
        );

        // Act
        Throwable thrown = catchThrowable(() -> transformer.transform("container3", batch));

        // Assert — exception is thrown (fail-fast preserved)
        assertThat(thrown).isInstanceOf(ConnectException.class);
        assertThat(thrown.getMessage()).contains("Cannot generate ID");
    }

    // ============================================================
    // T4: All records valid — no errors, all records in output (regression)
    // ============================================================
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void allValidRecords_allInOutput() throws Exception {
        // Arrange
        IdStrategy idStrategy = createSelectivelyFailingIdStrategy();
        ErrantRecordReporter reporter = Mockito.mock(ErrantRecordReporter.class);
        SinkRecordTransformer transformer = new SinkRecordTransformer(idStrategy, reporter, ToleranceOnErrorLevel.NONE);

        Map<String, Object> value1 = new HashMap<>();
        value1.put("data", "a");
        Map<String, Object> value2 = new HashMap<>();
        value2.put("data", "b");
        Map<String, Object> value3 = new HashMap<>();
        value3.put("data", "c");

        List<SinkRecord> batch = Arrays.asList(
            createMapRecord("topicC", 0, 100L, value1),
            createMapRecord("topicC", 0, 101L, value2),
            createMapRecord("topicC", 0, 102L, value3)
        );

        // Act
        List<SinkRecord> result = transformer.transform("container4", batch);

        // Assert — all 3 records in output
        assertThat(result.size()).isEqualTo(3);
        for (int i = 0; i < 3; i++) {
            assertThat(((Map<String, Object>) result.get(i).value()).get("id"))
                .isEqualTo("generated-id-" + (100 + i));
        }

        // Assert — reporter never called
        verify(reporter, never()).report(any(), any());
    }

    // ============================================================
    // T5: All records bad with reporter + tolerance ALL — all reported to DLQ, empty output
    // ============================================================
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void allBadRecordsWithReporterToleranceAll_allReportedEmptyOutput() throws Exception {
        // Arrange
        IdStrategy idStrategy = createSelectivelyFailingIdStrategy();
        ErrantRecordReporter reporter = Mockito.mock(ErrantRecordReporter.class);
        Future<Void> mockFuture = Mockito.mock(Future.class);
        when(reporter.report(any(SinkRecord.class), any(Throwable.class))).thenReturn(mockFuture);

        SinkRecordTransformer transformer = new SinkRecordTransformer(idStrategy, reporter, ToleranceOnErrorLevel.ALL);

        Map<String, Object> bad1 = new HashMap<>();
        bad1.put("fail", true);
        Map<String, Object> bad2 = new HashMap<>();
        bad2.put("fail", true);
        Map<String, Object> bad3 = new HashMap<>();
        bad3.put("fail", true);

        List<SinkRecord> batch = Arrays.asList(
            createMapRecord("topicD", 0, 50L, bad1),
            createMapRecord("topicD", 0, 51L, bad2),
            createMapRecord("topicD", 0, 52L, bad3)
        );

        // Act
        List<SinkRecord> result = transformer.transform("container5", batch);

        // Assert — empty output
        assertThat(result.size()).isEqualTo(0);

        // Assert — reporter called 3 times
        verify(reporter, times(3)).report(any(SinkRecord.class), any(ConnectException.class));
    }

    // ============================================================
    // T6: Reporter itself throws — with tolerance NONE, original exception rethrown
    // ============================================================
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void reporterThrows_toleranceNone_originalExceptionRethrown() throws Exception {
        // Arrange
        IdStrategy idStrategy = createSelectivelyFailingIdStrategy();
        ErrantRecordReporter reporter = Mockito.mock(ErrantRecordReporter.class);
        when(reporter.report(any(SinkRecord.class), any(Throwable.class)))
            .thenThrow(new ConnectException("DLQ write failed"));

        SinkRecordTransformer transformer = new SinkRecordTransformer(idStrategy, reporter, ToleranceOnErrorLevel.NONE);

        Map<String, Object> badValue = new HashMap<>();
        badValue.put("fail", true);
        Map<String, Object> goodValue = new HashMap<>();
        goodValue.put("data", "after-bad");

        List<SinkRecord> batch = Arrays.asList(
            createMapRecord("topicF", 0, 0L, badValue),
            createMapRecord("topicF", 0, 1L, goodValue)
        );

        // Act
        Throwable thrown = catchThrowable(() -> transformer.transform("container7", batch));

        // Assert — original transform exception, NOT the DLQ exception
        assertThat(thrown).isInstanceOf(ConnectException.class);
        assertThat(thrown.getMessage()).contains("Cannot generate ID");
    }

    // ============================================================
    // T7: Reporter itself throws — with tolerance ALL, record skipped and processing continues
    // ============================================================
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void reporterThrows_toleranceAll_recordSkippedProcessingContinues() throws Exception {
        // Arrange
        IdStrategy idStrategy = createSelectivelyFailingIdStrategy();
        ErrantRecordReporter reporter = Mockito.mock(ErrantRecordReporter.class);
        when(reporter.report(any(SinkRecord.class), any(Throwable.class)))
            .thenThrow(new ConnectException("DLQ write failed"));

        SinkRecordTransformer transformer = new SinkRecordTransformer(idStrategy, reporter, ToleranceOnErrorLevel.ALL);

        Map<String, Object> badValue = new HashMap<>();
        badValue.put("fail", true);
        Map<String, Object> goodValue = new HashMap<>();
        goodValue.put("data", "survives");

        List<SinkRecord> batch = Arrays.asList(
            createMapRecord("topicG", 0, 0L, badValue),
            createMapRecord("topicG", 0, 1L, goodValue)
        );

        // Act — should NOT throw
        List<SinkRecord> result = transformer.transform("container8", batch);

        // Assert — only the good record survives
        assertThat(result.size()).isEqualTo(1);
        assertThat(((Map<String, Object>) result.get(0).value()).get("id")).isEqualTo("generated-id-1");
    }

    // ============================================================
    // T8: Tolerance NONE with reporter — record reported to DLQ AND exception thrown
    //     (consistent with writer-level pattern: DLQ is side-effect, tolerance controls flow)
    // ============================================================
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void toleranceNoneWithReporter_reportedToDlqAndExceptionThrown() throws Exception {
        // Arrange
        IdStrategy idStrategy = createSelectivelyFailingIdStrategy();
        ErrantRecordReporter reporter = Mockito.mock(ErrantRecordReporter.class);
        Future<Void> mockFuture = Mockito.mock(Future.class);
        when(reporter.report(any(SinkRecord.class), any(Throwable.class))).thenReturn(mockFuture);

        // Tolerance is NONE — task should fail even though reporter is available
        SinkRecordTransformer transformer = new SinkRecordTransformer(idStrategy, reporter, ToleranceOnErrorLevel.NONE);

        Map<String, Object> badValue = new HashMap<>();
        badValue.put("fail", true);

        List<SinkRecord> batch = Arrays.asList(
            createMapRecord("topicH", 0, 0L, badValue)
        );

        // Act
        Throwable thrown = catchThrowable(() -> transformer.transform("container9", batch));

        // Assert — exception IS thrown (tolerance NONE means fail)
        assertThat(thrown).isInstanceOf(ConnectException.class);
        assertThat(thrown.getMessage()).contains("Cannot generate ID");

        // Assert — reporter WAS called (DLQ is side-effect for observability)
        verify(reporter, times(1)).report(any(SinkRecord.class), any(ConnectException.class));
    }

    // ============================================================
    // T9: Value conversion failure (Struct → JSON) with reporter + tolerance ALL
    //     — exercises the broader catch scope beyond just ID generation
    // ============================================================
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    @SuppressWarnings("unchecked")
    public void structConversionFailure_toleranceAll_reportedToDlqAndSkipped() throws Exception {
        // Arrange — ID strategy that always succeeds; the failure comes from Struct conversion
        IdStrategy idStrategy = Mockito.mock(IdStrategy.class);
        when(idStrategy.generateId(any(SinkRecord.class))).thenReturn("any-id");

        ErrantRecordReporter reporter = Mockito.mock(ErrantRecordReporter.class);
        Future<Void> mockFuture = Mockito.mock(Future.class);
        when(reporter.report(any(SinkRecord.class), any(Throwable.class))).thenReturn(mockFuture);

        SinkRecordTransformer transformer = new SinkRecordTransformer(idStrategy, reporter, ToleranceOnErrorLevel.ALL);

        // A Struct whose schema has a field, but accessing that field throws
        // (simulates malformed Struct data that fails during StructToJsonMap.toJsonMap())
        Schema schema = SchemaBuilder.struct().field("data", Schema.STRING_SCHEMA).build();
        Struct malformedStruct = Mockito.mock(Struct.class);
        when(malformedStruct.schema()).thenReturn(schema);
        when(malformedStruct.getString("data")).thenThrow(new org.apache.kafka.connect.errors.DataException("Schema mismatch"));

        SinkRecord badStructRecord = new SinkRecord("topicI", 0, null, "key-bad", schema, malformedStruct, 0L);

        Map<String, Object> goodValue = new HashMap<>();
        goodValue.put("data", "ok");
        SinkRecord goodRecord = createMapRecord("topicI", 0, 1L, goodValue);

        List<SinkRecord> batch = Arrays.asList(badStructRecord, goodRecord);

        // Act — should NOT throw
        List<SinkRecord> result = transformer.transform("container10", batch);

        // Assert — only the good Map record survives
        assertThat(result.size()).isEqualTo(1);
        assertThat(((Map<String, Object>) result.get(0).value()).get("id")).isEqualTo("any-id");

        // Assert — reporter called once for the malformed Struct record
        ArgumentCaptor<SinkRecord> recordCaptor = ArgumentCaptor.forClass(SinkRecord.class);
        ArgumentCaptor<Throwable> errorCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(reporter, times(1)).report(recordCaptor.capture(), errorCaptor.capture());
        assertThat(recordCaptor.getValue().kafkaOffset()).isEqualTo(0L);
        assertThat(errorCaptor.getValue()).isInstanceOf(org.apache.kafka.connect.errors.DataException.class);
    }

    // ============================================================
    // T10: Value conversion failure with tolerance NONE — exception thrown (fail-fast)
    // ============================================================
    @Test(groups = {"unit"}, timeOut = TIMEOUT)
    public void structConversionFailure_toleranceNone_exceptionThrown() throws Exception {
        // Arrange
        IdStrategy idStrategy = Mockito.mock(IdStrategy.class);
        when(idStrategy.generateId(any(SinkRecord.class))).thenReturn("any-id");

        SinkRecordTransformer transformer = new SinkRecordTransformer(idStrategy, null, ToleranceOnErrorLevel.NONE);

        Schema schema = SchemaBuilder.struct().field("data", Schema.STRING_SCHEMA).build();
        Struct malformedStruct = Mockito.mock(Struct.class);
        when(malformedStruct.schema()).thenReturn(schema);
        when(malformedStruct.getString("data")).thenThrow(new org.apache.kafka.connect.errors.DataException("Schema mismatch"));

        SinkRecord badStructRecord = new SinkRecord("topicJ", 0, null, "key-bad", schema, malformedStruct, 0L);

        List<SinkRecord> batch = Collections.singletonList(badStructRecord);

        // Act
        Throwable thrown = catchThrowable(() -> transformer.transform("container11", batch));

        // Assert — DataException is thrown (fail-fast preserved)
        assertThat(thrown).isInstanceOf(org.apache.kafka.connect.errors.DataException.class);
        assertThat(thrown.getMessage()).contains("Schema mismatch");
    }
}
