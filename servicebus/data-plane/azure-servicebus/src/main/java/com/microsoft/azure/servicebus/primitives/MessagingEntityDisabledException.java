package com.microsoft.azure.servicebus.primitives;

/**
 * This exception is thrown when a client attempts to send messages to or receive messages from a disabled entity. An entity can be disabled for Send operations or RECEIVE operations or both.
 * @since 1.0
 *
 */
public class MessagingEntityDisabledException extends ServiceBusException{
	
	private static final long serialVersionUID = 9086472912026637605L;

	public MessagingEntityDisabledException()
	{
		super(false);
	}
	
	public MessagingEntityDisabledException(String message)
	{
		super(false, message);
	}
	
	public MessagingEntityDisabledException(Throwable cause)
	{
		super(false, cause);
	}
	
	public MessagingEntityDisabledException(String message, Throwable cause)
	{
		super(false, message, cause);
	}
}
