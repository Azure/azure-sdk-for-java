package com.azure.identity.implementation.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.experimental.credential.TokenRequestContextExperimental;
import com.azure.core.util.CoreUtils;

public class IdentityUtil {

    /**
     * Convert a resource to a list of scopes.
     * @param currentTenantId the current tenant Id.
     * @param requestContext the user passed in {@link TokenRequestContext}
     * @param allowMultiTenantAuthentication the flag indicating if multi-tenant authentication is enabled
     * on the credential or not.
     */
    public static String resolveTenantId(String currentTenantId, TokenRequestContext requestContext,
                                           boolean allowMultiTenantAuthentication) {

        TokenRequestContextExperimental requestContextExperimental;
        if (requestContext instanceof TokenRequestContextExperimental) {
            requestContextExperimental = (TokenRequestContextExperimental) requestContext;
        } else {
            return currentTenantId;
        }

        if (!allowMultiTenantAuthentication) {
//            if (requestContext.getTenantId() != null && currentTenantId != requestContext.getTenantId()) {
//            }
            return CoreUtils.isNullOrEmpty(currentTenantId) ? requestContextExperimental.getTenantId() : currentTenantId;
        }

        return CoreUtils.isNullOrEmpty(requestContextExperimental.getTenantId()) ? currentTenantId
                : requestContextExperimental.getTenantId();
    }
}
