// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator.models;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BomDependencyErrorInfo {
    private BomDependency expectedDependency;
    private Set<ConflictingDependency> conflictingDependencies;

    public BomDependencyErrorInfo(BomDependency expectedDependency) {
        this.expectedDependency = expectedDependency;
        conflictingDependencies = new HashSet<>();
    }

    public void addConflictingDependency(BomDependency actualDependency, BomDependency expectedDependency) {
        conflictingDependencies.add(new ConflictingDependency(actualDependency, expectedDependency));
    }

    public Set<ConflictingDependency> getConflictingDependencies() {
        return Collections.unmodifiableSet(this.conflictingDependencies);
    }

    public BomDependency getExpectedDependency() {
        return this.expectedDependency;
    }
}


