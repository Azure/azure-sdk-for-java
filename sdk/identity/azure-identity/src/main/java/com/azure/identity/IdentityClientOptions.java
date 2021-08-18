package com.azure.identity;

import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;

public class IdentityClientOptions extends ClientOptions {
    private boolean allowPiilogging;

    /**
     * Sets the flag indicating whether client side Pii logs should be logged or not. The default value is false.
     * <p>
     * The Pii logs can contain sensitive information and should be enabled on protected machines only.
     * <p>
     *
     * @param allowPiiLogging The flag indicating if client side pii logging should be enabled or not.
     * @return The updated ClientOptions object.
     */
    public ClientOptions setAllowPiiLogging(boolean allowPiiLogging) {
        this.allowPiilogging = allowPiiLogging;
        return this;
    }

    /**
     * Gets the status indicating if Pii logging is allowed or not.
     *
     * @return The flag indicating if client side pii logging should be enabled or not.
     */
    public boolean isPiiLoggingAllowed() {
        return allowPiilogging;
    }

    /**
     * {@inheritDoc}
     */
    public IdentityClientOptions setApplicationId(String applicationId) {
        super.setApplicationId(applicationId);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public ClientOptions setHeaders(Iterable<Header> headers) {
        super.setHeaders(headers);
        return this;
    }
}
