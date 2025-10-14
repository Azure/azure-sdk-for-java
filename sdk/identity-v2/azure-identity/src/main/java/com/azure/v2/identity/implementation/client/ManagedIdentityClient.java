// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.client;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.implementation.models.MsalToken;
import com.azure.v2.identity.implementation.models.ManagedIdentityClientOptions;
import com.azure.v2.identity.implementation.util.ScopeUtil;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityId;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.SharedExecutorService;

/**
 * The Managed Identity Client offers authentication support for Managed Identity authentication flow.
 */
public class ManagedIdentityClient extends ClientBase {
    private static final ClientLogger LOGGER = new ClientLogger(ManagedIdentityClient.class);
    final ManagedIdentityClientOptions managedIdentityClientOptions;
    final String resourceId;
    final String objectId;

    private final SynchronousAccessor<ManagedIdentityApplication> miClientApplicationAccessor;

    /**
     * Creates an IdentityClient with the given options.
     *
     * @param options the options configuring the client.
     */
    public ManagedIdentityClient(ManagedIdentityClientOptions options) {
        super(options);
        this.managedIdentityClientOptions = options == null ? new ManagedIdentityClientOptions() : options;
        this.resourceId = managedIdentityClientOptions.getResourceId();
        this.objectId = managedIdentityClientOptions.getObjectId();

        this.miClientApplicationAccessor = new SynchronousAccessor<>(() -> this.getManagedIdentityClient());
    }

    /**
     * Authenticates the MI authentication request.
     *
     * @param request the token request context.
     * @return the access token.
     * @throws CredentialAuthenticationException if the authentication fails
     */
    public AccessToken authenticate(TokenRequestContext request) {
        String resource = ScopeUtil.scopesToResource(request.getScopes());

        ManagedIdentityApplication managedIdentityApplication = miClientApplicationAccessor.getValue();

        com.microsoft.aad.msal4j.ManagedIdentityParameters.ManagedIdentityParametersBuilder builder
            = com.microsoft.aad.msal4j.ManagedIdentityParameters.builder(resource);
        try {
            return new MsalToken(managedIdentityApplication.acquireTokenForManagedIdentity(builder.build()).get());
        } catch (Exception e) {
            throw LOGGER.throwableAtError()
                .log("Managed Identity authentication is not available.", e, CredentialAuthenticationException::new);
        }
    }

    ManagedIdentityApplication getManagedIdentityClient() {

        ManagedIdentityId managedIdentityId;

        if (!CoreUtils.isNullOrEmpty(clientId)) {
            managedIdentityId = ManagedIdentityId.userAssignedClientId(clientId);
        } else if (!CoreUtils.isNullOrEmpty(managedIdentityClientOptions.getResourceId())) {
            managedIdentityId = ManagedIdentityId.userAssignedResourceId(resourceId);
        } else if (!CoreUtils.isNullOrEmpty(objectId)) {
            managedIdentityId = ManagedIdentityId.userAssignedObjectId(objectId);
        } else {
            managedIdentityId = ManagedIdentityId.systemAssigned();
        }

        ManagedIdentityApplication.Builder miBuilder = ManagedIdentityApplication.builder(managedIdentityId)
            .logPii(managedIdentityClientOptions.isUnsafeSupportLoggingEnabled());

        ManagedIdentitySourceType managedIdentitySourceType = ManagedIdentityApplication.getManagedIdentitySource();

        if (ManagedIdentitySourceType.DEFAULT_TO_IMDS.equals(managedIdentitySourceType)) {
            managedIdentityClientOptions.setUseImdsRetryStrategy(true);
        }
        initializeHttpPipelineAdapter();
        miBuilder.httpClient(httpPipelineAdapter);

        if (clientOptions.getExecutorService() != null) {
            miBuilder.executorService(clientOptions.getExecutorService());
        } else {
            miBuilder.executorService(SharedExecutorService.getInstance());
        }

        return miBuilder.build();
    }
}
