// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;

public final class IdentityUtil {
    private static final ClientLogger LOGGER = new ClientLogger(IdentityUtil.class);

    private IdentityUtil() { }
    /**
     * Resolve the Tenant Id to be used in the authentication requests.
     * @param currentTenantId the current tenant Id.
     * @param requestContext the user passed in {@link TokenRequestContext}
     * @param options the identity client options bag.
     * on the credential or not.
     */
    public static String resolveTenantId(String currentTenantId, TokenRequestContext requestContext,
                                         IdentityClientOptions options) {

        String contextTenantId = requestContext.getTenantId();

        if (contextTenantId != null && currentTenantId != null && !currentTenantId.equals(contextTenantId)) {
            if (options.isMultiTenantAuthenticationDisabled()) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("The Multi Tenant Authentication "
                    + "is disabled. An updated Tenant Id provided via TokenRequestContext cannot be used in this "
                    + "scenario. To resolve this issue, set the env var AZURE_IDENTITY_DISABLE_MULTITENANTAUTH"
                    + " to false ",
                    null));
            } else if (currentTenantId.equals("adfs")) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("The credential is configured with" +
                    "`adfs` tenant id and it cannot be replaced with a tenant id challenge provided via "
                    + "TokenRequestContext class. ", null));
            }
            return CoreUtils.isNullOrEmpty(contextTenantId) ? currentTenantId
                : contextTenantId;
        }

        return currentTenantId;

    }
}
