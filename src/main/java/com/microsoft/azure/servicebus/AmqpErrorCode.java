package com.microsoft.azure.servicebus;

import org.apache.qpid.proton.amqp.Symbol;

public final class AmqpErrorCode {

	public static final Symbol NotFound = Symbol.getSymbol("amqp:not-found");
	public static final Symbol Stolen = Symbol.getSymbol("amqp:link:stolen");
}
