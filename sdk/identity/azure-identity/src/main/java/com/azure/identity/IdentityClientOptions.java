package com.azure.identity;

import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;

public final class IdentityClientOptions extends ClientOptions {
    private boolean allowLoggingAccountIdentifiers;

    /**
     * Sets the flag indicating whether client side account identifier logs should be logged or not.
     * The default value is false.
     * <p>
     * The Account Identifier logs can contain sensitive information and should be enabled on protected machines only.
     * <p>
     *
     * @param allowLoggingAccountIdentifiers The flag indicating if client side account identifier logging should be
     * enabled or not.
     * @return The updated IdentityClientOptions object.
     */
    public IdentityClientOptions setAllowLoggingAccountIdentifiers(boolean allowLoggingAccountIdentifiers) {
        this.allowLoggingAccountIdentifiers = allowLoggingAccountIdentifiers;
        return this;
    }

    /**
     * Gets the status indicating if Account Identifier Logging is allowed or not.
     *
     * @return The flag indicating if client side account identifier logging should be enabled or not.
     */
    public boolean isLoggingAccountIdentifiersAllowed() {
        return allowLoggingAccountIdentifiers;
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
    public IdentityClientOptions setHeaders(Iterable<Header> headers) {
        super.setHeaders(headers);
        return this;
    }
}
