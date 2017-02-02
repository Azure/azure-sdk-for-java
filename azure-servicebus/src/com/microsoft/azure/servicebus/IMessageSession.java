package com.microsoft.azure.servicebus;

import java.io.InputStream;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public interface IMessageSession extends IMessageReceiver {
	String getSessionId();
	
	Instant getLockedUntilUtc();
	
	void renewLock();
	
	CompletableFuture<Void> renewLockAsync();
	
	void setState(InputStream stream);
	
	CompletableFuture<Void> setStateAsync(InputStream stream);
	
	InputStream getState();
	
	CompletableFuture<InputStream> getStateAsync();	
}
