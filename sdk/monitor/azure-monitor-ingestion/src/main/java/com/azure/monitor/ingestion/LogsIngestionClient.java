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
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.monitor.ingestion.models.UploadLogsOptions;
import com.azure.monitor.ingestion.models.UploadLogsResult;

import java.util.List;

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

    private final LogsIngestionAsyncClient asyncClient;

    LogsIngestionClient(LogsIngestionAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
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
     * UploadLogsResult result = logsIngestionClient.upload&#40;&quot;&lt;data-collection-rule-id&gt;&quot;, &quot;&lt;stream-name&gt;&quot;, logs&#41;;
     * System.out.println&#40;&quot;Logs upload result status &quot; + result.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.monitor.ingestion.LogsIngestionClient.upload -->
     *
     * @param dataCollectionRuleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @return the result of the logs upload request.
     * @throws NullPointerException if any of {@code dataCollectionRuleId}, {@code streamName} or {@code logs} are null.
     * @throws IllegalArgumentException if {@code logs} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadLogsResult upload(String dataCollectionRuleId, String streamName, List<Object> logs) {
        return asyncClient.upload(dataCollectionRuleId, streamName, logs).block();
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
     * UploadLogsOptions uploadLogsOptions = new UploadLogsOptions&#40;&#41;.setMaxConcurrency&#40;4&#41;;
     * UploadLogsResult result = logsIngestionClient.upload&#40;&quot;&lt;data-collection-rule-id&gt;&quot;, &quot;&lt;stream-name&gt;&quot;, logs,
     *         uploadLogsOptions, Context.NONE&#41;;
     * System.out.println&#40;&quot;Logs upload result status &quot; + result.getStatus&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.monitor.ingestion.LogsIngestionClient.uploadWithConcurrency -->
     * @param dataCollectionRuleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @param options the options to configure the upload request.
     * @return the result of the logs upload request.
     * @throws NullPointerException if any of {@code dataCollectionRuleId}, {@code streamName} or {@code logs} are null.
     * @throws IllegalArgumentException if {@code logs} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadLogsResult upload(String dataCollectionRuleId, String streamName,
                                   List<Object> logs, UploadLogsOptions options) {
        return asyncClient.upload(dataCollectionRuleId, streamName, logs, options, Context.NONE).block();
    }

    /**
     * Uploads logs to Azure Monitor with specified data collection rule id and stream name. The input logs may be
     * too large to be sent as a single request to the Azure Monitor service. In such cases, this method will split
     * the input logs into multiple smaller requests before sending to the service.
     *
     * @param dataCollectionRuleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @param options the options to configure the upload request.
     * @param context additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return the result of the logs upload request.
     * @throws NullPointerException if any of {@code dataCollectionRuleId}, {@code streamName} or {@code logs} are null.
     * @throws IllegalArgumentException if {@code logs} is empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadLogsResult upload(String dataCollectionRuleId, String streamName,
                                                         List<Object> logs, UploadLogsOptions options, Context context) {
        return asyncClient.upload(dataCollectionRuleId, streamName, logs, options, context).block();
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
     * @param dataCollectionRuleId The immutable Id of the Data Collection Rule resource.
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
            String dataCollectionRuleId, String streamName, BinaryData logs, RequestOptions requestOptions) {
        return asyncClient.uploadWithResponse(dataCollectionRuleId, streamName, logs, requestOptions).block();
    }
}
