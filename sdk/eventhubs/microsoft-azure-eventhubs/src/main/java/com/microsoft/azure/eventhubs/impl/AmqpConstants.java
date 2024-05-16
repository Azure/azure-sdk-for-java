// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.apache.qpid.proton.amqp.Symbol;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class AmqpConstants {

    public static final String APACHE = "apache.org";
    public static final String PROTON = "proton";
    public static final String VENDOR = "com.microsoft";
    public static final String AMQP_ANNOTATION_FORMAT = "amqp.annotation.%s >%s '%s'";
    public static final String OFFSET_ANNOTATION_NAME = "x-opt-offset";
    public static final String ENQUEUED_TIME_UTC_ANNOTATION_NAME = "x-opt-enqueued-time";
    public static final String PARTITION_KEY_ANNOTATION_NAME = "x-opt-partition-key";
    public static final String SEQUENCE_NUMBER_ANNOTATION_NAME = "x-opt-sequence-number";
    public static final String PUBLISHER_ANNOTATION_NAME = "x-opt-publisher";
    public static final Symbol PARTITION_KEY = Symbol.getSymbol(PARTITION_KEY_ANNOTATION_NAME);
    public static final Symbol OFFSET = Symbol.getSymbol(OFFSET_ANNOTATION_NAME);
    public static final Symbol SEQUENCE_NUMBER = Symbol.getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME);
    public static final Symbol ENQUEUED_TIME_UTC = Symbol.getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME);
    public static final Symbol STRING_FILTER = Symbol.valueOf(APACHE + ":selector-filter:string");
    public static final Symbol EPOCH = Symbol.valueOf(VENDOR + ":epoch");
    public static final Symbol PRODUCT = Symbol.valueOf("product");
    public static final Symbol VERSION = Symbol.valueOf("version");
    public static final Symbol PLATFORM = Symbol.valueOf("platform");
    public static final Symbol FRAMEWORK = Symbol.valueOf("framework");
    public static final Symbol USER_AGENT = Symbol.valueOf("user-agent");
    public static final int MAX_USER_AGENT_LENGTH = 128;
    public static final int AMQP_BATCH_MESSAGE_FORMAT = 0x80013700; // 2147563264L;
    public static final int MAX_FRAME_SIZE = 65536;
    public static final int TRANSPORT_IDLE_TIMEOUT_MILLIS = 60000;
    public static final long LINK_ERROR_DELAY_MILLIS = 5000;
    public static final String AMQP_PROPERTY_MESSAGE_ID = "message-id";
    public static final String AMQP_PROPERTY_USER_ID = "user-id";
    public static final String AMQP_PROPERTY_TO = "to";
    public static final String AMQP_PROPERTY_SUBJECT = "subject";
    public static final String AMQP_PROPERTY_REPLY_TO = "reply-to";
    public static final String AMQP_PROPERTY_CORRELATION_ID = "correlation-id";
    public static final String AMQP_PROPERTY_CONTENT_TYPE = "content-type";
    public static final String AMQP_PROPERTY_CONTENT_ENCODING = "content-encoding";
    public static final String AMQP_PROPERTY_ABSOLUTE_EXPRITY_TIME = "absolute-expiry-time";
    public static final String AMQP_PROPERTY_CREATION_TIME = "creation-time";
    public static final String AMQP_PROPERTY_GROUP_ID = "group-id";
    public static final String AMQP_PROPERTY_GROUP_SEQUENCE = "group-sequence";
    public static final String AMQP_PROPERTY_REPLY_TO_GROUP_ID = "reply-to-group-id";
    @SuppressWarnings("serial")
    public static final Set<String> RESERVED_PROPERTY_NAMES = Collections.unmodifiableSet(new HashSet<String>() {{
            add(AMQP_PROPERTY_MESSAGE_ID);
            add(AMQP_PROPERTY_USER_ID);
            add(AMQP_PROPERTY_TO);
            add(AMQP_PROPERTY_SUBJECT);
            add(AMQP_PROPERTY_REPLY_TO);
            add(AMQP_PROPERTY_CORRELATION_ID);
            add(AMQP_PROPERTY_CONTENT_TYPE);
            add(AMQP_PROPERTY_CONTENT_ENCODING);
            add(AMQP_PROPERTY_ABSOLUTE_EXPRITY_TIME);
            add(AMQP_PROPERTY_CREATION_TIME);
            add(AMQP_PROPERTY_GROUP_ID);
            add(AMQP_PROPERTY_GROUP_SEQUENCE);
            add(AMQP_PROPERTY_REPLY_TO_GROUP_ID);
            }});
    public static final Symbol ENABLE_RECEIVER_RUNTIME_METRIC_NAME = Symbol.valueOf(VENDOR + ":enable-receiver-runtime-metric");
    public static final Symbol RECEIVER_IDENTIFIER_NAME = Symbol.valueOf(AmqpConstants.VENDOR + ":receiver-name");
    private AmqpConstants() {
    }
}
