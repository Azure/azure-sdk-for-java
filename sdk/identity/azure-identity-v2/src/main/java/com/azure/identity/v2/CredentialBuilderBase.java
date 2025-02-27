// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2;

import com.azure.identity.v2.implementation.models.ClientOptionsBase;
import com.azure.identity.v2.implementation.models.HttpPipelineOptions;
import com.azure.identity.v2.implementation.models.MsalCommonOptions;
import com.azure.identity.v2.implementation.util.ValidationUtil;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.configuration.Configuration;


/**
 * The base class for all the credential builders.
 * @param <T> the type of the credential builder
 */
public abstract class CredentialBuilderBase<T extends CredentialBuilderBase<T>> {
    private static final ClientLogger LOGGER = new ClientLogger(CredentialBuilderBase.class);

    CredentialBuilderBase() { }


    /**
     * Sets the configuration store that is used during construction of the credential.
     *
     * The default configuration store is a clone of the {@link Configuration#getGlobalConfiguration() global
     * configuration store}.
     *
     * @param configuration The configuration store used to load Env variables and/or properties from.
     *
     * @return An updated instance of this builder with the configuration store set as specified.
     */
    @SuppressWarnings("unchecked")
    public T configuration(Configuration configuration) {
        getClientOptions().setConfigurationStore(configuration);
        return (T) this;
    }

    /**
     * Sets the client ID of the application.
     *
     * @param clientId the client ID of the application.
     * @return An updated instance of this builder with the client id set as specified.
     */
    @SuppressWarnings("unchecked")
    public T clientId(String clientId) {
        getMsalCommonOptions().setClientId(clientId);
        return (T) this;
    }

    /**
     * Sets the tenant ID of the application.
     *
     * @param tenantId the tenant ID of the application.
     * @return An updated instance of this builder with the tenant id set as specified.
     */
    @SuppressWarnings("unchecked")
    public T tenantId(String tenantId) {
        ValidationUtil.validateTenantIdCharacterRange(tenantId, LOGGER);
        getMsalCommonOptions().setTenantId(tenantId);
        return (T) this;
    }

    abstract ClientOptionsBase getClientOptions();

    MsalCommonOptions getMsalCommonOptions() {
        return getClientOptions().getMsalCommonOptions();
    }

    HttpPipelineOptions getHttpPipelineOptions() {
        return getClientOptions().getHttpPipelineOptions();
    }
}
