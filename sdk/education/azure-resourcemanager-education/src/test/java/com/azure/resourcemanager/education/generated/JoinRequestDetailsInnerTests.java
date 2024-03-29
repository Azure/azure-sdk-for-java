// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.education.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.education.fluent.models.JoinRequestDetailsInner;
import com.azure.resourcemanager.education.models.JoinRequestStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class JoinRequestDetailsInnerTests {
    @Test
    public void testDeserialize() {
        JoinRequestDetailsInner model =
            BinaryData
                .fromString(
                    "{\"properties\":{\"firstName\":\"rl\",\"lastName\":\"ugjzzdatqxhocdge\",\"email\":\"lgphu\",\"status\":\"Denied\"},\"id\":\"dvkaozw\",\"name\":\"i\",\"type\":\"tyhxhurokft\"}")
                .toObject(JoinRequestDetailsInner.class);
        Assertions.assertEquals("rl", model.firstName());
        Assertions.assertEquals("ugjzzdatqxhocdge", model.lastName());
        Assertions.assertEquals("lgphu", model.email());
        Assertions.assertEquals(JoinRequestStatus.DENIED, model.status());
    }

    @Test
    public void testSerialize() {
        JoinRequestDetailsInner model =
            new JoinRequestDetailsInner()
                .withFirstName("rl")
                .withLastName("ugjzzdatqxhocdge")
                .withEmail("lgphu")
                .withStatus(JoinRequestStatus.DENIED);
        model = BinaryData.fromObject(model).toObject(JoinRequestDetailsInner.class);
        Assertions.assertEquals("rl", model.firstName());
        Assertions.assertEquals("ugjzzdatqxhocdge", model.lastName());
        Assertions.assertEquals("lgphu", model.email());
        Assertions.assertEquals(JoinRequestStatus.DENIED, model.status());
    }
}
