package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

public abstract class TestSessionHandler implements ISessionHandler
{
	@Override
	public void notifyException(Throwable exception, ExceptionPhase phase) {
		System.out.println(phase + "-" + exception.getMessage());
	}
}
