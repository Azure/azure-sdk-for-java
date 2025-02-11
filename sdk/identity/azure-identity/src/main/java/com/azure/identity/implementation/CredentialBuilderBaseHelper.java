// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.CredentialBuilderBase;

/**
 * Helper type for accessing private fields of {@link CredentialBuilderBase}.
 */
public final class CredentialBuilderBaseHelper {
    private static final ClientLogger LOGGER = new ClientLogger(CredentialBuilderBaseHelper.class);
    private static CredentialBuilderBaseAccessor accessor;

    private CredentialBuilderBaseHelper() {
    }

    public interface CredentialBuilderBaseAccessor {
        IdentityClientOptions getClientOptions(CredentialBuilderBase<?> builder);
    }

    public static void setAccessor(final CredentialBuilderBaseAccessor newAccessor) {
        if (accessor != null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("Accessor must be non-null"));
        }
        accessor = newAccessor;
    }

    public static CredentialBuilderBaseAccessor getAccessor() {
        if (accessor == null) {
            throw LOGGER
                .logExceptionAsError(new IllegalStateException("CredentialBuilderBaseHelper must be initialized"));
        }
        return accessor;
    }

    public static IdentityClientOptions getClientOptions(CredentialBuilderBase<?> builder) {
        return getAccessor().getClientOptions(builder);
    }
}
