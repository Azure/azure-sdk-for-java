// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.experimental.credential.TokenRequestContextExperimental;
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

        String contextTenantId;
        if (requestContext instanceof TokenRequestContextExperimental) {
            TokenRequestContextExperimental experimental = ((TokenRequestContextExperimental) requestContext);
            contextTenantId = experimental.getTenantId();
        } else {
            return currentTenantId;
        }

        if (!options.isMultiTenantAuthenticationAllowed()) {
            if (contextTenantId != null && !currentTenantId.equals(contextTenantId)
                && !options.isLegacyTenantSelectionEnabled()) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("The TenantId received from a"
                    + " challenge did not match the configured TenantId and AllowMultiTenantAuthentication is false.",
                    null));
            }
            return CoreUtils.isNullOrEmpty(currentTenantId) ? contextTenantId : currentTenantId;
        }

        return CoreUtils.isNullOrEmpty(contextTenantId) ? currentTenantId
                : contextTenantId;
    }
}
