// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import org.apache.kafka.connect.sink.ErrantRecordReporter;
import org.apache.kafka.connect.sink.SinkRecord;
import org.slf4j.Logger;

/**
 * Shared helper for DLQ (Dead Letter Queue) reporting.
 *
 * <p>Both {@link CosmosWriterBase} and {@link SinkRecordTransformer} need fire-and-forget
 * DLQ reporting that guards against reporter failures. This helper centralises that logic.
 */
final class DlqReportHelper {

    private DlqReportHelper() {
    }

    /**
     * Reports a failed record to the DLQ if a reporter is configured.
     *
     * <p>Per Kafka Connect best practices, DLQ reporting is a side-effect for observability —
     * reporter failures are swallowed so they do not mask the original processing error.
     *
     * @param reporter the errant record reporter, may be {@code null}
     * @param record the sink record that failed processing
     * @param error the original processing error
     * @param logger the caller's logger for error reporting
     */
    static void reportToDlqIfConfigured(
        ErrantRecordReporter reporter,
        SinkRecord record,
        Throwable error,
        Logger logger) {

        if (reporter == null) {
            return;
        }
        try {
            reporter.report(record, error);
        } catch (Exception reportException) {
            logger.error(
                "Failed to report errant record to DLQ for topic {}, partition {}, offset {}.",
                record.topic(),
                record.kafkaPartition(),
                record.kafkaOffset(),
                reportException);
        }
    }
}
