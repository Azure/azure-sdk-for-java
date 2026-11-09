/*
 * Copyright (C) 2008 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.guava25.errorprone.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Indicates that the return value of the annotated API is ignorable.
 *
 * <p>This is the opposite of {@link CheckReturnValue}. It can be used inside classes or packages
 * annotated with {@code @CheckReturnValue} to exempt specific APIs from the default.
 */
@Documented
// Note: annotating a type with @CanIgnoreReturnValue is discouraged (and banned inside of Google)
@Target({METHOD, CONSTRUCTOR, TYPE})
@Retention(CLASS)
public @interface CanIgnoreReturnValue {}
