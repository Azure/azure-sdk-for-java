package com.microsoft.windowsazure.serviceruntime;

/**
 * Represents the listener for the stopping event.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 *  
 * @author mariok
 */
public interface RoleEnvironmentStoppingListener {
    /**
     * Occurs when the role instance is about to be stopped.
     * <p>
     * This event is raised after the instance has been taken out of the load
     * balancer's rotation before the <code>OnStop</code> method is called. You
     * can use this event to run code that is required for the role instance
     * to shut down in an orderly fashion.
     */

    public void roleEnvironmentStopping();

}