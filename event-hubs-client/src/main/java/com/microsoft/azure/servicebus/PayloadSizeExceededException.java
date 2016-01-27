package com.microsoft.azure.servicebus;

public class PayloadSizeExceededException extends ServiceBusException
{

	private static final long serialVersionUID = -7474944260042213247L;

	public PayloadSizeExceededException(String message)
	{
		super(message);
	}
	
	@Override
	public boolean getIsTransient()
	{
		// TODO Auto-generated method stub
		return false;
	}

}
