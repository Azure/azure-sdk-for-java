// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

public final class EventHubsConstants {
    // Cannot null or empty
    public static final String CONNECTION_STRING_CANNOT_NULL = "'connectionString' cannot be null.";
    public static final String CONNECTION_STRING_CANNOT_EMPTY = "'connectionString' cannot be an empty string.";
    public static final String PARTITION_ID_CANNOT_NULL = "'partitionId' cannot be null";
    public static final String PARTITION_ID_CANNOT_EMPTY = "'partitionId' cannot be an empty string.";
    public static final String EVENT_DATA_CANNOT_NULL = "eventData cannot be null";
    public static final String CONNECTION_OPTIONS_CANNOT_NULL = "'connectionOptions' cannot be null.";
    public static final String REACTOR_PROVIDER_CANNOT_NULL = "'provider' cannot be null.";
    public static final String REACTOR_HANDLER_PROVIDER_CANNOT_NULL = "'handlerProvider' cannot be null.";
    public static final String OPTIONS_CANNOT_NULL = "'options' cannot be null.";
    public static final String EVENT_POSITION_CANNOT_NULL = "'eventPosition' cannot be null.";
    public static final String CONSUMER_GROUP_CANNOT_NULL = "'consumerGroup' cannot be null.";
    public static final String CONSUMER_GROUP_CANNOT_EMPTY = "'consumerGroup' cannot be an empty string.";
    public static final String EVENT_CANNOT_NULL = "'event' cannot be null.";
    public static final String EVENTS_CANNOT_NULL = "'events' cannot be null.";
    public static final String BATCH_CANNOT_NULL = "'batch' cannot be null.";
    public static final String CLIENT_CANNOT_NULL = "'client' cannot be null.";
    public static final String HOST_CANNOT_NULL = "'host' cannot be null.";
    public static final String HOST_CANNOT_EMPTY = "'host' cannot be an empty string.";
    public static final String CREDENTIAL_CANNOT_NULL = "'credential' cannot be null.";
    public static final String CONSUMER_CANNOT_NULL = "'consumer' cannot be null.";
    public static final String PRODUCER_CANNOT_NULL = "'producer' cannot be null.";
    public static final String TRY_TIME_OUT_CANNOT_NULL = "'tryTimeout' cannot be null.";
    public static final String SHARED_ACCESS_KEY_CANNOT_NULL = "'sharedAccessKey' cannot be null.";
    public static final String SHARED_ACCESS_KEY_CANNOT_EMPTY = "'sharedAccessKey' cannot be an empty string.";
    public static final String POLICY_NAME_CANNOT_NULL = "'policyName' cannot be null.";
    public static final String POLICY_NAME_CANNOT_EMPTY = "'policyName' cannot be an empty string.";
    public static final String TOKEN_VALIDITY_CANNOT_NULL = "'tokenValidity' cannot be null.";
    public static final String RESOURCE_CANNOT_EMPTY = "resource cannot be empty";
    public static final String EVENTHUB_ASYNC_CLIENT_CANNOT_NULL = "eventHubAsyncClient cannot be null";
    public static final String PARTITION_PROCESSOR_FACTORY_CANNOT_NULL = "partitionProcessorFactory cannot be null";
    public static final String PARTITION_MANAGER_CANNOT_NULL = "partitionManager cannot be null";
    public static final String INITIAL_EVENT_POSITION_CANNOT_NULL = "initialEventPosition cannot be null";
    public static final String EVENTHUB_NAME_CANNOT_NULL = "eventHubName cannot be null";
    public static final String EVENTHUB_NAME_CANNOT_EMPTY = "'eventHubName' cannot be an empty string.";
    public static final String CONSUMER_GROUP_NAME_CANNOT_NULL = "consumerGroupName cannot be null";
    public static final String OWNER_ID_CANNOT_NULL = "ownerId cannot be null";
    public static final String AUTHENTICATION_CANNOT_NULL = "'authentication' cannot be null.";
    public static final String OFFSET_CANNOT_NULL = "'offset' cannot be null.";
    public static final String BODY_CANNOT_NULL = "'body' cannot be null.";
    public static final String PROPERTY_KEY_CANNOT_NULL = "'key' cannot be null.";
    public static final String PROPERTY_VALUE_CANNOT_NULL = "'value' cannot be null.";
    public static final String MESSAGE_CANNOT_NULL = "message cannot be null";
    public static final String MESSAGE_ID_SHOULD_BE_NULL = "message.getMessageId() should be null";
    public static final String MESSAGE_REPLY_TO_SHOULD_BE_NULL = "message.getReplyTo() should be null";


    public static final String NULL = "null";
    public static final String NULL_SEQUENCE_NUM_IN_MAP = "sequenceNumber: %s should always be in map.";
    public static final String NON_NEGATIVE_TIMEOUT = "timeout should be non-negative";
    public static final String UNSUPPORTED_AUTHORIZATION_TYPE = "'%s' is not supported authorization type for token audience.";
    public static final String TOKEN_TIME_TO_LIVE_ERROR_MSG = "'tokenTimeToLive' has to positive and in the order-of seconds";
    public static final String SCOPES_RULES = "'scopes' should only contain a single argument that is the token audience or resource name.";
    public static final String CANNOT_AUTHORIZE_CBS = "Cannot authorize with CBS node when this token manager has been disposed of.";
    public static final String ILLEGAL_CONNECTION_STRING_PARAMS = "Illegal connection string parameter name: %s";
    public static final String INCORRECT_SCHEME_ENDPOINT = "Endpoint is not the correct scheme. Expected: %s. Actual Endpoint: %s";
    public static final String INVALID_ENDPOINT_MSG = "Invalid endpoint: %s";
    public static final String CONNECTION_STRING_HAS_INVALID_KV_PAIR = "Connection string has invalid key value pair: %s";
    public static final String UNRESERVED_PROPERTY_NAME = "Property is not a recognized reserved property name: %s";
    public static final String ENCODING_TYPE_NOT_SUPPORTED = "Encoding Type: %s is not supported";
    public static final String MESSAGE_BODY_EXPECT_AMQP_VALUE = "Expected message.getBody() to be AmqpValue, but is: ";
    public static final String MESSAGE_BODY_VALUE_EXPECT_MAP_TYPE = "Expected message.getBody().getValue() to be of type Map";
    public static final String REACTOR_DISPATCHER_CLOSED = "ReactorDispatcher instance is closed.";
    public static final String REACTOR_FAILED_EXECUTOR_DOWN = "Scheduling reactor failed because the executor has been shut down";
    public static final String TRANSPORT_TYPE_NOT_SUPPORTED = "This transport type '%s' is not supported.";
    public static final String MAX_FRAME_SIZE_REQUIRE_POSITIVE_NUM = "'maxFrameSize' must be a positive number.";
    public static final String PAYLOAD_EXCEEDED_MAX_SIZE = "Error sending. Size of the payload exceeded maximum message size: %s kb";
    public static final String ENTITY_SEND_FAILED = "Entity(%s): send operation failed. Please see cause for more details";
    public static final String ENTITY_SEND_FAILED_DELIVERY = "Entity(%s): send operation failed while advancing delivery(tag: %s).";
    public static final String ENTITY_SEND_FAILED_SCHEDULE_RETRY =
        "Entity(%s): send operation failed while scheduling a retry on Reactor, see cause for more details.";
    public static final String ENTITY_SEND_TIMEOUT = "Entity(%s): Send operation timed out";
    public static final String UNABLE_CLOSE_CONNECTION_TO_SERVICE = "Unable to close connection to service";
    public static final String NO_STARTING_POSITION_SET = "No starting position was set.";
    public static final String BATCH_OPTIONS_LARGER_THAN_LINK_SIZE = "BatchOptions.maximumSizeInBytes (%s bytes) is larger than the link size (%s bytes).";
    public static final String PARTITION_KEY_EXCEEDS_MAX_LENGTH = "PartitionKey '%s' exceeds the maximum allowed length: '%s'.";
    public static final String EVENT_DATA_EXCEEDS_MAX_NUM_BATCHES = "EventData does not fit into maximum number of batches. '%s'";
    public static final String CANNOT_CREATE_EVENTHUB_SAS_KEY_CREDENTIAL = "Could not create the EventHubSharedAccessKeyCredential.";
    public static final String CANNOT_USE_PROXY_FOR_AMQP_TRANSPORT_TYPE = "Cannot use a proxy when TransportType is not AMQP.";
    public static final String HTTP_PROXY_CANNOT_PARSED_TO_PROXY = "HTTP_PROXY cannot be parsed into a proxy";


}
