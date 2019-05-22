// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.exception.ErrorCondition;
import org.apache.qpid.proton.amqp.Symbol;

public final class AmqpConstants {
    static final String VENDOR = "com.microsoft";

    public static final String APACHE = "apache.org";
    public static final String PROTON = "proton";
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

    public static final Symbol ENABLE_RECEIVER_RUNTIME_METRIC_NAME = Symbol.valueOf(VENDOR + ":enable-receiver-runtime-metric");
    public static final Symbol RECEIVER_IDENTIFIER_NAME = Symbol.valueOf(AmqpConstants.VENDOR + ":receiver-name");

    // These are AMQP errors that are vendor specific.
    public static final Symbol PROTON_IO_ERROR = Symbol.getSymbol(AmqpConstants.PROTON + ":io");
    public static final Symbol SERVER_BUSY_ERROR = Symbol.getSymbol(ErrorCondition.SERVER_BUSY_ERROR.getErrorCondition());
    public static final Symbol ARGUMENT_ERROR = Symbol.getSymbol(ErrorCondition.ARGUMENT_ERROR.getErrorCondition());
    public static final Symbol ARGUMENT_OUT_OF_RANGE_ERROR = Symbol.getSymbol(ErrorCondition.ARGUMENT_OUT_OF_RANGE_ERROR.getErrorCondition());
    public static final Symbol ENTITY_DISABLED_ERROR = Symbol.getSymbol(ErrorCondition.ENTITY_DISABLED_ERROR.getErrorCondition());
    public static final Symbol PARTITION_NOT_OWNED_ERROR = Symbol.getSymbol(ErrorCondition.PARTITION_NOT_OWNED_ERROR.getErrorCondition());
    public static final Symbol STORE_LOCK_LOST_ERROR = Symbol.getSymbol(ErrorCondition.STORE_LOCK_LOST_ERROR.getErrorCondition());
    public static final Symbol PUBLISHER_REVOKED_ERROR = Symbol.getSymbol(ErrorCondition.PUBLISHER_REVOKED_ERROR.getErrorCondition());
    public static final Symbol TIMEOUT_ERROR = Symbol.getSymbol(ErrorCondition.TIMEOUT_ERROR.getErrorCondition());
    public static final Symbol TRACKING_ID_PROPERTY = Symbol.getSymbol(ErrorCondition.TRACKING_ID_PROPERTY.getErrorCondition());

    private AmqpConstants() {
    }
}
