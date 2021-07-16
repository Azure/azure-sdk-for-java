// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.condition.aad;

import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ContextConsumer;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractCondition {

    final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    static class Config {

        @Bean
        String myBean() {
            return "myBean";
        }
    }

    protected ContextConsumer<AssertableApplicationContext> assertConditionMatch(boolean mustHaveBean) {
        return (context) -> {
            if (mustHaveBean) {
                assertThat(context).hasBean("myBean");
            } else {
                assertThat(context).doesNotHaveBean("myBean");
            }
        };
    }
}
