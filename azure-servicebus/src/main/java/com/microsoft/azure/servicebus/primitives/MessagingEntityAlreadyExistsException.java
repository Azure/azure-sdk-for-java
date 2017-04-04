package com.microsoft.azure.servicebus.primitives;

public class MessagingEntityAlreadyExistsException extends ServiceBusException {
	
	private static final long serialVersionUID = -3652949479773950838L;

	public MessagingEntityAlreadyExistsException(String message)
	{
		super(false, message);
	}
	
	public MessagingEntityAlreadyExistsException(String message, Throwable cause)
	{
		super(false, message, cause);
	}
}
