// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
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

import static com.azure.monitor.ingestion.implementation.Utils.GZIP;
import static com.azure.monitor.ingestion.implementation.Utils.createThreadPool;
import static com.azure.monitor.ingestion.implementation.Utils.getConcurrency;
import static com.azure.monitor.ingestion.implementation.Utils.gzipRequest;
import static com.azure.monitor.ingestion.implementation.Utils.registerShutdownHook;

/**
 * <p>This class provides a synchronous client for uploading custom logs to an Azure Monitor Log Analytics workspace.
 * This client encapsulates REST API calls, used to send data to a Log Analytics workspace, into a set of synchronous
 * operations.</p>
 *
 * <h2>Getting Started</h2>
 *
 * <p>To create an instance of the {@link LogsIngestionClient}, use the {@link LogsIngestionClientBuilder} and configure
 * the various options provided by the builder to customize the client as per your requirements. There are two required
 * properties that should be set to build a client:
 * <ol>
 * <li>{@code endpoint} - The <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-endpoint-overview?tabs=portal#create-a-data-collection-endpoint">data collection endpoint</a>.
 * See {@link LogsIngestionClientBuilder#endpoint(String) endpoint} method for more details.</li>
 * <li>{@code credential} - The AAD authentication credential that has the "Monitoring Metrics Publisher" role assigned to it.
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity</a>
 * provides a variety of AAD credential types that can be used. See
 * {@link LogsIngestionClientBuilder#credential(TokenCredential) credential} method for more details.</li>
 * </ol>
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
 *
 * <h3>Client Usage</h3>
 *
 * <p>
 *     For additional information on how to use this client, see the following method documentation:
 * </p>
 *
 * <ul>
 *     <li>
 *         {@link #upload(String, String, Iterable) upload(String, String, Iterable)} - Uploads logs to a Log Analytics
 *         workspace.
 *     </li>
 *     <li>
 *         {@link #upload(String, String, Iterable, LogsUploadOptions) upload(String, String, Iterable, LogsUploadOptions)}
 *         - Uploads logs to a Log Analytics workspace with options to configure the upload request.
 *     </li>
 *     <li>
 *         {@link #uploadWithResponse(String, String, BinaryData, RequestOptions) uploadWithResponse(String, String, BinaryData, RequestOptions)}
 *         - Uploads logs to a Log Analytics workspace with options to configure the HTTP request.
 *     </li>
 * </ul>
 *
 * @see LogsIngestionClientBuilder
 * @see LogsIngestionAsyncClient
 * @see com.azure.monitor.ingestion
 */
@ServiceClient(builder = LogsIngestionClientBuilder.class)
public final class LogsIngestionClient implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(LogsIngestionClient.class);
    private final IngestionUsingDataCollectionRulesClient client;

    // dynamic thread pool that scales up and down on demand.
    private final ExecutorService threadPool;
    private final Thread shutdownHook;

    /**
     * Creates a {@link LogsIngestionClient} that sends requests to the data collection endpoint.
     *
     * @param client The {@link IngestionUsingDataCollectionRulesClient} that the client routes its request through.
     */
    LogsIngestionClient(IngestionUsingDataCollectionRulesClient client) {
        this.client = client;
        this.threadPool = createThreadPool();
        this.shutdownHook = registerShutdownHook(this.threadPool, 5);
    }

    /**
     * Uploads logs to Azure Monitor with specified data collection rule id and stream name. The input logs may be
     * too large to be sent as a single request to the Azure Monitor service. In such cases, this method will split
     * the input logs into multiple smaller requests before sending to the service. This method will block until all
     * the logs are uploaded or an error occurs.
     *
     * <p>
     * Each log in the input collection must be a valid JSON object. The JSON object should match the
     * <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-structure#streamdeclarations">schema defined
     * by the stream name</a>. The stream's schema can be found in the Azure portal.
     * </p>
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
     * the input logs into multiple smaller requests before sending to the service. This method will block until all
     * the logs are uploaded or an error occurs. If an
     * {@link LogsUploadOptions#setLogsUploadErrorConsumer(Consumer) error handler} is set, then the service errors are
     * surfaced to the error handler and this method won't throw an exception.
     *
     * <p>
     * Each log in the input collection must be a valid JSON object. The JSON object should match the
     * <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-structure#streamdeclarations">schema defined
     * by the stream name</a>. The stream's schema can be found in the Azure portal.
     * </p>
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
     *
     * @param ruleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @param options the options to configure the upload request.
     * @throws NullPointerException if any of {@code ruleId}, {@code streamName} or {@code logs} are null.
     * @throws IllegalArgumentException if {@code logs} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void upload(String ruleId, String streamName, Iterable<Object> logs, LogsUploadOptions options) {
        upload(ruleId, streamName, logs, options, Context.NONE);
    }

    /**
     * Uploads logs to Azure Monitor with specified data collection rule id and stream name. The input logs may be
     * too large to be sent as a single request to the Azure Monitor service. In such cases, this method will split
     * the input logs into multiple smaller requests before sending to the service. This method will block until all
     * the logs are uploaded or an error occurs. If an
     * {@link LogsUploadOptions#setLogsUploadErrorConsumer(Consumer) error handler} is set, then the service errors are
     * surfaced to the error handler and this method won't throw an exception.
     *
     * <p>
     * Each log in the input collection must be a valid JSON object. The JSON object should match the
     * <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-structure#streamdeclarations">schema defined
     * by the stream name</a>. The stream's schema can be found in the Azure portal.
     * </p>
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
    public void upload(String ruleId, String streamName, Iterable<Object> logs, LogsUploadOptions options,
        Context context) {
        Objects.requireNonNull(ruleId, "'ruleId' cannot be null.");
        Objects.requireNonNull(streamName, "'streamName' cannot be null.");
        Objects.requireNonNull(logs, "'logs' cannot be null.");

        Consumer<LogsUploadError> uploadLogsErrorConsumer = options == null
            ? null
            : options.getLogsUploadErrorConsumer();

        RequestOptions requestOptions = new RequestOptions();
        requestOptions.addHeader(HttpHeaderName.CONTENT_ENCODING, GZIP);
        requestOptions.setContext(context);

        Stream<UploadLogsResponseHolder> responses = new Batcher(options, logs).toStream()
            .map(r -> uploadToService(ruleId, streamName, requestOptions, r));

        responses = submit(responses, getConcurrency(options)).filter(response -> response.getException() != null);

        if (uploadLogsErrorConsumer != null) {
            responses.forEach(response -> uploadLogsErrorConsumer.accept(
                new LogsUploadError(response.getException(), response.getRequest().getLogs())));
            return;
        }

        final int[] failedLogCount = new int[1];
        List<HttpResponseException> exceptions = responses.map(response -> {
            failedLogCount[0] += response.getRequest().getLogs().size();
            return response.getException();
        }).collect(Collectors.toList());

        if (!exceptions.isEmpty()) {
            throw LOGGER.logExceptionAsError(new LogsUploadException(exceptions, failedLogCount[0]));
        }
    }

    private Stream<UploadLogsResponseHolder> submit(Stream<UploadLogsResponseHolder> responseStream, int concurrency) {
        if (concurrency == 1) {
            return responseStream;
        }

        try {
            return threadPool.submit(() -> responseStream).get();
        } catch (InterruptedException | ExecutionException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    private UploadLogsResponseHolder uploadToService(String ruleId, String streamName, RequestOptions requestOptions,
        LogsIngestionRequest request) {
        HttpResponseException exception = null;
        try {
            client.uploadWithResponse(ruleId, streamName, BinaryData.fromBytes(request.getRequestBody()),
                requestOptions);
        } catch (HttpResponseException ex) {
            exception = ex;
        }

        return new UploadLogsResponseHolder(request, exception);
    }

    /**
     * This method is used to upload logs to Azure Monitor Log Analytics with specified data collection rule id and
     * stream name. This upload method provides a more granular control of the HTTP request sent to the service. Use
     * {@link RequestOptions} to configure the HTTP request.
     *
     * <p>
     * The input logs should be a JSON array with each element in the array
     * matching the <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-structure#streamdeclarations">schema defined
     * by the stream name</a>. The stream's schema can be found in the Azure portal. This content will be gzipped before
     * sending to the service. If the content is already gzipped, then set the {@code Content-Encoding} header to
     * {@code gzip} using {@link RequestOptions#setHeader(HttpHeaderName, String) requestOptions} and pass the content
     * as is.
     * </p>
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
     * @return the {@link Response}.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> uploadWithResponse(String ruleId, String streamName, BinaryData logs,
        RequestOptions requestOptions) {
        Objects.requireNonNull(ruleId, "'ruleId' cannot be null.");
        Objects.requireNonNull(streamName, "'streamName' cannot be null.");
        Objects.requireNonNull(logs, "'logs' cannot be null.");

        if (requestOptions == null) {
            requestOptions = new RequestOptions();
        }

        requestOptions.addRequestCallback(request -> {
            HttpHeader httpHeader = request.getHeaders().get(HttpHeaderName.CONTENT_ENCODING);
            if (httpHeader == null) {
                BinaryData gzippedRequest = BinaryData.fromBytes(gzipRequest(logs.toBytes()));
                request.setBody(gzippedRequest);
                request.setHeader(HttpHeaderName.CONTENT_ENCODING, GZIP);
            }
        });
        return client.uploadWithResponse(ruleId, streamName, logs, requestOptions);
    }

    @Override
    public void close() {
        threadPool.shutdown();
        Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }
}
