package com.microsoft.azure.servicebus;

public abstract class ServiceBusException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3654294093967132325L;

	  public ServiceBusException() {
	    super();
	  }

	  public ServiceBusException(String message) {
	    super(message);
	  }
	
	  public ServiceBusException(Throwable cause) {
	    super(cause);
	  }
	
	  public ServiceBusException(String message, Throwable cause) {
	    super(message, cause);
	  }
	  
	  public abstract boolean getIsTransient();
}
