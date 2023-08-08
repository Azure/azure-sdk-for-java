// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.ValidationUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * Fluent credential builder for instantiating a {@link EnvironmentCredential}.
 *
 * <p>The {@link EnvironmentCredential} is appropriate for scenarios where the application is looking to read credential
 * information from environment variables. The credential supports service principal and user credential based
 * authentication and requires a set of environment variables to be configured for each scenario.</p>
 *
 * <p><strong>Sample: Construct EnvironmentCredential</strong></p>
 *
 * <p>The following code sample demonstrates the creation of a {@link com.azure.identity.EnvironmentCredential},
 * using the {@link com.azure.identity.EnvironmentCredentialBuilder} to configure it. Once this credential is
 * created, it may be passed into the builder of many of the Azure SDK for Java client builders as the 'credential'
 * parameter.</p>
 *
 * <!-- src_embed com.azure.identity.credential.environmentcredential.construct -->
 * <pre>
 * TokenCredential environmentCredential = new EnvironmentCredentialBuilder&#40;&#41;
 *     .build&#40;&#41;;
 * </pre>
 * <!-- end com.azure.identity.credential.environmentcredential.construct -->
 *
 * @see EnvironmentCredential
 */
public class EnvironmentCredentialBuilder extends CredentialBuilderBase<EnvironmentCredentialBuilder> {
    private static final ClientLogger LOGGER = new ClientLogger(EnvironmentCredentialBuilder.class);

    private String authorityHost;

    /**
     * Specifies the Azure Active Directory endpoint to acquire tokens.
     * @param authorityHost the Azure Active Directory endpoint
     * @return An updated instance of this builder with the authority host set as specified.
     */
    public EnvironmentCredentialBuilder authorityHost(String authorityHost) {
        ValidationUtil.validateAuthHost(authorityHost, LOGGER);
        this.authorityHost = authorityHost;
        return this;
    }

    /**
     * Specifies the ExecutorService to be used to execute the authentication requests.
     * Developer is responsible for maintaining the lifecycle of the ExecutorService.
     *
     * <p>
     * If this is not configured, the {@link ForkJoinPool#commonPool()} will be used which is
     * also shared with other application tasks. If the common pool is heavily used for other tasks, authentication
     * requests might starve and setting up this executor service should be considered.
     * </p>
     *
     * <p> The executor service and can be safely shutdown if the TokenCredential is no longer being used by the
     * Azure SDK clients and should be shutdown before the application exits. </p>
     *
     * @param executorService the executor service to use for executing authentication requests.
     * @return An updated instance of this builder with the executor service set as specified.
     */
    public EnvironmentCredentialBuilder executorService(ExecutorService executorService) {
        this.identityClientOptions.setExecutorService(executorService);
        return this;
    }

    /**
     * Creates a new {@link EnvironmentCredential} with the current configurations.
     *
     * @return a {@link EnvironmentCredential} with the current configurations.
     */
    public EnvironmentCredential build() {
        if (!CoreUtils.isNullOrEmpty(authorityHost)) {
            identityClientOptions.setAuthorityHost(authorityHost);
        }
        return new EnvironmentCredential(identityClientOptions);
    }
}
