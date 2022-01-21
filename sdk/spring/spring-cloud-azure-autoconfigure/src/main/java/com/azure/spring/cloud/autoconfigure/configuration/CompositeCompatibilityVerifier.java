package com.azure.spring.cloud.autoconfigure.configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class CompositeCompatibilityVerifier {
    private static final Log log = LogFactory.getLog(CompositeCompatibilityVerifier.class);
    private final List<CompatibilityVerifier> verifiers;

    CompositeCompatibilityVerifier(List<CompatibilityVerifier> verifiers) {
        this.verifiers = verifiers;
    }

    void verifyDependencies() {
        List<VerificationResult> errors = this.verifierErrors();
        if (errors.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("All conditions are passing");
            }

        } else {
            throw new CompatibilityNotMetException(errors);
        }
    }

    private List<VerificationResult> verifierErrors() {
        List<VerificationResult> errors = new ArrayList();
        Iterator var2 = this.verifiers.iterator();

        while(var2.hasNext()) {
            CompatibilityVerifier verifier = (CompatibilityVerifier)var2.next();
            VerificationResult result = verifier.verify();
            if (result.isNotCompatible()) {
                errors.add(result);
            }
        }

        return errors;
    }
}
