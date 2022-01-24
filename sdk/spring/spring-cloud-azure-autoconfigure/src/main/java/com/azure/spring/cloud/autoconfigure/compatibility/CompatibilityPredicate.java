// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

/**
 * CompatibilityPredicate for spring-boot version check
 */
public interface CompatibilityPredicate {

    /**
     * Compatible of the current spring-boot version
     * @return the version supported or not
     */
    boolean isCompatible();
}
