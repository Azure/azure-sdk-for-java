package com.microsoft.azure.servicebus.primitives;

/**
 * This exception is thrown when a client attempts to accept a session that is already locked by another client.
 * @since 1.0
 *
 */
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
