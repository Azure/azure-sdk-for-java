// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.util;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ TYPE, METHOD, PARAMETER, CONSTRUCTOR, FIELD })
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
    SinceVersion value() default SinceVersion.V1;

    /**
     * Azure library version numbers
     */
    enum SinceVersion {
        /** v1 */
        V1
    }
}
