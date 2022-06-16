// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.models.ResponseError;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.monitor.ingestion.implementation.DefaultJsonSerializer;
import com.azure.monitor.ingestion.implementation.IngestionUsingDataCollectionRulesAsyncClient;
import com.azure.monitor.ingestion.implementation.UploadLogsResponseHolder;
import com.azure.monitor.ingestion.models.UploadLogsError;
import com.azure.monitor.ingestion.models.UploadLogsOptions;
import com.azure.monitor.ingestion.models.UploadLogsResult;
import com.azure.monitor.ingestion.models.UploadLogsStatus;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * The asynchronous client for uploading logs to Azure Monitor.
 */
@ServiceClient(isAsync = true, builder = LogsIngestionClientBuilder.class)
public final class LogsIngestionAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(LogsIngestionAsyncClient.class);
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final long MAX_REQUEST_PAYLOAD_SIZE = 1024 * 1024; // 1 MB
    private static final String GZIP = "gzip";
    // TODO (srnagar): Move DefaultJsonSerializer in azure-core to public package
    private static final DefaultJsonSerializer DEFAULT_SERIALIZER = new DefaultJsonSerializer();

    private final IngestionUsingDataCollectionRulesAsyncClient service;

    LogsIngestionAsyncClient(IngestionUsingDataCollectionRulesAsyncClient service) {
        this.service = service;
    }

    /**
     * Uploads logs to Azure Monitor with specified data collection rule id and stream name. The input logs may be
     * too large to be sent as a single request to the Azure Monitor service. In such cases, this method will split
     * the input logs into multiple smaller requests before sending to the service.
     * @param dataCollectionRuleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @return the result of the logs upload request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UploadLogsResult> upload(String dataCollectionRuleId, String streamName, List<Object> logs) {
        return upload(dataCollectionRuleId, streamName, logs, new UploadLogsOptions());
    }

    /**
     * Uploads logs to Azure Monitor with specified data collection rule id and stream name. The input logs may be
     * too large to be sent as a single request to the Azure Monitor service. In such cases, this method will split
     * the input logs into multiple smaller requests before sending to the service.
     * @param dataCollectionRuleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @param options the options to configure the upload request.
     * @return the result of the logs upload request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UploadLogsResult> upload(String dataCollectionRuleId, String streamName,
                                         List<Object> logs, UploadLogsOptions options) {
        return withContext(context -> upload(dataCollectionRuleId, streamName, logs, options, context));
    }

    Mono<UploadLogsResult> upload(String dataCollectionRuleId, String streamName,
                                  List<Object> logs, UploadLogsOptions options,
                                  Context context) {
        return Mono.defer(() -> splitAndUpload(dataCollectionRuleId, streamName, logs, options, context));
    }

    private Mono<UploadLogsResult> splitAndUpload(String dataCollectionRuleId, String streamName, List<Object> logs, UploadLogsOptions options, Context context) {
        try {
            Objects.requireNonNull(dataCollectionRuleId, "'dataCollectionRuleId' cannot be null.");
            Objects.requireNonNull(dataCollectionRuleId, "'streamName' cannot be null.");
            Objects.requireNonNull(dataCollectionRuleId, "'logs' cannot be null.");

            if (logs.isEmpty()) {
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("'logs' cannot be empty."));
            }

            ObjectSerializer serializer = DEFAULT_SERIALIZER;

            // set concurrency to 1 as default
            int concurrency = 1;
            if (options != null) {
                if (options.getObjectSerializer() != null) {
                    serializer = options.getObjectSerializer();
                }
                if (options.getMaxConcurrency() != null) {
                    concurrency = options.getMaxConcurrency();
                }
            }

            List<List<Object>> logBatches = new ArrayList<>();
            List<byte[]> requests = createGzipRequests(logs, serializer, logBatches);
            RequestOptions requestOptions = new RequestOptions()
                    .addHeader(CONTENT_ENCODING, GZIP)
                    .setContext(context);

            Iterator<List<Object>> logBatchesIterator = logBatches.iterator();
            return Flux.fromIterable(requests)
                    .flatMapSequential(bytes ->
                            uploadToService(dataCollectionRuleId, streamName, requestOptions, bytes), concurrency)
                    .map(responseHolder -> mapResult(logBatchesIterator, responseHolder))
                    .collectList()
                    .map(this::createResponse);
        } catch (RuntimeException ex) {
            return monoError(LOGGER, ex);
        }
    }

    private UploadLogsResult mapResult(Iterator<List<Object>> logBatchesIterator, UploadLogsResponseHolder responseHolder) {
        List<Object> logsBatch = logBatchesIterator.next();
        if (responseHolder.getStatus() == UploadLogsStatus.FAILURE) {
            return new UploadLogsResult(responseHolder.getStatus(),
                    Arrays.asList(new UploadLogsError(responseHolder.getResponseError(), logsBatch)));
        }
        return new UploadLogsResult(UploadLogsStatus.SUCCESS, null);
    }

    private Mono<UploadLogsResponseHolder> uploadToService(String dataCollectionRuleId, String streamName, RequestOptions requestOptions, byte[] bytes) {
        return service.uploadWithResponse(dataCollectionRuleId, streamName,
                        BinaryData.fromBytes(bytes), requestOptions)
                .map(response -> new UploadLogsResponseHolder(UploadLogsStatus.SUCCESS, null))
                .onErrorResume(HttpResponseException.class,
                        ex -> Mono.just(new UploadLogsResponseHolder(UploadLogsStatus.FAILURE,
                                mapToResponseError(ex))));
    }

    private ResponseError mapToResponseError(HttpResponseException ex) {
        ResponseError responseError = null;
        if (ex.getValue() instanceof LinkedHashMap<?, ?>) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> errorMap = (LinkedHashMap<String, Object>) ex.getValue();
            if (errorMap.containsKey("error")) {
                Object error = errorMap.get("error");
                if (error instanceof LinkedHashMap<?, ?>) {
                    @SuppressWarnings("unchecked")
                    LinkedHashMap<String, String> errorDetails = (LinkedHashMap<String, String>) error;
                    if (errorDetails.containsKey("code") && errorDetails.containsKey("message")) {
                        responseError = new ResponseError(errorDetails.get("code"), errorDetails.get("message"));
                    }
                }
            }
        }
        return responseError;
    }

    private UploadLogsResult createResponse(List<UploadLogsResult> results) {
        boolean allErrors = results.stream().allMatch(result -> result.getStatus() == UploadLogsStatus.FAILURE);
        if (allErrors) {
            return new UploadLogsResult(UploadLogsStatus.FAILURE,
                    results.stream().flatMap(result -> result.getErrors().stream()).collect(Collectors.toList()));
        }

        boolean anyErrors = results.stream().anyMatch(result -> result.getStatus() == UploadLogsStatus.FAILURE);
        if (anyErrors) {
            return new UploadLogsResult(UploadLogsStatus.PARTIAL_FAILURE,
                    results.stream().filter(result -> result.getStatus() == UploadLogsStatus.FAILURE)
                            .flatMap(result -> result.getErrors().stream()).collect(Collectors.toList()));
        }
        return new UploadLogsResult(UploadLogsStatus.SUCCESS, null);
    }

    private List<byte[]> createGzipRequests(List<Object> logs, ObjectSerializer serializer,
                                            List<List<Object>> logBatches) {
        try {
            List<byte[]> requests = new ArrayList<>();
            long currentBatchSize = 0;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            JsonGenerator generator = JsonFactory.builder().build().createGenerator(byteArrayOutputStream);
            generator.writeStartArray();
            List<String> serializedLogs = new ArrayList<>();

            int currentBatchStart = 0;
            for (int i = 0; i < logs.size(); i++) {
                byte[] bytes = serializer.serializeToBytes(logs.get(i));
                int currentLogSize = bytes.length;
                currentBatchSize += currentLogSize;
                if (currentBatchSize > MAX_REQUEST_PAYLOAD_SIZE) {
                    writeLogsAndCloseJsonGenerator(generator, serializedLogs);
                    requests.add(gzipRequest(byteArrayOutputStream.toByteArray()));

                    byteArrayOutputStream = new ByteArrayOutputStream();
                    generator = JsonFactory.builder().build().createGenerator(byteArrayOutputStream);
                    generator.writeStartArray();
                    currentBatchSize = currentLogSize;
                    serializedLogs.clear();
                    logBatches.add(logs.subList(currentBatchStart, i));
                    currentBatchStart = i;
                }
                serializedLogs.add(new String(bytes, StandardCharsets.UTF_8));
            }
            if (currentBatchSize > 0) {
                writeLogsAndCloseJsonGenerator(generator, serializedLogs);
                requests.add(gzipRequest(byteArrayOutputStream.toByteArray()));
                logBatches.add(logs.subList(currentBatchStart, logs.size()));
            }
            return requests;
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(exception));
        }
    }

    private void writeLogsAndCloseJsonGenerator(JsonGenerator generator, List<String> serializedLogs) throws IOException {
        generator.writeRaw(serializedLogs.stream()
                .collect(Collectors.joining(",")));
        generator.writeEndArray();
        generator.close();
    }

    /**
     * Gzips the input byte array.
     * @param bytes The input byte array.
     * @return gzipped byte array.
     */
    private byte[] gzipRequest(byte[] bytes) {
        // This should be moved to azure-core and should be enabled when the client library requests for gzipping the
        // request body content.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream zip = new GZIPOutputStream(byteArrayOutputStream)) {
            zip.write(bytes);
        } catch (IOException exception) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(exception));
        }
        return byteArrayOutputStream.toByteArray();
    }
}
