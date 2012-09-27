/**
 * 
 */
package com.microsoft.windowsazure.services.core;

/**
 * Exception indicating a service operation has timed out.
 */
public class ServiceTimeoutException extends ServiceException {

    private static final long serialVersionUID = 6612846403178749361L;

    /**
     * Construct a ServiceTimeoutException instance
     */
    public ServiceTimeoutException() {
    }

    /**
     * Construct a ServiceTimeoutException instance
     * 
     * @param message
     *            Exception message
     */
    public ServiceTimeoutException(String message) {
        super(message);
    }

    /**
     * Construct a ServiceTimeoutException instance
     * 
     * @param message
     *            Exception message
     * @param cause
     *            Exception that caused this exception to occur
     */
    public ServiceTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a ServiceTimeoutException instance
     * 
     * @param cause
     *            Exception that caused this exception to occur
     */
    public ServiceTimeoutException(Throwable cause) {
        super(cause);
    }
}
