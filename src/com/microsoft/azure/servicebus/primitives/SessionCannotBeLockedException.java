package com.microsoft.azure.servicebus.primitives;

public class SessionCannotBeLockedException extends ServiceBusException {
	
	private static final long serialVersionUID = -421016051252808254L;

	public SessionCannotBeLockedException()
	{
		super(false);
	}
	
	public SessionCannotBeLockedException(String message)
	{
		super(false, message);
	}
	
	public SessionCannotBeLockedException(Throwable cause)
	{
		super(false, cause);
	}
	
	public SessionCannotBeLockedException(String message, Throwable cause)
	{
		super(false, message, cause);
	}
}
