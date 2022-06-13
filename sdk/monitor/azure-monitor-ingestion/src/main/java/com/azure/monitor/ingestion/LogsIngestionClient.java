package com.azure.monitor.ingestion;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.Context;
import com.azure.monitor.ingestion.models.UploadLogsOptions;
import com.azure.monitor.ingestion.models.UploadLogsResult;

import java.util.List;

/**
 *
 */
@ServiceClient(builder = LogsIngestionClientBuilder.class)
public final class LogsIngestionClient {

    private final LogsIngestionAsyncClient asyncClient;

    LogsIngestionClient(LogsIngestionAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * @param dataCollectionRuleId
     * @param streamName
     * @param logs
     * @param options
     * @param context
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadLogsResult upload(String dataCollectionRuleId, String streamName,
                                                         List<Object> logs, UploadLogsOptions options, Context context) {
        return asyncClient.upload(dataCollectionRuleId, streamName, logs, options, context).block();
    }

    /**
     * @param dataCollectionRuleId
     * @param streamName
     * @param logs
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UploadLogsResult upload(String dataCollectionRuleId, String streamName, List<Object> logs) {
        return upload(dataCollectionRuleId, streamName, logs);
    }
}
