// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation;

import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

public class WebApplicationContextRunnerUtils {

    public static WebApplicationContextRunner oauthClientAndResourceServerRunner() {
        return new WebApplicationContextRunner()
            .withUserConfiguration(AADAutoConfiguration.class)
            .withInitializer(new ConditionEvaluationReportLoggingListener(LogLevel.INFO));
    }

    public static WebApplicationContextRunner oauthClientRunner() {
        return oauthClientAndResourceServerRunner()
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class));
    }

    public static WebApplicationContextRunner resourceServerRunner() {
        return oauthClientAndResourceServerRunner()
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class));
    }

    public static WebApplicationContextRunner webApplicationContextRunner() {
        return oauthClientRunner()
            .withPropertyValues(withWebApplicationOrResourceServerWithOboPropertyValues());
    }

    public static WebApplicationContextRunner resourceServerContextRunner() {
        return resourceServerRunner()
            .withPropertyValues(withResourceServerPropertyValues());
    }

    public static WebApplicationContextRunner resourceServerWithOboContextRunner() {
        return oauthClientAndResourceServerRunner()
            .withPropertyValues(withWebApplicationOrResourceServerWithOboPropertyValues())
            .withPropertyValues(withResourceServerPropertyValues());
    }

    @SuppressWarnings("unchecked")
    public static MultiValueMap<String, String> toMultiValueMap(RequestEntity<?> entity) {
        return (MultiValueMap<String, String>) Optional.ofNullable(entity)
                                                       .map(HttpEntity::getBody)
                                                       .orElse(null);
    }

    public static String[] withWebApplicationOrResourceServerWithOboPropertyValues() {
        return new String[] {
            "spring.cloud.azure.active-directory.enabled = true",
            "spring.cloud.azure.active-directory.client-id = fake-client-id",
            "spring.cloud.azure.active-directory.client-secret = fake-client-secret",
            "spring.cloud.azure.active-directory.tenant-id = fake-tenant-id"};
    }

    public static String[] withResourceServerPropertyValues() {
        return new String[] {
            "spring.cloud.azure.active-directory.tenant-id=fake-tenant-id",
            "spring.cloud.azure.active-directory.app-id-uri=fake-app-id-uri"};
    }
}
