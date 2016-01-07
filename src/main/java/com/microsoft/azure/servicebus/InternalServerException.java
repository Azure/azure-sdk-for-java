package com.microsoft.azure.servicebus;

public class InternalServerException extends ServiceBusException
{

	public InternalServerException()
	{
	}

	public InternalServerException(String message)
	{
		super(message);
	}

	public InternalServerException(Throwable cause)
	{
		super(cause);
	}

	public InternalServerException(String message, Throwable cause)
	{
		super(message, cause);
	}

	@Override
	public boolean getIsTransient()
	{
		return true;
	}

}
