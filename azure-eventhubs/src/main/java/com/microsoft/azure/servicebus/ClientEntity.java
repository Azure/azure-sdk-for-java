/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;

/**
 *  Contract for all client entities with Open-Close/Abort state m/c
 *  main-purpose: closeAll related entities
 *  Internal-class
 */
public abstract class ClientEntity
{
	private String clientId;
	protected ClientEntity(final String clientId)
	{
		this.clientId = clientId;
	}
	
	public abstract CompletableFuture<Void> close();
	
	public String getClientId()
	{
		return this.clientId;
	}
}
