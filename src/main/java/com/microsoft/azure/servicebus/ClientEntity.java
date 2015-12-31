package com.microsoft.azure.servicebus;

// TODO: contract for all client entities with Open-Close/Abort state m/c 
// main-purpose: closeAll related entities
public abstract class ClientEntity
{
	private String clientId;
	protected ClientEntity(final String clientId)
	{
		this.clientId = clientId;
	}
	
	public abstract void close();
	
	public String getClientId()
	{
		return this.clientId;
	}
}
