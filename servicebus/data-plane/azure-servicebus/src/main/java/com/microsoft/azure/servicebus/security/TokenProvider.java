package com.microsoft.azure.servicebus.security;

import java.net.MalformedURLException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.ClientCredential;

/**
 * This abstract class defines the contract of a token provider. All token providers should inherit from this class.
 * An instance of token provider is used to obtain a security token for a given audience.
 * @since 1.2.0
 *
 */
public abstract class TokenProvider
{
    /**
     * Asynchronously gets a security token for the given audience. Implementations of this method may choose to create a new token for every call
     * or return a cached token. But the token returned must be valid.
     * @param audience path of the entity for which this security token is to be presented
     * @return an instance of CompletableFuture which returns a {@link SecurityToken} on completion.
     */
    public abstract CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience);
    
    /**
     * Creates a Shared Access Signature token provider with the given key name and key value. Returned token provider creates tokens
     * with validity of 20 minutes. This is a utility method.
     * @param sasKeyName SAS key name
     * @param sasKey SAS key value
     * @return an instance of Shared Access Signature token provider with the given key name, key value.
     */
    public static TokenProvider createSharedAccessSignatureTokenProvider(String sasKeyName, String sasKey)
    {
        return new SharedAccessSignatureTokenProvider(sasKeyName, sasKey, SecurityConstants.DEFAULT_SAS_TOKEN_VALIDITY_IN_SECONDS);
    }
    
    /**
     * Creates a Shared Access Signature token provider that always returns an already created token. This is a utility method.
     * @param sasToken Already created Shared Access Signature token to be returned by {@link #getSecurityTokenAsync(String)} method.
     * @param sasTokenValidUntil Instant when the token expires
     * @return an instance of Shared Access Signature token provider that always returns an already created token.
     */
    public static TokenProvider createSharedAccessSignatureTokenProvider(String sasToken, Instant sasTokenValidUntil)
    {
        return new SharedAccessSignatureTokenProvider(sasToken, sasTokenValidUntil);
    }
    
    /**
     * Creates an Azure Active Directory token provider that acquires a token from the given active directory instance using the given clientId, username and password.
     * This is a utility method.
     * @param authorityUrl URL of the Azure Active Directory instance
     * @param clientId client id of the application
     * @param userName user name
     * @param password password
     * @return an instance of Azure Active Directory token provider
     * @throws MalformedURLException if the authority URL is not well formed
     */
    public static TokenProvider createAzureActiveDirectoryTokenProvider(String authorityUrl, String clientId, String userName, String password) throws MalformedURLException
    {
        AuthenticationContext authContext = createAuthenticationContext(authorityUrl);
        return new AzureActiveDirectoryTokenProvider(authContext, clientId, userName, password);
    }
    
    /**
     * Creates an Azure Active Directory token provider that acquires a token from the given active directory instance using the given clientId and client secret.
     * This is a utility method.
     * @param authorityUrl URL of the Azure Active Directory instance
     * @param clientId client id of the application
     * @param clientSecret client secret of the application
     * @return an instance of Azure Active Directory token provider
     * @throws MalformedURLException if the authority URL is not well formed
     */
    public static TokenProvider createAzureActiveDirectoryTokenProvider(String authorityUrl, String clientId, String clientSecret) throws MalformedURLException
    {
        AuthenticationContext authContext = createAuthenticationContext(authorityUrl);
        return new AzureActiveDirectoryTokenProvider(authContext, new ClientCredential(clientId, clientSecret));
    }
    
    /**
     * Creates a Managed Service Identity token provider. This is a utility method.
     * @return an instance of Managed Service Identity token provider
     */
    public static TokenProvider createManagedServiceIdentityTokenProvider()
    {
        return new ManagedServiceIdentityTokenProvider();
    }
    
    private static AuthenticationContext createAuthenticationContext(String authorityUrl) throws MalformedURLException
    {
        return new AuthenticationContext(authorityUrl, true, ForkJoinPool.commonPool());
    }
}
