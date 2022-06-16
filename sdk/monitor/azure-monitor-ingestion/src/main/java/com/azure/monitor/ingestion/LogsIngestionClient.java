// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.Context;
import com.azure.monitor.ingestion.models.UploadLogsOptions;
import com.azure.monitor.ingestion.models.UploadLogsResult;

import java.util.List;

/**
 * The synchronous client for uploading logs to Azure Monitor.
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
     * @param dataCollectionRuleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @return the result of the logs upload request.
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
     * @param dataCollectionRuleId the data collection rule id that is configured to collect and transform the logs.
     * @param streamName the stream name configured in data collection rule that matches defines the structure of the
     * logs sent in this request.
     * @param logs the collection of logs to be uploaded.
     * @param options the options to configure the upload request.
     * @param context additional context that is passed through the Http pipeline during the service call. If no
     * additional context is required, pass {@link Context#NONE} instead.
     * @return the result of the logs upload request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadLogsResult upload(String dataCollectionRuleId, String streamName,
                                                         List<Object> logs, UploadLogsOptions options, Context context) {
        return asyncClient.upload(dataCollectionRuleId, streamName, logs, options, context).block();
    }
}
