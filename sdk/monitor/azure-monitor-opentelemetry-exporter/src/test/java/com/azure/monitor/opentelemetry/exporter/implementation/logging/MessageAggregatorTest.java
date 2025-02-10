// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.logging;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.INGESTION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD) // disable parallel test run
class MessageAggregatorTest {

    private static OperationLogger networkExceptionStats;

    private static final String NEWLINE = System.getProperty("line.separator");

    @BeforeAll
    static void setUp() {
        // one-time initialization code
        networkExceptionStats = new OperationLogger(MessageAggregatorTest.class, "intro", 1);
    }

    @Disabled
    @Test
    void testWarnAndExceptionsAreLogged() throws InterruptedException {
        LogCaptor logCaptor = LogCaptor.forClass(MessageAggregatorTest.class);
        networkExceptionStats.recordSuccess();
        Exception ex = new IllegalArgumentException();
        networkExceptionStats.recordFailure("Test Message", ex, INGESTION_ERROR);
        networkExceptionStats.recordFailure("Test Message2", ex, INGESTION_ERROR);
        networkExceptionStats.recordFailure("Test Message2", ex, INGESTION_ERROR);
        networkExceptionStats.recordFailure("Test Message3", ex, INGESTION_ERROR);
        // wait for more than 1 second
        Thread.sleep(3000);

        assertThat(logCaptor.getWarnLogs()).hasSize(2);
        assertThat(logCaptor.getWarnLogs().get(0))
            .contains("intro: Test Message (future warnings will be aggregated and logged once every 0 minutes)");
        assertThat(logCaptor.getWarnLogs().get(1))
            .contains("In the last 0 minutes, the following operation has failed 3 times (out of 4): intro:" + NEWLINE
                + " * Test Message2 (2 times)" + NEWLINE + " * Test Message3 (1 times)");
    }
}
