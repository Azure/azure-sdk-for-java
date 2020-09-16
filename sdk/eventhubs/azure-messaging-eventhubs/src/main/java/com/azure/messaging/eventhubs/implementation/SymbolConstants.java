// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import com.azure.core.amqp.AmqpMessageConstant;
import org.apache.qpid.proton.amqp.Symbol;

import static com.azure.core.amqp.implementation.AmqpConstants.VENDOR;

public final class SymbolConstants {
    public static final Symbol EPOCH = Symbol.valueOf(VENDOR + ":epoch");
    public static final Symbol ENABLE_RECEIVER_RUNTIME_METRIC_NAME = Symbol.valueOf(
        VENDOR + ":enable-receiver-runtime-metric");
    public static final Symbol ENABLE_IDEMPOTENT_PRODUCER = Symbol.valueOf(VENDOR + ":idempotent-producer");

    public static final Symbol PRODUCER_EPOCH = Symbol.valueOf(
        AmqpMessageConstant.PRODUCER_EPOCH_ANNOTATION_NAME.getValue());
    public static final Symbol PRODUCER_ID = Symbol.valueOf(AmqpMessageConstant.PRODUCER_ID_ANNOTATION_NAME.getValue());
    public static final Symbol PRODUCER_SEQUENCE_NUMBER = Symbol.valueOf(
        AmqpMessageConstant.PRODUCER_SEQUENCE_NUMBER_ANNOTATION_NAME.getValue());
}
