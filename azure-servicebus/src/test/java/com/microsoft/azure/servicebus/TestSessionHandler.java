package com.microsoft.azure.servicebus;

import java.time.Instant;

public abstract class TestSessionHandler implements ISessionHandler
{
	@Override
	public void notifyException(Throwable exception, ExceptionPhase phase) {
		System.out.println(phase + "-" + exception.getMessage()  + ":" + Instant.now());
	}
}
