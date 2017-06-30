package com.microsoft.azure.servicebus.primitives;

/**
 * This exception is thrown when a receiver attempts <code>complete</code> or <code>abandon</code> or <code>renew-lock</code> or <code>deadLetter</code> or <code>defer</code> operation
 * on a peek-locked message whose lock had already expired.
 * @since 1.0
 *
 */
public class MessageLockLostException extends ServiceBusException {
		
	private static final long serialVersionUID = -203350475131556600L;

	public MessageLockLostException()
	{
		super(false);
	}
	
	public MessageLockLostException(String message)
	{
		super(false, message);
	}
	
	public MessageLockLostException(Throwable cause)
	{
		super(false, cause);
	}
	
	public MessageLockLostException(String message, Throwable cause)
	{
		super(false, message, cause);
	}
}
