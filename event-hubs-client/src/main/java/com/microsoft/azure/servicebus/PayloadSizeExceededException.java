package com.microsoft.azure.servicebus;

public class PayloadSizeExceededException extends ServiceBusException
{
	
	public PayloadSizeExceededException(String message)
	{
		super(message);
	}
	
	public PayloadSizeExceededException(String message, Throwable cause)
	{
		super(message, cause);
	}
	
	@Override
	public boolean getIsTransient()
	{
		return false;
	}

}
