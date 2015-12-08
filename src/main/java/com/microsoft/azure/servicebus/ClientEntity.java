package com.microsoft.azure.servicebus;

// contract for all client entities with Open-Close/Abort state m/c 
public abstract class ClientEntity {
	
	public abstract void close();
	
}
