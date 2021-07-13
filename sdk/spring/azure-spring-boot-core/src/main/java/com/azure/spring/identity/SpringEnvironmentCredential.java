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
import reactor.core.publisher.Mono;

/**
 * An equivalent of {@link com.azure.identity.EnvironmentCredential} which can accept a
 * {@link CredentialPropertiesProvider} as a constructor parameter.
 */
public class SpringEnvironmentCredential implements TokenCredential {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringEnvironmentCredential.class);
    private final CredentialPropertiesProvider credentialPropertiesProvider;
    private final TokenCredential tokenCredential;

    SpringEnvironmentCredential(CredentialPropertiesProvider credentialPropertiesProvider,
                                IdentityClientOptions identityClientOptions) {
        this.credentialPropertiesProvider = credentialPropertiesProvider;
        this.tokenCredential = populateTokenCredential(identityClientOptions);
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        if (tokenCredential == null) {
            return Mono.error(() -> {
                final CredentialUnavailableException exception = new CredentialUnavailableException(
                    "SpringEnvironmentCredential authentication unavailable. Environment variables " + "are not fully"
                        + " configured.");
                LOGGER.error(exception.getMessage(), exception);
                return exception;
            });
        } else {
            return tokenCredential.getToken(request);
        }
    }

    private TokenCredential populateTokenCredential(IdentityClientOptions identityClientOptions) {
        final String tenantId = credentialPropertiesProvider.getTenantId();
        final String clientId = credentialPropertiesProvider.getClientId();
        final String clientSecret = credentialPropertiesProvider.getClientSecret();
        final String authorityHost = credentialPropertiesProvider.getAuthorityHost();

        if (tenantId != null && clientId != null && clientSecret != null) {
            final ClientSecretCredentialBuilder builder = new ClientSecretCredentialBuilder()
                                                              .tenantId(tenantId)
                                                              .clientId(clientId)
                                                              .clientSecret(clientSecret)
                                                              .authorityHost(authorityHost);
            configureIdentityOptions(builder, identityClientOptions);
            return builder.build();
        }

        final String certPath = credentialPropertiesProvider.getClientCertificatePath();

        if (tenantId != null && clientId != null && certPath != null) {
            final ClientCertificateCredentialBuilder builder = new ClientCertificateCredentialBuilder()
                                                                   .tenantId(tenantId)
                                                                   .clientId(clientId)
                                                                   .pemCertificate(certPath)
                                                                   .authorityHost(authorityHost);

            configureIdentityOptions(builder, identityClientOptions);
            return builder.build();
        }

        final String username = credentialPropertiesProvider.getUsername();
        final String password = credentialPropertiesProvider.getPassword();

        if (clientId != null && username != null && password != null) {
            final UsernamePasswordCredentialBuilder builder = new UsernamePasswordCredentialBuilder()
                                                                  .tenantId(tenantId)
                                                                  .clientId(clientId)
                                                                  .username(username)
                                                                  .password(password)
                                                                  .authorityHost(authorityHost);
            configureIdentityOptions(builder, identityClientOptions);
            return builder.build();

        }

        return null;
    }

    // TODO (xiada) this IdentityClientOptions is not exposed
    private void configureIdentityOptions(AadCredentialBuilderBase<?> aadCredentialBuilderBase,
                                          IdentityClientOptions identityClientOptions) {
/*        aadCredentialBuilderBase.authorityHost(identityClientOptions.getAuthorityHost())
                                .executorService(identityClientOptions.getExecutorService())
                                .httpClient(identityClientOptions.getHttpClient())
                                .httpPipeline(identityClientOptions.getHttpPipeline())
                                .maxRetry(identityClientOptions.getMaxRetry())
                                .retryTimeout(identityClientOptions.getRetryTimeout());*/
    }

}
