package com.microsoft.azure.servicebus;

public class ServerBusyException extends ServiceBusException 
{
	private static final long serialVersionUID = -1106827749824999989L;
	
	public ServerBusyException(String message)
	{
		super(message);
	}
	
	public ServerBusyException() {
		super();
	}

	@Override 
	public boolean getIsTransient()
	{
		return true;
	}
}
