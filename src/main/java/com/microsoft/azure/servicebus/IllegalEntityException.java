package com.microsoft.azure.servicebus;

public class IllegalEntityException extends ServiceBusException 
{

	IllegalEntityException(final String errorMessage)
	{
		super(errorMessage);
	}
	
	@Override public boolean getIsTransient()
	{
		  return false;
	}
}
