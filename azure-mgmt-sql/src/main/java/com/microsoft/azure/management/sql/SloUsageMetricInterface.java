/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

    import com.microsoft.azure.management.apigeneration.Fluent;
    import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
    import java.util.UUID;


/**
 * An immutable client-side representation of an Azure SQL database's SloUsageMetric.
 */
@Fluent
public interface SloUsageMetricInterface extends
        HasInner<SloUsageMetric> {
    /**
     * @return the serviceLevelObjective for SLO usage metric.
     */
    ServiceObjectiveName serviceLevelObjective();

    /**
     * @return the serviceLevelObjectiveId for SLO usage metric.
     */
    UUID serviceLevelObjectiveId();

    /**
     * @return inRangeTimeRatio for SLO usage metric.
     */
    double inRangeTimeRatio();

}

