// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class VersionSelectorTests {

    @Test
    public void setVersionSelectionRuleWrapsRuleInList() {
        VersionSelectionRule rule = new FixedRatioVersionSelectionRule(100).setAgentVersion("1");

        VersionSelector selector = new VersionSelector().setVersionSelectionRule(rule);

        assertEquals(1, selector.getVersionSelectionRules().size());
        assertSame(rule, selector.getVersionSelectionRules().get(0));
    }
}
