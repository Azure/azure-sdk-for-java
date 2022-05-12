// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.core.test.TestMode;
import com.azure.storage.common.test.shared.TestEnvironment;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public class PlaybackOnlyExtension implements IAnnotationDrivenExtension<PlaybackOnly> {

    @Override
    public void visitFeatureAnnotation(PlaybackOnly annotation, FeatureInfo feature) {
        validateExpiryTime(annotation);

        TestMode testMode = TestEnvironment.getInstance().getTestMode();
        if (testMode != TestMode.PLAYBACK) {
            feature.skip(String.format("Test ignored in %s mode", testMode));
        }
    }

    @Override
    public void visitSpecAnnotation(PlaybackOnly annotation, SpecInfo spec) {
        validateExpiryTime(annotation);

        TestMode testMode = TestEnvironment.getInstance().getTestMode();
        if (testMode != TestMode.PLAYBACK) {
            spec.skip(String.format("Test ignored in %s mode", testMode));
        }
    }

    private void validateExpiryTime(PlaybackOnly annotation) {
        String expiryStr = annotation.expiryTime();
        if ("".equals(expiryStr)) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST")));
        String nowStr = now.getYear() + "/" + String.format("%02d",now.getMonthValue()) + "/"
            + String.format("%02d",now.getDayOfMonth());
        if (expiryStr.compareTo(nowStr) < 0) {
            throw new RuntimeException("PlaybackOnly has expired. Test must be reenabled");
        }
    }
}
