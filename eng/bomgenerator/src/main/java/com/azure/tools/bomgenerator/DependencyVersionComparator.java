// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.bomgenerator;

import org.apache.maven.artifact.versioning.ComparableVersion;

import java.util.Comparator;

public class DependencyVersionComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        ComparableVersion version1 = new ComparableVersion(o1);
        ComparableVersion version2 = new ComparableVersion(o2);

        return version1.compareTo(version2);
    }
}
