package com.azure.monitor.ingestion;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.ingestion.models.SendLogsOptions;
import com.azure.monitor.ingestion.models.SendLogsResult;

import java.util.List;

/**
 *
 */
@ServiceClient(builder = LogsIngestionClientBuilder.class)
public final class LogsIngestionClient {

    LogsIngestionClient(LogsIngestionAsyncClient asyncClient) {

    }

    /**
     * @param dataCollectionRuleId
     * @param streamDeclaration
     * @param logs
     * @param options
     * @param context
     * @param <T>
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Response<SendLogsResult> sendLogsWithResponse(String dataCollectionRuleId, String streamDeclaration,
                                                             List<T> logs, SendLogsOptions options, Context context) {
        return null;
    }

    /**
     * @param dataCollectionRuleId
     * @param streamDeclaration
     * @param logs
     * @param <T>
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> SendLogsResult sendLogs(String dataCollectionRuleId, String streamDeclaration, List<T> logs) {
        return null;
    }
}
