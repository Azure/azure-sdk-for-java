package com.microsoft.azure.servicebus.primitives;

public class MessageNotFoundException extends ServiceBusException {
	
	private static final long serialVersionUID = -7138414297734634975L;

	public MessageNotFoundException()
	{
		super(false);
	}
	
	public MessageNotFoundException(String message)
	{
		super(false, message);
	}
	
	public MessageNotFoundException(Throwable cause)
	{
		super(false, cause);
	}
	
	public MessageNotFoundException(String message, Throwable cause)
	{
		super(false, message, cause);
	}
}
