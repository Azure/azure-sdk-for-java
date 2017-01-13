package com.microsoft.azure.servicebus.primitives;

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
