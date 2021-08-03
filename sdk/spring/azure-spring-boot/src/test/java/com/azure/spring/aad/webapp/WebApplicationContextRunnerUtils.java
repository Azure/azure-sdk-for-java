// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAutoConfiguration;
import org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

public class WebApplicationContextRunnerUtils {

    public static WebApplicationContextRunner getContextRunnerWithRequiredProperties() {
        return getContextRunner().withPropertyValues(
                "azure.activedirectory.client-id = fake-client-id",
                "azure.activedirectory.client-secret = fake-client-secret",
                "azure.activedirectory.tenant-id = fake-tenant-id");
    }

    public static WebApplicationContextRunner getContextRunner() {
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

}
