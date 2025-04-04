// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.timeseriesinsights.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.timeseriesinsights.models.AccessPolicyRole;
import com.azure.resourcemanager.timeseriesinsights.models.AccessPolicyUpdateParameters;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class AccessPolicyUpdateParametersTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AccessPolicyUpdateParameters model
            = BinaryData.fromString("{\"properties\":{\"description\":\"tsdbpgn\",\"roles\":[\"Reader\"]}}")
                .toObject(AccessPolicyUpdateParameters.class);
        Assertions.assertEquals("tsdbpgn", model.description());
        Assertions.assertEquals(AccessPolicyRole.READER, model.roles().get(0));
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AccessPolicyUpdateParameters model = new AccessPolicyUpdateParameters().withDescription("tsdbpgn")
            .withRoles(Arrays.asList(AccessPolicyRole.READER));
        model = BinaryData.fromObject(model).toObject(AccessPolicyUpdateParameters.class);
        Assertions.assertEquals("tsdbpgn", model.description());
        Assertions.assertEquals(AccessPolicyRole.READER, model.roles().get(0));
    }
}
