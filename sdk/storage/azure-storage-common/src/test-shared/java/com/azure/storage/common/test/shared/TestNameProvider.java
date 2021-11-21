// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.IterationInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class TestNameProvider {
    public static String getTestName(IterationInfo iterationInfo) {
        FeatureInfo featureInfo = iterationInfo.getFeature();
        SpecInfo specInfo = featureInfo.getSpec();

        String fullName = specInfo.getName() + Arrays.stream(featureInfo.getName().split(" "))
            .map(it -> it.substring(0, 1).toUpperCase() + it.substring(1))
            .collect(Collectors.joining(""));

        return (iterationInfo.getDataValues().length == 0)
            ? fullName
            : String.format("%s[%d]", fullName, iterationInfo.getIterationIndex());
    }
}
