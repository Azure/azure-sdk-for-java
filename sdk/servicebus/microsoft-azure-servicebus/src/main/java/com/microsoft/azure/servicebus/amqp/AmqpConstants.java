// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.amqp.Symbol;

public final class AmqpConstants {
    private AmqpConstants() { }

    public static final String APACHE = "apache.org";
    public static final String VENDOR = "com.microsoft";

    public static final Symbol STRING_FILTER = Symbol.valueOf(AmqpConstants.APACHE + ":selector-filter:string");
    public static final Symbol EPOCH = Symbol.valueOf(AmqpConstants.VENDOR + ":epoch");

    public static final int AMQP_BATCH_MESSAGE_FORMAT = 0x80013700; // 2147563264L;

    public static final int MAX_FRAME_SIZE = 65536;
    public static final int WEBSOCKET_MAX_FRAME_SIZE = 4096;
    public static final int TRANSPORT_IDLE_TIMEOUT_MILLIS = 60000;

    public static final String MANAGEMENT_NODE_ADDRESS_SEGMENT = "$management";
    public static final String CBS_NODE_ADDRESS_SEGMENT = "$cbs";

    public static final Symbol PRODUCT = Symbol.valueOf("product");
    public static final Symbol VERSION = Symbol.valueOf("version");
    public static final Symbol PLATFORM = Symbol.valueOf("platform");
}
