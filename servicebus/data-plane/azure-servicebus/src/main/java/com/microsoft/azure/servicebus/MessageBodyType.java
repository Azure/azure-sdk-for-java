package com.microsoft.azure.servicebus;

/**
 * Enumeration to represent body type of a message.
 * 
 */
public enum MessageBodyType {
	/**
	 * Message content is byte array, equivalent to AMQP Data.
	 */
    BINARY,
    /**
     * Message content is a list of objects, equivalent to AMQP Sequence. Each object must be of a type supported by AMQP.
     */
    SEQUENCE,
    /**
     * Message content is a single object, equivalent to AMQP Value. The object must be of a type supported by AMQP.
     */
    VALUE
}
