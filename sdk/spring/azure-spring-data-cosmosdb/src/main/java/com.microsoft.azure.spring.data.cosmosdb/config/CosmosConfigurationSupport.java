// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.config;

import com.microsoft.azure.spring.data.cosmosdb.common.ExpressionResolver;
import com.microsoft.azure.spring.data.cosmosdb.core.mapping.CosmosMappingContext;
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

public abstract class CosmosConfigurationSupport {

    @Bean
    public ExpressionResolver expressionResolver(BeanFactory beanFactory) {
        return new ExpressionResolver(beanFactory);
    }

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

    protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
        final Set<Class<?>> initialEntitySet = new HashSet<>();

        for (final String basePackage : getMappingBasePackages()) {
            initialEntitySet.addAll(scanForEntities(basePackage));
        }

        return initialEntitySet;
    }

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
