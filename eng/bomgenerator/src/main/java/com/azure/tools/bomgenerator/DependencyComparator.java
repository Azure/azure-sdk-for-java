// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator;

import org.apache.maven.model.Dependency;

import java.util.Comparator;

public class DependencyComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        Dependency dependency1 = (Dependency) o1;
        Dependency dependency2 = (Dependency) o2;

        return dependency1.toString().compareTo(dependency2.toString());
    }
}
