// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.servicefabricmanagedclusters.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.FailureAction;
import com.azure.resourcemanager.servicefabricmanagedclusters.models.RollingUpgradeMonitoringPolicy;
import org.junit.jupiter.api.Assertions;

public final class RollingUpgradeMonitoringPolicyTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        RollingUpgradeMonitoringPolicy model = BinaryData.fromString(
            "{\"failureAction\":\"Rollback\",\"healthCheckWaitDuration\":\"a\",\"healthCheckStableDuration\":\"a\",\"healthCheckRetryTimeout\":\"hrzayvvtpgvdf\",\"upgradeTimeout\":\"iotkftutqxl\",\"upgradeDomainTimeout\":\"gxlefgugnxkrxd\"}")
            .toObject(RollingUpgradeMonitoringPolicy.class);
        Assertions.assertEquals(FailureAction.ROLLBACK, model.failureAction());
        Assertions.assertEquals("a", model.healthCheckWaitDuration());
        Assertions.assertEquals("a", model.healthCheckStableDuration());
        Assertions.assertEquals("hrzayvvtpgvdf", model.healthCheckRetryTimeout());
        Assertions.assertEquals("iotkftutqxl", model.upgradeTimeout());
        Assertions.assertEquals("gxlefgugnxkrxd", model.upgradeDomainTimeout());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        RollingUpgradeMonitoringPolicy model
            = new RollingUpgradeMonitoringPolicy().withFailureAction(FailureAction.ROLLBACK)
                .withHealthCheckWaitDuration("a")
                .withHealthCheckStableDuration("a")
                .withHealthCheckRetryTimeout("hrzayvvtpgvdf")
                .withUpgradeTimeout("iotkftutqxl")
                .withUpgradeDomainTimeout("gxlefgugnxkrxd");
        model = BinaryData.fromObject(model).toObject(RollingUpgradeMonitoringPolicy.class);
        Assertions.assertEquals(FailureAction.ROLLBACK, model.failureAction());
        Assertions.assertEquals("a", model.healthCheckWaitDuration());
        Assertions.assertEquals("a", model.healthCheckStableDuration());
        Assertions.assertEquals("hrzayvvtpgvdf", model.healthCheckRetryTimeout());
        Assertions.assertEquals("iotkftutqxl", model.upgradeTimeout());
        Assertions.assertEquals("gxlefgugnxkrxd", model.upgradeDomainTimeout());
    }
}
