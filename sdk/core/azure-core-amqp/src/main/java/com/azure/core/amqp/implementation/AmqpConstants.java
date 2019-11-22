// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import org.apache.qpid.proton.amqp.Symbol;

import static com.azure.core.amqp.AmqpMessageConstant.PARTITION_KEY_ANNOTATION_NAME;

public final class AmqpConstants {
    public static final String APACHE = "apache.org";
    public static final String PROTON = "proton";
    public static final String AMQP_ANNOTATION_FORMAT = "amqp.annotation.%s >%s '%s'";
    public static final Symbol PARTITION_KEY = Symbol.getSymbol(PARTITION_KEY_ANNOTATION_NAME.getValue());

    public static final String VENDOR = "com.microsoft";

    public static final Symbol STRING_FILTER = Symbol.getSymbol(APACHE + ":selector-filter:string");

    static final int AMQP_BATCH_MESSAGE_FORMAT = 0x80013700; // 2147563264L;

}
