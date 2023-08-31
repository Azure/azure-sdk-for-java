// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * This package contains implementation details that perform reflective operations.
 * <p>
 * The purpose of the package is to contain implementation-agnostic ways to invoke APIs reflectively. There are two
 * implementations, one that uses {@code java.lang.reflect} and one that uses {@code java.lang.invoke}. The default is
 * to use {@link java.lang.invoke} if it is available, otherwise it will fall back to {@code java.lang.reflect}, as
 * {@code java.lang.invoke} is newer and provides better optimizations.
 */
package com.azure.core.implementation.reflection;
