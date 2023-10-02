// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.annotation;

import com.typespec.core.test.SyncAsyncExtension;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;

/**
 * Indicates that test should be branched out into sync and async branch resulting in two separate test cases.
 * The {@link com.typespec.core.test.SyncAsyncExtension#execute(Callable, Callable)} should be used in the test
 * to branch out.
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(SyncAsyncExtension.class)
public @interface SyncAsyncTest {
}
