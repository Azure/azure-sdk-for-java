// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.utils;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;

/**
 * Indicates functionality that is in preview and as such is subject to change in non-backwards compatible ways in
 * future releases, including removal, regardless of any compatibility expectations set by the containing library
 * version.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ TYPE, METHOD, PARAMETER, CONSTRUCTOR, FIELD })
@Inherited
public @interface Beta {

    /**
     * Default warning text.
     */
    String PREVIEW_SUBJECT_TO_CHANGE_WARNING = "Preview API - subject to change in non-backwards compatible way";

    /**
     * Gets the warning text describing the preview status of the annotated API.
     *
     * @return the warning text.
     */
    String warningText() default PREVIEW_SUBJECT_TO_CHANGE_WARNING;
}
