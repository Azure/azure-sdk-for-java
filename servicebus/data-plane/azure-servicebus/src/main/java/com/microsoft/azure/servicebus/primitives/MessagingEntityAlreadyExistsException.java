package com.microsoft.azure.servicebus.primitives;

/**
 * This exception is thrown when a subscription client tries to create a rule with the name of an already existing rule.
 * @since 1.0
 *
 */
public class MessagingEntityAlreadyExistsException extends ServiceBusException {
	
	private static final long serialVersionUID = -3652949479773950838L;

	public MessagingEntityAlreadyExistsException(String message)
	{
		super(false, message);
	}
	
	public MessagingEntityAlreadyExistsException(String message, Throwable cause)
	{
		super(false, message, cause);
	}
}
