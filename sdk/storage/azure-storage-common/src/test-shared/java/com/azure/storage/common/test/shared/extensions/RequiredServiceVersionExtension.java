// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.storage.common.test.shared.TestEnvironment;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

public class RequiredServiceVersionExtension implements IAnnotationDrivenExtension<RequiredServiceVersion> {

    @Override
    public void visitFeatureAnnotation(RequiredServiceVersion annotation, FeatureInfo feature) {
        String targetServiceVersion = TestEnvironment.getInstance().getServiceVersion();
        String minServiceVersion = annotation.min();
        if (shouldSkip(targetServiceVersion, minServiceVersion, annotation.clazz())) {
            feature.skip(String.format("Test ignored to run with %s service version", targetServiceVersion));
        }
    }

    @Override
    public void visitSpecAnnotation(RequiredServiceVersion annotation, SpecInfo spec) {
        String targetServiceVersion = TestEnvironment.getInstance().getServiceVersion();
        String minServiceVersion = annotation.min();
        if (shouldSkip(targetServiceVersion, minServiceVersion, annotation.clazz())) {
            spec.skip(String.format("Test ignored to run with %s service version", targetServiceVersion));
        }
    }

    private boolean shouldSkip(String targetServiceVersion, String minServiceVersion, Class clazz) {
        if (targetServiceVersion == null) {
            return false;
        }

        int targetOrdinal = Enum.valueOf(clazz, targetServiceVersion).ordinal();
        int minOrdinal = Enum.valueOf(clazz, minServiceVersion).ordinal();
        return targetOrdinal < minOrdinal;
    }
}
