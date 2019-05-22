// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import com.azure.core.exception.AzureException;
import com.azure.core.implementation.util.ImplUtils;

import java.util.Locale;

/**
 * General exception for AMQP related failures.
 *
 * @see ErrorCondition
 * @see <a href="http://go.microsoft.com/fwlink/?LinkId=761101">Azure Messaging Exceptions</a>
 */
public class AmqpException extends AzureException {
    private static final long serialVersionUID = -3654294093967132325L;

    private ErrorContext errorContext;
    private final boolean isTransient;
    private final ErrorCondition errorCondition;

    /**
     * Initializes a new instance of the AmqpException class.
     *
     * @param isTransient A boolean indicating if the exception is a transient error or not. If true, then the request
     * can be retried; otherwise not.
     * @param message Text containing any supplementary details of the exception.
     */
    public AmqpException(boolean isTransient, String message) {
        this(isTransient, null, message);
    }

    /**
     * Initializes a new instance of the AmqpException class.
     *
     * @param isTransient A boolean indicating if the exception is a transient error or not. If true, then the request
     * can be retried; otherwise not.
     * @param errorCondition The symbolic value indicating the error condition.
     * @param message Text containing any supplementary details not indicated by the condition field. This text can
     * be logged as an aid to resolving issues.
     */
    public AmqpException(boolean isTransient, ErrorCondition errorCondition, String message) {
        super(message);
        this.errorCondition = errorCondition;
        this.isTransient = isTransient;
    }

    /**
     * Initializes a new instance of the AmqpException class.
     *
     * @param isTransient A boolean indicating if the exception is a transient error or not. If true, then the request
     * can be retried; otherwise not.
     * @param errorCondition The symbolic value indicating the error condition.
     * @param cause The Throwable which caused the creation of this AmqpException.
     */
    public AmqpException(boolean isTransient, ErrorCondition errorCondition, Throwable cause) {
        super(cause.getMessage(), cause);
        this.errorCondition = errorCondition;
        this.isTransient = isTransient;
    }

    @Override
    public String getMessage() {
        final String baseMessage = super.getMessage();

        if (this.errorContext == null) {
            return super.getMessage();
        }

        return !ImplUtils.isNullOrEmpty(baseMessage)
            ? String.format(Locale.US, "%s, %s[%s]", baseMessage, "errorContext", errorContext.toString())
            : String.format(Locale.US, "%s[%s]", "errorContext", errorContext.toString());
    }

    /**
     * A boolean indicating if the exception is a transient error or not.
     *
     * @return returns true when user can retry the operation that generated the exception without additional intervention.
     */
    public boolean isTransient() {
        return this.isTransient;
    }

    /**
     * Gets the ErrorCondition for this exception.
     *
     * @return The ErrorCondition for this exception, or {@code null} if nothing was set.
     */
    public ErrorCondition getErrorCondition() {
        return this.errorCondition;
    }

    /**
     * Gets the context for this exception.
     *
     * @return The context for this exception.
     */
    public ErrorContext getContext() {
        return this.errorContext;
    }

    /**
     * Sets the context for this exception.
     *
     * @param errorContext The context for this exception.
     */
    public void setContext(ErrorContext errorContext) {
        this.errorContext = errorContext;
    }
}
