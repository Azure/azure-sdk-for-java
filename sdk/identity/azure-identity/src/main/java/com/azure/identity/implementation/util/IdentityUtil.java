// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClientOptions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public final class IdentityUtil {
    private static final ClientLogger LOGGER = new ClientLogger(IdentityUtil.class);
    public static final String AZURE_ADDITIONALLY_ALLOWED_TENANTS = "AZURE_ADDITIONALLY_ALLOWED_TENANTS";
    public static final String ALL_TENANTS = "*";
    public static final String DEFAULT_TENANT = "organizations";


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

        if (contextTenantId != null && currentTenantId != null && !currentTenantId.equalsIgnoreCase(contextTenantId)) {
            if (options.isMultiTenantAuthenticationDisabled()) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("The Multi Tenant Authentication "
                    + "is disabled. An updated Tenant Id provided via TokenRequestContext cannot be used in this "
                    + "scenario. To resolve this issue, set the env var AZURE_IDENTITY_DISABLE_MULTITENANTAUTH"
                    + " to false ",
                    null));
            } else if ("adfs".equals(currentTenantId)) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("The credential is configured with"
                    + "`adfs` tenant id and it cannot be replaced with a tenant id challenge provided via "
                    + "TokenRequestContext class. ", null));
            }
            String resolvedTenantId =  CoreUtils.isNullOrEmpty(contextTenantId) ? currentTenantId
                : contextTenantId;

            if (!resolvedTenantId.equalsIgnoreCase(currentTenantId) && !options.getAdditionallyAllowedTenants().contains(ALL_TENANTS)
                && !options.getAdditionallyAllowedTenants().contains(resolvedTenantId)) {
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException("The current credential is not configured to acquire tokens for tenant "
                    +  resolvedTenantId + ". To enable acquiring tokens for this tenant add it to the AdditionallyAllowedTenants on the credential options, "
                    + "or add \"*\" to AdditionallyAllowedTenants to allow acquiring tokens for any tenant. See the troubleshooting guide for more information. https://aka.ms/azsdk/java/identity/multitenant/troubleshoot", null));
            }
            return resolvedTenantId;
        }

        return currentTenantId;

    }

    public static List<String> resolveAdditionalTenants(List<String> additionallyAllowedTenants) {
        if (additionallyAllowedTenants == null) {
            return Collections.emptyList();
        }

        if (additionallyAllowedTenants.contains(ALL_TENANTS)) {
            return Arrays.asList(ALL_TENANTS);
        }
        return additionallyAllowedTenants;
    }

    public static List<String> getAdditionalTenantsFromEnvironment(Configuration configuration) {
        String additionalTenantsFromEnv = configuration.get(AZURE_ADDITIONALLY_ALLOWED_TENANTS);
        if (!CoreUtils.isNullOrEmpty(additionalTenantsFromEnv)) {
            return resolveAdditionalTenants(Arrays.asList(configuration.get(AZURE_ADDITIONALLY_ALLOWED_TENANTS).split(";")));
        } else {
            return Collections.emptyList();
        }
    }
}
