// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

/**
 * Annotation to indicate that a functionality is in preview.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ TYPE, METHOD, PARAMETER, CONSTRUCTOR })
@Inherited
/**
 * Indicates functionality that is in preview and as such is subject to change in non-backwards compatible ways in
 * future releases, including removal, regardless of any compatibility expectations set by the containing library
 * version.
 *
 *  Examples:
 *
 *  {@literal @}Beta
 *  {@literal @}Beta(since="v1.0.0")
 *  {@literal @}Beta(since="v1.2.0", reason="the feature is in preview")
 *  {@literal @}Beta("introducing Foo which eventually replaces Bar")
 */
public @interface Beta {
    /**
     * @return the free-form value for the annotation (used if details cannot be provided using since and reason
     *     attributes).
     */
    String value() default "";

    /**
     * @return the version number indicating when the annotated target was first introduced to the library as in beta.
     */
    String since() default "";

    /**
     * @return the reason for annotating the target as beta.
     */
    String reason() default "";

    /**
     * @return the warning message.
     */
    String warningText() default "This functionality is in preview and as such is subject to change in non-backwards "
        + "compatible ways in future releases, including removal, regardless of any compatibility expectations set by"
        + " the containing library version.";
}
