// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator.models;

public class ConflictingDependency {
    private BomDependency expectedDependency;
    private BomDependency actualDependency;

    ConflictingDependency(BomDependency actualDependency, BomDependency expectedDependency) {
        this.expectedDependency = expectedDependency;
        this.actualDependency = actualDependency;
    }

    public BomDependency getExpectedDependency() {
        return this.expectedDependency;
    }

    public BomDependency getActualDependency() {
        return this.actualDependency;
    }

    @Override
    public String toString() {
        return this.actualDependency.toString() + this.expectedDependency.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }

        if (o instanceof ConflictingDependency) {
            ConflictingDependency bomSource = this;
            ConflictingDependency bomTarget = (ConflictingDependency) o;

            return bomSource.toString().equals(bomTarget.toString());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
