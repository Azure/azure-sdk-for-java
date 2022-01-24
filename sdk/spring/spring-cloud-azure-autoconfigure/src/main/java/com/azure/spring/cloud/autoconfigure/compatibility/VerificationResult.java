// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.compatibility;

import java.util.Objects;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.StringUtils;

final class VerificationResult {
    final String description;
    final String action;

    private VerificationResult() {
        this.description = "";
        this.action = "";
    }

    private VerificationResult(String errorDescription, String action) {
        this.description = errorDescription;
        this.action = action;
    }

    static VerificationResult compatible() {
        return new VerificationResult();
    }

    static VerificationResult notCompatible(String errorDescription, String action) {
        return new VerificationResult(errorDescription, action);
    }

    boolean isNotCompatible() {
        return StringUtils.hasText(this.description) || StringUtils.hasText(this.action);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof VerificationResult)) {
            return false;
        } else {
            VerificationResult that = (VerificationResult) o;
            return this.description.equals(that.description) && this.action.equals(that.action);
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.description, this.action});
    }

    public String toString() {
        ToStringCreator toStringCreator = new ToStringCreator(this);
        toStringCreator.append("description", this.description);
        toStringCreator.append("action", this.action);
        return toStringCreator.toString();
    }
}
