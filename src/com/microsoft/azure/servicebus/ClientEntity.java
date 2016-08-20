/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *  Contract for all client entities with Open-Close/Abort state m/c
 *  main-purpose: closeAll related entities
 *  Internal-class
 */
public abstract class ClientEntity
{
	private final String clientId;
	private final Object syncClose;
	private final ClientEntity parent;

	private boolean isClosing;
	private boolean isClosed;

	protected ClientEntity(final String clientId, final ClientEntity parent)
	{
		this.clientId = clientId;
		this.parent = parent;

		this.syncClose = new Object();
	}

	protected abstract CompletableFuture<Void> onClose();

	public String getClientId()
	{
		return this.clientId;
	}

	boolean getIsClosed()
	{
		final boolean isParentClosed = this.parent != null && this.parent.getIsClosed();
		synchronized (this.syncClose)
		{
			return isParentClosed || this.isClosed;
		}
	}

	// returns true even if the Parent is (being) Closed
	boolean getIsClosingOrClosed()
	{
		final boolean isParentClosingOrClosed = this.parent != null && this.parent.getIsClosingOrClosed();
		synchronized (this.syncClose)
		{
			return isParentClosingOrClosed || this.isClosing || this.isClosed;
		}
	}

	// used to force close when entity is faulted
	protected final void setClosed()
	{
		synchronized (this.syncClose)
		{
			this.isClosed = true;
		}
	}

	public final CompletableFuture<Void> close()
	{
		synchronized (this.syncClose)
		{
			this.isClosing = true;
		}

		return this.onClose().thenRunAsync(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized (ClientEntity.this.syncClose)
				{
					ClientEntity.this.isClosing = false;
					ClientEntity.this.isClosed = true;
				}
			}});
	}

	public final void closeSync() throws ServiceBusException
	{
		try
		{
			this.close().get();
		}
		catch (InterruptedException|ExecutionException exception)
		{
			if (exception instanceof InterruptedException)
			{
				// Re-assert the thread's interrupted status
				Thread.currentThread().interrupt();
			}

			Throwable throwable = exception.getCause();
			if (throwable != null)
			{
				if (throwable instanceof RuntimeException)
				{
					throw (RuntimeException)throwable;
				}

				if (throwable instanceof ServiceBusException)
				{
					throw (ServiceBusException)throwable;
				}

				throw new ServiceBusException(true, throwable);
			}
		}
	}

	protected final void throwIfClosed(Throwable cause)
	{
		if (this.getIsClosingOrClosed())
		{
			throw new IllegalStateException(String.format(Locale.US, "Operation not allowed after the %s instance is Closed.", this.getClass().getName()), cause);
		}
	}
}
