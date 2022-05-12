// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.core.test.TestMode;
import com.azure.storage.common.test.shared.TestEnvironment;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.time.OffsetDateTime;

public class PlaybackOnlyUntilExtension implements IAnnotationDrivenExtension<PlaybackOnlyUntil> {
    @Override
    public void visitFeatureAnnotation(PlaybackOnlyUntil annotation, FeatureInfo feature) {
        validateExpiryTime(annotation);

        TestMode testMode = TestEnvironment.getInstance().getTestMode();
        if (testMode != TestMode.PLAYBACK) {
            feature.skip(String.format("Test ignored in %s mode", testMode));
        }
    }

    @Override
    public void visitSpecAnnotation(PlaybackOnlyUntil annotation, SpecInfo spec) {
        validateExpiryTime(annotation);

        TestMode testMode = TestEnvironment.getInstance().getTestMode();
        if (testMode != TestMode.PLAYBACK) {
            spec.skip(String.format("Test ignored in %s mode", testMode));
        }
    }

    private void validateExpiryTime(PlaybackOnlyUntil annotation) {
        String expiryStr = annotation.expiryTime();
        OffsetDateTime now = OffsetDateTime.now();
        String nowStr = now.getYear() + "/" + String.format("%02d",now.getMonthValue()) + "/"
            + String.format("%02d",now.getDayOfMonth());
        if (expiryStr.compareTo(nowStr) < 0) {
            throw new RuntimeException("PlaybackOnly has expired. Test must be reenabled");
        }
    }
}
