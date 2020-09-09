// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.UUID;

/** An immutable client-side representation of an Azure SQL database's service level objective usage metric. */
@Fluent
public interface ServiceLevelObjectiveUsageMetric extends HasInner<SloUsageMetric> {
    /** @return the serviceLevelObjective for SLO usage metric. */
    ServiceObjectiveName serviceLevelObjective();

    /** @return the serviceLevelObjectiveId for SLO usage metric. */
    UUID serviceLevelObjectiveId();

    /** @return inRangeTimeRatio for SLO usage metric. */
    double inRangeTimeRatio();
}
