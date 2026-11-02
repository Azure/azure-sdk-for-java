// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

/**
 * Exercises public reference cleanup behavior with the packaged direct Cleaner implementation.
 */
@EnabledForJreRange(min = JRE.JAVA_11)
public class ReferenceManagerTestsIT extends ReferenceManagerTests {
}
