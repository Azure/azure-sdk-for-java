// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import com.azure.core.util.logging.ClientLogger;

import java.io.File;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class LocalFileCache {

    private static final ClientLogger logger = new ClientLogger(LocalFileCache.class);

    /**
     * Track a list of active filenames persisted on disk. FIFO (First-In-First-Out) read will avoid
     * an additional sorting at every read.
     *
     * <p>There isn't a unique way to identify each java app. C# uses "User@processName" to identify
     * each app, but Java can't rely on process name since it's a system property that can be
     * customized via the command line.
     */
    private final Queue<File> persistedFilesCache = new ConcurrentLinkedDeque<>();

    LocalFileCache(File folder) {
        persistedFilesCache.addAll(loadPersistedFiles(folder));
    }

    // Track the newly persisted filename to the concurrent hashmap.
    void addPersistedFile(File file) {
        persistedFilesCache.add(file);
    }

    File poll() {
        return persistedFilesCache.poll();
    }

    // only used by tests
    Queue<File> getPersistedFilesCache() {
        return persistedFilesCache;
    }

    // load existing files that are not older than 48 hours
    // this will avoid data loss in the case of app crashes and restarts.
    private static List<File> loadPersistedFiles(File folder) {
        return FileUtil.listTrnFiles(folder)
            .stream()
            .filter(file -> !isExpired(file, TimeUnit.DAYS.toSeconds(2))) // filter before sorting
            .sorted(Comparator.comparing(File::lastModified))
            .collect(Collectors.toList());
    }

    // files that are older than expiredIntervalSeconds (default 48 hours) are expired
    static boolean isExpired(File file, long expiredIntervalSeconds) {
        String name = file.getName();
        int index = name.indexOf('-');
        if (index == -1) {
            logger.verbose("unexpected .trn file name: {}", name);
            return true;
        }
        long timestamp;
        try {
            timestamp = Long.parseLong(name.substring(0, index));
        } catch (NumberFormatException e) {
            logger.verbose("unexpected .trn file name: {}", name);
            return true;
        }
        Date expirationDate = new Date(System.currentTimeMillis() - 1000 * expiredIntervalSeconds);
        Date fileDate = new Date(timestamp);
        return fileDate.before(expirationDate);
    }
}
