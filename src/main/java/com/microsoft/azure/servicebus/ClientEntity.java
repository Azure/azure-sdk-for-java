package com.microsoft.azure.servicebus;

// TODO: contract for all client entities with Open-Close/Abort state m/c 
// main-purpose: closeAll related entities
public abstract class ClientEntity {
	
	public abstract void close();
	
}
