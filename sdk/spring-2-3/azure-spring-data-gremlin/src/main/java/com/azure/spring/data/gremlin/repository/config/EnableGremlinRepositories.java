// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.repository.config;

import com.azure.spring.data.gremlin.common.Constants;
import com.azure.spring.data.gremlin.repository.support.GremlinRepositoryFactoryBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(GremlinRepositoryRegistrar.class)
public @interface EnableGremlinRepositories {

    /**
     * @return Alias for basePackages().
     */
    String[] value() default {};

    /**
     * @return Base packages to scan for components with annotations.
     */
    String[] basePackages() default {};

    /**
     * @return Type-safe alternative to basePackages() for specifying the packages to scan. The package of each class
     * specified will be scanned.
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * @return Types are eligible for component scanning.
     */
    Filter[] includeFilters() default {};

    /**
     * @return Types are not eligible for component scanning.
     */
    Filter[] excludeFilters() default {};

    /**
     * @return Specifics the postfix to be used for custom repository implementation class name.
     */
    String repositoryImplementationPostfix() default Constants.DEFAULT_REPOSITORY_IMPLEMENT_POSTFIX;

    /**
     * @return Configures the repository base class to be used to create repository.
     */
    Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

    /**
     * @return Configures whether nested repository interface.
     */
    boolean considerNestedRepositories() default false;

    /**
     * @return Configure the class of repository factory bean.
     */
    Class<?> repositoryFactoryBeanClass() default GremlinRepositoryFactoryBean.class;

    /**
     * @return Specific the namedQuery location.
     */
    String namedQueriesLocation() default "";
}

