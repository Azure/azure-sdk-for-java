// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator.models;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class BomDependencyErrorInfo {
    private BomDependency dependencyWithConflict;
    private Set<ConflictingDependency> conflictingDependencies;

    public BomDependencyErrorInfo(BomDependency dependencyWithConflict) {
        this.dependencyWithConflict = dependencyWithConflict;
        conflictingDependencies = new HashSet<>();
    }

    public void addConflictingDependency(BomDependency actualDependency, BomDependency dependencyWithConflict) {
        conflictingDependencies.add(new ConflictingDependency(actualDependency, dependencyWithConflict));
    }

    public Set<ConflictingDependency> getConflictingDependencies() {
        return Collections.unmodifiableSet(this.conflictingDependencies);
    }

    public BomDependency getDependencyWithConflict() {
        return this.dependencyWithConflict;
    }
}


