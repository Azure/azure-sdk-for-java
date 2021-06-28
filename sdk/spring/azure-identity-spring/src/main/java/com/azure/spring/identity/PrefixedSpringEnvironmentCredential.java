// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.AadCredentialBuilderBase;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.identity.implementation.IdentityClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.CLIENT_CERTIFICATE_PATH;
import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.CLIENT_ID;
import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.CLIENT_SECRET;
import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.PASSWORD;
import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.TENANT_ID;
import static com.azure.spring.identity.AzureEnvironment.AzureSpringPropertyConstants.USERNAME;

/**
 * Spring token credential built from the prefixed properties.
 */
public class PrefixedSpringEnvironmentCredential implements TokenCredential {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrefixedSpringEnvironmentCredential.class);

    private final AzureEnvironment azureEnvironment;
    private final String prefix;
    private final TokenCredential tokenCredential;

    PrefixedSpringEnvironmentCredential(Environment environment, String prefix, IdentityClientOptions identityClientOptions) {
        this.azureEnvironment = new AzureEnvironment(environment);
        this.prefix = prefix;
        this.tokenCredential = populateTokenCredential(prefix, identityClientOptions);
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (tokenCredential == null) {
            return Mono.error(() -> {
                final CredentialUnavailableException exception = new CredentialUnavailableException(
                    "PrefixedSpringEnvironmentCredential(" + prefix + ") authentication unavailable. Environment " +
                        "variables are not fully configured.");
                LOGGER.error(exception.getMessage(), exception);
                return exception;
            });
        } else {
            return tokenCredential.getToken(request);
        }
    }

    private TokenCredential populateTokenCredential(String prefix, IdentityClientOptions identityClientOptions) {
        String tenantId = azureEnvironment.getPropertyValue(prefix + TENANT_ID);
        String clientId = azureEnvironment.getPropertyValue(prefix + CLIENT_ID);
        String clientSecret = azureEnvironment.getPropertyValue(prefix + CLIENT_SECRET);

        if (tenantId != null && clientId != null && clientSecret != null) {
            final ClientSecretCredentialBuilder builder = new ClientSecretCredentialBuilder()
                                                              .tenantId(tenantId)
                                                              .clientId(clientId)
                                                              .clientSecret(clientSecret);
            configureIdentityOptions(builder, identityClientOptions);
            return builder.build();
        }

        String certPath = azureEnvironment.getPropertyValue(prefix + CLIENT_CERTIFICATE_PATH);

        if (tenantId != null && clientId != null && certPath != null) {
            final ClientCertificateCredentialBuilder builder = new ClientCertificateCredentialBuilder()
                                                                   .tenantId(tenantId)
                                                                   .clientId(clientId)
                                                                   .pemCertificate(certPath);

            configureIdentityOptions(builder, identityClientOptions);
            return builder.build();
        }

        String username = azureEnvironment.getPropertyValue(prefix + USERNAME);
        String password = azureEnvironment.getPropertyValue(prefix + PASSWORD);

        if (clientId != null && username != null && password != null) {// tenant-id is not required
            final UsernamePasswordCredentialBuilder builder = new UsernamePasswordCredentialBuilder()
                                                                  .tenantId(tenantId)
                                                                  .clientId(clientId)
                                                                  .username(username)
                                                                  .password(password);
            configureIdentityOptions(builder, identityClientOptions);
            return builder.build();
        }

        return null;
    }

    // TODO (xiada) better abstraction of this class and SpringEnvironmentCredential
    private void configureIdentityOptions(AadCredentialBuilderBase<?> aadCredentialBuilderBase,
                                          IdentityClientOptions identityClientOptions) {
        aadCredentialBuilderBase.authorityHost(identityClientOptions.getAuthorityHost())
                                .executorService(identityClientOptions.getExecutorService())
                                .httpClient(identityClientOptions.getHttpClient())
                                .httpPipeline(identityClientOptions.getHttpPipeline())
                                .maxRetry(identityClientOptions.getMaxRetry())
                                .retryTimeout(identityClientOptions.getRetryTimeout());
    }

}
