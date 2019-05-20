// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.amqp.exception;

import com.azure.core.exception.AzureException;
import com.azure.core.implementation.util.ImplUtils;

import java.util.Locale;

/**
 * General exception for AMQP related failures, Azure Service Bus and Event Hubs.
 *
 * @see ErrorCondition
 * @see <a href="http://go.microsoft.com/fwlink/?LinkId=761101">Azure Messaging Exceptions</a>
 */
public class AmqpException extends AzureException {
    private static final long serialVersionUID = -3654294093967132325L;

    private ErrorContext errorContext;
    private final boolean isTransient;
    private final ErrorCondition errorCondition;

    public AmqpException(boolean isTransient, String description) {
        this(isTransient, null, description);
    }

    public AmqpException(boolean isTransient, ErrorCondition errorCondition, String description) {
        super(description);
        this.errorCondition = errorCondition;
        this.isTransient = isTransient;
    }

    public AmqpException(boolean isTransient, ErrorCondition errorCondition, Throwable cause) {
        this(isTransient, errorCondition, cause.toString(), cause);
    }

    public AmqpException(boolean isTransient, ErrorCondition errorCondition, String description, Throwable cause) {
        super(description, cause);
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
    public boolean getIsTransient() {
        return this.isTransient;
    }

    /**
     * Gets the ErrorCondition for this exception.
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
