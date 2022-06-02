package com.azure.monitor.ingestion;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.monitor.ingestion.implementation.IngestionUsingDataCollectionRulesImpl;
import com.azure.monitor.ingestion.models.SendLogsOptions;
import com.azure.monitor.ingestion.models.SendLogsResult;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 *
 */
@ServiceClient(isAsync = true, builder = LogsIngestionClientBuilder.class)
public final class LogsIngestionAsyncClient {

    LogsIngestionAsyncClient(IngestionUsingDataCollectionRulesImpl innerClient) {
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
    public <T> Mono<Response<SendLogsResult>> sendLogsWithResponse(String dataCollectionRuleId, String streamDeclaration,
                                                                   List<T> logs, SendLogsOptions options, Context context) {
        return Mono.empty();
    }

    /**
     * @param dataCollectionRuleId
     * @param streamDeclaration
     * @param logs
     * @param <T>
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<SendLogsResult> sendLogs(String dataCollectionRuleId, String streamDeclaration, List<T> logs) {
        return Mono.empty();
    }
}
