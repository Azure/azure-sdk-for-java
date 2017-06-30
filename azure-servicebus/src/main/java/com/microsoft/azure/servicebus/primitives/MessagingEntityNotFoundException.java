package com.microsoft.azure.servicebus.primitives;

/**
 * This exception is thrown when a client attempts to create a sender or receiver or client to a non existent entity.
 * @since 1.0
 *
 */
public class MessagingEntityNotFoundException extends ServiceBusException {
		
	private static final long serialVersionUID = -4624769494653591824L;

	public MessagingEntityNotFoundException()
	{
		super(false);
	}
	
	public MessagingEntityNotFoundException(String message)
	{
		super(false, message);
	}
	
	public MessagingEntityNotFoundException(Throwable cause)
	{
		super(false, cause);
	}
	
	public MessagingEntityNotFoundException(String message, Throwable cause)
	{
		super(false, message, cause);
	}
}
