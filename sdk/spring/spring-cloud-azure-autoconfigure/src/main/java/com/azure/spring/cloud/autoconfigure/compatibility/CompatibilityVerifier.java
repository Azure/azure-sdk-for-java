// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

/**
 * interface of CompatibilityVerifier
 */
public interface CompatibilityVerifier {

    /**
     * @return VerificationResult
     */
    VerificationResult verify();
}
