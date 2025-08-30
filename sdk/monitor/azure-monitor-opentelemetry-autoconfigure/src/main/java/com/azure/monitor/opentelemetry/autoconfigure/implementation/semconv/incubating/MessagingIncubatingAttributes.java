// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.azure.monitor.opentelemetry.autoconfigure.implementation.semconv.incubating;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import java.util.List;

// this is a copy of io.opentelemetry.semconv.incubating.MessagingIncubatingAttributes (1.37.0)

// DO NOT EDIT, this is an Auto-generated file from
// buildscripts/templates/registry/incubating_java/IncubatingSemanticAttributes.java.j2
@SuppressWarnings("unused")
public final class MessagingIncubatingAttributes {
    /**
     * The number of messages sent, received, or processed in the scope of the batching operation.
     *
     * <p>Notes:
     *
     * <p>Instrumentations SHOULD NOT set {@code messaging.batch.message_count} on spans that operate
     * with a single message. When a messaging client library supports both batch and single-message
     * API for the same operation, instrumentations SHOULD use {@code messaging.batch.message_count}
     * for batching APIs and SHOULD NOT use it for single-message APIs.
     */
    public static final AttributeKey<Long> MESSAGING_BATCH_MESSAGE_COUNT = longKey("messaging.batch.message_count");

    /** A unique identifier for the client that consumes or produces a message. */
    public static final AttributeKey<String> MESSAGING_CLIENT_ID = stringKey("messaging.client.id");

    /**
     * The name of the consumer group with which a consumer is associated.
     *
     * <p>Notes:
     *
     * <p>Semantic conventions for individual messaging systems SHOULD document whether {@code
     * messaging.consumer.group.name} is applicable and what it means in the context of that system.
     */
    public static final AttributeKey<String> MESSAGING_CONSUMER_GROUP_NAME = stringKey("messaging.consumer.group.name");

    /**
     * A boolean that is true if the message destination is anonymous (could be unnamed or have
     * auto-generated name).
     */
    public static final AttributeKey<Boolean> MESSAGING_DESTINATION_ANONYMOUS
        = booleanKey("messaging.destination.anonymous");

    /**
     * The message destination name
     *
     * <p>Notes:
     *
     * <p>Destination name SHOULD uniquely identify a specific queue, topic or other entity within the
     * broker. If the broker doesn't have such notion, the destination name SHOULD uniquely identify
     * the broker.
     */
    public static final AttributeKey<String> MESSAGING_DESTINATION_NAME = stringKey("messaging.destination.name");

    /**
     * The identifier of the partition messages are sent to or received from, unique within the {@code
     * messaging.destination.name}.
     */
    public static final AttributeKey<String> MESSAGING_DESTINATION_PARTITION_ID
        = stringKey("messaging.destination.partition.id");

    /**
     * The name of the destination subscription from which a message is consumed.
     *
     * <p>Notes:
     *
     * <p>Semantic conventions for individual messaging systems SHOULD document whether {@code
     * messaging.destination.subscription.name} is applicable and what it means in the context of that
     * system.
     */
    public static final AttributeKey<String> MESSAGING_DESTINATION_SUBSCRIPTION_NAME
        = stringKey("messaging.destination.subscription.name");

    /**
     * Low cardinality representation of the messaging destination name
     *
     * <p>Notes:
     *
     * <p>Destination names could be constructed from templates. An example would be a destination
     * name involving a user name or product id. Although the destination name in this case is of high
     * cardinality, the underlying template is of low cardinality and can be effectively used for
     * grouping and aggregation.
     */
    public static final AttributeKey<String> MESSAGING_DESTINATION_TEMPLATE
        = stringKey("messaging.destination.template");

    /**
     * A boolean that is true if the message destination is temporary and might not exist anymore
     * after messages are processed.
     */
    public static final AttributeKey<Boolean> MESSAGING_DESTINATION_TEMPORARY
        = booleanKey("messaging.destination.temporary");

    /**
     * Deprecated, no replacement at this time.
     *
     * @deprecated Removed. No replacement at this time.
     */
    @Deprecated
    public static final AttributeKey<Boolean> MESSAGING_DESTINATION_PUBLISH_ANONYMOUS
        = booleanKey("messaging.destination_publish.anonymous");

    /**
     * Deprecated, no replacement at this time.
     *
     * @deprecated Removed. No replacement at this time.
     */
    @Deprecated
    public static final AttributeKey<String> MESSAGING_DESTINATION_PUBLISH_NAME
        = stringKey("messaging.destination_publish.name");

    /**
     * Deprecated, use {@code messaging.consumer.group.name} instead.
     *
     * @deprecated Replaced by {@code messaging.consumer.group.name}.
     */
    @Deprecated
    public static final AttributeKey<String> MESSAGING_EVENTHUBS_CONSUMER_GROUP
        = stringKey("messaging.eventhubs.consumer.group");

    /** The UTC epoch seconds at which the message has been accepted and stored in the entity. */
    public static final AttributeKey<Long> MESSAGING_EVENTHUBS_MESSAGE_ENQUEUED_TIME
        = longKey("messaging.eventhubs.message.enqueued_time");

    /** The ack deadline in seconds set for the modify ack deadline request. */
    public static final AttributeKey<Long> MESSAGING_GCP_PUBSUB_MESSAGE_ACK_DEADLINE
        = longKey("messaging.gcp_pubsub.message.ack_deadline");

    /** The ack id for a given message. */
    public static final AttributeKey<String> MESSAGING_GCP_PUBSUB_MESSAGE_ACK_ID
        = stringKey("messaging.gcp_pubsub.message.ack_id");

    /** The delivery attempt for a given message. */
    public static final AttributeKey<Long> MESSAGING_GCP_PUBSUB_MESSAGE_DELIVERY_ATTEMPT
        = longKey("messaging.gcp_pubsub.message.delivery_attempt");

    /**
     * The ordering key for a given message. If the attribute is not present, the message does not
     * have an ordering key.
     */
    public static final AttributeKey<String> MESSAGING_GCP_PUBSUB_MESSAGE_ORDERING_KEY
        = stringKey("messaging.gcp_pubsub.message.ordering_key");

    /**
     * Deprecated, use {@code messaging.consumer.group.name} instead.
     *
     * @deprecated Replaced by {@code messaging.consumer.group.name}.
     */
    @Deprecated
    public static final AttributeKey<String> MESSAGING_KAFKA_CONSUMER_GROUP
        = stringKey("messaging.kafka.consumer.group");

    /**
     * Deprecated, use {@code messaging.destination.partition.id} instead.
     *
     * @deprecated Replaced by {@code messaging.destination.partition.id}.
     */
    @Deprecated
    public static final AttributeKey<Long> MESSAGING_KAFKA_DESTINATION_PARTITION
        = longKey("messaging.kafka.destination.partition");

    /**
     * Message keys in Kafka are used for grouping alike messages to ensure they're processed on the
     * same partition. They differ from {@code messaging.message.id} in that they're not unique. If
     * the key is {@code null}, the attribute MUST NOT be set.
     *
     * <p>Notes:
     *
     * <p>If the key type is not string, it's string representation has to be supplied for the
     * attribute. If the key has no unambiguous, canonical string form, don't include its value.
     */
    public static final AttributeKey<String> MESSAGING_KAFKA_MESSAGE_KEY = stringKey("messaging.kafka.message.key");

    /**
     * Deprecated, use {@code messaging.kafka.offset} instead.
     *
     * @deprecated Replaced by {@code messaging.kafka.offset}.
     */
    @Deprecated
    public static final AttributeKey<Long> MESSAGING_KAFKA_MESSAGE_OFFSET = longKey("messaging.kafka.message.offset");

    /** A boolean that is true if the message is a tombstone. */
    public static final AttributeKey<Boolean> MESSAGING_KAFKA_MESSAGE_TOMBSTONE
        = booleanKey("messaging.kafka.message.tombstone");

    /** The offset of a record in the corresponding Kafka partition. */
    public static final AttributeKey<Long> MESSAGING_KAFKA_OFFSET = longKey("messaging.kafka.offset");

    /**
     * The size of the message body in bytes.
     *
     * <p>Notes:
     *
     * <p>This can refer to both the compressed or uncompressed body size. If both sizes are known,
     * the uncompressed body size should be used.
     */
    public static final AttributeKey<Long> MESSAGING_MESSAGE_BODY_SIZE = longKey("messaging.message.body.size");

    /**
     * The conversation ID identifying the conversation to which the message belongs, represented as a
     * string. Sometimes called "Correlation ID".
     */
    public static final AttributeKey<String> MESSAGING_MESSAGE_CONVERSATION_ID
        = stringKey("messaging.message.conversation_id");

    /**
     * The size of the message body and metadata in bytes.
     *
     * <p>Notes:
     *
     * <p>This can refer to both the compressed or uncompressed size. If both sizes are known, the
     * uncompressed size should be used.
     */
    public static final AttributeKey<Long> MESSAGING_MESSAGE_ENVELOPE_SIZE = longKey("messaging.message.envelope.size");

    /**
     * A value used by the messaging system as an identifier for the message, represented as a string.
     */
    public static final AttributeKey<String> MESSAGING_MESSAGE_ID = stringKey("messaging.message.id");

    /**
     * Deprecated, use {@code messaging.operation.type} instead.
     *
     * @deprecated Replaced by {@code messaging.operation.type}.
     */
    @Deprecated
    public static final AttributeKey<String> MESSAGING_OPERATION = stringKey("messaging.operation");

    /** The system-specific name of the messaging operation. */
    public static final AttributeKey<String> MESSAGING_OPERATION_NAME = stringKey("messaging.operation.name");

    /**
     * A string identifying the type of the messaging operation.
     *
     * <p>Notes:
     *
     * <p>If a custom value is used, it MUST be of low cardinality.
     */
    public static final AttributeKey<String> MESSAGING_OPERATION_TYPE = stringKey("messaging.operation.type");

    /** RabbitMQ message routing key. */
    public static final AttributeKey<String> MESSAGING_RABBITMQ_DESTINATION_ROUTING_KEY
        = stringKey("messaging.rabbitmq.destination.routing_key");

    /** RabbitMQ message delivery tag */
    public static final AttributeKey<Long> MESSAGING_RABBITMQ_MESSAGE_DELIVERY_TAG
        = longKey("messaging.rabbitmq.message.delivery_tag");

    /**
     * Deprecated, use {@code messaging.consumer.group.name} instead.
     *
     * @deprecated Replaced by {@code messaging.consumer.group.name} on the consumer spans. No
     *     replacement for producer spans.
     */
    @Deprecated
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_CLIENT_GROUP
        = stringKey("messaging.rocketmq.client_group");

    /** Model of message consumption. This only applies to consumer spans. */
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_CONSUMPTION_MODEL
        = stringKey("messaging.rocketmq.consumption_model");

    /** The delay time level for delay message, which determines the message delay time. */
    public static final AttributeKey<Long> MESSAGING_ROCKETMQ_MESSAGE_DELAY_TIME_LEVEL
        = longKey("messaging.rocketmq.message.delay_time_level");

    /**
     * The timestamp in milliseconds that the delay message is expected to be delivered to consumer.
     */
    public static final AttributeKey<Long> MESSAGING_ROCKETMQ_MESSAGE_DELIVERY_TIMESTAMP
        = longKey("messaging.rocketmq.message.delivery_timestamp");

    /**
     * It is essential for FIFO message. Messages that belong to the same message group are always
     * processed one by one within the same consumer group.
     */
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_MESSAGE_GROUP
        = stringKey("messaging.rocketmq.message.group");

    /** Key(s) of message, another way to mark message besides message id. */
    public static final AttributeKey<List<String>> MESSAGING_ROCKETMQ_MESSAGE_KEYS
        = stringArrayKey("messaging.rocketmq.message.keys");

    /** The secondary classifier of message besides topic. */
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_MESSAGE_TAG
        = stringKey("messaging.rocketmq.message.tag");

    /** Type of message. */
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_MESSAGE_TYPE
        = stringKey("messaging.rocketmq.message.type");

    /** Namespace of RocketMQ resources, resources in different namespaces are individual. */
    public static final AttributeKey<String> MESSAGING_ROCKETMQ_NAMESPACE = stringKey("messaging.rocketmq.namespace");

    /**
     * Deprecated, use {@code messaging.destination.subscription.name} instead.
     *
     * @deprecated Replaced by {@code messaging.destination.subscription.name}.
     */
    @Deprecated
    public static final AttributeKey<String> MESSAGING_SERVICEBUS_DESTINATION_SUBSCRIPTION_NAME
        = stringKey("messaging.servicebus.destination.subscription_name");

    /**
     * Describes the <a
     * href="https://learn.microsoft.com/azure/service-bus-messaging/message-transfers-locks-settlement#peeklock">settlement
     * type</a>.
     */
    public static final AttributeKey<String> MESSAGING_SERVICEBUS_DISPOSITION_STATUS
        = stringKey("messaging.servicebus.disposition_status");

    /** Number of deliveries that have been attempted for this message. */
    public static final AttributeKey<Long> MESSAGING_SERVICEBUS_MESSAGE_DELIVERY_COUNT
        = longKey("messaging.servicebus.message.delivery_count");

    /** The UTC epoch seconds at which the message has been accepted and stored in the entity. */
    public static final AttributeKey<Long> MESSAGING_SERVICEBUS_MESSAGE_ENQUEUED_TIME
        = longKey("messaging.servicebus.message.enqueued_time");

    /**
     * The messaging system as identified by the client instrumentation.
     *
     * <p>Notes:
     *
     * <p>The actual messaging system may differ from the one known by the client. For example, when
     * using Kafka client libraries to communicate with Azure Event Hubs, the {@code messaging.system}
     * is set to {@code kafka} based on the instrumentation's best knowledge.
     */
    public static final AttributeKey<String> MESSAGING_SYSTEM = stringKey("messaging.system");

    // Enum definitions

    /** Values for {@link #MESSAGING_OPERATION_TYPE}. */
    public static final class MessagingOperationTypeIncubatingValues {
        /**
         * A message is created. "Create" spans always refer to a single message and are used to provide
         * a unique creation context for messages in batch sending scenarios.
         */
        public static final String CREATE = "create";

        /**
         * One or more messages are provided for sending to an intermediary. If a single message is
         * sent, the context of the "Send" span can be used as the creation context and no "Create" span
         * needs to be created.
         */
        public static final String SEND = "send";

        /**
         * One or more messages are requested by a consumer. This operation refers to pull-based
         * scenarios, where consumers explicitly call methods of messaging SDKs to receive messages.
         */
        public static final String RECEIVE = "receive";

        /** One or more messages are processed by a consumer. */
        public static final String PROCESS = "process";

        /** One or more messages are settled. */
        public static final String SETTLE = "settle";

        /**
         * Deprecated. Use {@code process} instead.
         *
         * @deprecated Replaced by {@code process}.
         */
        @Deprecated
        public static final String DELIVER = "deliver";

        /**
         * Deprecated. Use {@code send} instead.
         *
         * @deprecated Replaced by {@code send}.
         */
        @Deprecated
        public static final String PUBLISH = "publish";

        private MessagingOperationTypeIncubatingValues() {
        }
    }

    /** Values for {@link #MESSAGING_ROCKETMQ_CONSUMPTION_MODEL}. */
    public static final class MessagingRocketmqConsumptionModelIncubatingValues {
        /** Clustering consumption model */
        public static final String CLUSTERING = "clustering";

        /** Broadcasting consumption model */
        public static final String BROADCASTING = "broadcasting";

        private MessagingRocketmqConsumptionModelIncubatingValues() {
        }
    }

    /** Values for {@link #MESSAGING_ROCKETMQ_MESSAGE_TYPE}. */
    public static final class MessagingRocketmqMessageTypeIncubatingValues {
        /** Normal message */
        public static final String NORMAL = "normal";

        /** FIFO message */
        public static final String FIFO = "fifo";

        /** Delay message */
        public static final String DELAY = "delay";

        /** Transaction message */
        public static final String TRANSACTION = "transaction";

        private MessagingRocketmqMessageTypeIncubatingValues() {
        }
    }

    /** Values for {@link #MESSAGING_SERVICEBUS_DISPOSITION_STATUS}. */
    public static final class MessagingServicebusDispositionStatusIncubatingValues {
        /** Message is completed */
        public static final String COMPLETE = "complete";

        /** Message is abandoned */
        public static final String ABANDON = "abandon";

        /** Message is sent to dead letter queue */
        public static final String DEAD_LETTER = "dead_letter";

        /** Message is deferred */
        public static final String DEFER = "defer";

        private MessagingServicebusDispositionStatusIncubatingValues() {
        }
    }

    /** Values for {@link #MESSAGING_SYSTEM}. */
    public static final class MessagingSystemIncubatingValues {
        /** Apache ActiveMQ */
        public static final String ACTIVEMQ = "activemq";

        /** Amazon Simple Notification Service (SNS) */
        public static final String AWS_SNS = "aws.sns";

        /** Amazon Simple Queue Service (SQS) */
        public static final String AWS_SQS = "aws_sqs";

        /** Azure Event Grid */
        public static final String EVENTGRID = "eventgrid";

        /** Azure Event Hubs */
        public static final String EVENTHUBS = "eventhubs";

        /** Azure Service Bus */
        public static final String SERVICEBUS = "servicebus";

        /** Google Cloud Pub/Sub */
        public static final String GCP_PUBSUB = "gcp_pubsub";

        /** Java Message Service */
        public static final String JMS = "jms";

        /** Apache Kafka */
        public static final String KAFKA = "kafka";

        /** RabbitMQ */
        public static final String RABBITMQ = "rabbitmq";

        /** Apache RocketMQ */
        public static final String ROCKETMQ = "rocketmq";

        /** Apache Pulsar */
        public static final String PULSAR = "pulsar";

        private MessagingSystemIncubatingValues() {
        }
    }

    private MessagingIncubatingAttributes() {
    }
}
