// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import org.apache.qpid.proton.amqp.Symbol;

import static com.azure.core.amqp.MessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.OFFSET_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.PARTITION_KEY_ANNOTATION_NAME;
import static com.azure.core.amqp.MessageConstant.SEQUENCE_NUMBER_ANNOTATION_NAME;

public final class AmqpConstants {
    static final String VENDOR = "com.microsoft";

    public static final String APACHE = "apache.org";
    public static final String PROTON = "proton";
    public static final String AMQP_ANNOTATION_FORMAT = "amqp.annotation.%s >%s '%s'";

    public static final Symbol PARTITION_KEY = Symbol.getSymbol(PARTITION_KEY_ANNOTATION_NAME.getValue());
    public static final Symbol OFFSET = Symbol.getSymbol(OFFSET_ANNOTATION_NAME.getValue());
    public static final Symbol SEQUENCE_NUMBER = Symbol.getSymbol(SEQUENCE_NUMBER_ANNOTATION_NAME.getValue());
    public static final Symbol ENQUEUED_TIME_UTC = Symbol.getSymbol(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
    public static final Symbol STRING_FILTER = Symbol.valueOf(APACHE + ":selector-filter:string");
    public static final Symbol EPOCH = Symbol.valueOf(VENDOR + ":epoch");

    public static final int AMQP_BATCH_MESSAGE_FORMAT = 0x80013700; // 2147563264L;

    public static final Symbol ENABLE_RECEIVER_RUNTIME_METRIC_NAME = Symbol.valueOf(VENDOR + ":enable-receiver-runtime-metric");
    public static final Symbol RECEIVER_IDENTIFIER_NAME = Symbol.valueOf(AmqpConstants.VENDOR + ":receiver-name");
}
