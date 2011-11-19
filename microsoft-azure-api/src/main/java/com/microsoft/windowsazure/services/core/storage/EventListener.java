package com.microsoft.windowsazure.services.core.storage;

/**
 * 
 * Abstract class that represents a generic event listener.
 * 
 * @param <T>
 *            The type of the event to be received.
 * 
 *            Copyright (c)2011 Microsoft. All rights reserved.
 */
public abstract class EventListener<T> {
    /**
     * Represents an event that occurred.
     * 
     * @param eventArg
     *            The event object.
     */
    public abstract void eventOccurred(T eventArg);
}
