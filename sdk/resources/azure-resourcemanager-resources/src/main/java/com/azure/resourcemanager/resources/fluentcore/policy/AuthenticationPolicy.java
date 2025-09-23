// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.http.policy.ArmChallengeAuthenticationPolicy;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

/**
 * Rewrite the BearerTokenAuthenticationPolicy, it will use default scope when scopes parameter is empty.
 */
public class AuthenticationPolicy extends ArmChallengeAuthenticationPolicy {

    private final AzureEnvironment environment;

    /**
     * Creates AuthenticationPolicy.
     *
     * @param credential the token credential to authenticate the request
     * @param environment the environment with endpoints for authentication
     * @param scopes the scopes used in credential, using default scopes when empty
     */
    public AuthenticationPolicy(TokenCredential credential, AzureEnvironment environment, String... scopes) {
        super(credential, scopes);
        this.environment = environment;
    }

    @Override
    public String[] getScopes(HttpPipelineCallContext context, String[] scopes) {
        if (CoreUtils.isNullOrEmpty(scopes)) {
            scopes = new String[1];
            scopes[0] = ResourceManagerUtils.getDefaultScopeFromRequest(context.getHttpRequest(), environment);
        }
        return scopes;
    }
}
