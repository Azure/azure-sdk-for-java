// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.data.cosmosdb.repository.config;

import com.microsoft.azure.spring.data.cosmosdb.Constants;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.ReactiveCosmosRepositoryFactoryBean;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.config.DefaultRepositoryBaseClass;
import org.springframework.data.repository.query.QueryLookupStrategy;

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
@Import(ReactiveCosmosRepositoriesRegistrar.class)
public @interface EnableReactiveCosmosRepositories {

    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Filter[] includeFilters() default {};

    Filter[] excludeFilters() default {};

    String repositoryImplementationPostfix() default Constants.DEFAULT_REPOSITORY_IMPLEMENT_POSTFIX;

    String namedQueriesLocation() default "";

    QueryLookupStrategy.Key queryLookupStrategy() default QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND;

    Class<?> repositoryFactoryBeanClass() default ReactiveCosmosRepositoryFactoryBean.class;

    Class<?> repositoryBaseClass() default DefaultRepositoryBaseClass.class;

    boolean considerNestedRepositories() default false;
}

