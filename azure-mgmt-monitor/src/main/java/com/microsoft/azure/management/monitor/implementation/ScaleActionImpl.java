/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.ScaleAction;
import com.microsoft.azure.management.monitor.ScaleDirection;
import com.microsoft.azure.management.monitor.ScaleRule;
import com.microsoft.azure.management.monitor.ScaleType;
import org.joda.time.Period;

/**
 * Implementation for CdnProfile.
 */
@LangDefinition
class ScaleActionImpl
        implements
            ScaleAction,
            ScaleAction.StandaloneDefinition,
            ScaleAction.StandaloneUpdate,
            ScaleAction.Definition,
            ScaleAction.ParentUpdateDefinition,
            ScaleAction.UpdateDefinition,
            ScaleAction.Update {

    ScaleActionImpl(String name) {
    }

    @Override
    public ScaleActionImpl withDirection(ScaleDirection direction) {
        return null;
    }

    @Override
    public ScaleActionImpl withType(ScaleType type) {
        return null;
    }

    @Override
    public ScaleActionImpl withCooldown(Period cooldown) {
        return null;
    }

    @Override
    public ScaleActionImpl withValue(String value) {
        return null;
    }

    @Override
    public ScaleRuleImpl attach() {
        return null;
    }

    @Override
    public ScaleRuleImpl parent() {
        return null;
    }

    @Override
    public ScaleActionImpl create() {
        return this;
    }

    @Override
    public ScaleActionImpl update() {
        return this;
    }

    @Override
    public ScaleActionImpl apply() {
        return this;
    }

}
