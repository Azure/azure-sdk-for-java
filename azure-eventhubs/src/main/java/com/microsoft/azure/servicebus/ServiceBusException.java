package com.microsoft.azure.servicebus;

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
	  static ServiceBusException create(final boolean isTransient, final String message)
	  {
		  return (ServiceBusException) new InternalServiceBusException(isTransient, message);
	  }
	  
	  static ServiceBusException create(final boolean isTransient, Throwable cause)
	  {
		  return (ServiceBusException) new InternalServiceBusException(isTransient, cause);
	  }
	  
	  static ServiceBusException create(final boolean isTransient, final String message, final Throwable cause)
	  {
		  return (ServiceBusException) new InternalServiceBusException(isTransient, message, cause);
	  }
	  
	  private static final class InternalServiceBusException extends ServiceBusException
	  {
		boolean isTransient;
		  
		private InternalServiceBusException(final boolean isTransient, final String message)
		{
		  super(message);
		  this.isTransient = isTransient;
		}
		  
		private InternalServiceBusException(final boolean isTransient, final Throwable exception)
		{
			super(exception);
			this.isTransient = isTransient;
		}
		
		private InternalServiceBusException(final boolean isTransient, final String message, final Throwable cause)
		{
			super(message, cause);
			this.isTransient = isTransient;
		}
		  
		@Override
		public boolean getIsTransient()
		{
			return this.isTransient;
		}		  
	  }
}
