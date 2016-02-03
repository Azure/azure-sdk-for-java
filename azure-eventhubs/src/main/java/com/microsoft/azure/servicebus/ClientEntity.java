package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

// TODO: contract for all client entities with Open-Close/Abort state m/c
// TODO: Add retryPolicy to ClientEntity
// main-purpose: closeAll related entities
public abstract class ClientEntity
{
	private String clientId;
	protected ClientEntity(final String clientId)
	{
		this.clientId = clientId;
	}
	
	public abstract CompletableFuture<Void> closeAsync();
	
	public String getClientId()
	{
		return this.clientId;
	}
}
