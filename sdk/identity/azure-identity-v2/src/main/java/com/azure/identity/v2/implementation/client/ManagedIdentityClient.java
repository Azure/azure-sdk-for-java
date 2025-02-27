// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.client;

import com.azure.identity.v2.CredentialAuthenticationException;
import com.azure.identity.v2.implementation.models.MsalToken;
import com.azure.identity.v2.implementation.models.ManagedIdentityClientOptions;
import com.azure.identity.v2.implementation.util.ScopeUtil;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.core.utils.CoreUtils;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityId;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.SharedExecutorService;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

public class ManagedIdentityClient extends ClientBase {

    static final ClientLogger LOGGER = new ClientLogger(ManagedIdentityClient.class);
    private static final String SDK_NAME = "name";
    private static final String SDK_VERSION = "version";
    static final Pattern TRAILING_FORWARD_SLASHES = Pattern.compile("/+$");
    static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);

    private final Map<String, String> properties = new HashMap<>();
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


    public AccessToken authenticate(TokenRequestContext request) {
        String resource = ScopeUtil.scopesToResource(request.getScopes());

        ManagedIdentityApplication managedIdentityApplication = miClientApplicationAccessor.getValue();

        com.microsoft.aad.msal4j.ManagedIdentityParameters.ManagedIdentityParametersBuilder builder
            = com.microsoft.aad.msal4j.ManagedIdentityParameters.builder(resource);
        try {
            return new MsalToken(managedIdentityApplication.acquireTokenForManagedIdentity(builder.build()).get());
        } catch (Exception e) {
            throw new CredentialAuthenticationException("Managed Identity authentication is not available.", e);
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

        ManagedIdentityApplication.Builder miBuilder
            = ManagedIdentityApplication.builder(managedIdentityId);


        initializeHttpPipelineAdapter();
        miBuilder.httpClient(httpPipelineAdapter);

        if (getMsalOptions().getExecutorService() != null) {
            miBuilder.executorService(getMsalOptions().getExecutorService());
        } else {
            miBuilder.executorService(SharedExecutorService.getInstance());
        }

        return miBuilder.build();
    }
}
