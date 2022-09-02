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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.DISK_PERSISTENCE_LOADER_ERROR;

/**
 * This class manages loading a list of {@link ByteBuffer} from the disk.
 */
class LocalFileLoader {

    private static final String TEMPORARY_FILE_EXTENSION = ".tmp";

    private final LocalFileCache localFileCache;
    private final File telemetryFolder;
    private final LocalStorageStats stats;

    private final OperationLogger operationLogger;
    private final OperationLogger updateOperationLogger;

    LocalFileLoader(
        LocalFileCache localFileCache,
        File telemetryFolder,
        LocalStorageStats stats,
        boolean suppressWarnings) { // used to suppress warnings from statsbeat
        this.localFileCache = localFileCache;
        this.telemetryFolder = telemetryFolder;
        this.stats = stats;

        operationLogger =
            suppressWarnings
                ? OperationLogger.NOOP
                : new OperationLogger(LocalFileLoader.class, "Loading telemetry from disk");

        updateOperationLogger =
            suppressWarnings
                ? OperationLogger.NOOP
                : new OperationLogger(LocalFileLoader.class, "Updating local telemetry on disk");
    }

    // Load ByteBuffer from persisted files on disk in FIFO order.
    @Nullable
    @SuppressFBWarnings(
        value = "SECPTI", // Potential Path Traversal
        justification =
            "The constructed file path cannot be controlled by an end user of the instrumented application")
    PersistedFile loadTelemetriesFromDisk() {
        File fileToBeLoaded = localFileCache.poll();
        if (fileToBeLoaded == null) {
            return null;
        }

        // when reading a file from the disk, loader renames the source file to "*.tmp" to prevent other
        // threads from processing the same file over and over again. this will prevent same data gets
        // sent to Application Insights more than once. after reading raw bytes from the .tmp file,
        // loader will delete the temp file when http
        // response confirms it is sent successfully; otherwise, temp file will get renamed back to the
        // source file extension.
        File tempFile;
        try {
            if (!fileToBeLoaded.exists()) {
                return null;
            }

            tempFile =
                new File(
                    telemetryFolder, FileUtil.getBaseName(fileToBeLoaded) + TEMPORARY_FILE_EXTENSION);
            FileUtil.moveFile(fileToBeLoaded, tempFile);
        } catch (IOException e) {
            operationLogger.recordFailure(
                "Error renaming file: " + fileToBeLoaded.getAbsolutePath(),
                DISK_PERSISTENCE_LOADER_ERROR);
            stats.incrementReadFailureCount();
            return null;
        }

        if (tempFile.length() <= 36) {
            deleteFile(tempFile);
            return null;
        }

        String connectionString;
        byte[] telemetryBytes;

        try (DataInputStream dataInputStream =
                 new DataInputStream(Files.newInputStream(tempFile.toPath()))) {

            int version = dataInputStream.readInt();
            if (version != 1) {
                // probably old format where ikey chars were written first
                // note: ikey character int values would be minimum 48 (ascii value for '0')

                // need to close FileInputStream before delete
                deleteFile(tempFile);
                return null;
            }

            connectionString = dataInputStream.readUTF();

            int numBytes = dataInputStream.readInt();
            telemetryBytes = new byte[numBytes];
            dataInputStream.readFully(telemetryBytes);

        } catch (IOException e) {
            operationLogger.recordFailure(
                "Error reading file: " + tempFile.getAbsolutePath(), e, DISK_PERSISTENCE_LOADER_ERROR);
            stats.incrementReadFailureCount();
            return null;
        }

        operationLogger.recordSuccess();
        return new PersistedFile(tempFile, connectionString, ByteBuffer.wrap(telemetryBytes));
    }

    private void deleteFile(File tempFile) {
        if (!FileUtil.deleteFileWithRetries(tempFile)) {
            operationLogger.recordFailure(
                "Unable to delete file: " + tempFile.getAbsolutePath(), DISK_PERSISTENCE_LOADER_ERROR);
        }
    }

    // either delete it permanently on success or add it back to cache to be processed again later on
    // failure
    @SuppressFBWarnings(
        value = "SECPTI", // Potential Path Traversal
        justification =
            "The constructed file path cannot be controlled by an end user of the instrumented application")
    void updateProcessedFileStatus(boolean successOrNonRetryableError, File file) {
        if (!file.exists()) {
            // not sure why this would happen
            updateOperationLogger.recordFailure(
                "File no longer exists: " + file.getAbsolutePath(), DISK_PERSISTENCE_LOADER_ERROR);
            return;
        }
        if (successOrNonRetryableError) {
            // delete a file on the queue permanently when http response returns success.
            if (!FileUtil.deleteFileWithRetries(file)) {
                updateOperationLogger.recordFailure(
                    "Unable to delete file: " + file.getAbsolutePath(), DISK_PERSISTENCE_LOADER_ERROR);
            } else {
                updateOperationLogger.recordSuccess();
            }
        } else {
            // rename the temp file back to .trn source file extension
            File sourceFile = new File(telemetryFolder, FileUtil.getBaseName(file) + ".trn");
            try {
                FileUtil.moveFile(file, sourceFile);
            } catch (IOException e) {
                updateOperationLogger.recordFailure(
                    "Error renaming file: " + file.getAbsolutePath(), e, DISK_PERSISTENCE_LOADER_ERROR);
                return;
            }
            updateOperationLogger.recordSuccess();

            // add the source filename back to local file cache to be processed later.
            localFileCache.addPersistedFile(sourceFile);
        }
    }

    static class PersistedFile {
        final File file;
        final String connectionString;
        final ByteBuffer rawBytes;

        PersistedFile(File file, String connectionString, ByteBuffer byteBuffer) {
            if (connectionString == null) {
                throw new IllegalArgumentException("instrumentation key can not be null.");
            }

            this.file = file;
            this.connectionString = connectionString;
            this.rawBytes = byteBuffer;
        }
    }
}
