package com.microsoft.azure.servicebus.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

/**
 * This is a token provider that obtains tokens from Azure Active Directory. It supports multiple modes of authentication with active directory
 * to obtain tokens.
 * @since 1.2.0
 *
 */
public class AzureActiveDirectoryTokenProvider extends TokenProvider
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(AzureActiveDirectoryTokenProvider.class);
    
    /**
     * Enumeration representing different authentication modes supported by this token provider.
     */
    enum AuthenticationMode
    {
        CLIENT_CREDENTIAL,
        USER_PASSWORD_CREDENTIAL,
        CERTIFICATE
    }
    
    private AuthenticationMode authenticationMode;
    private AuthenticationContext authenticationContext;
    private ClientCredential clientCredential;
    private AsymmetricKeyCredential asymmetricKeyCredential;
    private String clientId;
    private String userName;
    private String password;
    
    /**
     * Creates a token provider that authenticates with active directory using client secret.
     * @param authenticationContext Authentication context pointing to the instance of Azure Active Directory
     * @param clientCredential Client credential containing client id and client secret of the application
     */
    public AzureActiveDirectoryTokenProvider(AuthenticationContext authenticationContext, ClientCredential clientCredential)
    {
        this.authenticationMode = AuthenticationMode.CLIENT_CREDENTIAL;
        this.authenticationContext = authenticationContext;
        this.clientCredential = clientCredential;
    }
    
    /**
     * Creates a token provider that authenticates with active directory using certificate.
     * @param authenticationContext Authentication context pointing to the instance of Azure Active Directory
     * @param asymmetricKeyCredential Key credential containing the certificate to be used for authentication
     */
    public AzureActiveDirectoryTokenProvider(AuthenticationContext authenticationContext, AsymmetricKeyCredential asymmetricKeyCredential)
    {
        this.authenticationMode = AuthenticationMode.CERTIFICATE;
        this.authenticationContext = authenticationContext;
        this.asymmetricKeyCredential = asymmetricKeyCredential;
    }
    
    /**
     * Creates a token provider that authenticates with active directory using username and password.
     * @param authenticationContext Authentication context pointing to the instance of Azure Active Directory
     * @param clientId client id of the application
     * @param userName user name
     * @param password password
     */
    public AzureActiveDirectoryTokenProvider(AuthenticationContext authenticationContext, String clientId, String userName, String password)
    {
        this.authenticationMode = AuthenticationMode.USER_PASSWORD_CREDENTIAL;
        this.authenticationContext = authenticationContext;
        this.clientId = clientId;
        this.userName = userName;
        this.password = password;
    }
    
    @Override
    public CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience) {
        String addAudienceForSB = SecurityConstants.SERVICEBUS_AAD_AUDIENCE_RESOURCE_URL;
        CompletableFuture<SecurityToken> tokenGeneratingFuture = new CompletableFuture<>();
        switch(this.authenticationMode)
        {
            case CLIENT_CREDENTIAL:
                this.authenticationContext.acquireToken(addAudienceForSB, this.clientCredential, new FutureCompletingAuthenticationCallback(tokenGeneratingFuture, audience));
                break;
            case USER_PASSWORD_CREDENTIAL:
                this.authenticationContext.acquireToken(addAudienceForSB, this.clientId, this.userName, this.password, new FutureCompletingAuthenticationCallback(tokenGeneratingFuture, audience));
                break;
            case CERTIFICATE:
                this.authenticationContext.acquireToken(addAudienceForSB, this.asymmetricKeyCredential, new FutureCompletingAuthenticationCallback(tokenGeneratingFuture, audience));
                break;
        }
        return tokenGeneratingFuture;
    }
    
    private static class FutureCompletingAuthenticationCallback implements AuthenticationCallback
    {
        private CompletableFuture<SecurityToken> tokenGeneratingFutue;
        private String audience;
        
        public FutureCompletingAuthenticationCallback(CompletableFuture<SecurityToken> tokenGeneratingFutue, String audience)
        {
            this.tokenGeneratingFutue = tokenGeneratingFutue;
            this.audience = audience;
        }
        
        @Override
        public void onFailure(Throwable authException) {
            TRACE_LOGGER.error("Getting token from Azure Active Directory failed", authException);
            this.tokenGeneratingFutue.completeExceptionally(authException);
        }

        @Override
        public void onSuccess(AuthenticationResult authResult) {
            SecurityToken generatedToken = new SecurityToken(SecurityTokenType.JWT, this.audience, authResult.getAccessToken(), Instant.now(), Instant.now().plus(Duration.ofSeconds(authResult.getExpiresAfter())));
            tokenGeneratingFutue.complete(generatedToken);
        }
        
    }
}
