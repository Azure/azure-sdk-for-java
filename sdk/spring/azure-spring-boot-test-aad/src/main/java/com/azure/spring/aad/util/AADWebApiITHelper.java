// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.util;

import com.azure.spring.aad.util.ropc.AADOauth2ROPCGrantClient;
import com.azure.test.utils.AppRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.spring.aad.util.EnvironmentVariables.AAD_TENANT_ID_1;
import static com.azure.spring.aad.util.EnvironmentVariables.AAD_USER_NAME_1;
import static com.azure.spring.aad.util.EnvironmentVariables.AAD_USER_PASSWORD_1;

public class AADWebApiITHelper {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private final AppRunner app;
    private final AADOauth2ROPCGrantClient.OAuth2ROPCResponse oAuth2ROPCResponse;

    public AADWebApiITHelper(Class<?> appClass,
                             Map<String, String> appProperties,
                             String clientId,
                             String clientSecret,
                             List<String> accessTokenScopes) {
        app = new AppRunner(appClass);
        appProperties.forEach(app::property);
        app.start();

        oAuth2ROPCResponse = AADOauth2ROPCGrantClient.getOAuth2ROPCResponseByROPCGrant(
            AAD_TENANT_ID_1,
            clientId,
            clientSecret,
            AAD_USER_NAME_1,
            AAD_USER_PASSWORD_1,
            String.join(" ", accessTokenScopes));
    }

    public String httpGetByAccessToken(String endpoint) {
        return httpGetWithToken(endpoint, oAuth2ROPCResponse.getAccessToken());
    }

    public String httpGetByIdToken(String endpoint) {
        return httpGetWithToken(endpoint, oAuth2ROPCResponse.getIdToken());
    }

    public String httpGetWithToken(String endpoint, String token) {
        endpoint = addSlash(endpoint);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", token));
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        return REST_TEMPLATE
            .exchange(
                app.root() + endpoint,
                HttpMethod.GET,
                entity,
                String.class,
                new HashMap<>())
            .getBody();
    }

    private String addSlash(String endpoint) {
        return endpoint.startsWith("/") ? endpoint : "/" + endpoint;
    }
}
