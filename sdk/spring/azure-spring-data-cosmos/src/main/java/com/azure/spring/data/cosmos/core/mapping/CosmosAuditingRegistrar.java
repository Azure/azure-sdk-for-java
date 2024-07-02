// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.mapping;

import com.azure.spring.data.cosmos.Constants;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.Ordered;
import org.springframework.data.auditing.IsNewAwareAuditingHandler;
import org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport;
import org.springframework.data.auditing.config.AuditingConfiguration;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;

/**
 * Adapted from <a href="https://github.com/spring-projects/spring-data-mongodb/blob/master/spring-data-mongodb
 * /src/main/java/org/springframework/data/mongodb/config/MongoAuditingRegistrar.java">MongoAuditingRegistrar.java</a>
 */
class CosmosAuditingRegistrar extends AuditingBeanDefinitionRegistrarSupport implements Ordered {

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#getAnnotation()
     */
    @Override
    protected Class<? extends Annotation> getAnnotation() {
        return EnableCosmosAuditing.class;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#getAuditingHandlerBeanName()
     */
    @Override
    protected String getAuditingHandlerBeanName() {
        return Constants.AUDITING_HANDLER_BEAN_NAME;
    }

    @Override
    protected void postProcess(BeanDefinitionBuilder builder, AuditingConfiguration configuration, BeanDefinitionRegistry registry) {
        final BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(CosmosMappingContext.class);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR);

        builder.setFactoryMethod("from").addConstructorArgValue(definition.getBeanDefinition());
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#
     * getAuditHandlerBeanDefinitionBuilder(org.springframework.data.auditing.config.AuditingConfiguration)
     */
    @Override
    protected BeanDefinitionBuilder getAuditHandlerBeanDefinitionBuilder(AuditingConfiguration configuration) {
        Assert.notNull(configuration, "AuditingConfiguration must not be null!");

        return configureDefaultAuditHandlerAttributes(configuration,
            BeanDefinitionBuilder.rootBeanDefinition(IsNewAwareAuditingHandler.class));
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.auditing.config.AuditingBeanDefinitionRegistrarSupport#
     * registerAuditListener(org.springframework.beans.factory.config.BeanDefinition,
     * org.springframework.beans.factory.support.BeanDefinitionRegistry)
     */
    @Override
    protected void registerAuditListenerBeanDefinition(BeanDefinition auditingHandlerDefinition,
                                                       BeanDefinitionRegistry registry) {
        // TODO: consider moving to event listener for auditing rather than injecting the
        // IsNewAwareAuditingHandler directly - this would require integrating CosmosTemplate with
        // the spring eventing system which would be a chunk of work beyond the scope of this PR
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
