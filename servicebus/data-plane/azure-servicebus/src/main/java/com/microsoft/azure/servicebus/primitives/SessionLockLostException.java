package com.microsoft.azure.servicebus.primitives;

/**
 * This exception is thrown when a session receiver performs an operation on a session after its lock is expired. When a client accepts a session, the session is locked to the receiver
 * for a duration specified in the entity definition. When the accepted session remains idle for the duration of lock, that is no operations performed on the session, the lock expires and the session is made available
 * to other clients.
 * @since 1.0
 *
 */
public class SessionLockLostException extends ServiceBusException {
	
	private static final long serialVersionUID = -5861754850637792928L;

	public SessionLockLostException()
	{
		super(false);
	}
	
	public SessionLockLostException(String message)
	{
		super(false, message);
	}
	
	public SessionLockLostException(Throwable cause)
	{
		super(false, cause);
	}
	
	public SessionLockLostException(String message, Throwable cause)
	{
		super(false, message, cause);
	}
}
