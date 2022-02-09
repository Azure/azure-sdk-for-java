// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import org.junit.Test;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ExpressionResolverUnitTest {
    private static final String LITERAL_EXPRESSION = "literal expression";
    private static final String SPEL_EXPRESSION = "#{@environment.getProperty('dynamic.collection.name')}";

    @Test
    public void testLiteralExpressionsShouldNotBeAltered() {
        assertEquals(LITERAL_EXPRESSION, ExpressionResolver.resolveExpression(LITERAL_EXPRESSION));
    }

    @Test
    public void testExpressionsShouldBeResolved() {
        final AnnotationConfigApplicationContext applicationContext =
                new AnnotationConfigApplicationContext(TestConfiguration.class);

        assertNotNull(applicationContext.getBean(ExpressionResolver.class));
        assertEquals(TestConstants.DYNAMIC_PROPERTY_COLLECTION_NAME,
                ExpressionResolver.resolveExpression(SPEL_EXPRESSION));
    }

    @Configuration
    @PropertySource("application.properties")
    static class TestConfiguration {
        @Bean
        public ExpressionResolver expressionResolver(ConfigurableBeanFactory beanFactory) {
            return new ExpressionResolver(beanFactory);
        }
    }

}
