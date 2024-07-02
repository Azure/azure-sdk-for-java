// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad;

import com.azure.spring.cloud.autoconfigure.implementation.aad.configuration.AadAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

public class WebApplicationContextRunnerUtils {

    public static WebApplicationContextRunner oauthClientAndResourceServerRunner() {
        return new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    HttpMessageConvertersAutoConfiguration.class,
                    RestTemplateAutoConfiguration.class))
            .withUserConfiguration(AzureGlobalPropertiesAutoConfiguration.class, AadAutoConfiguration.class)
            .withInitializer(new ConditionEvaluationReportLoggingListener());
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

    public static WebApplicationContextRunner webApplicationAndResourceServerContextRunner() {
        return oauthClientAndResourceServerRunner()
            .withPropertyValues(withWebApplicationOrResourceServerWithOboPropertyValues())
            .withPropertyValues(withResourceServerPropertyValues())
            .withPropertyValues(withPropertyValueWebApplicationAndResourceServer());
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
            "spring.cloud.azure.active-directory.credential.client-id = fake-client-id",
            "spring.cloud.azure.active-directory.credential.client-secret = fake-client-secret",
            "spring.cloud.azure.active-directory.profile.tenant-id = fake-tenant-id"};
    }

    public static String[] withResourceServerPropertyValues() {
        return new String[] {
            "spring.cloud.azure.active-directory.enabled = true",
            "spring.cloud.azure.active-directory.profile.tenant-id=fake-tenant-id",
            "spring.cloud.azure.active-directory.app-id-uri=fake-app-id-uri"};
    }

    public static String[] withPropertyValueWebApplicationAndResourceServer() {
        return new String[] {
            "spring.cloud.azure.active-directory.application-type = web_application_and_resource_server"
        };
    }
}
