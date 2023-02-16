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
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.ObjectSerializer;
import com.azure.monitor.ingestion.implementation.IngestionUsingDataCollectionRulesAsyncClient;
import com.azure.monitor.ingestion.implementation.LogsIngestionRequest;
import com.azure.monitor.ingestion.implementation.UploadLogsResponseHolder;
import com.azure.monitor.ingestion.models.LogsUploadError;
import com.azure.monitor.ingestion.models.LogsUploadException;
import com.azure.monitor.ingestion.models.LogsUploadOptions;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

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
     *         .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.monitor.ingestion.LogsIngestionAsyncClient.upload -->
     *
     * @param ruleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @return the {@link Mono} that completes on completion of the upload request.
     * @throws NullPointerException if any of {@code ruleId}, {@code streamName} or {@code logs} are null.
     * @throws IllegalArgumentException if {@code logs} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> upload(String ruleId, String streamName, Iterable<Object> logs) {
        return upload(ruleId, streamName, logs, new LogsUploadOptions());
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
     * LogsUploadOptions logsUploadOptions = new LogsUploadOptions&#40;&#41;.setMaxConcurrency&#40;4&#41;;
     * logsIngestionAsyncClient.upload&#40;&quot;&lt;data-collection-rule-id&gt;&quot;, &quot;&lt;stream-name&gt;&quot;, logs, logsUploadOptions&#41;
     *         .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.monitor.ingestion.LogsIngestionAsyncClient.uploadWithConcurrency -->
     *
     * @param ruleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @param options the options to configure the upload request.
     * @return the {@link Mono} that completes on completion of the upload request.
     * @throws NullPointerException if any of {@code ruleId}, {@code streamName} or {@code logs} are null.
     * @throws IllegalArgumentException if {@code logs} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> upload(String ruleId, String streamName,
                                         Iterable<Object> logs, LogsUploadOptions options) {
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

    Mono<Void> upload(String ruleId, String streamName,
                      Iterable<Object> logs, LogsUploadOptions options,
                      Context context) {
        return Mono.defer(() -> splitAndUpload(ruleId, streamName, logs, options, context));
    }


    /**
     * This method splits the input logs into < 1MB HTTP requests and uploads to the Azure Monitor service.
     * @param ruleId The data collection rule id.
     * @param streamName The stream name configured in the data collection rule.
     * @param logs The input logs to upload.
     * @param options The options to configure the upload request.
     * @param context additional context that is passed through the Http pipeline during the service call. If no
     *     additional context is required, pass {@link Context#NONE} instead.
     * @return the {@link Mono} that completes on completion of the upload request.
     */
    private Mono<Void> splitAndUpload(String ruleId, String streamName, Iterable<Object> logs,
                                      LogsUploadOptions options, Context context) {
        // set concurrency to 1 as default
        int concurrency = 1;
        ObjectSerializer objectSerializer = DEFAULT_SERIALIZER;
        if (options != null) {
            if (options.getObjectSerializer() != null) {
                objectSerializer = options.getObjectSerializer();
            }
            if (options.getMaxConcurrency() != null) {
                concurrency = options.getMaxConcurrency();
            }
        }

        ObjectSerializer serializer = objectSerializer;
        Iterator<Object> iterator = logs.iterator();

        return Flux.<LogsIngestionRequest>create(emitter -> createHttpRequest(serializer, iterator, emitter))
                .flatMapSequential(request -> uploadToService(ruleId, streamName, context, request), concurrency)
                .<LogsUploadException>handle((responseHolder, sink) -> processResponse(options, responseHolder, sink))
                .collectList()
                .handle((result, sink) -> processExceptions(result, sink));
    }

    /**
     * This method converts raw {@link Object} logs into serialized and gzipped byte array that forms the request
     * body of the service call.
     * @param serializer The serializer used to serialize logs.
     * @param iterator The input logs interator.
     * @param emitter The Flux emitter to which the HTTP request is emitted.
     */
    private void createHttpRequest(ObjectSerializer serializer, Iterator<Object> iterator, FluxSink<LogsIngestionRequest> emitter) {
        try {
            long currentBatchSize = 0;
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JsonGenerator generator = JsonFactory.builder().build().createGenerator(byteArrayOutputStream);
            generator.writeStartArray();
            List<String> serializedLogs = new ArrayList<>();
            List<Object> originalLogsRequest = new ArrayList<>();
            while (iterator.hasNext()) {
                Object currentLog = iterator.next();
                byte[] bytes = serializer.serializeToBytes(currentLog);
                int currentLogSize = bytes.length;
                currentBatchSize += currentLogSize;
                if (currentBatchSize > MAX_REQUEST_PAYLOAD_SIZE) {
                    writeLogsAndCloseJsonGenerator(generator, serializedLogs);
                    byte[] zippedRequestBody = gzipRequest(byteArrayOutputStream.toByteArray());
                    emitter.next(new LogsIngestionRequest(originalLogsRequest, zippedRequestBody));
                    byteArrayOutputStream = new ByteArrayOutputStream();
                    generator = JsonFactory.builder().build().createGenerator(byteArrayOutputStream);
                    generator.writeStartArray();
                    currentBatchSize = currentLogSize;
                    originalLogsRequest = new ArrayList<>();
                    serializedLogs.clear();
                }
                serializedLogs.add(new String(bytes, StandardCharsets.UTF_8));
                originalLogsRequest.add(currentLog);
            }
            if (currentBatchSize > 0) {
                writeLogsAndCloseJsonGenerator(generator, serializedLogs);
                byte[] zippedRequestBody = gzipRequest(byteArrayOutputStream.toByteArray());
                emitter.next(new LogsIngestionRequest(originalLogsRequest, zippedRequestBody));
            }
            emitter.complete();
        } catch (IOException e) {
            emitter.error(e);
        }
    }

    private void processExceptions(List<LogsUploadException> result, SynchronousSink<Void> sink) {
        long failedLogsCount = 0L;
        List<HttpResponseException> exceptions = new ArrayList<>();
        for (LogsUploadException exception : result) {
            exceptions.addAll(exception.getLogsUploadErrors());
            failedLogsCount += exception.getFailedLogsCount();
        }
        if (!exceptions.isEmpty()) {
            sink.error(new LogsUploadException(exceptions, failedLogsCount));
        } else {
            sink.complete();
        }
    }

    private void processResponse(LogsUploadOptions options, UploadLogsResponseHolder responseHolder, SynchronousSink<LogsUploadException> sink) {
        if (responseHolder.getException() != null) {
            Consumer<LogsUploadError> uploadLogsErrorConsumer = null;
            if (options != null) {
                uploadLogsErrorConsumer = options.getLogsUploadErrorConsumer();
            }
            if (uploadLogsErrorConsumer != null) {
                uploadLogsErrorConsumer.accept(new LogsUploadError(responseHolder.getException(), responseHolder.getRequest().getLogs()));
                return;
            }
            // emit the responseHolder without the original logs only if there's an error and there's no
            // error consumer
            sink.next(new LogsUploadException(Collections.singletonList(responseHolder.getException()),
                    responseHolder.getRequest().getLogs().size()));
        }
    }

    private Mono<UploadLogsResponseHolder> uploadToService(String ruleId, String streamName,
                                                           Context context,
                                                           LogsIngestionRequest request) {
        RequestOptions requestOptions = new RequestOptions()
                .addHeader(CONTENT_ENCODING, GZIP)
                .setContext(context);
        return service.uploadWithResponse(ruleId, streamName,
                        BinaryData.fromBytes(request.getRequestBody()), requestOptions)
                .map(response -> new UploadLogsResponseHolder(null, null))
                .onErrorResume(HttpResponseException.class,
                        ex -> Mono.fromSupplier(() -> new UploadLogsResponseHolder(request, ex)));
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
