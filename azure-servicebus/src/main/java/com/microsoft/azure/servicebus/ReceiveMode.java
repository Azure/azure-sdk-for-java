package com.microsoft.azure.servicebus;

/**
 * Enumeration to represent the two receive modes Azure Service Bus supports.
 * @since 1.0
 *
 */
public enum ReceiveMode {
    /**
     * In this mode, received message is not deleted from the queue or subscription, instead it is temporarily locked to the receiver, making it invisible to other receivers. Then the service waits for one of the three events
     * <ul>
     * <li>If the receiver processes the message successfully, it calls <code>complete</code> and the message will be deleted.</li>
     * <li>If the receiver decides that it can't process the message successfully, it calls <code>abandon</code> and the message will be unlocked and made available to other receivers.</li>
     * <li>If the receiver wants to defer the processing of the message to a later point in time, it calls <code>defer</code> and the message will be deferred. A deferred can only be received by its sequence number.</li>
     * <li>If the receiver wants to dead-letter the message, it calls <code>deadLetter</code> and the message will be moved to a special sub-queue called deadletter queue.</li>
     * <li>If the receiver calls neither of these methods within a configurable period of time (by default, 60 seconds), the service assumes the receiver has failed. In this case, it behaves as if the receiver had called <code>abandon</code>, making the message available to other receivers</li>
     * </ul>
     */
	PEEKLOCK,
	/**
	 * In this mode, received message is removed from the queue or subscription and immediately deleted. This option is simple, but if the receiver crashes
	 *  before it finishes processing the message, the message is lost. Because it's been removed from the queue, no other receiver can access it.
	 */
	RECEIVEANDDELETE
}
