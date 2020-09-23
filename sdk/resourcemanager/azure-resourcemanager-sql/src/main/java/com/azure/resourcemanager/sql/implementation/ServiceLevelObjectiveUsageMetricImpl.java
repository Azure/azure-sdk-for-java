// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;
import com.azure.resourcemanager.sql.models.ServiceLevelObjectiveUsageMetric;
import com.azure.resourcemanager.sql.models.ServiceObjectiveName;
import com.azure.resourcemanager.sql.models.SloUsageMetric;
import java.util.UUID;

/** Implementation for Azure SQL Database's SloUsageMetric. */
class ServiceLevelObjectiveUsageMetricImpl extends WrapperImpl<SloUsageMetric>
    implements ServiceLevelObjectiveUsageMetric {

    protected ServiceLevelObjectiveUsageMetricImpl(SloUsageMetric innerObject) {
        super(innerObject);
    }

    @Override
    public ServiceObjectiveName serviceLevelObjective() {
        return this.innerModel().serviceLevelObjective();
    }

    @Override
    public UUID serviceLevelObjectiveId() {
        return this.innerModel().serviceLevelObjectiveId();
    }

    @Override
    public double inRangeTimeRatio() {
        return this.innerModel().inRangeTimeRatio();
    }
}
