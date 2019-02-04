package com.microsoft.windowsazure.media.authentication;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.IntegrationTestBase;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.microsoft.windowsazure.services.media.MediaContract;
import com.microsoft.windowsazure.services.media.MediaService;
import com.microsoft.windowsazure.services.media.authentication.AzureAdClientSymmetricKey;
import com.microsoft.windowsazure.services.media.authentication.AzureAdClientUsernamePassword;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenCredentials;
import com.microsoft.windowsazure.services.media.authentication.AzureAdTokenProvider;
import com.microsoft.windowsazure.services.media.authentication.AzureEnvironments;
import com.microsoft.windowsazure.services.media.authentication.TokenProvider;
import com.microsoft.windowsazure.services.media.models.Asset;
import com.microsoft.windowsazure.services.media.models.AssetInfo;
import com.microsoft.windowsazure.services.media.models.ListResult;

public class AzureAdTokenProviderTest extends IntegrationTestBase {

	@Test
    public void ServicePrincipalClientSymmetricKeyShouldWork() throws Exception {
        // Arrange
		String tenant = config.getProperty("media.azuread.test.tenant").toString();
    	String restApiEndpoint = config.getProperty("media.azuread.test.account_api_uri").toString();
		String clientId = config.getProperty("media.azuread.test.clientid").toString();
    	String clientKey = config.getProperty("media.azuread.test.clientkey").toString();
    	AzureAdTokenCredentials credentials = new AzureAdTokenCredentials(
    			tenant,
    			new AzureAdClientSymmetricKey(clientId, clientKey),
    			AzureEnvironments.AZURE_CLOUD_ENVIRONMENT);
    	TokenProvider provider = new AzureAdTokenProvider(credentials, executorService);
    	Configuration configuration = MediaConfiguration.configureWithAzureAdTokenProvider(
    			new URI(restApiEndpoint),
    			provider);
      	MediaContract mediaService = MediaService.create(configuration);
      	
      	//	Act
      	ListResult<AssetInfo> assets = mediaService.list(Asset.list());
        
        // Assert
        assertNotNull(assets);
	}
	
	@Test
	public void UserPasswordShouldWork() throws Exception {		
		// Arrange
		String tenant = config.getProperty("media.azuread.test.tenant").toString();
    	String restApiEndpoint = config.getProperty("media.azuread.test.account_api_uri").toString();
    	String username = config.getProperty("media.azuread.test.useraccount").toString();
    	String password = config.getProperty("media.azuread.test.userpassword").toString();
    	
    	assumeFalse(username.equals("undefined"));
    	assumeFalse(password.equals("undefined"));    	
    	
    	AzureAdTokenCredentials credentials = new AzureAdTokenCredentials(
    			tenant,
    			new AzureAdClientUsernamePassword(username, password),
    			AzureEnvironments.AZURE_CLOUD_ENVIRONMENT);
    	TokenProvider provider = new AzureAdTokenProvider(credentials, executorService);
    	Configuration configuration = MediaConfiguration.configureWithAzureAdTokenProvider(
    			new URI(restApiEndpoint),
    			provider);
      	MediaContract mediaService = MediaService.create(configuration);

      	//	Act
      	ListResult<AssetInfo> assets = mediaService.list(Asset.list());
        
        // Assert
        assertNotNull(assets);
	}
	
	@Test
    public void ServicePrincipalWithCertificateShouldWork() throws Exception {
        // Arrange
		String tenant = config.getProperty("media.azuread.test.tenant").toString();
    	String restApiEndpoint = config.getProperty("media.azuread.test.account_api_uri").toString();
		String clientId = config.getProperty("media.azuread.test.clientid").toString();
    	String pfxFile = config.getProperty("media.azuread.test.pfxfile").toString();
    	String pfxPassword = config.getProperty("media.azuread.test.pfxpassword").toString();
    	
    	assumeFalse(pfxFile.equals("undefined"));
    	assumeFalse(pfxPassword.equals("undefined"));

    	InputStream pfx = new FileInputStream(pfxFile);
    	AzureAdTokenCredentials credentials = new AzureAdTokenCredentials(
    			tenant,
    			AsymmetricKeyCredential.create(clientId, pfx, pfxPassword),
    			AzureEnvironments.AZURE_CLOUD_ENVIRONMENT);
    	TokenProvider provider = new AzureAdTokenProvider(credentials, executorService);
    	Configuration configuration = MediaConfiguration.configureWithAzureAdTokenProvider(
    			new URI(restApiEndpoint),
    			provider);
      	MediaContract mediaService = MediaService.create(configuration);
      	
      	//	Act
      	ListResult<AssetInfo> assets = mediaService.list(Asset.list());
        
        // Assert
        assertNotNull(assets);
	}
}
