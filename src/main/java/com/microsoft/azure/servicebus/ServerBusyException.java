package com.microsoft.azure.servicebus;

public class ServerBusyException extends ServiceBusException 
{
	private static final long serialVersionUID = -1106827749824999989L;
	
	ServerBusyException(String message) {
		super(message);
	}
	
	@Override 
	public boolean getIsTransient()
	{
		  return false;
	}
}
