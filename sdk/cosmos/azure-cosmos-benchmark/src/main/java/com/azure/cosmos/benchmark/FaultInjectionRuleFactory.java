// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark;

import com.azure.cosmos.ConnectionMode;
import com.azure.cosmos.test.faultinjection.FaultInjectionCondition;
import com.azure.cosmos.test.faultinjection.FaultInjectionConditionBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionConnectionType;
import com.azure.cosmos.test.faultinjection.FaultInjectionResultBuilders;
import com.azure.cosmos.test.faultinjection.FaultInjectionRule;
import com.azure.cosmos.test.faultinjection.FaultInjectionRuleBuilder;
import com.azure.cosmos.test.faultinjection.FaultInjectionServerErrorResult;

final class FaultInjectionRuleFactory {

    private FaultInjectionRuleFactory() {
    }

    static FaultInjectionRule create(
        String ruleId,
        ConnectionMode connectionMode,
        FaultInjectionConfig config) {

        FaultInjectionConnectionType connectionType = connectionMode == ConnectionMode.DIRECT
            ? FaultInjectionConnectionType.DIRECT
            : FaultInjectionConnectionType.GATEWAY;

        FaultInjectionConditionBuilder conditionBuilder = new FaultInjectionConditionBuilder()
            .connectionType(connectionType);

        if (config.getRegion() != null) {
            conditionBuilder.region(config.getRegion());
        }

        FaultInjectionCondition condition = conditionBuilder.build();

        FaultInjectionServerErrorResult result = FaultInjectionResultBuilders
            .getResultBuilder(config.getServerErrorType())
            .injectionRate(config.getInjectionRate())
            .build();

        return new FaultInjectionRuleBuilder(ruleId)
            .condition(condition)
            .result(result)
            .startDelay(config.getStartDelay())
            .duration(config.getDuration())
            .build();
    }
}