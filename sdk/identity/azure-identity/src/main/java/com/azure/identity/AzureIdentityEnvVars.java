// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * <p>Defines well-known Azure Identity environment variable names that can be used with 
 * {@link DefaultAzureCredentialBuilder#requireEnvVars(AzureIdentityEnvVars...)}.</p>
 *
 * <p>This expandable enum provides a type-safe way to reference common Azure Identity environment
 * variables while still allowing for custom environment variable names.</p>
 *
 * @see DefaultAzureCredentialBuilder#requireEnvVars(AzureIdentityEnvVars...)
 */
public final class AzureIdentityEnvVars extends ExpandableStringEnum<AzureIdentityEnvVars> {

    /**
     * Creates a new instance of {@link AzureIdentityEnvVars} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link AzureIdentityEnvVars} which doesn't have a String enum
     * value.
     *
     * @deprecated Use one of the constants or the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public AzureIdentityEnvVars() {
    }

    /**
     * The Azure tenant ID environment variable.
     */
    public static final AzureIdentityEnvVars AZURE_TENANT_ID = fromString("AZURE_TENANT_ID");

    /**
     * The Azure client ID environment variable.
     */
    public static final AzureIdentityEnvVars AZURE_CLIENT_ID = fromString("AZURE_CLIENT_ID");

    /**
     * The Azure client secret environment variable.
     */
    public static final AzureIdentityEnvVars AZURE_CLIENT_SECRET = fromString("AZURE_CLIENT_SECRET");

    /**
     * The Azure client certificate path environment variable.
     */
    public static final AzureIdentityEnvVars AZURE_CLIENT_CERTIFICATE_PATH
        = fromString("AZURE_CLIENT_CERTIFICATE_PATH");

    /**
     * The Azure client certificate password environment variable.
     */
    public static final AzureIdentityEnvVars AZURE_CLIENT_CERTIFICATE_PASSWORD
        = fromString("AZURE_CLIENT_CERTIFICATE_PASSWORD");

    /**
     * The Azure authority host environment variable.
     */
    public static final AzureIdentityEnvVars AZURE_AUTHORITY_HOST = fromString("AZURE_AUTHORITY_HOST");

    /**
     * The Azure token credentials environment variable for selecting credential types.
     */
    public static final AzureIdentityEnvVars AZURE_TOKEN_CREDENTIALS = fromString("AZURE_TOKEN_CREDENTIALS");

    /**
     * The Azure client send certificate chain environment variable.
     */
    public static final AzureIdentityEnvVars AZURE_CLIENT_SEND_CERTIFICATE_CHAIN
        = fromString("AZURE_CLIENT_SEND_CERTIFICATE_CHAIN");

    /**
     * Creates or finds an AzureIdentityEnvVars from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding AzureIdentityEnvVars.
     */
    public static AzureIdentityEnvVars fromString(String name) {
        if (name == null) {
            return null;
        }
        return fromString(name, AzureIdentityEnvVars.class);
    }

    /**
     * Gets known AzureIdentityEnvVars values.
     *
     * @return known AzureIdentityEnvVars values.
     */
    public static Collection<AzureIdentityEnvVars> values() {
        return values(AzureIdentityEnvVars.class);
    }
}
