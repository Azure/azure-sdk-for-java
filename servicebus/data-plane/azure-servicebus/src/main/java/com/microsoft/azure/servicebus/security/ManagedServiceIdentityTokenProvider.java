// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.security;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.microsoft.azure.servicebus.primitives.MessagingFactory;

/**
 * This is a token provider that obtains token using Managed Service Identity(MSI). This token provider automatically detects MSI settings.
 * @since 1.2.0
 *
 */
public class ManagedServiceIdentityTokenProvider extends TokenProvider {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ManagedServiceIdentityTokenProvider.class);
    
    private static final String STATIC_LOCAL_REST_MSI_ENDPOINT_URL = "http://169.254.169.254/metadata/identity/oauth2/token";
    private static final String APIVERSION = "api-version=2018-02-01";
    private static final String MSI_ENDPOINT_ENV_VARIABLE = "MSI_ENDPOINT";
    private static final String MSI_SECRET_ENV_VARIABLE = "MSI_SECRET";
    private static final String MSI_URL_FORMAT = "%s?resource=%s&%s";
    private static final String METADATA_HEADER_NAME = "Metadata";
    private static final String SECRET_HEADER_NAME = "Secret";
    
    @Override
    public CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience) {
        String addAudienceForSB = SecurityConstants.SERVICEBUS_AAD_AUDIENCE_RESOURCE_URL;
        CompletableFuture<SecurityToken> tokenGeneratingFuture = new CompletableFuture<>();
        MessagingFactory.INTERNAL_THREAD_POOL.execute(() -> {
            try {
                MSIToken msiToken = getMSIToken(addAudienceForSB);
                SecurityToken generatedToken = new SecurityToken(SecurityTokenType.JWT, audience, msiToken.getAccessToken(), Instant.EPOCH.plus(Duration.ofSeconds(msiToken.getNotBefore())), Instant.now().plus(Duration.ofSeconds(msiToken.getExpiresIn())));
                tokenGeneratingFuture.complete(generatedToken);
            } catch (IOException ioe) {
                TRACE_LOGGER.error("ManagedServiceIdentity token generation failed.", ioe);
                tokenGeneratingFuture.completeExceptionally(ioe);
            }
        });
        
        return tokenGeneratingFuture;
    }
    
    private static MSIToken getMSIToken(String audience) throws IOException {
        boolean useStaticHttpUrl;
        String localMsiEndPointURL = System.getenv(MSI_ENDPOINT_ENV_VARIABLE);
        String msiSecret = System.getenv(MSI_SECRET_ENV_VARIABLE);
        if (localMsiEndPointURL == null || localMsiEndPointURL.isEmpty() || msiSecret == null || msiSecret.isEmpty()) {
            useStaticHttpUrl = true;
        } else {
            useStaticHttpUrl = false;
        }
        
        HttpURLConnection httpConnection;
        if (useStaticHttpUrl) {
            String localMSIURLForResouce =
                    String.format(MSI_URL_FORMAT, STATIC_LOCAL_REST_MSI_ENDPOINT_URL, audience, APIVERSION);
            URL msiURL = new URL(localMSIURLForResouce);
            httpConnection = (HttpURLConnection) msiURL.openConnection();
            httpConnection.setRequestProperty(METADATA_HEADER_NAME, "true");
        } else {
            String localMSIURLForResouce = String.format(MSI_URL_FORMAT, localMsiEndPointURL, audience, APIVERSION);
            URL msiURL = new URL(localMSIURLForResouce);
            httpConnection = (HttpURLConnection) msiURL.openConnection();
            httpConnection.setRequestProperty(SECRET_HEADER_NAME, msiSecret);
        }
        
        httpConnection.setRequestMethod("GET");
        httpConnection.setDoInput(true);
        httpConnection.connect();
        StringBuilder responseBuilder = new StringBuilder();
        try (Reader reader = new InputStreamReader(httpConnection.getInputStream(), StandardCharsets.UTF_8)) {
            char[] buffer = new char[1024];
            int numBytesRead = -1;
            while ((numBytesRead = reader.read(buffer)) != -1) {
                responseBuilder.append(buffer, 0, numBytesRead);
            }
        }
        
        Gson gson = new Gson();
        MSIToken msiToken = gson.fromJson(responseBuilder.toString(), MSIToken.class);
        return msiToken;
    }
    
    private static class MSIToken {
        private String ACCESS_TOKEN;
        private String REFRESH_TOKEN;
        // Token validity in number of seconds
        private int EXPIRES_IN;
        // Seconds from 1970-01-01T0:0:0Z UTC when the token will expire
        private long EXPIRES_ON;
        // Seconds from 1970-01-01T0:0:0Z UTC after which the token takes effect
        private long NOT_BEFORE;
        // Resource for which token is requested
        private String RESOURCE;
        // Token type
        private String TOKEN_TYPE;
        
        public String getAccessToken() {
            return ACCESS_TOKEN;
        }
        public int getExpiresIn() {
            return EXPIRES_IN;
        }
        public long getExpiresOn() {
            return EXPIRES_ON;
        }
        public long getNotBefore() {
            return NOT_BEFORE;
        }
        public String getResource() {
            return RESOURCE;
        }
        public String getTokenType() {
            return TOKEN_TYPE;
        }
    }
}
