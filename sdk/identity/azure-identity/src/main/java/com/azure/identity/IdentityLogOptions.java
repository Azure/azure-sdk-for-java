// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.identity;

import com.azure.core.annotation.Fluent;

/**
 * Represents the Identity Log options used to configure logging on the Token Credentials.
 */
@Fluent
public final class IdentityLogOptions {
    private boolean allowLoggingAccountIdentifiers;

    /**
     * Sets the flag indicating whether client side account identifier logs should be logged or not.
     * The default value is false.
     * <p>
     * The Account Identifier logs can contain sensitive information and should be enabled on protected machines only.
     * Enabling this logs Application ID, Object ID, Tenant ID and User Principal Name at INFO level when an
     * access token is successfully retrieved. Ensure that INFO level logs are enabled to
     * see the account identifier logs.
     * <p>
     *
     * @param allowLoggingAccountIdentifiers The flag indicating if client side account identifier logging should be
     * enabled or not.
     * @return The updated IdentityClientOptions object.
     */
    public IdentityLogOptions setLoggingAccountIdentifiersAllowed(boolean allowLoggingAccountIdentifiers) {
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
}
