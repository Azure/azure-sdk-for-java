// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.util;

import com.azure.test.util.ropc.AADOauth2ROPCGrantClient;
import com.azure.test.utils.AppRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AADWebApiITHelper {
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    private final AppRunner app;
    private final HttpEntity<Object> entity;

    public AADWebApiITHelper(Class<?> appClass, Map<String, String> appProperties, List<String> accessTokenScopes) {
        app = new AppRunner(appClass);
        appProperties.forEach(app::property);
        app.start();

        String accessToken = AADOauth2ROPCGrantClient.getAccessTokenForTestAccount(String.join(" ", accessTokenScopes));
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", accessToken));
        entity = new HttpEntity<>(headers);
    }

    public String httpGet(String endpoint) {
        endpoint = endpoint.startsWith("/") ? endpoint : "/" + endpoint;
        return REST_TEMPLATE
            .exchange(
                app.root() + endpoint,
                HttpMethod.GET,
                entity,
                String.class,
                new HashMap<>())
            .getBody();
    }
}
