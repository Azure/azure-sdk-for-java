// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

class CompositeCompatibilityVerifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeCompatibilityVerifier.class);
    private final List<CompatibilityVerifier> verifiers;

    CompositeCompatibilityVerifier(List<CompatibilityVerifier> verifiers) {
        this.verifiers = verifiers;
    }

    void verifyDependencies() {
        List<VerificationResult> errors = this.verifierErrors();
        if (errors.isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("All conditions are passed");
            }
        } else {
            throw new CompatibilityNotMetException(errors);
        }
    }

    private List<VerificationResult> verifierErrors() {
        List<VerificationResult> errors = new ArrayList<VerificationResult>();
        for (CompatibilityVerifier verifier : verifiers) {
            VerificationResult result = verifier.verify();
            if (result.isNotCompatible()) {
                errors.add(result);
            }
        }
        return errors;
    }
}
