package com.microsoft.azure.servicebus;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.apache.qpid.proton.amqp.messaging.Outcome;

public class UpdateStateWorkItem extends WorkItem<Void>
{
	final Outcome expectedOutcome;
	
	public UpdateStateWorkItem(final CompletableFuture<Void> completableFuture, Outcome expectedOutcome,  Duration timeout)
	{
		super(completableFuture, timeout);
		this.expectedOutcome = expectedOutcome;
	}
	
	public UpdateStateWorkItem(final CompletableFuture<Void> completableFuture, Outcome expectedOutcome, final TimeoutTracker tracker)
	{
		super(completableFuture, tracker);
		this.expectedOutcome = expectedOutcome;
	}
}
