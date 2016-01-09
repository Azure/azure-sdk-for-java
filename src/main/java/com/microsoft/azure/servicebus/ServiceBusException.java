package com.microsoft.azure.servicebus;

import java.util.*;

public abstract class ServiceBusException extends Exception
{
	  private static final long serialVersionUID = -3654294093967132325L;
	  
	  public ServiceBusException()
	  {
	    super();
	  }
	
	  public ServiceBusException(String message)
	  {
	    super(message);
	  }
	
	  public ServiceBusException(Throwable cause)
	  {
	    super(cause);
	  }
	
	  public ServiceBusException(String message, Throwable cause)
	  {
	    super(message, cause);
	  }
	  
	  public abstract boolean getIsTransient();
	  
	  /**
	   *  internal-only; Sub-classing is allowed for {@link ServiceBusException} 
	   *  - but fx's other than ServiceBus sdk's cannot directly create instance & throw it
	   */
	  static ServiceBusException Create(final boolean isTransient, final String message)
	  {
		  return new InternalServiceBusException(isTransient, message);
	  }
	  
	  private static final class InternalServiceBusException extends ServiceBusException
	  {
		  boolean isTransient;
		  
		  public InternalServiceBusException(final boolean isTransient, final String message)
		  {
			  super(message);
			  this.isTransient = isTransient;
		  }
		  
		@Override
		public boolean getIsTransient()
		{
			return this.isTransient;
		}		  
	  }
}
