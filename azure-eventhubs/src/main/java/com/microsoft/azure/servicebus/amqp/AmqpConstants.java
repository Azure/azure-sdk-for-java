/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
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
}
