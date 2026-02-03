// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.annotations;

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
@Target({ TYPE, METHOD, CONSTRUCTOR, FIELD })
public @interface Metadata {
    /**
     * The properties that apply to the annotated class.
     *
     * @return The properties that apply to the annotated class.
     */
    MetadataProperties[] properties() default { };
}
