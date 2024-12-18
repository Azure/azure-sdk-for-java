// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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

    private final ScheduledExecutorService scheduledExecutor
        = Executors.newSingleThreadScheduledExecutor(ThreadPoolUtils.createDaemonThreadFactory(LocalFilePurger.class));

    LocalFilePurger(File folder, boolean suppressWarnings) { // used to suppress warnings from statsbeat
        this(folder, TimeUnit.DAYS.toSeconds(2), TimeUnit.DAYS.toSeconds(1), suppressWarnings);
    }

    // visible for testing
    LocalFilePurger(File folder, long expiredIntervalSeconds, long purgeIntervalSeconds, boolean suppressWarnings) { // used to suppress warnings from statsbeat
        this.folder = folder;
        this.expiredIntervalSeconds = expiredIntervalSeconds;

        operationLogger = suppressWarnings
            ? OperationLogger.NOOP
            : new OperationLogger(LocalFilePurger.class, "Purging expired telemetry from disk");

        scheduledExecutor.scheduleWithFixedDelay(this, Math.min(purgeIntervalSeconds, 60), purgeIntervalSeconds,
            SECONDS);
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
                    operationLogger.recordFailure("Unable to delete file: " + file.getAbsolutePath(),
                        DISK_PERSISTENCE_PURGE_ERROR);
                } else {
                    operationLogger.recordSuccess();
                }
            }
        }
    }
}
