package com.azure.spring.integration.servicebus.converter;

public class ServiceBusMessageHeaders {

    /**
     * There are 16 items can be set for service bus IMessage.
     * (1). 2 items are deprecated: ScheduledEnqueuedTimeUtc, Body.
     * (2). 3 items should be set by Spring message: ContentType, MessageBody. ReplyTo
     * (3). 1 item should not be set: Properties.
     * (4). The rest 10 items can be set by Spring message header:
     */
    public static final String MESSAGE_ID = "azure_service_bus_message_id";
    public static final String TIME_TO_LIVE = "azure_service_bus_time_to_live";
    public static final String SCHEDULED_ENQUEUE_TIME_UTC = "azure_service_bus_scheduled_enqueue_time_utc";
    public static final String SESSION_ID = "azure_service_bus_session_id";
    public static final String CORRELATION_ID = "azure_service_bus_correlation_id";
    public static final String TO = "azure_service_bus_to";
    public static final String LABEL = "azure_service_bus_label";
    public static final String REPLY_TO_SESSION_ID = "azure_service_bus_reply_to_session_id";
    public static final String PARTITION_KEY = "azure_service_bus_partition_key";
    public static final String VIA_PARTITION_KEY = "azure_service_bus_via_partition_key";
}
