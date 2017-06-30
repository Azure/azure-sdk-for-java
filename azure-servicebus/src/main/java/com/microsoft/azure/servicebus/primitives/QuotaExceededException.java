package com.microsoft.azure.servicebus.primitives;

/**
 * This exception is thrown to signal that a service bus entity or namespace exceeded its quota. A quota can be a limit on entity size, message size, maximum concurrent connections or
 * maximum concurrent operations.
 * @since 1.0
 *
 */
public class QuotaExceededException extends ServiceBusException {
	
	private static final long serialVersionUID = -6963913971977282430L;
	
	public QuotaExceededException()
	{
		super(false);
	}
	
	public QuotaExceededException(String message)
	{
		super(false, message);
	}
	
	public QuotaExceededException(Throwable cause)
	{
		super(false, cause);
	}
	
	public QuotaExceededException(String message, Throwable cause)
	{
		super(false, message, cause);
	}

}
