package com.microsoft.azure.servicebus.primitives;

import java.util.UUID;

import org.apache.qpid.proton.message.Message;

public class MessageWithLockToken {
	private final Message message;
	private final UUID lockToken;
	
	public MessageWithLockToken(Message message, UUID lockToken)
	{
		this.message = message;
		this.lockToken = lockToken;
	}

	public Message getMessage() {
		return message;
	}

	public UUID getLockToken() {
		return lockToken;
	}
}
