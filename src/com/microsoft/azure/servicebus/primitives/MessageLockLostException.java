package com.microsoft.azure.servicebus.primitives;

public class MessageLockLostException extends ServiceBusException {
		
	private static final long serialVersionUID = -203350475131556600L;

	public MessageLockLostException()
	{
		super(false);
	}
	
	public MessageLockLostException(String message)
	{
		super(false, message);
	}
	
	public MessageLockLostException(Throwable cause)
	{
		super(false, cause);
	}
	
	public MessageLockLostException(String message, Throwable cause)
	{
		super(false, message, cause);
	}
}
