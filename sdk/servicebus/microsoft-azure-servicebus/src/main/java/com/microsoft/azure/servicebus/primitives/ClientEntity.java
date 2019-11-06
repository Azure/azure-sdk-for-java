// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 *  Contract for all client entities with Open-Close/Abort state m/c <p>
 *  main-purpose: closeAll related entities <p>
 *  Internal-class
 *  @since 1.0
 */
public abstract class ClientEntity {
    private final String clientId;
    private final Object syncClose;

    private boolean isClosing;
    private boolean isClosed;

    protected ClientEntity(final String clientId) {
        this.clientId = clientId;
        this.syncClose = new Object();
    }

    protected abstract CompletableFuture<Void> onClose();

    public String getClientId() {
        return this.clientId;
    }

    protected boolean getIsClosed() {
        synchronized (this.syncClose) {
            return this.isClosed;
        }
    }

    protected boolean getIsClosingOrClosed() {
        synchronized (this.syncClose) {
            return this.isClosing || this.isClosed;
        }
    }

    // used to force close when entity is faulted
    protected final void setClosed() {
        synchronized (this.syncClose) {
        	this.isClosing = false;
            this.isClosed = true;
        }
    }
    
    protected final void setClosing() {
        synchronized (this.syncClose) {
        	if (!this.isClosed) {
        		this.isClosing = true;
        	}            
        }
    }

    public final CompletableFuture<Void> closeAsync() {
        if (this.getIsClosingOrClosed()) {
            return CompletableFuture.completedFuture(null);
        }

        synchronized (this.syncClose) {
            this.isClosing = true;
        }

        return this.onClose().thenRunAsync(() -> {
            synchronized (ClientEntity.this.syncClose) {
                ClientEntity.this.isClosing = false;
                ClientEntity.this.isClosed = true;
            }
        }, MessagingFactory.INTERNAL_THREAD_POOL);
    }

    public final void close() throws ServiceBusException {
        try {
            this.closeAsync().get();
        } catch (InterruptedException | ExecutionException exception) {
            if (exception instanceof InterruptedException) {
                // Re-assert the thread's interrupted status
                Thread.currentThread().interrupt();
            }

            Throwable throwable = exception.getCause();
            if (throwable != null) {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }

                if (throwable instanceof ServiceBusException) {
                    throw (ServiceBusException) throwable;
                }

                throw new ServiceBusException(true, throwable);
            }
        }
    }

    protected final void throwIfClosed(Throwable cause) {
        if (this.getIsClosingOrClosed()) {
            throw new IllegalStateException(String.format(Locale.US, "Operation not allowed after the %s instance is closed.", this.getClass().getName()), cause);
        }
    }

    @Override
    protected void finalize() throws Throwable {
    	if (!this.getIsClosingOrClosed()) {
    		this.closeAsync();
    	}
    	super.finalize();
    }
}
