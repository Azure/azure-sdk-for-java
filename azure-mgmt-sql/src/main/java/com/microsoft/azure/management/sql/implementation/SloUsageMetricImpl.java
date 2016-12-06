/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.sql.ServiceObjectiveName;
import com.microsoft.azure.management.sql.SloUsageMetric;

import java.util.UUID;

/**
 * Implementation for Azure SQL Database's SloUsageMetric.
 */
@LangDefinition
class SloUsageMetricImpl
        extends WrapperImpl<SloUsageMetricInner>
        implements SloUsageMetric {

    protected SloUsageMetricImpl(SloUsageMetricInner innerObject) {
        super(innerObject);
    }

    @Override
    public ServiceObjectiveName serviceLevelObjective() {
        return this.inner().serviceLevelObjective();
    }

    @Override
    public UUID serviceLevelObjectiveId() {
        return this.inner().serviceLevelObjectiveId();
    }

    @Override
    public double inRangeTimeRatio() {
        return this.inner().inRangeTimeRatio();
    }
}
