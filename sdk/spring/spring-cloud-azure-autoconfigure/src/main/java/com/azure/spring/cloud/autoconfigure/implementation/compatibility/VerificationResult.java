// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

import java.io.Serializable;
import java.util.Objects;
import org.springframework.core.style.ToStringCreator;

final class VerificationResult implements Serializable {
    private static final long serialVersionUID = 7175132562403990299L;
    private final String description;
    private final String action;

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

    String getDescription() {
        return description;
    }

    String getAction() {
        return action;
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
        return Objects.hash(this.description, this.action);
    }

    public String toString() {
        ToStringCreator toStringCreator = new ToStringCreator(this);
        toStringCreator.append("description", this.description);
        toStringCreator.append("action", this.action);
        return toStringCreator.toString();
    }
}
