package com.microsoft.azure.sevicebus.security;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;


public class ManagedServiceIdentityTokenProvider extends TokenProvider
{
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ManagedServiceIdentityTokenProvider.class);
    
    private static final String STATIC_LOCAL_REST_MSI_ENDPOINT_URL = "http://localhost:50342/oauth2/token";
    private static final String APIVERSION = "api-version=2017-09-01";
    private static final String MSI_ENDPOINT_ENV_VARIABLE = "MSI_ENDPOINT";
    private static final String MSI_SECRET_ENV_VARIABLE = "MSI_SECRET";
    private static final String STATIC_MSI_URL_FORMAT = "%s?resource=%s";
    private static final String DYNAMIC_MSI_URL_FORMAT = "%s?resource=%s&%s";
    private static final String METADATA_HEADER_NAME = "Metadata";
    private static final String SECRET_HEADER_NAME = "Secret";
    
    @Override
    public CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience) {
        String addAudienceForSB = SecurityConstants.SERVICEBUS_AAD_AUDIENCE_RESOURCE_URL;
        CompletableFuture<SecurityToken> tokenGeneratingFuture = new CompletableFuture<>();
        ForkJoinPool.commonPool().execute(() -> {
            try
            {
                MSIToken msiToken = getMSIToken(addAudienceForSB);
                SecurityToken generatedToken = new SecurityToken(SecurityTokenType.JWT, audience, msiToken.getAccessToken(), Instant.EPOCH.plus(Duration.ofSeconds(msiToken.getNotBefore())), Instant.now().plus(Duration.ofSeconds(msiToken.getExpiresIn())));
                tokenGeneratingFuture.complete(generatedToken);
            }
            catch(IOException ioe)
            {
                TRACE_LOGGER.error("ManagedServiceIdentity token generation failed.", ioe);
                tokenGeneratingFuture.completeExceptionally(ioe);
            }
        });
        
        return tokenGeneratingFuture;
    }
    
    private static MSIToken getMSIToken(String audience) throws IOException
    {
        boolean useStaticHttpUrl;
        String localMsiEndPointURL = System.getenv(MSI_ENDPOINT_ENV_VARIABLE);
        String msiSecret = System.getenv(MSI_SECRET_ENV_VARIABLE);
        if(localMsiEndPointURL == null || localMsiEndPointURL.isEmpty() || msiSecret == null || msiSecret.isEmpty())
        {
            useStaticHttpUrl = true;
        }
        else
        {
            useStaticHttpUrl = false;
        }
        
        HttpURLConnection httpConnection;
        if(useStaticHttpUrl)
        {
            String localMSIURLForResouce = String.format(STATIC_MSI_URL_FORMAT, STATIC_LOCAL_REST_MSI_ENDPOINT_URL, audience);
            URL msiURL = new URL(localMSIURLForResouce);
            httpConnection = (HttpURLConnection) msiURL.openConnection();
            httpConnection.setRequestProperty(METADATA_HEADER_NAME, "true");
        }
        else
        {
            String localMSIURLForResouce = String.format(DYNAMIC_MSI_URL_FORMAT, localMsiEndPointURL, audience, APIVERSION);
            URL msiURL = new URL(localMSIURLForResouce);
            httpConnection = (HttpURLConnection) msiURL.openConnection();
            httpConnection.setRequestProperty(SECRET_HEADER_NAME, msiSecret);
        }
        
        httpConnection.setRequestMethod("GET");
        httpConnection.setDoInput(true);
        httpConnection.connect();
        StringBuilder responseBuilder = new StringBuilder();
        try(Reader reader = new InputStreamReader(httpConnection.getInputStream(), StandardCharsets.UTF_8))
        {
            char[] buffer = new char[1024];
            int numBytesRead = -1;
            while((numBytesRead = reader.read(buffer)) != -1)
            {
                responseBuilder.append(buffer, 0, numBytesRead);
            }
        }
        
        Gson gson = new Gson();
        MSIToken msiToken = gson.fromJson(responseBuilder.toString(), MSIToken.class);
        return msiToken;
    }
    
    private static class MSIToken
    {
        private String access_token;
        private String refresh_token;
        // Token validity in number of seconds
        private int expires_in;
        // Seconds from 1970-01-01T0:0:0Z UTC when the token will expire
        private long expires_on;
     // Seconds from 1970-01-01T0:0:0Z UTC after which the token takes effect
        private long not_before;
        // Resource for which token is requested
        private String resource;
        // Token type
        private String token_type;
        
        public String getAccessToken() {
            return access_token;
        }
        public int getExpiresIn() {
            return expires_in;
        }
        public long getExpiresOn() {
            return expires_on;
        }
        public long getNotBefore() {
            return not_before;
        }
        public String getResource() {
            return resource;
        }
        public String getTokenType() {
            return token_type;
        }
    }
}
