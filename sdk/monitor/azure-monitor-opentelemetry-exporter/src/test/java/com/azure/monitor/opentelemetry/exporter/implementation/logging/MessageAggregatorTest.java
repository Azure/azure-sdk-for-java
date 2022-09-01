/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.logging;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.INGESTION_ERROR;
import static org.assertj.core.api.Assertions.assertThat;

class MessageAggregatorTest {
  private static OperationLogger networkExceptionStats;

  @BeforeAll
  static void setUp() {
    // one-time initialization code
    networkExceptionStats = new OperationLogger(MessageAggregatorTest.class, "intro", 1);
  }

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
        .contains(
            "intro: Test Message (future warnings will be aggregated and logged once every 0 minutes)");
    assertThat(logCaptor.getWarnLogs().get(1))
        .contains(
            "In the last 0 minutes, the following operation has failed 3 times (out of 4): intro:\n"
                + " * Test Message2 (2 times)\n"
                + " * Test Message3 (1 times)");
  }
}
