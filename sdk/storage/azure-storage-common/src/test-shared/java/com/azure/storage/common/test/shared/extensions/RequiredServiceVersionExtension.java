// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.storage.common.test.shared.TestEnvironment;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.lang.reflect.InvocationTargetException;

public class RequiredServiceVersionExtension implements IAnnotationDrivenExtension<RequiredServiceVersion> {

    @Override
    public void visitFeatureAnnotation(RequiredServiceVersion annotation, FeatureInfo feature) {
        Enum targetServiceVersion = getTargetServiceVersion(annotation.clazz());
        String minServiceVersion = annotation.min();
        if (shouldSkip(targetServiceVersion, minServiceVersion, annotation.clazz())) {
            feature.skip(String.format("Test ignored to run with %s service version", targetServiceVersion));
        }
    }

    @Override
    public void visitSpecAnnotation(RequiredServiceVersion annotation, SpecInfo spec) {
        Enum targetServiceVersion = getTargetServiceVersion(annotation.clazz());
        String minServiceVersion = annotation.min();
        if (shouldSkip(targetServiceVersion, minServiceVersion, annotation.clazz())) {
            spec.skip(String.format("Test ignored to run with %s service version", targetServiceVersion));
        }
    }

    private Enum getTargetServiceVersion(Class clazz) {
        String targetServiceVersionFromEnvironment = TestEnvironment.getInstance().getServiceVersion();
        if (targetServiceVersionFromEnvironment != null) {
            // Use environment defined version first.
            return Enum.valueOf(clazz, targetServiceVersionFromEnvironment);
        } else {
            // Fall back to "latest" service version otherwise.
            try {
                return (Enum) clazz.getMethod("getLatest").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean shouldSkip(Enum targetServiceVersion, String minServiceVersion, Class clazz) {
        int targetOrdinal = targetServiceVersion.ordinal();
        int minOrdinal = Enum.valueOf(clazz, minServiceVersion).ordinal();
        return targetOrdinal < minOrdinal;
    }
}
