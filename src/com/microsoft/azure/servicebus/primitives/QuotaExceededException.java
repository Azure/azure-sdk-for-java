package com.microsoft.azure.servicebus.primitives;

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
