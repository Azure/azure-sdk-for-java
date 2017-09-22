package com.microsoft.windowsazure.services.media.authentication;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.inject.Named;

import org.apache.commons.lang.NotImplementedException;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.windowsazure.services.media.MediaConfiguration;

public class AzureAdTokenProvider {
	private final AuthenticationContext authenticationContext;
    private final AzureAdTokenCredentials tokenCredentials;
    private final ExecutorService executorService;
    
    public AzureAdTokenProvider(AzureAdTokenCredentials tokenCredentials, ExecutorService executorService) throws MalformedURLException
    {
        if (tokenCredentials == null)
        {
            throw new NullPointerException("tokenCredentials");
        }
        
        if (executorService == null)
        {
            throw new NullPointerException("executorService");
        }

        this.tokenCredentials = tokenCredentials;
        
        StringBuilder authority = new StringBuilder();
        
        authority.append(canonicalizeUri(this.tokenCredentials.getAzureEnvironment().getActiveDirectoryEndpoint().toString()));
        authority.append(tokenCredentials.getTenant());
        
        this.executorService = executorService;
        this.authenticationContext = new AuthenticationContext(authority.toString(), false, this.executorService);
    }
    
    public AzureAdAccessToken acquireAccessToken() {
    	Future<AuthenticationResult> futureToken = getToken();
    	AzureAdAccessToken result = null;
    	
		try {
			AuthenticationResult authResult = futureToken.get();
			result = new AzureAdAccessToken(authResult.getAccessToken(), authResult.getExpiresOnDate());
		} catch (Throwable e) {
			// returns null if any error occurs
		}
    	
    	return result; 
    }
    
    private Future<AuthenticationResult> getToken()
    {
        String mediaServicesResource = this.tokenCredentials.getAzureEnvironment().getMediaServicesResource();

        switch (this.tokenCredentials.getCredentialType())
        {
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
            default:
                throw new NotImplementedException(
                    String.format(
                        "Token Credential type %1 is not supported.",
                        this.tokenCredentials.getCredentialType()));
        }
    }
    
    private String canonicalizeUri(String authority) {
        if (authority != null &&
        	!authority.trim().isEmpty() &&
        	!authority.endsWith("/")) {
        	
            authority += "/";
        }
        
        return authority;
    }

}
