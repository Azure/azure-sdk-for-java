package com.microsoft.azure.servicebus;

import java.time.*;
import org.apache.qpid.proton.amqp.*;

import com.microsoft.azure.servicebus.amqp.AmqpConstants;

public final class ClientConstants
{

	// TODO: add trackingId which comes as part of Link.attach: properties={com.microsoft:tracking-id=}
	private ClientConstants() { }

	public final static int AmqpsPort = 5671;
	public final static int MaxPartitionKeyLength = 128;
	
	public final static Symbol ServerBusyError = Symbol.getSymbol(AmqpConstants.Vendor + ":server-busy");
	public final static Symbol ArgumentError = Symbol.getSymbol(AmqpConstants.Vendor + ":argument-error");
	public final static Symbol ArgumentOutOfRangeError = Symbol.getSymbol(AmqpConstants.Vendor + ":argument-out-of-range");
	public final static Symbol EntityDisabledError = Symbol.getSymbol(AmqpConstants.Vendor + ":entity-disabled");
	public final static Symbol PartitionNotOwnedError = Symbol.getSymbol(AmqpConstants.Vendor + ":partition-not-owned");
	public final static Symbol StoreLockLostError = Symbol.getSymbol(AmqpConstants.Vendor + ":store-lock-lost");
	public final static Symbol PublisherRevokedError = Symbol.getSymbol(AmqpConstants.Vendor + ":publisher-revoked");
	public final static Symbol TimeoutError = Symbol.getSymbol(AmqpConstants.Vendor + ":timeout");
	
	public final static Duration TimerTolerance = Duration.ofSeconds(5);
	
	public final static Duration DefaultRetryMinBackoff = Duration.ofSeconds(0);
	public final static Duration DefaultRetryMaxBackoff = Duration.ofSeconds(30);
	
	public final static int DefaultMaxRetryCount = 10;
	
	public final static String ServiceBusClientTrace = "servicebus.trace";
	
	public final static int AmqpLinkDetachTimeoutInMin = 8;
}
