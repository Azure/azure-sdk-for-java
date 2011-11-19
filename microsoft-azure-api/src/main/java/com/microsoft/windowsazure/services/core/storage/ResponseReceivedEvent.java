package com.microsoft.windowsazure.services.core.storage;

/**
 * Represents an event that is fired when a response is received.
 * 
 * Copyright (c)2011 Microsoft
 */
public final class ResponseReceivedEvent {

    /**
     * Represents a connection object. Currently only <code>java.net.HttpURLConnection</code> is supported as a
     * connection object.
     */
    private Object connectionObject;

    /**
     * Represents a context for the current operation. This object is used to track requests to the storage service, and
     * to provide additional runtime information about the operation.
     */
    private OperationContext opContext;

    /**
     * Creates an instance of the <code>ResponseReceivedEvent</code> class.
     * 
     * @param opContext
     *            An {@link OperationContext} object that represents the context for the current operation. This object
     *            is used to track requests to the storage service, and to provide additional runtime information about
     *            the operation.
     * @param connectionObject
     *            Represents a connection object. Currently only <code>java.net.HttpURLConnection</code> is supported as
     *            a connection object.
     */
    public ResponseReceivedEvent(final OperationContext opContext, final Object connectionObject) {
        this.opContext = opContext;
        this.connectionObject = connectionObject;
    }

    /**
     * @return the connectionObject
     */
    public Object getConnectionObject() {
        return this.connectionObject;
    }

    /**
     * @return the opContext
     */
    public OperationContext getOpContext() {
        return this.opContext;
    }
}
