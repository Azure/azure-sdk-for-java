package com.microsoft.azure.eventhubs.common;

// contract for all client entities with Open-Close/Abort state m/c 
public abstract class ClientEntity {
	
	public abstract void close();
	
}
