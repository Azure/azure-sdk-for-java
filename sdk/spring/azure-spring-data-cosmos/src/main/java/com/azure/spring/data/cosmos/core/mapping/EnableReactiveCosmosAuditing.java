// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.core.mapping;

import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.ReactiveAuditorAware;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to enable reactive auditing for Spring Data Cosmos.
 * <p>
 * Use this annotation when working with reactive repositories and {@link ReactiveAuditorAware}.
 * For imperative auditing, use {@link EnableCosmosAuditing} instead.
 * Both annotations can coexist in the same application context.
 *
 * Adapted from spring-data-mongodb
 * @see <a href="https://github.com/spring-projects/spring-data-mongodb/blob/main/spring-data-mongodb/src/main/java/org/springframework/data/mongodb/config/EnableReactiveMongoAuditing.java">EnableReactiveMongoAuditing.java</a>
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(ReactiveCosmosAuditingRegistrar.class)
public @interface EnableReactiveCosmosAuditing {

    /**
     * Configures the {@link ReactiveAuditorAware} bean to be used to lookup the current principal.
     *
     * @return default empty string
     */
    String auditorAwareRef() default "";

    /**
     * Configures whether the creation and modification dates are set. Defaults to {@literal true}.
     *
     * @return default true
     */
    boolean setDates() default true;

    /**
     * Configures whether the entity shall be marked as modified on creation. Defaults to {@literal true}.
     *
     * @return default true
     */
    boolean modifyOnCreate() default true;

    /**
     * Configures a {@link DateTimeProvider} bean name that allows customizing the DateTime to be
     * used for setting creation and modification dates.
     *
     * @return default empty string
     */
    String dateTimeProviderRef() default "";
}
