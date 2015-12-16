package com.microsoft.azure.servicebus;

import org.apache.qpid.proton.amqp.Symbol;

public final class AmqpConstants {

	public static final String OffsetName = "x-opt-offset";
	
	public static final Symbol PartitionKey = Symbol.getSymbol("x-opt-partition-key");
	public static final Symbol Offset = Symbol.getSymbol(AmqpConstants.OffsetName);
	public static final Symbol SequenceNumber = Symbol.getSymbol("x-opt-sequence-number");
	public static final Symbol EnqueuedTimeUtc = Symbol.getSymbol("x-opt-enqueued-time");
}
