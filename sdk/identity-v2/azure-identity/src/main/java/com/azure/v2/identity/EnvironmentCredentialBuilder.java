// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.models.ClientOptions;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.util.ValidationUtil;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.concurrent.ExecutorService;

/**
 * Fluent credential builder for instantiating a {@link EnvironmentCredential}.
 *
 * <p>The {@link EnvironmentCredential} is appropriate for scenarios where the application is looking to read credential
 * information from environment variables. The credential supports service principal and user credential based
 * authentication and requires a set of environment variables to be configured for each scenario.</p>
 *
 * <p><strong>Sample: Construct EnvironmentCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link EnvironmentCredential},
 * using the {@link EnvironmentCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <pre>
 * TokenCredential environmentCredential = new EnvironmentCredentialBuilder&#40;&#41;.build&#40;&#41;;
 * </pre>
 *
 * @see EnvironmentCredential
 */
public class EnvironmentCredentialBuilder extends EntraIdCredentialBuilderBase<EnvironmentCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(EnvironmentCredentialBuilder.class);
    private final ConfidentialClientOptions confidentialClientOptions;

    /**
     * Constructs an instance of EnvironmentCredentialBuilder.
     */
    public EnvironmentCredentialBuilder() {
        super();
        confidentialClientOptions = new ConfidentialClientOptions();
    }

    /**
     * Specifies the Microsoft Entra endpoint to acquire tokens.
     * @param authorityHost the Microsoft Entra endpoint
     * @return An updated instance of this builder with the authority host set as specified.
     */
    public EnvironmentCredentialBuilder authorityHost(String authorityHost) {
        ValidationUtil.validateAuthHost(authorityHost, LOGGER);
        this.confidentialClientOptions.setAuthorityHost(authorityHost);
        return this;
    }

    /**
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p>
     * If this is not configured, the {@link io.clientcore.core.utils.SharedExecutorService} will be used which is
     * also shared with other SDK libraries. If there are many concurrent SDK tasks occurring, authentication
     * requests might starve and configuring a separate executor service should be considered.
     * </p>
     *
     * <p> The executor service and can be safely shutdown if the TokenCredential is no longer being used by the
     * Azure SDK clients and should be shutdown before the application exits. </p>
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return An updated instance of this builder with the executor service set as specified.
     */
    public EnvironmentCredentialBuilder executorService(ExecutorService executorService) {
        this.confidentialClientOptions.setExecutorService(executorService);
        return this;
    }

    /**
     * Creates a new {@link EnvironmentCredential} with the current configurations.
     *
     * @return a {@link EnvironmentCredential} with the current configurations.
     */
    public EnvironmentCredential build() {
        return new EnvironmentCredential(confidentialClientOptions);
    }

    @Override
    ClientOptions getClientOptions() {
        return confidentialClientOptions;
    }
}
