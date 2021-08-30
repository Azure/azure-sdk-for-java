// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad;

import com.azure.spring.autoconfigure.aad.AADAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
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

    public static WebApplicationContextRunner getWebApplicationRunner() {
        return new WebApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withUserConfiguration(AADAutoConfiguration.class)
            .withInitializer(new ConditionEvaluationReportLoggingListener(LogLevel.INFO));
    }

    @SuppressWarnings("unchecked")
    public static MultiValueMap<String, String> toMultiValueMap(RequestEntity<?> entity) {
        return (MultiValueMap<String, String>) Optional.ofNullable(entity)
                                                       .map(HttpEntity::getBody)
                                                       .orElse(null);
    }

    public static String[] withWebApplicationOrOAuthClientPropertyValues() {
        return new String[] {
            "azure.activedirectory.client-id = fake-client-id",
            "azure.activedirectory.client-secret = fake-client-secret",
            "azure.activedirectory.tenant-id = fake-tenant-id"};
    }

    public static String[] withResourceServerPropertyValues() {
        return new String[] {
            "azure.activedirectory.tenant-id=fake-tenant-id",
            "azure.activedirectory.app-id-uri=fake-app-id-uri"};
    }

    public static WebApplicationContextRunner webApplicationContextRunner() {
        return getWebApplicationRunner()
            .withPropertyValues(withWebApplicationOrOAuthClientPropertyValues());
    }

    public static WebApplicationContextRunner resourceServerContextRunner() {
        return new WebApplicationContextRunner()
            .withUserConfiguration(AADAutoConfiguration.class)
            .withClassLoader(new FilteredClassLoader(ClientRegistration.class))
            .withPropertyValues(withResourceServerPropertyValues());
    }

    public static WebApplicationContextRunner oauthClientContextRunner() {
        return new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AADAutoConfiguration.class))
            .withPropertyValues(withWebApplicationOrOAuthClientPropertyValues());
    }

    public static WebApplicationContextRunner oauthClientAndResourceServerContextRunner() {
        return new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AADAutoConfiguration.class))
            .withPropertyValues(withWebApplicationOrOAuthClientPropertyValues())
            .withPropertyValues(withResourceServerPropertyValues())
            .withPropertyValues("azure.activedirectory.application-type=web_application_and_resource_server");
    }
}
