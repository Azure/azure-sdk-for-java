package com.microsoft.azure.servicebus;

public class EntityNotFoundException extends ServiceBusException 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1106827749824999989L;

	EntityNotFoundException(final String errorMessage)
	{
		super(errorMessage);
	}
	
	@Override public boolean getIsTransient(){
		  return false;
	  }
}
