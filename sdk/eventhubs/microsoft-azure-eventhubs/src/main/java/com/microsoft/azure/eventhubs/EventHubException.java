// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import com.microsoft.azure.eventhubs.impl.StringUtil;

import java.util.Locale;

/**
 * This is the base exception that service bus will produce for all error cases.
 */
public class EventHubException extends Exception {
    private static final long serialVersionUID = -3654294093967132325L;

    private final boolean isTransient;
    private ErrorContext errorContext;

    EventHubException(final boolean isTransient) {
        super();
        this.isTransient = isTransient;
    }

    public EventHubException(final boolean isTransient, final String message) {
        super(message);
        this.isTransient = isTransient;
    }

    public EventHubException(final boolean isTransient, final Throwable cause) {
        super(cause);
        this.isTransient = isTransient;
    }

    public EventHubException(final boolean isTransient, final String message, final Throwable cause) {
        super(message, cause);
        this.isTransient = isTransient;
    }

    @Override
    public String getMessage() {
        final String baseMessage = super.getMessage();
        return this.errorContext == null || StringUtil.isNullOrEmpty(this.errorContext.toString())
                ? baseMessage
                : (!StringUtil.isNullOrEmpty(baseMessage)
                ? String.format(Locale.US, "%s, %s[%s]", baseMessage, "errorContext", this.errorContext.toString())
                : String.format(Locale.US, "%s[%s]", "errorContext", this.errorContext.toString()));
    }

    /**
     * A boolean indicating if the exception is a transient error or not.
     *
     * @return returns true when user can retry the operation that generated the exception without additional intervention.
     */
    public boolean getIsTransient() {
        return this.isTransient;
    }

    public ErrorContext getContext() {
        return this.errorContext;
    }

    public void setContext(ErrorContext errorContext) {
        this.errorContext = errorContext;
    }
}
