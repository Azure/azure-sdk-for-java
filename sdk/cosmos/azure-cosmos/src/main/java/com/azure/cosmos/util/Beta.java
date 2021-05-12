// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ TYPE, METHOD, PARAMETER, CONSTRUCTOR })
@Inherited
/**
 * Indicates functionality that is in preview and as such is subject to change in non-backwards compatible ways in future releases,
 * including removal, regardless of any compatibility expectations set by the containing library version.
 */
public @interface Beta {

    String PREVIEW_SUBJECT_TO_CHANGE_WARNING = "Preview API - subject to change in non-backwards compatible way";

    String warningText() default PREVIEW_SUBJECT_TO_CHANGE_WARNING;

    /**
     * @return the version number when the annotated API was first introduced to the library as in Beta
     */
    SinceVersion value() default SinceVersion.V4_5_0;

    /**
     * Azure library version numbers
     */
    public enum SinceVersion {
        /** v4.3.0 */
        V4_3_0,
        /** v4.4.0 */
        V4_4_0,
        /** v4.5.0 */
        V4_5_0,
        /** v4.5.1 */
        V4_5_1,
        /** v4.6.0 */
        V4_6_0,
        /** v4.7.0 */
        V4_7_0,
        /** v4.8.0 */
        V4_8_0,
        /** v4.9.0 */
        V4_9_0,
        /** v4.11.0 */
        V4_11_0,
        /** v4.12.0 */
        V4_12_0,
        /** v4.13.0 */
        V4_13_0,
        /** v4.14.0 */
        V4_14_0,
        /** v4.15.0 */
        V4_15_0;
    }
}
