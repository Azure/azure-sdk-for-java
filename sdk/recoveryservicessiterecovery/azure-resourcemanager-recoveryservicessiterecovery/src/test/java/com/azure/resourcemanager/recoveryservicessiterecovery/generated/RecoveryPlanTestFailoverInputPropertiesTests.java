// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicessiterecovery.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.recoveryservicessiterecovery.models.PossibleOperationsDirections;
import com.azure.resourcemanager.recoveryservicessiterecovery.models.RecoveryPlanProviderSpecificFailoverInput;
import com.azure.resourcemanager.recoveryservicessiterecovery.models.RecoveryPlanTestFailoverInputProperties;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;

public final class RecoveryPlanTestFailoverInputPropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        RecoveryPlanTestFailoverInputProperties model = BinaryData.fromString(
            "{\"failoverDirection\":\"RecoveryToPrimary\",\"networkType\":\"pijubyq\",\"networkId\":\"kakfqfr\",\"providerSpecificDetails\":[{\"instanceType\":\"RecoveryPlanProviderSpecificFailoverInput\"},{\"instanceType\":\"RecoveryPlanProviderSpecificFailoverInput\"},{\"instanceType\":\"RecoveryPlanProviderSpecificFailoverInput\"}]}")
            .toObject(RecoveryPlanTestFailoverInputProperties.class);
        Assertions.assertEquals(PossibleOperationsDirections.RECOVERY_TO_PRIMARY, model.failoverDirection());
        Assertions.assertEquals("pijubyq", model.networkType());
        Assertions.assertEquals("kakfqfr", model.networkId());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        RecoveryPlanTestFailoverInputProperties model = new RecoveryPlanTestFailoverInputProperties()
            .withFailoverDirection(PossibleOperationsDirections.RECOVERY_TO_PRIMARY)
            .withNetworkType("pijubyq")
            .withNetworkId("kakfqfr")
            .withProviderSpecificDetails(Arrays.asList(new RecoveryPlanProviderSpecificFailoverInput(),
                new RecoveryPlanProviderSpecificFailoverInput(), new RecoveryPlanProviderSpecificFailoverInput()));
        model = BinaryData.fromObject(model).toObject(RecoveryPlanTestFailoverInputProperties.class);
        Assertions.assertEquals(PossibleOperationsDirections.RECOVERY_TO_PRIMARY, model.failoverDirection());
        Assertions.assertEquals("pijubyq", model.networkType());
        Assertions.assertEquals("kakfqfr", model.networkId());
    }
}
