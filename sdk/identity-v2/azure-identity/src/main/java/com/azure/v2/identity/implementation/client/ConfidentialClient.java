// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.client;

import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import com.azure.v2.identity.models.TokenCachePersistenceOptions;
import com.azure.v2.identity.implementation.models.ConfidentialClientOptions;
import com.azure.v2.identity.implementation.models.MsalToken;
import com.azure.v2.identity.implementation.util.CertificateUtil;
import com.azure.v2.identity.implementation.util.IdentityUtil;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ClaimsRequest;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.SilentParameters;
import com.microsoft.aad.msal4j.OnBehalfOfParameters;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.SharedExecutorService;
import io.clientcore.core.instrumentation.logging.LogLevel;

import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

/**
 * The Confidential client holds authetnciation logic for MSAL Confidential client based auth flows.
 */
public class ConfidentialClient extends ClientBase {

    static final ClientLogger LOGGER = new ClientLogger(ConfidentialClient.class);
    static final Pattern TRAILING_FORWARD_SLASHES = Pattern.compile("/+$");
    static final Duration REFRESH_OFFSET = Duration.ofMinutes(5);
    final ConfidentialClientOptions confidentialClientOptions;

    private final SynchronousAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessor;
    private final SynchronousAccessor<ConfidentialClientApplication> confidentialClientApplicationAccessorWithCae;

    /**
     * Creates an IdentityClient with the given options.
     *
     * @param options the options configuring the client.
     */
    public ConfidentialClient(ConfidentialClientOptions options) {
        super(options);
        this.confidentialClientOptions = options == null ? new ConfidentialClientOptions() : options;

        this.confidentialClientApplicationAccessor = new SynchronousAccessor<>(() -> this.getClient(false));

        this.confidentialClientApplicationAccessorWithCae = new SynchronousAccessor<>(() -> this.getClient(true));
    }

    /**
     * Asynchronously acquire a token from Active Directory with a client secret.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticate(TokenRequestContext request) {
        ConfidentialClientApplication confidentialClient = getConfidentialClientInstance(request).getValue();
        ClientCredentialParameters.ClientCredentialParametersBuilder builder
            = ClientCredentialParameters.builder(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, clientOptions));

        if (confidentialClientOptions.getClientAssertionSupplier() != null) {
            builder.clientCredential(ClientCredentialFactory
                .createFromClientAssertion(confidentialClientOptions.getClientAssertionSupplier().get()));
        } else if (confidentialClientOptions.getClientAssertionFunction() != null) {
            builder.clientCredential(ClientCredentialFactory.createFromClientAssertion(
                confidentialClientOptions.getClientAssertionFunction().apply(getPipeline())));
        }
        try {
            return new MsalToken(confidentialClient.acquireToken(builder.build()).get());
        } catch (InterruptedException | ExecutionException e) {
            throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(e.getMessage(), e));
        }
    }

    /**
     * Acquire a token from the confidential client.
     *
     * @param request the details of the token request
     * @return An access token, or null if no token exists in the cache.
     */
    @SuppressWarnings("deprecation")
    public AccessToken authenticateWithCache(TokenRequestContext request) {
        return authenticateWithCache(request, null);
    }

    /**
     * Acquire a token from the confidential client.
     *
     * @param request the details of the token request
     * @param account the details of the token request
     * @return An access token, or null if no token exists in the cache.
     */
    @SuppressWarnings("deprecation")
    public AccessToken authenticateWithCache(TokenRequestContext request, IAccount account) {
        ConfidentialClientApplication confidentialClientApplication = getConfidentialClientInstance(request).getValue();
        SilentParameters.SilentParametersBuilder parametersBuilder
            = SilentParameters.builder(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, clientOptions));

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(claimsRequest);
            parametersBuilder.forceRefresh(true);
        }

        if (account != null) {
            parametersBuilder.account(account);
        }

        try {
            IAuthenticationResult authenticationResult
                = confidentialClientApplication.acquireTokenSilently(parametersBuilder.build()).get();
            AccessToken accessToken = new MsalToken(authenticationResult);
            if (OffsetDateTime.now().isBefore(accessToken.getExpiresAt().minus(REFRESH_OFFSET))) {
                return accessToken;
            } else {
                throw LOGGER.logThrowableAsError(new IllegalStateException("Received token is close to expiry."));
            }
        } catch (MalformedURLException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        } catch (ExecutionException | InterruptedException e) {
            // Cache misses should not throw an exception, but should log.
            if (e.getMessage().contains("Token not found in the cache")) {
                LOGGER.atLevel(LogLevel.VERBOSE).log("Token not found in the MSAL cache.");
                return null;
            } else {
                throw LOGGER.logThrowableAsError(new CredentialAuthenticationException(e.getMessage()));
            }
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with Azure PowerShell.
     *
     * @param request the details of the token request
     * @return a Publisher that emits an AccessToken
     */
    public AccessToken authenticateWithOBO(TokenRequestContext request) {
        ConfidentialClientApplication cc = getConfidentialClientInstance(request).getValue();
        try {
            return new MsalToken(cc.acquireToken(buildOBOFlowParameters(request)).get());
        } catch (Exception e) {
            throw LOGGER.logThrowableAsError(
                new CredentialAuthenticationException("Failed to acquire token with On Behalf Of Authentication.", e));
        }
    }

    ConfidentialClientApplication getClient(boolean enableCae) {

        if (clientId == null) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException(
                "A non-null value for client ID must be provided for user authentication."));
        }
        String authorityUrl
            = TRAILING_FORWARD_SLASHES.matcher(clientOptions.getAuthorityHost()).replaceAll("") + "/" + tenantId;
        IClientCredential credential;

        if (confidentialClientOptions.getClientSecret() != null) {
            credential = ClientCredentialFactory.createFromSecret(confidentialClientOptions.getClientSecret());
        } else if (confidentialClientOptions.getCertificateBytes() != null
            || confidentialClientOptions.getCertificatePath() != null) {
            try {
                byte[] certificateBytes = getCertificateBytes();
                if (CertificateUtil.isPem(certificateBytes)) {

                    List<X509Certificate> x509CertificateList = CertificateUtil.publicKeyFromPem(certificateBytes);
                    PrivateKey privateKey = CertificateUtil.privateKeyFromPem(certificateBytes);
                    if (x509CertificateList.size() == 1) {
                        credential
                            = ClientCredentialFactory.createFromCertificate(privateKey, x509CertificateList.get(0));
                    } else {
                        credential
                            = ClientCredentialFactory.createFromCertificateChain(privateKey, x509CertificateList);
                    }
                } else {
                    try (InputStream pfxCertificateStream = getCertificateInputStream()) {
                        credential = ClientCredentialFactory.createFromCertificate(pfxCertificateStream,
                            confidentialClientOptions.getCertificatePassword());
                    }
                }
            } catch (IOException | GeneralSecurityException e) {
                throw LOGGER.logThrowableAsError(new IllegalStateException(
                    "Failed to parse the certificate for the credential: " + e.getMessage(), e));
            }
        } else if (confidentialClientOptions.getClientAssertionSupplier() != null) {
            credential = ClientCredentialFactory
                .createFromClientAssertion(confidentialClientOptions.getClientAssertionSupplier().get());
        } else if (confidentialClientOptions.getClientAssertionFunction() != null) {
            credential = ClientCredentialFactory
                .createFromClientAssertion(confidentialClientOptions.getClientAssertionFunction().apply(getPipeline()));
        } else {
            throw LOGGER.logThrowableAsError(
                new IllegalArgumentException("Must provide client secret or client certificate path."
                    + " To mitigate this issue, please refer to the troubleshooting guidelines here at "
                    + "https://aka.ms/azsdk/java/identity/serviceprincipalauthentication/troubleshoot"));
        }

        ConfidentialClientApplication.Builder applicationBuilder
            = ConfidentialClientApplication.builder(clientId, credential);
        try {
            applicationBuilder = applicationBuilder.authority(authorityUrl)
                .instanceDiscovery(clientOptions.isInstanceDiscoveryEnabled())
                .logPii(clientOptions.isUnsafeSupportLoggingEnabled());

            if (!clientOptions.isInstanceDiscoveryEnabled()) {
                LOGGER.atLevel(LogLevel.VERBOSE)
                    .log("Instance discovery and authority validation is disabled. In this"
                        + " state, the library will not fetch metadata to validate the specified authority host. As a"
                        + " result, it is crucial to ensure that the configured authority host is valid and trustworthy.");
            }
        } catch (MalformedURLException e) {
            throw LOGGER.logThrowableAsError(new IllegalStateException(e));
        }

        if (enableCae) {
            Set<String> set = new HashSet<>(1);
            set.add("CP1");
            applicationBuilder.clientCapabilities(set);
        }

        applicationBuilder.sendX5c(confidentialClientOptions.isIncludeX5c());
        initializeHttpPipelineAdapter();

        if (httpPipelineAdapter != null) {
            applicationBuilder.httpClient(httpPipelineAdapter);
        }

        if (clientOptions.getExecutorService() != null) {
            applicationBuilder.executorService(clientOptions.getExecutorService());
        } else {
            applicationBuilder.executorService(SharedExecutorService.getInstance());
        }

        TokenCachePersistenceOptions tokenCachePersistenceOptions = clientOptions.getTokenCacheOptions();
        PersistentTokenCacheImpl tokenCache = null;
        if (tokenCachePersistenceOptions != null) {
            try {
                tokenCache = new PersistentTokenCacheImpl(enableCae)
                    .setAllowUnencryptedStorage(tokenCachePersistenceOptions.isUnencryptedStorageAllowed())
                    .setName(tokenCachePersistenceOptions.getName());
                applicationBuilder.setTokenCacheAccessAspect(tokenCache);
            } catch (Throwable t) {
                throw LOGGER.logThrowableAsError(
                    new CredentialAuthenticationException("Shared token cache is unavailable in this environment.", t));
            }
        }

        ConfidentialClientApplication confidentialClientApplication = applicationBuilder.build();

        if (tokenCache != null) {
            tokenCache.registerCache();
        }
        return confidentialClientApplication;
    }

    OnBehalfOfParameters buildOBOFlowParameters(TokenRequestContext request) {
        OnBehalfOfParameters.OnBehalfOfParametersBuilder builder = OnBehalfOfParameters
            .builder(new HashSet<>(request.getScopes()), confidentialClientOptions.getUserAssertion())
            .tenant(IdentityUtil.resolveTenantId(tenantId, request, confidentialClientOptions));

        if (request.isCaeEnabled() && request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            builder.claims(claimsRequest);
        }
        return builder.build();
    }

    private byte[] getCertificateBytes() throws IOException {
        String certPath = confidentialClientOptions.getCertificatePath();
        byte[] certificate = confidentialClientOptions.getCertificateBytes();
        if (certPath != null) {
            return Files.readAllBytes(Paths.get(certPath));
        } else if (certificate != null) {
            return certificate;
        } else {
            return new byte[0];
        }
    }

    /**
     * Asynchronously acquire a token from Active Directory with an authorization code from an oauth flow.
     *
     * @param request the details of the token request
     * @param authorizationCode the oauth2 authorization code
     * @param redirectUrl the redirectUrl where the authorization code is sent to
     * @return a Publisher that emits an AccessToken
     * @throws CredentialAuthenticationException if the authentication fails.
     */
    public MsalToken authenticateWithAuthorizationCode(TokenRequestContext request, String authorizationCode,
        URI redirectUrl) {
        AuthorizationCodeParameters.AuthorizationCodeParametersBuilder parametersBuilder
            = AuthorizationCodeParameters.builder(authorizationCode, redirectUrl)
                .scopes(new HashSet<>(request.getScopes()))
                .tenant(IdentityUtil.resolveTenantId(tenantId, request, confidentialClientOptions));

        if (request.getClaims() != null) {
            ClaimsRequest claimsRequest = ClaimsRequest.formatAsClaimsRequest(request.getClaims());
            parametersBuilder.claims(claimsRequest);
        }

        try {
            return new MsalToken(
                getConfidentialClientInstance(request).getValue().acquireToken(parametersBuilder.build()).get());
        } catch (InterruptedException | ExecutionException e) {
            throw LOGGER.logThrowableAsError(
                new CredentialAuthenticationException("Failed to acquire token with authorization code", e));
        }
    }

    private SynchronousAccessor<ConfidentialClientApplication>
        getConfidentialClientInstance(TokenRequestContext request) {
        return request.isCaeEnabled()
            ? confidentialClientApplicationAccessorWithCae
            : confidentialClientApplicationAccessor;
    }

    private InputStream getCertificateInputStream() throws IOException {
        if (confidentialClientOptions.getCertificatePath() != null) {
            return new BufferedInputStream(new FileInputStream(confidentialClientOptions.getCertificatePath()));
        } else {
            return new ByteArrayInputStream(confidentialClientOptions.getCertificateBytes());
        }
    }
}
