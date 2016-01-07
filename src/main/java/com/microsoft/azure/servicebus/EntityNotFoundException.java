package com.microsoft.azure.servicebus;

public class EntityNotFoundException extends ServiceBusException 
{

	EntityNotFoundException(final String errorMessage)
	{
		super(errorMessage);
	}
	
	@Override public boolean getIsTransient()
	{
		  return false;
	}
}
