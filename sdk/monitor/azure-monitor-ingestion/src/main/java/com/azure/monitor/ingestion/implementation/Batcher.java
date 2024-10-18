// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import com.azure.monitor.ingestion.models.LogsUploadOptions;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.azure.monitor.ingestion.implementation.Utils.MAX_REQUEST_PAYLOAD_SIZE;
import static com.azure.monitor.ingestion.implementation.Utils.getConcurrency;
import static com.azure.monitor.ingestion.implementation.Utils.gzipRequest;

/**
 * Provides iterator and streams for batches over log objects.
 */
public class Batcher implements Iterator<LogsIngestionRequest> {
    private static final ClientLogger LOGGER = new ClientLogger(Batcher.class);
    private static final JsonSerializer DEFAULT_SERIALIZER = JsonSerializerProviders.createInstance(true);
    private final ObjectSerializer serializer;
    private final int concurrency;
    private final Iterator<Object> iterator;
    private long currentBatchSize;
    private List<String> serializedLogs;
    private List<Object> originalLogsRequest;

    public Batcher(LogsUploadOptions options, Iterable<Object> logs) {
        this.serializer = getSerializer(options);
        this.concurrency = getConcurrency(options);
        this.serializedLogs = new ArrayList<>();
        this.originalLogsRequest = new ArrayList<>();
        this.iterator = logs.iterator();
    }

    /**
     * Checks if there are more logs to batch. This method is not thread safe!
     *
     * When used concurrently, it should be synchronized along with {@link Batcher#next()}:
     *
     * <pre>{@code
     * synchronized (batcher) {
     *     if (batcher.hasNext()) {
     *         request = batcher.next();
     *     }
     * }
     * }</pre>
     */
    @Override
    public boolean hasNext() {
        return iterator.hasNext() || currentBatchSize > 0;
    }

    /**
     * Collects next batch and serializes it into {@link LogsIngestionRequest}. This method is not thread-safe!
     *
     * Returns null when complete.
     */
    @Override
    public LogsIngestionRequest next() {
        try {
            return nextInternal();
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    /**
     * Creates stream of requests split for configured concurrency. Returns parallel stream if concurrency is bigger than 1.
     */
    public Stream<LogsIngestionRequest> toStream() {
        if (concurrency == 1) {
            return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(this, Spliterator.NONNULL | Spliterator.ORDERED), false);
        }

        return StreamSupport.stream(new ConcurrencyLimitingSpliterator<>(this, concurrency), true);
    }

    /**
     * Creates flux with requests.
     */
    public Flux<LogsIngestionRequest> toFlux() {
        return Flux.create(emitter -> {
            try {
                while (hasNext()) {
                    LogsIngestionRequest next = nextInternal();
                    if (next != null) {
                        emitter.next(next);
                    }
                }
            } catch (IOException ex) {
                emitter.error(ex);
            }

            emitter.complete();
        });
    }

    private LogsIngestionRequest nextInternal() throws IOException {
        LogsIngestionRequest result = null;
        while (iterator.hasNext() && result == null) {
            Object currentLog = iterator.next();
            byte[] bytes = serializer.serializeToBytes(currentLog);
            currentBatchSize += bytes.length;
            if (currentBatchSize > MAX_REQUEST_PAYLOAD_SIZE) {
                result = createRequest(false);
                currentBatchSize = bytes.length;
            }

            serializedLogs.add(new String(bytes, StandardCharsets.UTF_8));
            originalLogsRequest.add(currentLog);
        }

        if (result == null && currentBatchSize > 0) {
            currentBatchSize = 0;
            return createRequest(true);
        }

        return result;
    }

    private LogsIngestionRequest createRequest(boolean last) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(byteArrayOutputStream)) {
            writer.writeStartArray();
            for (String log : serializedLogs) {
                writer.writeRawValue(log);
            }
            writer.writeEndArray();
            writer.flush();
            byte[] zippedRequestBody = gzipRequest(byteArrayOutputStream.toByteArray());
            return new LogsIngestionRequest(originalLogsRequest, zippedRequestBody);
        } finally {
            if (!last) {
                originalLogsRequest = new ArrayList<>();
                serializedLogs.clear();
            }
        }
    }

    private static ObjectSerializer getSerializer(LogsUploadOptions options) {
        if (options != null && options.getObjectSerializer() != null) {
            return options.getObjectSerializer();
        }

        return DEFAULT_SERIALIZER;
    }
}
