// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.mapping;

import com.azure.spring.data.cosmos.Constants;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.Ordered;
import org.springframework.data.auditing.ReactiveIsNewAwareAuditingHandler;
import org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport;
import org.springframework.data.auditing.config.AuditingConfiguration;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;

/**
 * Reactive variant of {@link CosmosAuditingRegistrar}. Registers a
 * {@link ReactiveIsNewAwareAuditingHandler} bean that works with
 * {@link org.springframework.data.domain.ReactiveAuditorAware}.
 *
 * Adapted from spring-data-mongodb's ReactiveMongoAuditingRegistrar.
 */
class ReactiveCosmosAuditingRegistrar extends AuditingBeanDefinitionRegistrarSupport implements Ordered {

    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableReactiveCosmosAuditing.class;
    }

    @Override
    protected String getAuditingHandlerBeanName() {
        return Constants.REACTIVE_AUDITING_HANDLER_BEAN_NAME;
    }

    @Override
    protected void postProcess(BeanDefinitionBuilder builder, AuditingConfiguration configuration,
                               BeanDefinitionRegistry registry) {
        final BeanDefinitionBuilder definition =
            BeanDefinitionBuilder.genericBeanDefinition(CosmosMappingContext.class);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);

        builder.setFactoryMethod("from").addConstructorArgValue(definition.getBeanDefinition());
    }

    @Override
    protected BeanDefinitionBuilder getAuditHandlerBeanDefinitionBuilder(AuditingConfiguration configuration) {
        Assert.notNull(configuration, "AuditingConfiguration must not be null!");

        return configureDefaultAuditHandlerAttributes(configuration,
            BeanDefinitionBuilder.rootBeanDefinition(ReactiveIsNewAwareAuditingHandler.class));
    }

    @Override
    protected void registerAuditListenerBeanDefinition(BeanDefinition auditingHandlerDefinition,
                                                       BeanDefinitionRegistry registry) {
        // Auditing is handled directly by ReactiveCosmosTemplate via the injected
        // ReactiveIsNewAwareAuditingHandler rather than through entity callbacks.
        // This is consistent with the existing blocking auditing approach.
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
