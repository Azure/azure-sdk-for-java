// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Annotation that defines the characteristics of a class.
 */
@Retention(SOURCE)
@Target({TYPE, METHOD, CONSTRUCTOR, FIELD})
public @interface Metadata {
    /**
     * The conditions that apply to the annotated class.
     *
     * @return The conditions that apply to the annotated class.
     */
    TypeConditions[] conditions() default {};

    /**
     * Indicates whether the class was automatically generated.
     *
     * @return {@code true} if the class is automatically generated, {@code false} otherwise.
     */
    boolean generated() default false;
}
