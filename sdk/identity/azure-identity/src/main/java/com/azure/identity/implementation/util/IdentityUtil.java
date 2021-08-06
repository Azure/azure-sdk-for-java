package com.azure.identity.implementation.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.experimental.credential.TokenRequestContextExperimental;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;

public class IdentityUtil {
    private static final ClientLogger logger = new ClientLogger(IdentityUtil.class);

    /**
     * Convert a resource to a list of scopes.
     * @param currentTenantId the current tenant Id.
     * @param requestContext the user passed in {@link TokenRequestContext}
     * @param options the identity client options bag.
     * on the credential or not.
     */
    public static String resolveTenantId(String currentTenantId, TokenRequestContext requestContext,
                                         IdentityClientOptions options) {

        String contextTenantId;
        if (requestContext instanceof TokenRequestContextExperimental) {
            contextTenantId = ((TokenRequestContextExperimental) requestContext).getTenantId();
        } else {
            return currentTenantId;
        }

        if (!options.getAllowMultiTenantAuthentication()) {
            if (contextTenantId != null && currentTenantId != contextTenantId
                && !options.isLegacyTenantSelectionEnabled()) {
                logger.logExceptionAsError(new RuntimeException("The TenantId received from a challenge did not match "
                    + "the configured TenantId and AllowMultiTenantAuthentication is false."));
            }
            return CoreUtils.isNullOrEmpty(currentTenantId) ? contextTenantId : currentTenantId;
        }

        return CoreUtils.isNullOrEmpty(contextTenantId) ? currentTenantId
                : contextTenantId;
    }
}
