package com.microsoft.azure.sevicebus.security;

import java.net.MalformedURLException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.ClientCredential;

public abstract class TokenProvider
{
    public abstract CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience);
    
    public static TokenProvider createSharedAccessSignatureTokenProvider(String sasKeyName, String sasKey)
    {
        return new SharedAccessSignatureTokenProvider(sasKeyName, sasKey, SecurityConstants.DEFAULT_SAS_TOKEN_VALIDITY_IN_SECONDS);
    }
    
    public static TokenProvider createSharedAccessSignatureTokenProvider(String sasToken, Instant sasTokenValidUntil)
    {
        return new SharedAccessSignatureTokenProvider(sasToken, sasTokenValidUntil);
    }
    
    public static TokenProvider createAzureActiveDirectoryTokenProvider(String authorityUrl, String clientId, String userName, String password) throws MalformedURLException
    {
        AuthenticationContext authContext = createAuthenticationContext(authorityUrl);
        return new AzureActiveDirectoryTokenProvider(authContext, clientId, userName, password);
    }
    
    public static TokenProvider createAzureActiveDirectoryTokenProvider(String authorityUrl, String clientId, String clientSecret) throws MalformedURLException
    {
        AuthenticationContext authContext = createAuthenticationContext(authorityUrl);
        return new AzureActiveDirectoryTokenProvider(authContext, new ClientCredential(clientId, clientSecret));
    }
    
//    private static TokenProvider createAzureActiveDirectoryTokenProvider(String authorityUrl, String clientId, PrivateKey privateKey, X509Certificate publicCertificate) throws MalformedURLException
//    {
//        AuthenticationContext authContext = createAuthenticationContext(authorityUrl);
//        return new AzureActiveDirectoryTokenProvider(authContext, AsymmetricKeyCredential.create(clientId, privateKey, publicCertificate));
//    }
    
    private static TokenProvider createManagedServiceIdentityTokenProvider()
    {
        return new ManagedServiceIdentityTokenProvider();
    }
    
    private static AuthenticationContext createAuthenticationContext(String authorityUrl) throws MalformedURLException
    {
        return new AuthenticationContext(authorityUrl, true, ForkJoinPool.commonPool());
    }
}
