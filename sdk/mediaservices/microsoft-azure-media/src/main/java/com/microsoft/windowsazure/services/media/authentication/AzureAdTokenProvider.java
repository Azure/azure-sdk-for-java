package com.microsoft.windowsazure.services.media.authentication;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.NotImplementedException;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;

/**
 * Azure Active Directory Access Token Provider for
 * the Azure Media Services JDK.
 */
public class AzureAdTokenProvider implements TokenProvider {
    private final AuthenticationContext authenticationContext;
    private final AzureAdTokenCredentials tokenCredentials;
    private final ExecutorService executorService;

    /**
     * Creates an instance of the AzureAdTokenProvider
     * @param tokenCredentials The credentials
     * @param executorService An ExecutorService
     * @throws MalformedURLException
     */
    public AzureAdTokenProvider(AzureAdTokenCredentials tokenCredentials, ExecutorService executorService) throws MalformedURLException {
        if (tokenCredentials == null) {
            throw new NullPointerException("tokenCredentials");
        }

        if (executorService == null) {
            throw new NullPointerException("executorService");
        }

        this.tokenCredentials = tokenCredentials;

        StringBuilder authority = new StringBuilder();

        authority.append(canonicalizeUri(this.tokenCredentials.getAzureEnvironment().getActiveDirectoryEndpoint().toString()));
        authority.append(tokenCredentials.getTenant());

        this.executorService = executorService;
        this.authenticationContext = new AuthenticationContext(authority.toString(), false, this.executorService);
    }

    /**
     * Acquires an access token
     * @see com.microsoft.windowsazure.services.media.authentication.TokenProvider#acquireAccessToken()
     */
    @Override
    public AzureAdAccessToken acquireAccessToken() throws Exception {
        AuthenticationResult authResult = getToken().get();
        return new AzureAdAccessToken(authResult.getAccessToken(), authResult.getExpiresOnDate());
    }

    private Future<AuthenticationResult> getToken() {
        String mediaServicesResource = this.tokenCredentials.getAzureEnvironment().getMediaServicesResource();

        switch (this.tokenCredentials.getCredentialType()) {
            case UserSecretCredential:
                return this.authenticationContext.acquireToken(
                        mediaServicesResource,
                        this.tokenCredentials.getAzureEnvironment().getMediaServicesSdkClientId(),
                        this.tokenCredentials.getAzureAdClientUsernamePassword().getUsername(),
                        this.tokenCredentials.getAzureAdClientUsernamePassword().getPassword(),
                        null);

            case ServicePrincipalWithClientSymmetricKey:
                return  this.authenticationContext.acquireToken(
                        mediaServicesResource,
                        this.tokenCredentials.getClientKey(),
                        null);

            case ServicePrincipalWithClientCertificate:
                return this.authenticationContext.acquireToken(
                        mediaServicesResource,
                        this.tokenCredentials.getAsymmetricKeyCredential(),
                        null);

            case UserCredential:
                throw new NotImplementedException(
                        String.format(
                            "Interactive user credential is currently not supported by the java sdk",
                            this.tokenCredentials.getCredentialType()));
            default:
                throw new NotImplementedException(
                    String.format(
                        "Token Credential type %s is not supported.",
                        this.tokenCredentials.getCredentialType()));
        }
    }

    private String canonicalizeUri(String authority) {
        if (authority != null
            && !authority.trim().isEmpty()
            && !authority.endsWith("/")) {

            authority += "/";
        }

        return authority;
    }
}
