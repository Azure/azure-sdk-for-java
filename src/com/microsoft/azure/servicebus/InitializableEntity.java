package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ClientEntity;

abstract class InitializableEntity extends ClientEntity {

	//TODO Init and close semantics are primitive now. Fix them with support for other states like Initializing, Closing, and concurrency.
	protected InitializableEntity(String clientId, ClientEntity parent) {
		super(clientId, parent);		
	}
	
	abstract CompletableFuture<Void> initializeAsync() throws Exception;

}
