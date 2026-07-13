// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit.models;

import com.azure.ai.voicelive.models.EouDetection;
import com.azure.ai.voicelive.models.EouDetectionModel;
import com.azure.ai.voicelive.models.EouThresholdLevel;
import com.azure.ai.voicelive.models.SmartEndOfTurnDetection;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SmartEndOfTurnDetection} and the {@link EouDetectionModel#SMART_END_OF_TURN_DETECTION}
 * polymorphic discriminator on {@link EouDetection}.
 */
class SmartEndOfTurnDetectionTest {

    @Test
    void testSmartEndOfTurnDetectionRoundTrip() {
        SmartEndOfTurnDetection eou
            = new SmartEndOfTurnDetection().setThresholdLevel(EouThresholdLevel.HIGH).setTimeoutMs(1500);

        SmartEndOfTurnDetection deserialized = BinaryData.fromObject(eou).toObject(SmartEndOfTurnDetection.class);

        assertEquals(EouDetectionModel.SMART_END_OF_TURN_DETECTION, deserialized.getModel());
        assertEquals(EouThresholdLevel.HIGH, deserialized.getThresholdLevel());
        assertEquals(1500, deserialized.getTimeoutMs());
    }

    @Test
    void testSmartEndOfTurnDetectionPolymorphic() {
        String json = "{\"model\":\"smart_end_of_turn_detection\",\"threshold_level\":\"medium\",\"timeout_ms\":800}";

        EouDetection eou = BinaryData.fromString(json).toObject(EouDetection.class);

        assertTrue(eou instanceof SmartEndOfTurnDetection, "Expected SmartEndOfTurnDetection, got " + eou.getClass());
        SmartEndOfTurnDetection smart = (SmartEndOfTurnDetection) eou;
        assertEquals(EouThresholdLevel.MEDIUM, smart.getThresholdLevel());
        assertEquals(800, smart.getTimeoutMs());
    }
}
