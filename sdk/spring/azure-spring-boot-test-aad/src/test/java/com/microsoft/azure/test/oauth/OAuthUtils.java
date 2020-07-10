// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.test.oauth;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

public class OAuthUtils {

    public static final String AAD_CLIENT_ID = "AAD_CLIENT_ID";
    public static final String AAD_CLIENT_SECRET = "AAD_CLIENT_SECRET";
    public static final String SINGLE_TENANT_AAD_CLIENT_ID = "SINGLE_TENANT_AAD_CLIENT_ID";
    public static final String SINGLE_TENANT_AAD_CLIENT_SECRET = "SINGLE_TENANT_AAD_CLIENT_SECRET";
    private static final String AAD_TENANT_ID = "AAD_TENANT_ID";
    private static final String AAD_USER_NAME = "AAD_USER_NAME";
    private static final String AAD_USER_PASSWORD = "AAD_USER_PASSWORD";

    private static final RestTemplate CLIENT = new RestTemplate();

    public static OAuthResponse executeOAuth2ROPCFlow(String aadClientId, String aadClientSecret) {
        final String tenantId = System.getenv().get(AAD_TENANT_ID);
        final String aadUsername = System.getenv(AAD_USER_NAME);
        final String aadUserPassword = System.getenv(AAD_USER_PASSWORD);

        assertNotEmpty(aadClientId, "client id");
        assertNotEmpty(aadClientSecret, "client secret");
        assertNotEmpty(aadUsername, AAD_USER_NAME);
        assertNotEmpty(aadUserPassword, AAD_USER_PASSWORD);

        String url = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", tenantId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("scope", "user.read openid profile offline_access");
        requestBody.add("grant_type", "password");
        requestBody.add("client_id", aadClientId);
        requestBody.add("client_secret", aadClientSecret);
        requestBody.add("username", aadUsername);
        requestBody.add("password", aadUserPassword);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        return CLIENT.postForObject(url, requestEntity, OAuthResponse.class);
    }

    private static void assertNotEmpty(String text, String key) {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException(String.format("%s is not set!", key));
        }
    }

}
