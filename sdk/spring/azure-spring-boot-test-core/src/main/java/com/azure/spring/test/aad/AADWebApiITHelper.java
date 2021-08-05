// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.aad;

import com.azure.spring.test.AppRunner;
import com.azure.spring.test.aad.ropc.AADOauth2ROPCGrantClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_TENANT_ID_1;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_NAME_1;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_PASSWORD_1;
import static org.springframework.http.HttpHeaders.COOKIE;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

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

    public String httpGetCookieByAccessTokenThenGetStringByCookie(String accessTokenEndpoint, String cookieEndpoint) {
        ResponseEntity<String> responseEntity =
            httpGetResponseByToken(accessTokenEndpoint, oAuth2ROPCResponse.getAccessToken());
        String jSessionIdCookie = responseEntity
            .getHeaders()
            .getOrDefault(SET_COOKIE, new ArrayList<>())
            .stream()
            .filter(s -> s.startsWith("JSESSIONID="))
            .findAny()
            .orElse(null);
        Assert.notNull(jSessionIdCookie, "jSessionIdCookie can not be null.");
        HttpHeaders headers = new HttpHeaders();
        headers.add(COOKIE, jSessionIdCookie);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        return httpGetResponseByEntity(cookieEndpoint, entity).getBody();
    }

    public String httpGetStringByAccessToken(String endpoint) {
        return httpGetStringByToken(endpoint, oAuth2ROPCResponse.getAccessToken());
    }

    public String httpGetStringWithoutAccessToken(String endpoint) {
        return httpGetStringByToken(endpoint, null);
    }

    public String httpGetStringByIdToken(String endpoint) {
        return httpGetStringByToken(endpoint, oAuth2ROPCResponse.getIdToken());
    }

    public String httpGetStringByToken(String endpoint, String token) {
        return httpGetResponseByToken(endpoint, token).getBody();
    }

    public ResponseEntity<String> httpGetResponseByToken(String endpoint, String token) {
        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(token)) {
            headers.set("Authorization", String.format("Bearer %s", token));
        }
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        return httpGetResponseByEntity(endpoint, entity);
    }

    public ResponseEntity<String> httpGetResponseByEntity(String endpoint, HttpEntity<Object> entity) {
        endpoint = addSlash(endpoint);
        return REST_TEMPLATE
            .exchange(
                app.root() + endpoint,
                HttpMethod.GET,
                entity,
                String.class,
                new HashMap<>());
    }

    private String addSlash(String endpoint) {
        return endpoint.startsWith("/") ? endpoint : "/" + endpoint;
    }
}
