/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

/**
 * Represents an exception indicating the role environment is not available.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 * 
 * 
 * 
 */
public class RoleEnvironmentNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = -6218741025124056882L;

    /**
     * Creates an instance of the
     * <code>RoleEnvironmentNotAvailableException</code> class.
     * 
     * @param t
     *            A <code>java.lang.Throwable</code> object that represents the
     *            cause for the exception.
     * 
     */

    public RoleEnvironmentNotAvailableException(Throwable t) {
        initCause(t);
    }
}
