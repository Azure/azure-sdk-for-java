// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.core.util.ServiceVersion;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark tests that require a specific service version to run.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@ExtendWith(RequiredServiceVersionExtension.class)
public @interface RequiredServiceVersion {
    /**
     * The service version enum that the test requires.
     *
     * @return The service version enum that the test requires.
     */
    Class<? extends Enum<? extends ServiceVersion>> clazz();

    /**
     * The minimum service version that the test requires.
     *
     * @return The minimum service version that the test requires.
     */
    String min();
}
