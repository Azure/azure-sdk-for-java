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
import com.azure.monitor.ingestion.implementation.Batcher;
import com.azure.monitor.ingestion.implementation.IngestionUsingDataCollectionRulesAsyncClient;
import com.azure.monitor.ingestion.implementation.IngestionUsingDataCollectionRulesClient;
import com.azure.monitor.ingestion.implementation.LogsIngestionRequest;
import com.azure.monitor.ingestion.implementation.UploadLogsResponseHolder;
import com.azure.monitor.ingestion.models.LogsUploadError;
import com.azure.monitor.ingestion.models.LogsUploadException;
import com.azure.monitor.ingestion.models.LogsUploadOptions;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.monitor.ingestion.implementation.Utils.GZIP;
import static com.azure.monitor.ingestion.implementation.Utils.getConcurrency;
import static com.azure.monitor.ingestion.implementation.Utils.gzipRequest;

/**
 * <p>This class provides an asynchronous client for uploading custom logs to an Azure Monitor Log Analytics workspace.
 * This client encapsulates REST API calls, used to send data to a Log Analytics workspace, into a set of asynchronous
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
 * <li>{@code credential} - The AAD authentication credential that has the "Monitoring Metrics Publisher" role assigned
 * to it.
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity</a>
 * provides a variety of AAD credential types that can be used. See
 * {@link LogsIngestionClientBuilder#credential(TokenCredential) credential} method for more details.</li>
 * </ol>
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
 *
 * <h3>Client Usage</h3>
 *
 * <p>
 *     For additional information on how to use this client, see the following method documentation:
 * </p>
 *
 * <ul>
 *     <li>
 *         {@link #upload(String, String, Iterable) upload(String, String, Iterable)} - Uploads
 *         logs to a Log Analytics workspace.
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
 * @see LogsIngestionClient
 * @see com.azure.monitor.ingestion
 */
@ServiceClient(isAsync = true, builder = LogsIngestionClientBuilder.class)
public final class LogsIngestionAsyncClient {
    private final IngestionUsingDataCollectionRulesAsyncClient service;

    /**
     * Creates a {@link LogsIngestionAsyncClient} that sends requests to the data collection endpoint.
     *
     * @param service The {@link IngestionUsingDataCollectionRulesClient} that the client routes its request through.
     */
    LogsIngestionAsyncClient(IngestionUsingDataCollectionRulesAsyncClient service) {
        this.service = service;
    }

    /**
     * Uploads logs to Azure Monitor with specified data collection rule id and stream name. The input logs may be
     * too large to be sent as a single request to the Azure Monitor service. In such cases, this method will split
     * the input logs into multiple smaller requests before sending to the service.
     *
     * <p>
     * Each log in the input collection must be a valid JSON object. The JSON object should match the
     * <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-structure#streamdeclarations">schema defined
     * by the stream name</a>. The stream's schema can be found in the Azure portal.
     * </p>
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
     * the input logs into multiple smaller requests before sending to the service. If an
     * {@link LogsUploadOptions#setLogsUploadErrorConsumer(Consumer) error handler} is set,
     * then the service errors are surfaced to the error handler and the subscriber of this method won't receive an
     * error signal.
     *
     * <p>
     * Each log in the input collection must be a valid JSON object. The JSON object should match the
     * <a href="https://learn.microsoft.com/azure/azure-monitor/essentials/data-collection-rule-structure#streamdeclarations">schema defined
     * by the stream name</a>. The stream's schema can be found in the Azure portal.
     * </p>
     *
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
    public Mono<Void> upload(String ruleId, String streamName, Iterable<Object> logs, LogsUploadOptions options) {
        return withContext(context -> upload(ruleId, streamName, logs, options, context));
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
     * @return the {@link Response} on successful completion of {@link Mono}.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws ClientAuthenticationException thrown if the request is rejected by server on status code 401.
     * @throws ResourceNotFoundException thrown if the request is rejected by server on status code 404.
     * @throws ResourceModifiedException thrown if the request is rejected by server on status code 409.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> uploadWithResponse(String ruleId, String streamName, BinaryData logs,
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
        return service.uploadWithResponse(ruleId, streamName, logs, requestOptions);
    }

    Mono<Void> upload(String ruleId, String streamName, Iterable<Object> logs, LogsUploadOptions options,
        Context context) {
        return Mono.defer(() -> splitAndUpload(ruleId, streamName, logs, options, context));
    }

    /**
     * This method splits the input logs into < 1MB HTTP requests and uploads to the Azure Monitor service.
     *
     * @param ruleId The data collection rule id.
     * @param streamName The stream name configured in the data collection rule.
     * @param logs The input logs to upload.
     * @param options The options to configure the upload request.
     * @param context additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return the {@link Mono} that completes on completion of the upload request.
     */
    private Mono<Void> splitAndUpload(String ruleId, String streamName, Iterable<Object> logs,
        LogsUploadOptions options, Context context) {

        int concurrency = getConcurrency(options);

        return new Batcher(options, logs).toFlux()
            .flatMapSequential(request -> uploadToService(ruleId, streamName, context, request), concurrency)
            .<LogsUploadException>handle((responseHolder, sink) -> processResponse(options, responseHolder, sink))
            .collectList()
            .handle(this::processExceptions);
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

    private void processResponse(LogsUploadOptions options, UploadLogsResponseHolder responseHolder,
        SynchronousSink<LogsUploadException> sink) {
        if (responseHolder.getException() != null) {
            Consumer<LogsUploadError> uploadLogsErrorConsumer = null;
            if (options != null) {
                uploadLogsErrorConsumer = options.getLogsUploadErrorConsumer();
            }
            if (uploadLogsErrorConsumer != null) {
                uploadLogsErrorConsumer
                    .accept(new LogsUploadError(responseHolder.getException(), responseHolder.getRequest().getLogs()));
                return;
            }
            // emit the responseHolder without the original logs only if there's an error and there's no
            // error consumer
            sink.next(new LogsUploadException(Collections.singletonList(responseHolder.getException()),
                responseHolder.getRequest().getLogs().size()));
        }
    }

    private Mono<UploadLogsResponseHolder> uploadToService(String ruleId, String streamName, Context context,
        LogsIngestionRequest request) {
        RequestOptions requestOptions
            = new RequestOptions().addHeader(HttpHeaderName.CONTENT_ENCODING, GZIP).setContext(context);
        return service
            .uploadWithResponse(ruleId, streamName, BinaryData.fromBytes(request.getRequestBody()), requestOptions)
            .map(response -> new UploadLogsResponseHolder(null, null))
            .onErrorResume(HttpResponseException.class,
                ex -> Mono.fromSupplier(() -> new UploadLogsResponseHolder(request, ex)));
    }
}
