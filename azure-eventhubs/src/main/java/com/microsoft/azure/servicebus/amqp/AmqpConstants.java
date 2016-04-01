/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.amqp;

import org.apache.qpid.proton.amqp.*;

public final class AmqpConstants
{
	private AmqpConstants() { }
	
	public static final String APACHE = "apache.org";
    public static final String VENDOR = "com.microsoft";
	
    public static final String AMQP_ANNOTATION_FORMAT = "amqp.annotation.%s >%s '%s'";
	public static final String OFFSET_ANNOTATION_NAME = "x-opt-offset";
	public static final String RECEIVED_AT_ANNOTATION_NAME = "x-opt-enqueued-time";
	
	public static final Symbol PARTITION_KEY = Symbol.getSymbol("x-opt-partition-key");
	public static final Symbol OFFSET = Symbol.getSymbol(AmqpConstants.OFFSET_ANNOTATION_NAME);
	public static final Symbol SEQUENCE_NUMBER = Symbol.getSymbol("x-opt-sequence-number");
	public static final Symbol ENQUEUED_TIME_UTC = Symbol.getSymbol("x-opt-enqueued-time");
	
	public static final Symbol STRING_FILTER = Symbol.valueOf(AmqpConstants.APACHE + ":selector-filter:string");
	public static final Symbol EPOCH = Symbol.valueOf(AmqpConstants.VENDOR + ":epoch");
	
	public static final int AMQP_BATCH_MESSAGE_FORMAT = 0x80013700; // 2147563264L;
	
	public static final int MAX_FRAME_SIZE = 65536;
}
