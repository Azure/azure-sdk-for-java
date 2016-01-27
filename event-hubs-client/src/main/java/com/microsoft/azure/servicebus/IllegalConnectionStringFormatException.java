package com.microsoft.azure.servicebus;

public class IllegalConnectionStringFormatException extends IllegalArgumentException
{

	public IllegalConnectionStringFormatException()
	{
	}

	public IllegalConnectionStringFormatException(String detail)
	{
		super(detail);
	}

	public IllegalConnectionStringFormatException(Throwable cause)
	{
		super(cause);
	}

	public IllegalConnectionStringFormatException(String detail, Throwable cause)
	{
		super(detail, cause);
	}

}
