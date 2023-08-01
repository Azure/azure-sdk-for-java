// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.extensions;

import com.azure.core.test.TestMode;
import com.azure.storage.common.test.shared.TestEnvironment;
import org.spockframework.runtime.extension.IAnnotationDrivenExtension;
import org.spockframework.runtime.model.FeatureInfo;
import org.spockframework.runtime.model.SpecInfo;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
        OffsetDateTime expiry = LocalDate.parse(expiryStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(0, 0)
            .atZone(ZoneId.of(ZoneId.SHORT_IDS.get("PST"))).toOffsetDateTime();
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get("PST")));
        if (now.isAfter(expiry)) {
            throw new RuntimeException("PlaybackOnly has expired. Test must be reenabled");
        }
    }
}
