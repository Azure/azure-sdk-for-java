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
import com.azure.monitor.ingestion.implementation.Batcher;
import com.azure.monitor.ingestion.implementation.IngestionUsingDataCollectionRulesClient;
import com.azure.monitor.ingestion.implementation.LogsIngestionRequest;
import com.azure.monitor.ingestion.implementation.UploadLogsResponseHolder;
import com.azure.monitor.ingestion.models.LogsUploadError;
import com.azure.monitor.ingestion.models.LogsUploadException;
import com.azure.monitor.ingestion.models.LogsUploadOptions;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.monitor.ingestion.implementation.Utils.CONTENT_ENCODING;
import static com.azure.monitor.ingestion.implementation.Utils.GZIP;
import static com.azure.monitor.ingestion.implementation.Utils.getConcurrency;
import static com.azure.monitor.ingestion.implementation.Utils.getThreadPoolWithShutDownHook;
import static com.azure.monitor.ingestion.implementation.Utils.gzipRequest;

/**
 * The synchronous client for uploading logs to Azure Monitor.
 *
 * <p><strong>Instantiating a synchronous Logs ingestion client</strong></p>
 * <!-- src_embed com.azure.monitor.ingestion.LogsIngestionClient.instantiation -->
 * <pre>
 * LogsIngestionClient logsIngestionClient = new LogsIngestionClientBuilder&#40;&#41;
 *         .credential&#40;tokenCredential&#41;
 *         .endpoint&#40;&quot;&lt;data-collection-endpoint&gt;&quot;&#41;
 *         .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.monitor.ingestion.LogsIngestionClient.instantiation -->
 */
@ServiceClient(builder = LogsIngestionClientBuilder.class)
public final class LogsIngestionClient {
    private static final ClientLogger LOGGER = new ClientLogger(LogsIngestionClient.class);
    private static final Runnable DO_NOTHING = () -> { };
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";
    private static final Context ENABLE_SYNC_CONTEXT = new Context(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    private final IngestionUsingDataCollectionRulesClient client;

    // dynamic thread pool that scales up and down on demand.
    private static final ExecutorService THREAD_POOL = getThreadPoolWithShutDownHook(5);

    LogsIngestionClient(IngestionUsingDataCollectionRulesClient client) {
        this.client = client;
    }

    /**
     * Uploads logs to Azure Monitor with specified data collection rule id and stream name. The input logs may be
     * too large to be sent as a single request to the Azure Monitor service. In such cases, this method will split
     * the input logs into multiple smaller requests before sending to the service.
     *
     * <p><strong>Upload logs to Azure Monitor</strong></p>
     * <!-- src_embed com.azure.monitor.ingestion.LogsIngestionClient.upload -->
     * <pre>
     * List&lt;Object&gt; logs = getLogs&#40;&#41;;
     * logsIngestionClient.upload&#40;&quot;&lt;data-collection-rule-id&gt;&quot;, &quot;&lt;stream-name&gt;&quot;, logs&#41;;
     * System.out.println&#40;&quot;Logs uploaded successfully&quot;&#41;;
     * </pre>
     * <!-- end com.azure.monitor.ingestion.LogsIngestionClient.upload -->
     *
     * @param ruleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @throws NullPointerException if any of {@code ruleId}, {@code streamName} or {@code logs} are null.
     * @throws IllegalArgumentException if {@code logs} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upload(String ruleId, String streamName, Iterable<Object> logs) {
        upload(ruleId, streamName, logs, null);
    }

    /**
     * Uploads logs to Azure Monitor with specified data collection rule id and stream name. The input logs may be
     * too large to be sent as a single request to the Azure Monitor service. In such cases, this method will split
     * the input logs into multiple smaller requests before sending to the service.
     *
     * <p><strong>Upload logs to Azure Monitor</strong></p>
     * <!-- src_embed com.azure.monitor.ingestion.LogsIngestionClient.uploadWithConcurrency -->
     * <pre>
     * List&lt;Object&gt; logs = getLogs&#40;&#41;;
     * LogsUploadOptions logsUploadOptions = new LogsUploadOptions&#40;&#41;.setMaxConcurrency&#40;4&#41;;
     * logsIngestionClient.upload&#40;&quot;&lt;data-collection-rule-id&gt;&quot;, &quot;&lt;stream-name&gt;&quot;, logs,
     *         logsUploadOptions, Context.NONE&#41;;
     * System.out.println&#40;&quot;Logs uploaded successfully&quot;&#41;;
     * </pre>
     * <!-- end com.azure.monitor.ingestion.LogsIngestionClient.uploadWithConcurrency -->
     * @param ruleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @param options the options to configure the upload request.
     * @throws NullPointerException if any of {@code ruleId}, {@code streamName} or {@code logs} are null.
     * @throws IllegalArgumentException if {@code logs} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upload(String ruleId, String streamName,
                                   Iterable<Object> logs, LogsUploadOptions options) {
        upload(ruleId, streamName, logs, options, Context.NONE);
    }

    /**
     * Uploads logs to Azure Monitor with specified data collection rule id and stream name. The input logs may be
     * too large to be sent as a single request to the Azure Monitor service. In such cases, this method will split
     * the input logs into multiple smaller requests before sending to the service.
     *
     * @param ruleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @param options the options to configure the upload request.
     * @param context additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @throws NullPointerException if any of {@code ruleId}, {@code streamName} or {@code logs} are null.
     * @throws IllegalArgumentException if {@code logs} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upload(String ruleId, String streamName,
                       Iterable<Object> logs, LogsUploadOptions options, Context context) {
        Objects.requireNonNull(ruleId, "'ruleId' cannot be null.");
        Objects.requireNonNull(streamName, "'streamName' cannot be null.");
        Objects.requireNonNull(logs, "'logs' cannot be null.");

        context = enableSync(context);

        Consumer<LogsUploadError> uploadLogsErrorConsumer = options == null ? null : options.getLogsUploadErrorConsumer();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addHeader(CONTENT_ENCODING, GZIP);
        requestOptions.setContext(context);

        Stream<UploadLogsResponseHolder> responses = new Batcher(options, logs)
            .toStream()
            .map(r -> uploadToService(ruleId, streamName, requestOptions, r));

        responses = submit(responses, getConcurrency(options))
            .filter(response -> response.getException() != null);

        if (uploadLogsErrorConsumer != null) {
            responses.forEach(response -> uploadLogsErrorConsumer.accept(new LogsUploadError(response.getException(), response.getRequest().getLogs())));
            return;
        }

        final int[] failedLogCount = new int[1];
        List<HttpResponseException> exceptions = responses
            .map(response -> {
                failedLogCount[0] += response.getRequest().getLogs().size();
                return response.getException();
            })
            .collect(Collectors.toList());

        if (exceptions.size() > 0) {
            throw LOGGER.logExceptionAsError(new LogsUploadException(exceptions, failedLogCount[0]));
        }
    }

    private Stream<UploadLogsResponseHolder> submit(Stream<UploadLogsResponseHolder> responseStream, int concurrency) {
        if (concurrency == 1) {
            return responseStream;
        }

        try {
            return THREAD_POOL.submit(() -> responseStream).get();
        } catch (InterruptedException | ExecutionException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private UploadLogsResponseHolder uploadToService(String ruleId, String streamName, RequestOptions requestOptions, LogsIngestionRequest request) {
        HttpResponseException exception = null;
        try {
            client.uploadWithResponse(ruleId, streamName, BinaryData.fromBytes(request.getRequestBody()), requestOptions);
        } catch (HttpResponseException ex) {
            exception = ex;
        }

        return new UploadLogsResponseHolder(request, exception);
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
     * @return the {@link Response}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> uploadWithResponse(
            String ruleId, String streamName, BinaryData logs, RequestOptions requestOptions) {
        Objects.requireNonNull(ruleId, "'ruleId' cannot be null.");
        Objects.requireNonNull(streamName, "'streamName' cannot be null.");
        Objects.requireNonNull(logs, "'logs' cannot be null.");

        if (requestOptions == null) {
            requestOptions = new RequestOptions();
        }

        requestOptions.setContext(enableSync(requestOptions.getContext()));
        requestOptions.addRequestCallback(request -> {
            HttpHeader httpHeader = request.getHeaders().get(CONTENT_ENCODING);
            if (httpHeader == null) {
                BinaryData gzippedRequest = BinaryData.fromBytes(gzipRequest(logs.toBytes()));
                request.setBody(gzippedRequest);
                request.setHeader(CONTENT_ENCODING, GZIP);
            }
        });
        return client.uploadWithResponse(ruleId, streamName, logs, requestOptions);
    }

    private static Context enableSync(Context context) {
        if (context == null || context == Context.NONE) {
            return ENABLE_SYNC_CONTEXT;
        }

        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }

}
