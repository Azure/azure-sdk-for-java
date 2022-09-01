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

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import com.azure.monitor.opentelemetry.exporter.implementation.logging.OperationLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.ThreadPoolUtils;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.DISK_PERSISTENCE_PURGE_ERROR;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Purge files that are older than 48 hours in both 'telemetry' and 'statsbeat' folders. Purge is
 * run every 24 hours.
 */
class LocalFilePurger implements Runnable {

  private final File folder;
  private final long expiredIntervalSeconds;

  private final OperationLogger operationLogger;

  private final ScheduledExecutorService scheduledExecutor =
      Executors.newSingleThreadScheduledExecutor(
          ThreadPoolUtils.createDaemonThreadFactory(LocalFilePurger.class));

  LocalFilePurger(
      File folder, boolean suppressWarnings) { // used to suppress warnings from statsbeat
    this(folder, TimeUnit.DAYS.toSeconds(2), TimeUnit.DAYS.toSeconds(1), suppressWarnings);
  }

  // visible for testing
  LocalFilePurger(
      File folder,
      long expiredIntervalSeconds,
      long purgeIntervalSeconds,
      boolean suppressWarnings) { // used to suppress warnings from statsbeat
    this.folder = folder;
    this.expiredIntervalSeconds = expiredIntervalSeconds;

    operationLogger =
        suppressWarnings
            ? OperationLogger.NOOP
            : new OperationLogger(LocalFilePurger.class, "Purging expired telemetry from disk");

    scheduledExecutor.scheduleWithFixedDelay(
        this, Math.min(purgeIntervalSeconds, 60), purgeIntervalSeconds, SECONDS);
  }

  void shutdown() {
    scheduledExecutor.shutdown();
  }

  @Override
  public void run() {
    purgedExpiredFiles(folder);
  }

  private void purgedExpiredFiles(File folder) {
    for (File file : FileUtil.listTrnFiles(folder)) {
      if (LocalFileCache.isExpired(file, expiredIntervalSeconds)) {
        if (!FileUtil.deleteFileWithRetries(file)) {
          operationLogger.recordFailure(
              "Unable to delete file: " + file.getAbsolutePath(), DISK_PERSISTENCE_PURGE_ERROR);
        } else {
          operationLogger.recordSuccess();
        }
      }
    }
  }
}
