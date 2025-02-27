// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.botservice.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.botservice.models.AlexaChannelProperties;
import org.junit.jupiter.api.Assertions;

public final class AlexaChannelPropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AlexaChannelProperties model = BinaryData.fromString(
            "{\"alexaSkillId\":\"azqugxywpmueefj\",\"urlFragment\":\"fqkquj\",\"serviceEndpointUri\":\"suyonobglaocq\",\"isEnabled\":false}")
            .toObject(AlexaChannelProperties.class);
        Assertions.assertEquals("azqugxywpmueefj", model.alexaSkillId());
        Assertions.assertEquals(false, model.isEnabled());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AlexaChannelProperties model
            = new AlexaChannelProperties().withAlexaSkillId("azqugxywpmueefj").withIsEnabled(false);
        model = BinaryData.fromObject(model).toObject(AlexaChannelProperties.class);
        Assertions.assertEquals("azqugxywpmueefj", model.alexaSkillId());
        Assertions.assertEquals(false, model.isEnabled());
    }
}
