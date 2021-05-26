// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.integration.servicebus.converter;

import com.azure.spring.integration.core.AzureHeaders;

/**
 * Azure service bus message headers.
 *
 * <p>
 * Usage example:
 * </p>
 *
 * <pre>{@code
 * import org.springframework.integration.support.MessageBuilder;
 * MessageBuilder.withPayload(payload)
 *               .setHeader(ServiceBusMessageHeaders.MESSAGE_ID, ...)
 *               .build();
 * }</pre>
 *
 * There are 16 items can be set for service bus IMessage.
 * <ul>
 *   <li> 2 items are deprecated: ScheduledEnqueuedTimeUtc, Body. </li>
 *   <li> 3 items should be set by Spring message: ContentType, MessageBody. ReplyTo. </li>
 *   <li> 1 item should not be set: Properties. </li>
 *   <li> The rest 10 items can be set by Spring message header: MessageId, TimeToLive, ScheduledEnqueueTimeUtc, SessionId, CorrelationId, To, Label, ReplyToSessionId, PartitionKey, ViaPartitionKey. </li>
 * </ul>
 *
 * There are 11 items can be set for ServiceBusMessage.
 * <ul>
 *   <li> 2 item should be set by Spring message: ContentType, ReplyTo.  </li>
 *   <li> The rest 9 items can be set by Spring message header: CorrelationId, Subject, MessageId, PartitionKey, To, TimeToLive, ScheduledEnqueueTime, ReplyToSessionId, SessionId </li>
 * </ul>
 *
 * For the items can be set by Spring message header there are 8 items can be set by both IMessage and ServiceBusMessage:
 * CorrelationId, MessageId, PartitionKey, To, TimeToLive, ScheduledEnqueueTime, ReplyToSessionId, SessionId
 *
 * @see com.azure.messaging.servicebus.ServiceBusMessage
 * @see <a href="https://github.com/Azure/azure-sdk-for-java/blob/azure-messaging-servicebus_7.1.0/sdk/servicebus/azure-messaging-servicebus/src/main/java/com/azure/messaging/servicebus/ServiceBusMessage.java">com.azure.messaging.servicebus.ServiceBusMessage</a>
 */
public class ServiceBusMessageHeaders extends AzureHeaders {

    private static final String PREFIX = AzureHeaders.PREFIX + "service_bus_";

    public static final String CORRELATION_ID = PREFIX + "correlation_id";
    public static final String MESSAGE_ID = PREFIX + "message_id";
    public static final String PARTITION_KEY = PREFIX + "partition_key";
    public static final String TO = PREFIX + "to";
    public static final String TIME_TO_LIVE = PREFIX + "time_to_live";
    // expected type is Instant
    public static final String SCHEDULED_ENQUEUE_TIME = PREFIX + "scheduled_enqueue_time";
    public static final String REPLY_TO_SESSION_ID = PREFIX + "reply_to_session_id";
    public static final String SESSION_ID = PREFIX + "session_id";
    public static final String RECEIVED_MESSAGE_CONTEXT = PREFIX + "received_message_context";
}
