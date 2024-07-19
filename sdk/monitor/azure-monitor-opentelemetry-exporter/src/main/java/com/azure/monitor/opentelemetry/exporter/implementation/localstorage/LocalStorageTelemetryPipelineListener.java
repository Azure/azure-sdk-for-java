// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.localstorage;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.exporter.implementation.logging.DiagnosticTelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.models.ResponseError;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineListener;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineRequest;
import com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryPipelineResponse;
import com.azure.monitor.opentelemetry.exporter.implementation.utils.StatusCode;
import io.opentelemetry.sdk.common.CompletableResultCode;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemSerialization.addNewLineAsLineDelimiter;
import static com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemSerialization.convertByteBufferListToByteArray;
import static com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemSerialization.countNewLines;
import static com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemSerialization.decode;
import static com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemSerialization.deserialize;
import static com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemSerialization.printJson;
import static com.azure.monitor.opentelemetry.exporter.implementation.pipeline.TelemetryItemSerialization.splitBytesByNewline;

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

//    private byte[]

    private void processStatusCode206(TelemetryPipelineRequest request, TelemetryPipelineResponse response) {
        Set<ResponseError> errors = response.getErrors();
        errors.forEach(error -> logger.verbose("Error in telemetry: {}", error));
        if (!errors.isEmpty()) {
            List<ByteBuffer> originalByteBuffers = request.getByteBuffers();
            byte[] bytes1 = convertByteBufferListToByteArray(originalByteBuffers);
            byte[] decodedBytes = decode(bytes1);
            List<byte[]> decodedByteArrayList = splitBytesByNewline(decodedBytes);
            System.out.println("decodedByteArrayList: " + decodedByteArrayList.size());

            List<ByteBuffer> toBePersisted = new ArrayList<>();
            for (ResponseError error : errors) {
                if (StatusCode.isRetryable(error.getStatusCode())) {
                    toBePersisted.add(ByteBuffer.wrap(decodedByteArrayList.get(error.getIndex())));
                }
            }



            // convert toBePersisted to TelemetryItems
            List<TelemetryItem> toBePersistedTelemetryItems = new ArrayList<>();
            for (ByteBuffer byteBuffer : toBePersisted) {
                TelemetryItem telemetryItem = deserialize(byteBuffer.array());
                toBePersistedTelemetryItems.add(telemetryItem);
            }
            System.out.println("toBePersistedTelemetryItems: " + toBePersistedTelemetryItems.size());

            System.out.println("==========================================================");
            try {
                System.out.println("toBePersistedTelemetryItems: \n" + printJson(toBePersistedTelemetryItems));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("==========================================================");


            // test with newline in the byte array
            List<ByteBuffer> byteBufferList = addNewLineAsLineDelimiter(toBePersisted);
            byte[] newByteArray = convertByteBufferListToByteArray(byteBufferList);
            List<byte[]> newByteArrayList = splitBytesByNewline(newByteArray);
            assert newByteArrayList.size() == byteBufferList.size();

            List<TelemetryItem> actualTelemetryItems = new ArrayList<>();
            for (byte[] bytes : newByteArrayList) {
                actualTelemetryItems.add(deserialize(bytes));
            }

            System.out.println("==========================================================");
            try {
                System.out.println("actualTelemetryItems: \n" + printJson(actualTelemetryItems));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("==========================================================");


            if (!toBePersisted.isEmpty()) {
                localFileWriter.writeToDisk(
                    request.getConnectionString(), addNewLineAsLineDelimiter(toBePersisted), "Received partial response code 206");
            }
        }
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
