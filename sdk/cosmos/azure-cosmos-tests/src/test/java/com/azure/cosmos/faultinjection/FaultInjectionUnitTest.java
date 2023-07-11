// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.faultinjection;

import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionOperationType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorType;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.time.Duration;

public class FaultInjectionUnitTest {

    @Test(groups = "unit")
    public void testFaultInjectionBuilder() {
        String ruleId = "rule_id_1";
        FaultInjectionCondition faultInjectionCondition = new FaultInjectionConditionBuilder()
            .operationType(FaultInjectionOperationType.CREATE_ITEM)
            .connectionType(FaultInjectionConnectionType.DIRECT)
            .build();
        FaultInjectionRule faultInjectionRule = new FaultInjectionRuleBuilder(ruleId)
            .condition(faultInjectionCondition)
            .duration(Duration.ofSeconds(1))
            .result(FaultInjectionResultBuilders
                .getResultBuilder(FaultInjectionServerErrorType.CONNECTION_DELAY)
                .delay(Duration.ofSeconds(6)) // default connection timeout is 5s
                .times(1)
                .build())
            .build();

        Assertions.assertThat(faultInjectionRule.getId()).isEqualTo(ruleId);
        Assertions.assertThat(faultInjectionRule.getCondition()).isEqualTo(faultInjectionCondition);
        Assertions.assertThat(faultInjectionRule.getDuration()).isEqualTo(Duration.ofSeconds(1));
        Assertions.assertThat(faultInjectionRule.getResult()).isNotNull();
    }
}
