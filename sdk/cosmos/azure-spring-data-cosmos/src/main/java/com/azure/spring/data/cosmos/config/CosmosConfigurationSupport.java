// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.config;

import com.azure.spring.data.cosmos.common.ExpressionResolver;
import com.azure.spring.data.cosmos.core.mapping.CosmosMappingContext;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A support class for cosmos configuration to scan beans and get initial entities
 */
public abstract class CosmosConfigurationSupport {

    /**
     * Return the name of the database to connect to
     *
     * @return must not be {@literal null}.
     */
    protected abstract String getDatabaseName();

    /**
     * Declare ExpressionResolver bean.
     * @param beanFactory used to initialize the embeddedValueResolver
     * @return ExpressionResolver bean
     */
    @Bean
    public ExpressionResolver expressionResolver(BeanFactory beanFactory) {
        return new ExpressionResolver(beanFactory);
    }

    /**
     * Declare CosmosMappingContext bean.
     * @return CosmosMappingContext bean
     * @throws ClassNotFoundException if the class type is invalid
     */
    @Bean
    public CosmosMappingContext cosmosMappingContext() throws ClassNotFoundException {
        final CosmosMappingContext mappingContext = new CosmosMappingContext();
        mappingContext.setInitialEntitySet(getInitialEntitySet());

        return mappingContext;
    }

    protected Collection<String> getMappingBasePackages() {
        final Package mappingBasePackage = getClass().getPackage();
        return Collections.singleton(mappingBasePackage == null ? null : mappingBasePackage.getName());
    }

    /**
     * Scan all base packages and get all beans
     * @return initial entity set
     * @throws ClassNotFoundException if the class type is invalid
     */
    protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
        final Set<Class<?>> initialEntitySet = new HashSet<>();

        for (final String basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(scanForEntities(basePackage));
        }

        return initialEntitySet;
    }

    /**
     * Scan all beans under the given base package
     * @param basePackage set the base location of beans
     * @return initial entity set for found beans
     * @throws ClassNotFoundException if the class type is invalid
     */
    protected Set<Class<?>> scanForEntities(String basePackage) throws ClassNotFoundException {
        if (!StringUtils.hasText(basePackage)) {
            return Collections.emptySet();
        }

        final Set<Class<?>> initialEntitySet = new HashSet<>();

        if (StringUtils.hasText(basePackage)) {
            final ClassPathScanningCandidateComponentProvider componentProvider =
                    new ClassPathScanningCandidateComponentProvider(false);

            componentProvider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

            for (final BeanDefinition candidate : componentProvider.findCandidateComponents(basePackage)) {
                final String className = candidate.getBeanClassName();
                Assert.notNull(className, "Bean class name is null.");

                initialEntitySet
                        .add(ClassUtils.forName(className, CosmosConfigurationSupport.class.getClassLoader()));
            }
        }

        return initialEntitySet;
    }
}
