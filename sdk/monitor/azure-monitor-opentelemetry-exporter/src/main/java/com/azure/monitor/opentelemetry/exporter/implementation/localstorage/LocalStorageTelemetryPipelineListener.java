// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.DiagnosticTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ResponseError;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.AppInsightsByteBufferPool;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.ByteBufferOutputStream;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineRequest;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.StatusCode;
import io.opentelemetry.sdk.common.CompletableResultCode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class LocalStorageTelemetryPipelineListener implements TelemetryPipelineListener {

    private static final ClientLogger logger = new ClientLogger(LocalStorageTelemetryPipelineListener.class);

    private final LocalFileWriter localFileWriter;
    private final LocalFileSender localFileSender;
    private final LocalFilePurger localFilePurger;

    private final AtomicBoolean shutdown = new AtomicBoolean();

    // telemetryFolder must already exist and be writable
    public LocalStorageTelemetryPipelineListener(
        int diskPersistenceMaxSizeMb,
        File telemetryFolder,
        TelemetryPipeline pipeline,
        LocalStorageStats stats,
        boolean suppressWarnings) { // used to suppress warnings from statsbeat

        LocalFileCache localFileCache = new LocalFileCache(telemetryFolder);
        LocalFileLoader loader =
            new LocalFileLoader(localFileCache, telemetryFolder, stats, suppressWarnings);
        localFileWriter =
            new LocalFileWriter(
                diskPersistenceMaxSizeMb, localFileCache, telemetryFolder, stats, suppressWarnings);

        // send persisted telemetries from local disk every 30 seconds by default.
        // if diskPersistenceMaxSizeMb is greater than 50, it will get changed to 10 seconds.
        long intervalSeconds = diskPersistenceMaxSizeMb > 50 ? 10 : 30;
        localFileSender = new LocalFileSender(intervalSeconds, loader, pipeline, suppressWarnings);
        localFilePurger = new LocalFilePurger(telemetryFolder, suppressWarnings);
    }

    @Override
    public void onResponse(TelemetryPipelineRequest request, TelemetryPipelineResponse response) {
        int statusCode = response.getStatusCode();
        if (StatusCode.isRetryable(statusCode)) {
            localFileWriter.writeToDisk(
                request.getConnectionString(), request.getByteBuffers(), getOriginalErrorMessage(response));
        } else if (statusCode == 206) {
            processStatusCode206(request, response);
        }
    }

    private void processStatusCode206(TelemetryPipelineRequest request, TelemetryPipelineResponse response) {
        Set<ResponseError> errors = response.getErrors();
        errors.forEach(error -> logger.verbose("Error in telemetry: {}", error));
        if (!errors.isEmpty()) {
            List<ByteBuffer> originalByteBuffers = request.getByteBuffers();
            byte[] gzippedBytes = convertByteBufferListToByteArray(originalByteBuffers);
            byte[] ungzippedBytes = ungzip(gzippedBytes); // ungzip is needed in order to split by newline correctly
            List<byte[]> serializedTelemetryItemsByteArrayList = splitBytesByNewline(ungzippedBytes);
            List<byte[]> toBePersisted = new ArrayList<>();
            for (ResponseError error : errors) {
                if (StatusCode.isRetryable(error.getStatusCode())) {
                    toBePersisted.add(serializedTelemetryItemsByteArrayList.get(error.getIndex()));
                }
            }

            if (!toBePersisted.isEmpty()) {
                localFileWriter.writeToDisk(
                    request.getConnectionString(), gzip(toBePersisted), "Received partial response code 206");
            }
        }
    }

    // convert a list of byte buffers to a big byte array
    private static byte[] convertByteBufferListToByteArray(List<ByteBuffer> byteBuffers) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (ByteBuffer buffer : byteBuffers) {
            byte[] arr = new byte[buffer.remaining()];
            buffer.get(arr);
            try {
                baos.write(arr);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return baos.toByteArray();
    }

    // gzip and adding newline delimiter are required before persisting to the offline disk for handling 206 status code
    private static List<ByteBuffer> gzip(List<byte[]> byteArrayList) {
        try (ByteBufferOutputStream result = new ByteBufferOutputStream(new AppInsightsByteBufferPool())) {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(result);
            for (int i = 0; i < byteArrayList.size(); i++) {
                gzipOutputStream.write(byteArrayList.get(i));
                if (i < byteArrayList.size() - 1) {
                    gzipOutputStream.write('\n');
                }
            }
            gzipOutputStream.close();
            List<ByteBuffer> resultByteBuffers = result.getByteBuffers();
            for (ByteBuffer byteBuffer : resultByteBuffers) {
                byteBuffer.flip();
            }
            return result.getByteBuffers();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to encode list of ByteBuffers before persisting to the offline disk", e);
        }
    }

    // un-gzip TelemetryItems raw bytes back to original un-gzipped TelemetryItems raw bytes
    public static byte[] ungzip(byte[] rawBytes) {
        try (GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(rawBytes));
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] data = new byte[1024];
            int read;
            while ((read = in.read(data)) != -1) {
                baos.write(data, 0, read);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to decode byte[]", e);
        }
    }

    // split the byte array by newline character
    private static List<byte[]> splitBytesByNewline(byte[] inputBytes) {
        List<byte[]> lines = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < inputBytes.length; i++) {
            if (inputBytes[i] == '\n') {
                byte[] line = new byte[i - start];
                System.arraycopy(inputBytes, start, line, 0, i - start);
                lines.add(line);
                start = i + 1;
            }
        }
        // Add the last line (if any)
        if (start < inputBytes.length) {
            byte[] lastLine = new byte[inputBytes.length - start];
            System.arraycopy(inputBytes, start, lastLine, 0, inputBytes.length - start);
            lines.add(lastLine);
        }
        return lines;
    }

    @Override
    public void onException(
        TelemetryPipelineRequest request, String errorMessage, Throwable throwable) {
        localFileWriter.writeToDisk(request.getConnectionString(), request.getByteBuffers(), errorMessage);
    }

    @Override
    public CompletableResultCode shutdown() {
        // guarding against multiple shutdown calls because this can get called if statsbeat shuts down
        // early because it cannot reach breeze and later on real shut down (when running not as agent)
        if (!shutdown.getAndSet(true)) {
            localFileSender.shutdown();
            localFilePurger.shutdown();
        }
        return CompletableResultCode.ofSuccess();
    }

    private static String getOriginalErrorMessage(TelemetryPipelineResponse response) {
        int statusCode = response.getStatusCode();
        if (statusCode == 401 || statusCode == 403) {
            return DiagnosticTelemetryPipelineListener
                .getErrorMessageFromCredentialRelatedResponse(statusCode, response.getBody());
        } else {
            return "Received response code " + statusCode;
        }
    }
}
