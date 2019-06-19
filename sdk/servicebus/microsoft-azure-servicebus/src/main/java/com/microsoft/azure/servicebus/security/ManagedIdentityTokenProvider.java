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
 * This is a token provider that obtains token using Managed Identity(MI). This token provider automatically detects MI settings.
 * @since 1.2.0
 *
 */
public class ManagedIdentityTokenProvider extends TokenProvider {
    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(ManagedIdentityTokenProvider.class);
    
    private static final String STATIC_LOCAL_REST_MSI_ENDPOINT_URL = "http://169.254.169.254/metadata/identity/oauth2/token";
    private static final String APIVERSION = "api-version=2018-02-01";
    private static final String MI_ENDPOINT_ENV_VARIABLE = "MI_ENDPOINT";
    private static final String MI_SECRET_ENV_VARIABLE = "MI_SECRET";
    private static final String MI_URL_FORMAT = "%s?resource=%s&%s";
    private static final String METADATA_HEADER_NAME = "Metadata";
    private static final String SECRET_HEADER_NAME = "Secret";
    
    @Override
    public CompletableFuture<SecurityToken> getSecurityTokenAsync(String audience) {
        String addAudienceForSB = SecurityConstants.SERVICEBUS_AAD_AUDIENCE_RESOURCE_URL;
        CompletableFuture<SecurityToken> tokenGeneratingFuture = new CompletableFuture<>();
        MessagingFactory.INTERNAL_THREAD_POOL.execute(() -> {
            try {
                MIToken miToken = getMIToken(addAudienceForSB);
                SecurityToken generatedToken = new SecurityToken(SecurityTokenType.JWT, audience, miToken.getAccessToken(), Instant.EPOCH.plus(Duration.ofSeconds(miToken.getNotBefore())), Instant.now().plus(Duration.ofSeconds(miToken.getExpiresIn())));
                tokenGeneratingFuture.complete(generatedToken);
            } catch (IOException ioe) {
                TRACE_LOGGER.error("ManagedIdentity token generation failed.", ioe);
                tokenGeneratingFuture.completeExceptionally(ioe);
            }
        });
        
        return tokenGeneratingFuture;
    }
    
    private static MIToken getMIToken(String audience) throws IOException {
        boolean useStaticHttpUrl;
        String localMiEndPointURL = System.getenv(MI_ENDPOINT_ENV_VARIABLE);
        String miSecret = System.getenv(MI_SECRET_ENV_VARIABLE);
        if (localMiEndPointURL == null || localMiEndPointURL.isEmpty() || miSecret == null || miSecret.isEmpty()) {
            useStaticHttpUrl = true;
        } else {
            useStaticHttpUrl = false;
        }
        
        HttpURLConnection httpConnection;
        if (useStaticHttpUrl) {
            String localMSIURLForResouce =
                    String.format(MI_URL_FORMAT, STATIC_LOCAL_REST_MSI_ENDPOINT_URL, audience, APIVERSION);
            URL msiURL = new URL(localMSIURLForResouce);
            httpConnection = (HttpURLConnection) msiURL.openConnection();
            httpConnection.setRequestProperty(METADATA_HEADER_NAME, "true");
        } else {
            String localMIURLForResouce = String.format(MI_URL_FORMAT, localMiEndPointURL, audience, APIVERSION);
            URL miURL = new URL(localMIURLForResouce);
            httpConnection = (HttpURLConnection) miURL.openConnection();
            httpConnection.setRequestProperty(SECRET_HEADER_NAME, miSecret);
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
        MIToken miToken = gson.fromJson(responseBuilder.toString(), MIToken.class);
        return miToken;
    }
    
    private static class MIToken {
        private String accessToken;
        private String refreshToken;
        // Token validity in number of seconds
        private int expiresIn;
        // Seconds from 1970-01-01T0:0:0Z UTC when the token will expire
        private long expiresOn;
        // Seconds from 1970-01-01T0:0:0Z UTC after which the token takes effect
        private long notBefore;
        // Resource for which token is requested
        private String resource;
        // Token type
        private String tokenType;
        
        public String getAccessToken() {
            return accessToken;
        }
        public int getExpiresIn() {
            return expiresIn;
        }
        public long getExpiresOn() {
            return expiresOn;
        }
        public long getNotBefore() {
            return notBefore;
        }
        public String getResource() {
            return resource;
        }
        public String getTokenType() {
            return tokenType;
        }
    }
}
