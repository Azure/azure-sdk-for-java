package com.microsoft.azure.servicebus.primitives;

public class SessionLockLostException extends ServiceBusException {
	
	private static final long serialVersionUID = -5861754850637792928L;

	public SessionLockLostException()
	{
		super(false);
	}
	
	public SessionLockLostException(String message)
	{
		super(false, message);
	}
	
	public SessionLockLostException(Throwable cause)
	{
		super(false, cause);
	}
	
	public SessionLockLostException(String message, Throwable cause)
	{
		super(false, message, cause);
	}
}
