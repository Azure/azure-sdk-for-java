// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.compatibility;

import java.io.Serializable;
import java.util.Objects;
import org.springframework.core.style.ToStringCreator;

public final class VerificationResult implements Serializable {
    private static final long serialVersionUID = 7175132562403990299L;
    public final String description;
    public final String action;

    private VerificationResult() {
        this.description = "";
        this.action = "";
    }

    private VerificationResult(String errorDescription, String action) {
        this.description = errorDescription;
        this.action = action;
    }

    public static VerificationResult compatible() {
        return new VerificationResult();
    }

    public static VerificationResult notCompatible(String errorDescription, String action) {
        return new VerificationResult(errorDescription, action);
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
