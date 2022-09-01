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

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.slf4j.MDC;

// JAVA reserves message id for App Service Diagnostics Logs from 2000 - 2999
// Reserve msgId 2100 - 2200 for Azure Monitor Exporter
public enum AzureMonitorMsgId {
  QUICK_PULSE_PING_ERROR("2100"),
  QUICK_PULSE_SEND_ERROR("2101"),
  DISK_PERSISTENCE_LOADER_ERROR("2102"),
  DISK_PERSISTENCE_WRITER_ERROR("2103"),
  DISK_PERSISTENCE_PURGE_ERROR("2104"),
  INGESTION_ERROR("2105"),
  TELEMETRY_ITEM_EXPORTER_ERROR("2106"),
  TELEMETRY_TRUNCATION_ERROR("2107"),
  CPU_METRIC_ERROR("2108"),
  HOSTNAME_ERROR("2109"),
  EXPORTER_MAPPING_ERROR("2110"),
  BATCH_ITEM_PROCESSOR_ERROR("2111"),
  APP_ID_ERROR("2112"),
  FRIENDLY_NETWORK_ERROR("2113");

  private final String value;

  AzureMonitorMsgId(String value) {
    this.value = value;
  }

  public MDC.MDCCloseable makeActive() {
    return MDC.putCloseable("msgId", value);
  }
}
