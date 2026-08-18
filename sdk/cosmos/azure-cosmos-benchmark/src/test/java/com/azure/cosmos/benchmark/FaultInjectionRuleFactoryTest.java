// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

public class FaultInjectionRuleFactoryTest {

    @DataProvider(name = "connectionModes")
    public Object[][] connectionModes() {
        return new Object[][] {
            {ConnectionMode.DIRECT, FaultInjectionConnectionType.DIRECT},
            {ConnectionMode.GATEWAY, FaultInjectionConnectionType.GATEWAY}
        };
    }

    @Test(dataProvider = "connectionModes", groups = {"unit"})
    public void createsContainerWideServerErrorRule(
        ConnectionMode connectionMode,
        FaultInjectionConnectionType expectedConnectionType) {

        FaultInjectionConfig config = new FaultInjectionConfig(
            FaultInjectionServerErrorType.SERVICE_UNAVAILABLE,
            Duration.ofSeconds(5),
            Duration.ofSeconds(10),
            0.25);

        FaultInjectionRule rule = FaultInjectionRuleFactory.create("benchmark-rule", connectionMode, config);

        assertThat(rule.getId()).isEqualTo("benchmark-rule");
        assertThat(rule.getStartDelay()).isEqualTo(Duration.ofSeconds(5));
        assertThat(rule.getDuration()).isEqualTo(Duration.ofSeconds(10));
        assertThat(rule.getCondition().getConnectionType()).isEqualTo(expectedConnectionType);
        assertThat(rule.getCondition().getEndpoints()).isNull();
        assertThat(rule.getCondition().getOperationType()).isNull();
        assertThat(rule.getCondition().getRegion()).isNull();

        FaultInjectionServerErrorResult result = (FaultInjectionServerErrorResult) rule.getResult();
        assertThat(result.getServerErrorType()).isEqualTo(FaultInjectionServerErrorType.SERVICE_UNAVAILABLE);
        assertThat(result.getInjectionRate()).isEqualTo(0.25);
    }

    @Test(groups = {"unit"})
    public void createsRegionScopedRule() {
        FaultInjectionConfig config = new FaultInjectionConfig(
            FaultInjectionServerErrorType.INTERNAL_SERVER_ERROR,
            Duration.ofSeconds(5),
            Duration.ofSeconds(10),
            1.0,
            "West US");

        FaultInjectionRule rule = FaultInjectionRuleFactory.create(
            "region-scoped-rule", ConnectionMode.GATEWAY, config);

        assertThat(rule.getCondition().getRegion()).isEqualTo("West US");
    }
}