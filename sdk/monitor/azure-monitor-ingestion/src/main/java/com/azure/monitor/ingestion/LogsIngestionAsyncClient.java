// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.models.ResponseError;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
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
import java.util.Collections;
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
 *
 * <p><strong>Instantiating an asynchronous Logs ingestion client</strong></p>
 * <!-- src_embed com.azure.monitor.ingestion.LogsIngestionAsyncClient.instantiation -->
 * <pre>
 * LogsIngestionAsyncClient logsIngestionAsyncClient = new LogsIngestionClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .endpoint&#40;&quot;&lt;data-collection-endpoint&gt;&quot;&#41;
 *         .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.ingestion.LogsIngestionAsyncClient.instantiation -->
 */
@ServiceClient(isAsync = true, builder = LogsIngestionClientBuilder.class)
public final class LogsIngestionAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(LogsIngestionAsyncClient.class);
    private static final String CONTENT_ENCODING = "Content-Encoding";
    private static final long MAX_REQUEST_PAYLOAD_SIZE = 1024 * 1024; // 1 MB
    private static final String GZIP = "gzip";
    private static final JsonSerializer DEFAULT_SERIALIZER = JsonSerializerProviders.createInstance(true);

    private final IngestionUsingDataCollectionRulesAsyncClient service;

    LogsIngestionAsyncClient(IngestionUsingDataCollectionRulesAsyncClient service) {
        this.service = service;
    }

    /**
     * Uploads logs to Azure Monitor with specified data collection rule id and stream name. The input logs may be
     * too large to be sent as a single request to the Azure Monitor service. In such cases, this method will split
     * the input logs into multiple smaller requests before sending to the service.
     *
     * <p><strong>Upload logs to Azure Monitor</strong></p>
     * <!-- src_embed com.azure.monitor.ingestion.LogsIngestionAsyncClient.upload -->
     * <pre>
     * List&lt;Object&gt; logs = getLogs&#40;&#41;;
     * logsIngestionAsyncClient.upload&#40;&quot;&lt;data-collection-rule-id&gt;&quot;, &quot;&lt;stream-name&gt;&quot;, logs&#41;
     *         .subscribe&#40;result -&gt; System.out.println&#40;&quot;Logs upload result status &quot; + result.getStatus&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.monitor.ingestion.LogsIngestionAsyncClient.upload -->
     *
     * @param ruleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @return the result of the logs upload request.
     * @throws NullPointerException if any of {@code ruleId}, {@code streamName} or {@code logs} are null.
     * @throws IllegalArgumentException if {@code logs} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UploadLogsResult> upload(String ruleId, String streamName, List<Object> logs) {
        return upload(ruleId, streamName, logs, new UploadLogsOptions());
    }

    /**
     * Uploads logs to Azure Monitor with specified data collection rule id and stream name. The input logs may be
     * too large to be sent as a single request to the Azure Monitor service. In such cases, this method will split
     * the input logs into multiple smaller requests before sending to the service.
     *
     * <p><strong>Upload logs to Azure Monitor</strong></p>
     * <!-- src_embed com.azure.monitor.ingestion.LogsIngestionAsyncClient.uploadWithConcurrency -->
     * <pre>
     * List&lt;Object&gt; logs = getLogs&#40;&#41;;
     * UploadLogsOptions uploadLogsOptions = new UploadLogsOptions&#40;&#41;.setMaxConcurrency&#40;4&#41;;
     * logsIngestionAsyncClient.upload&#40;&quot;&lt;data-collection-rule-id&gt;&quot;, &quot;&lt;stream-name&gt;&quot;, logs, uploadLogsOptions&#41;
     *         .subscribe&#40;result -&gt; System.out.println&#40;&quot;Logs upload result status &quot; + result.getStatus&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.monitor.ingestion.LogsIngestionAsyncClient.uploadWithConcurrency -->
     *
     * @param ruleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @param options the options to configure the upload request.
     * @return the result of the logs upload request.
     * @throws NullPointerException if any of {@code ruleId}, {@code streamName} or {@code logs} are null.
     * @throws IllegalArgumentException if {@code logs} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UploadLogsResult> upload(String ruleId, String streamName,
                                         List<Object> logs, UploadLogsOptions options) {
        return withContext(context -> upload(ruleId, streamName, logs, options, context));
    }

    /**
     * See error response code and error response message for more detail.
     *
     * <p><strong>Header Parameters</strong>
     *
     * <table border="1">
     *     <caption>Header Parameters</caption>
     *     <tr><th>Name</th><th>Type</th><th>Required</th><th>Description</th></tr>
     *     <tr><td>Content-Encoding</td><td>String</td><td>No</td><td>gzip</td></tr>
     *     <tr><td>x-ms-client-request-id</td><td>String</td><td>No</td><td>Client request Id</td></tr>
     * </table>
     *
     * <p><strong>Request Body Schema</strong>
     *
     * <pre>{@code
     * [
     *     Object
     * ]
     * }</pre>
     *
     * @param ruleId The immutable Id of the Data Collection Rule resource.
     * @param streamName The streamDeclaration name as defined in the Data Collection Rule.
     * @param logs An array of objects matching the schema defined by the provided stream.
     * @param requestOptions The options to configure the HTTP request before HTTP client sends it.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     * @return the {@link Response} on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> uploadWithResponse(
            String ruleId, String streamName, BinaryData logs, RequestOptions requestOptions) {
        Objects.requireNonNull(ruleId, "'ruleId' cannot be null.");
        Objects.requireNonNull(streamName, "'streamName' cannot be null.");
        Objects.requireNonNull(logs, "'logs' cannot be null.");

        if (requestOptions == null) {
            requestOptions = new RequestOptions();
        }
        requestOptions.addRequestCallback(request -> {
            HttpHeader httpHeader = request.getHeaders().get(CONTENT_ENCODING);
            if (httpHeader == null) {
                BinaryData gzippedRequest = BinaryData.fromBytes(gzipRequest(logs.toBytes()));
                request.setBody(gzippedRequest);
                request.setHeader(CONTENT_ENCODING, GZIP);
            }
        });
        return service.uploadWithResponse(ruleId, streamName, logs, requestOptions);
    }

    Mono<UploadLogsResult> upload(String ruleId, String streamName,
                                  List<Object> logs, UploadLogsOptions options,
                                  Context context) {
        return Mono.defer(() -> splitAndUpload(ruleId, streamName, logs, options, context));
    }

    private Mono<UploadLogsResult> splitAndUpload(String ruleId, String streamName, List<Object> logs, UploadLogsOptions options, Context context) {
        try {
            Objects.requireNonNull(ruleId, "'ruleId' cannot be null.");
            Objects.requireNonNull(streamName, "'streamName' cannot be null.");
            Objects.requireNonNull(logs, "'logs' cannot be null.");

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
            // TODO (srnagar): can improve memory usage by creating these request payloads right before sending the
            //  request if the allowed concurrency is lower than the number of requests.
            List<byte[]> requests = createGzipRequests(logs, serializer, logBatches);
            RequestOptions requestOptions = new RequestOptions()
                    .addHeader(CONTENT_ENCODING, GZIP)
                    .setContext(context);

            Iterator<List<Object>> logBatchesIterator = logBatches.iterator();
            return Flux.fromIterable(requests)
                    .flatMapSequential(bytes ->
                            uploadToService(ruleId, streamName, requestOptions, bytes), concurrency)
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
                    Collections.singletonList(new UploadLogsError(responseHolder.getResponseError(), logsBatch)));
        }
        return new UploadLogsResult(UploadLogsStatus.SUCCESS, null);
    }

    private Mono<UploadLogsResponseHolder> uploadToService(String ruleId, String streamName, RequestOptions requestOptions, byte[] bytes) {
        return service.uploadWithResponse(ruleId, streamName,
                        BinaryData.fromBytes(bytes), requestOptions)
                .map(response -> new UploadLogsResponseHolder(UploadLogsStatus.SUCCESS, null))
                .onErrorResume(HttpResponseException.class,
                        ex -> Mono.fromSupplier(() -> new UploadLogsResponseHolder(UploadLogsStatus.FAILURE,
                                mapToResponseError(ex))));
    }

    /**
     * Method to map the exception to {@link ResponseError}.
     * @param ex the {@link HttpResponseException}.
     * @return the mapped {@link ResponseError}.
     */
    private ResponseError mapToResponseError(HttpResponseException ex) {
        ResponseError responseError = null;
        // with DPG clients, the error object is just a LinkedHashMap and should map to the standard error
        // response structure
        // https://github.com/Azure/azure-rest-api-specs/blob/main/specification/common-types/data-plane/v1/types.json#L46
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
        int failureCount = 0;
        List<UploadLogsError> errors =  new ArrayList<>();
        for (UploadLogsResult result : results) {
            if (result.getStatus() != UploadLogsStatus.SUCCESS) {
                failureCount++;
                errors.addAll(result.getErrors());
            }
        }
        if (failureCount == 0) {
            return new UploadLogsResult(UploadLogsStatus.SUCCESS, errors);
        }
        if (failureCount < results.size()) {
            return new UploadLogsResult(UploadLogsStatus.PARTIAL_FAILURE, errors);
        }
        return new UploadLogsResult(UploadLogsStatus.FAILURE, errors);
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
