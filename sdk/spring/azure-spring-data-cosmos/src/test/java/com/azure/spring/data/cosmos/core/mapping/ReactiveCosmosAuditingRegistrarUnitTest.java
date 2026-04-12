// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core.mapping;

import com.azure.spring.data.cosmos.Constants;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.ReactiveIsNewAwareAuditingHandler;
import org.springframework.data.domain.ReactiveAuditorAware;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EnableReactiveCosmosAuditing} and {@link ReactiveCosmosAuditingRegistrar}.
 */
public class ReactiveCosmosAuditingRegistrarUnitTest {

    @Test
    public void enableReactiveCosmosAuditingShouldRegisterReactiveHandler() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ReactiveAuditingTestConfig.class)) {
            assertThat(context.containsBean(Constants.REACTIVE_AUDITING_HANDLER_BEAN_NAME)).isTrue();
            Object handler = context.getBean(Constants.REACTIVE_AUDITING_HANDLER_BEAN_NAME);
            assertThat(handler).isInstanceOf(ReactiveIsNewAwareAuditingHandler.class);
        }
    }

    @Test
    public void enableReactiveCosmosAuditingShouldNotRegisterBlockingHandler() {
        try (AnnotationConfigApplicationContext context =
                 new AnnotationConfigApplicationContext(ReactiveAuditingTestConfig.class)) {
            assertThat(context.containsBean(Constants.AUDITING_HANDLER_BEAN_NAME)).isFalse();
        }
    }

    @Configuration
    @EnableReactiveCosmosAuditing
    static class ReactiveAuditingTestConfig {

        @Bean
        public CosmosMappingContext cosmosMappingContext() {
            return new CosmosMappingContext();
        }

        @Bean
        public ReactiveAuditorAware<String> reactiveAuditorAware() {
            return () -> Mono.just("test-auditor");
        }
    }
}
