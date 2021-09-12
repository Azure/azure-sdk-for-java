package com.azure.spring.integration.eventhub.api;


import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.InitializationContext;

public interface EventProcessorListener {

    void onError(ErrorContext errorContext);

    void onEvent(EventContext eventContext);

    void onEventBatch(EventBatchContext eventBatchContext);

    void onPartitionClose(CloseContext closeContext);

    void onInitialization(InitializationContext initializationContext);

}
