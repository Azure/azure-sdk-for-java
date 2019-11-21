// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.exception;

import com.azure.core.exception.AzureException;
import com.azure.core.util.CoreUtils;

import java.util.Locale;

/**
 * General exception for AMQP related failures.
 *
 * @see AmqpErrorCondition
 * @see <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-messaging-exceptions">Azure Messaging
 *     Exceptions</a>
 */
public class AmqpException extends AzureException {
    private static final long serialVersionUID = -3654294093967132325L;

    private final AmqpErrorContext errorContext;
    private final boolean isTransient;
    private final AmqpErrorCondition errorCondition;

    /**
     * Initializes a new instance of the AmqpException class.
     *
     * @param isTransient A boolean indicating if the exception is a transient error or not. If true, then the
     *     request can be retried; otherwise not.
     * @param message Text containing any supplementary details of the exception.
     * @param errorContext The context that caused this AMQP error.
     */
    public AmqpException(boolean isTransient, String message, AmqpErrorContext errorContext) {
        this(isTransient, null, message, errorContext);
    }

    /**
     * Initializes a new instance of the AmqpException class.
     *
     * @param isTransient A boolean indicating if the exception is a transient error or not. If true, then the
     *     request can be retried; otherwise not.
     * @param errorCondition The symbolic value indicating the error condition.
     * @param message Text containing any supplementary details not indicated by the condition field. This text can
     *     be logged as an aid to resolving issues.
     * @param errorContext The context that caused this AMQP error.
     */
    public AmqpException(boolean isTransient, AmqpErrorCondition errorCondition, String message,
                         AmqpErrorContext errorContext) {
        super(message);
        this.errorCondition = errorCondition;
        this.isTransient = isTransient;
        this.errorContext = errorContext;
    }

    /**
     * Initializes a new instance of the AmqpException class.
     *
     * @param isTransient A boolean indicating if the exception is a transient error or not. If true, then the
     *     request can be retried; otherwise not.
     * @param errorCondition The symbolic value indicating the error condition.
     * @param message Text containing any supplementary details not indicated by the condition field. This text can
     *     be logged as an aid to resolving issues.
     * @param cause The Throwable which caused the creation of this AmqpException.
     * @param errorContext The context that caused this AMQP error.
     */
    public AmqpException(boolean isTransient, AmqpErrorCondition errorCondition, String message, Throwable cause,
                         AmqpErrorContext errorContext) {
        super(message, cause);
        this.errorCondition = errorCondition;
        this.isTransient = isTransient;
        this.errorContext = errorContext;
    }

    /**
     * Initializes a new instance of the AmqpException class.
     *
     * @param isTransient A boolean indicating if the exception is a transient error or not. If true, then the
     *     request can be retried; otherwise not.
     * @param errorCondition The symbolic value indicating the error condition.
     * @param cause The Throwable which caused the creation of this AmqpException.
     * @param errorContext The context that caused this AMQP error.
     */
    public AmqpException(boolean isTransient, AmqpErrorCondition errorCondition, Throwable cause,
                         AmqpErrorContext errorContext) {
        super(cause.getMessage(), cause);
        this.errorCondition = errorCondition;
        this.isTransient = isTransient;
        this.errorContext = errorContext;
    }

    /**
     * Initializes a new instance of the AmqpException class.
     *
     * @param isTransient A boolean indicating if the exception is a transient error or not. If true, then the
     *     request can be retried; otherwise not.
     * @param message Text containing any supplementary details not indicated by the condition field. This text can
     *     be logged as an aid to resolving issues.
     * @param cause The Throwable which caused the creation of this AmqpException.
     * @param errorContext The context that caused this AMQP error.
     */
    public AmqpException(boolean isTransient, String message, Throwable cause, AmqpErrorContext errorContext) {
        super(message, cause);
        this.errorCondition = null;
        this.isTransient = isTransient;
        this.errorContext = errorContext;
    }

    @Override
    public String getMessage() {
        final String baseMessage = super.getMessage();

        if (this.errorContext == null) {
            return super.getMessage();
        }

        return !CoreUtils.isNullOrEmpty(baseMessage)
            ? String.format(Locale.US, "%s, %s[%s]", baseMessage, "errorContext", errorContext.toString())
            : String.format(Locale.US, "%s[%s]", "errorContext", errorContext.toString());
    }

    /**
     * A boolean indicating if the exception is a transient error or not.
     *
     * @return returns true when user can retry the operation that generated the exception without additional
     * intervention.
     */
    public boolean isTransient() {
        return this.isTransient;
    }

    /**
     * Gets the {@link AmqpErrorCondition} for this exception.
     *
     * @return The {@link AmqpErrorCondition} for this exception, or {@code null} if nothing was set.
     */
    public AmqpErrorCondition getErrorCondition() {
        return this.errorCondition;
    }

    /**
     * Gets the context for this exception.
     *
     * @return The context for this exception.
     */
    public AmqpErrorContext getContext() {
        return this.errorContext;
    }
}
