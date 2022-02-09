// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.common;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;

/**
 *
 * @author Domenico Sibilio
 *
 */
public class ExpressionResolver {

    private static EmbeddedValueResolver embeddedValueResolver;

    /**
     * Initialize ExpressionResolver with ConfigurableBeanFactory
     * @param beanFactory used to initialize the embeddedValueResolver
     */
    public ExpressionResolver(BeanFactory beanFactory) {
        if (beanFactory instanceof ConfigurableBeanFactory) {
            setEmbeddedValueResolver(new EmbeddedValueResolver((ConfigurableBeanFactory) beanFactory));
        }
    }

    /**
     * Resolve the given string value via an {@link EmbeddedValueResolver}
     * @param expression the expression to be resolved
     * @return the resolved expression, may be {@literal null}
     */
    public static String resolveExpression(String expression) {
        return embeddedValueResolver != null
                ? embeddedValueResolver.resolveStringValue(expression)
                : expression;
    }

    private static void setEmbeddedValueResolver(EmbeddedValueResolver embeddedValueResolver) {
        ExpressionResolver.embeddedValueResolver = embeddedValueResolver;
    }

}
