// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.logic.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.logic.models.SegmentTerminatorSuffix;
import com.azure.resourcemanager.logic.models.X12DelimiterOverrides;
import org.junit.jupiter.api.Assertions;

public final class X12DelimiterOverridesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        X12DelimiterOverrides model =
            BinaryData
                .fromString(
                    "{\"protocolVersion\":\"h\",\"messageId\":\"qedcgzulwm\",\"dataElementSeparator\":874924204,\"componentSeparator\":652058818,\"segmentTerminator\":201174669,\"segmentTerminatorSuffix\":\"None\",\"replaceCharacter\":401684399,\"replaceSeparatorsInPayload\":true,\"targetNamespace\":\"vpglydz\"}")
                .toObject(X12DelimiterOverrides.class);
        Assertions.assertEquals("h", model.protocolVersion());
        Assertions.assertEquals("qedcgzulwm", model.messageId());
        Assertions.assertEquals(874924204, model.dataElementSeparator());
        Assertions.assertEquals(652058818, model.componentSeparator());
        Assertions.assertEquals(201174669, model.segmentTerminator());
        Assertions.assertEquals(SegmentTerminatorSuffix.NONE, model.segmentTerminatorSuffix());
        Assertions.assertEquals(401684399, model.replaceCharacter());
        Assertions.assertEquals(true, model.replaceSeparatorsInPayload());
        Assertions.assertEquals("vpglydz", model.targetNamespace());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        X12DelimiterOverrides model =
            new X12DelimiterOverrides()
                .withProtocolVersion("h")
                .withMessageId("qedcgzulwm")
                .withDataElementSeparator(874924204)
                .withComponentSeparator(652058818)
                .withSegmentTerminator(201174669)
                .withSegmentTerminatorSuffix(SegmentTerminatorSuffix.NONE)
                .withReplaceCharacter(401684399)
                .withReplaceSeparatorsInPayload(true)
                .withTargetNamespace("vpglydz");
        model = BinaryData.fromObject(model).toObject(X12DelimiterOverrides.class);
        Assertions.assertEquals("h", model.protocolVersion());
        Assertions.assertEquals("qedcgzulwm", model.messageId());
        Assertions.assertEquals(874924204, model.dataElementSeparator());
        Assertions.assertEquals(652058818, model.componentSeparator());
        Assertions.assertEquals(201174669, model.segmentTerminator());
        Assertions.assertEquals(SegmentTerminatorSuffix.NONE, model.segmentTerminatorSuffix());
        Assertions.assertEquals(401684399, model.replaceCharacter());
        Assertions.assertEquals(true, model.replaceSeparatorsInPayload());
        Assertions.assertEquals("vpglydz", model.targetNamespace());
    }
}
