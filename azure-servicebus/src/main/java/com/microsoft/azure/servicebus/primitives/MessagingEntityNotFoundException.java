package com.microsoft.azure.servicebus.primitives;

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
