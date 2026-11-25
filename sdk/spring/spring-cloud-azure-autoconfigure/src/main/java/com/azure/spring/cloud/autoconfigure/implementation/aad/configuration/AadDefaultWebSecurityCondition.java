// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.configuration;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Matches Spring Boot's default servlet security condition across supported Boot 4 versions.
 */
final class AadDefaultWebSecurityCondition extends AllNestedConditions {

    AadDefaultWebSecurityCondition() {
        super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnClass({ SecurityFilterChain.class, HttpSecurity.class })
    static class SecurityClasses {
    }

    @ConditionalOnMissingBean(SecurityFilterChain.class)
    static class SecurityFilterChainBean {
    }
}
