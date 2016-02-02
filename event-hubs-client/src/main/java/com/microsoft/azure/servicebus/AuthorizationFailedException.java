package com.microsoft.azure.servicebus;

public class AuthorizationFailedException extends ServiceBusException
{
	
	public AuthorizationFailedException(String description)
	{
		super(description);
	}
	
	@Override 
	public boolean getIsTransient()
	{
	  return false;
	}
	
}
