// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.config;

import com.azure.spring.data.gremlin.mapping.GremlinMappingContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.annotation.Persistent;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class GremlinConfigurationSupport {

    protected Collection<String> getMappingBasePackages() {
        final Package basePackage = this.getClass().getPackage();

        return Collections.singleton(basePackage == null ? null : basePackage.getName());
    }

    /**
     * Scan entities based on the basePackage provided.
     *
     * @param basePackage The base package in which to scan entities.
     * @return A set of entity classes.
     * @throws ClassNotFoundException If entity class could not be found.
     */
    protected Set<Class<?>> scanEntities(@NonNull String basePackage) throws ClassNotFoundException {
        if (!StringUtils.hasText(basePackage)) {
            return Collections.emptySet();
        }

        final Set<Class<?>> entitySet = new HashSet<>();
        final ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);

        provider.addIncludeFilter(new AnnotationTypeFilter(Persistent.class));

        for (final BeanDefinition candidate : provider.findCandidateComponents(basePackage)) {
            final String className = candidate.getBeanClassName();
            Assert.notNull(GremlinConfigurationSupport.class.getClassLoader(), "Class loader cannot be null");

            entitySet.add(ClassUtils.forName(className, GremlinConfigurationSupport.class.getClassLoader()));
        }

        return entitySet;
    }

    /**
     * Initialize entity set based on the package.
     *
     * @return The initial entity set.
     * @throws ClassNotFoundException If ClassNotFoundException has occurred.
     */
    protected Set<Class<?>> getInitialEntitySet() throws ClassNotFoundException {
        final Set<Class<?>> entitySet = new HashSet<>();

        for (final String basePackage : this.getMappingBasePackages()) {
            entitySet.addAll(this.scanEntities(basePackage));
        }

        return entitySet;
    }

    /**
     * Create the {@link GremlinMappingContext} bean.
     *
     * @return The {@link GremlinMappingContext} bean.
     * @throws ClassNotFoundException If ClassNotFoundException has occurred.
     */
    @Bean
    public GremlinMappingContext gremlinMappingContext() throws ClassNotFoundException {
        final GremlinMappingContext context = new GremlinMappingContext();

        context.setInitialEntitySet(this.getInitialEntitySet());

        return context;
    }

}

