// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.util;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ TYPE, METHOD, PARAMETER, CONSTRUCTOR })
@Inherited
/**
 * Indicates functionality that is in preview and as such is subject to change in non-backwards compatible ways in future releases,
 * including removal, regardless of any compatibility expectations set by the containing library version.
 */
public @interface Beta {
    String warningText() default "This functionality is in preview and as such is subject to change in non-backwards compatible ways in future releases, including removal, regardless of any compatibility expectations set by the containing library version.";

    /**
     * @return the version number when the annotated API was first introduced to the library as in Beta
     */
    SinceVersion value() default SinceVersion.V4_2_0;

    /**
     * Azure library version numbers
     */
    public enum SinceVersion {
        /** v4.1.0 */
        V4_1_0,

        /** v4.2.0 */
        V4_2_0,

        /** v4.3.0 */
        V4_3_0,

        /** v4.4.0 */
        V4_4_0,

        /** v4.5.0 */
        V4_5_0,

        /** v4.6.0 */
        V4_6_0,

        /** v4.7.0 */
        V4_7_0,

        /** v4.8.0 */
        V4_8_0,

        /** v4.9.0 */
        V4_9_0,

        /** v4.10.0 */
        V4_10_0,

        /** v4.11.0 */
        V4_11_0,

        /** v4.12.0 */
        V4_12_0,

        /** v4.13.0 */
        V4_13_0,

        /** v4.14.0 */
        V4_14_0,

        /** v4.15.0 */
        V4_15_0,

        /** v4.16.0 */
        V4_16_0,

        /** v4.17.0 */
        V4_17_0,

        /** v4.18.0 */
        V4_18_0,

        /** v4.19.0 */
        V4_19_0,

        /** v4.20.0 */
        V4_20_0,

        /** v5.0.0 */
        V5_0_0
    }
}