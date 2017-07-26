package com.microsoft.azure.servicebus;

/**
 * Enumeration to represent the phase in a message pump or session pump at which an exception occurred.
 * @since 1.0
 *
 */
public enum ExceptionPhase {
    /**
     * Receiving messages from Azure Service Bus threw an exception.
     */
	RECEIVE,
	/**
	 * Renewing the lock of a message threw an exception.
	 */
	RENEWMESSAGELOCK,
	/**
	 * Completing a message threw an exception
	 */
	COMPLETE,
	/**
	 * Abandoning a message threw an exception
	 */
	ABANDON,
	/**
	 * Application code in message handler or session handler threw an exception. Applications should ideally not throw exceptions from mesage handler or session handler.
	 */
	USERCALLBACK,
	/**
	 * Closing a session threw an exception.
	 */
	SESSIONCLOSE,
	/**
	 * Accepting a session threw an exception.
	 */
	ACCEPTSESSION,
	/**
	 * Renewing the lock of a session threw an exception.
	 */
	RENEWSESSIONLOCK
}
