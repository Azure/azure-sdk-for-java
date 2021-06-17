// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.util.MultiValueMap;
import org.springframework.web.filter.RequestContextFilter;

import javax.annotation.PostConstruct;
import java.util.Optional;

public class WebApplicationContextRunnerUtils {

    @EnableWebSecurity
    @Import(WebMvcAutoConfiguration.class)
    public static class WebApp {

        @Autowired
        private RequestContextFilter requestContextFilter;

        @PostConstruct
        public void postConstruct() {
            requestContextFilter.setThreadContextInheritable(true);
        }
    }

    public static WebApplicationContextRunner getContextRunnerWithRequiredProperties() {
        return getContextRunner().withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id");
    }

    public static WebApplicationContextRunner getContextRunner() {
        return new WebApplicationContextRunner()
            .withClassLoader(new FilteredClassLoader(BearerTokenAuthenticationToken.class))
            .withUserConfiguration(WebApp.class, AADWebAppConfiguration.class);
    }

    @SuppressWarnings("unchecked")
    public static MultiValueMap<String, String> toMultiValueMap(RequestEntity<?> entity) {
        return (MultiValueMap<String, String>) Optional.ofNullable(entity)
                                                       .map(HttpEntity::getBody)
                                                       .orElse(null);
    }

}
