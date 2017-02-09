/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.sql.implementation.ServiceTierAdvisorInner;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;


/**
 * An immutable client-side representation of an Azure SQL Service tier advisor.
 */
@Fluent
public interface ServiceTierAdvisor extends
        Refreshable<ServiceTierAdvisor>,
        HasInner<ServiceTierAdvisorInner>,
        HasResourceGroup,
        HasName,
        HasId {

    /**
     * @return name of the SQL Server to which this replication belongs
     */
    String sqlServerName();

    /**
     * @return name of the SQL Database to which this replication belongs
     */
    String databaseName();

    /**
     * @return the observation period start (ISO8601 format).
     */
    DateTime observationPeriodStart();

    /**
     * @return the observation period start (ISO8601 format).
     */
    DateTime observationPeriodEnd();

    /**
     * @return the activeTimeRatio for service tier advisor.
     */
    double activeTimeRatio();

    /**
     * @return or sets minDtu for service tier advisor.
     */
    double minDtu();

    /**
     * @return or sets avgDtu for service tier advisor.
     */
    double avgDtu();

    /**
     * @return or sets maxDtu for service tier advisor.
     */
    double maxDtu();

    /**
     * @return or sets maxSizeInGB for service tier advisor.
     */
    double maxSizeInGB();

    /**
     * @return serviceLevelObjectiveUsageMetrics for the service tier
     * advisor.
     */
    List<SloUsageMetricInterface> serviceLevelObjectiveUsageMetrics();

    /**
     * @return or sets currentServiceLevelObjective for service tier advisor.
     */
    String currentServiceLevelObjective();

    /**
     * @return or sets currentServiceLevelObjectiveId for service tier advisor.
     */
    UUID currentServiceLevelObjectiveId();

    /**
     * @return or sets usageBasedRecommendationServiceLevelObjective for service
     * tier advisor.
     */
    String usageBasedRecommendationServiceLevelObjective();

    /**
     * @return or sets usageBasedRecommendationServiceLevelObjectiveId for
     * service tier advisor.
     */
    UUID usageBasedRecommendationServiceLevelObjectiveId();

    /**
     * @return or sets databaseSizeBasedRecommendationServiceLevelObjective for
     * service tier advisor.
     */
    String databaseSizeBasedRecommendationServiceLevelObjective();

    /**
     * @return or sets databaseSizeBasedRecommendationServiceLevelObjectiveId for
     * service tier advisor.
     */
    UUID databaseSizeBasedRecommendationServiceLevelObjectiveId();

    /**
     * @return or sets disasterPlanBasedRecommendationServiceLevelObjective for
     * service tier advisor.
     */
    String disasterPlanBasedRecommendationServiceLevelObjective();

    /**
     * @return or sets disasterPlanBasedRecommendationServiceLevelObjectiveId for
     * service tier advisor.
     */
    UUID disasterPlanBasedRecommendationServiceLevelObjectiveId();

    /**
     * @return or sets overallRecommendationServiceLevelObjective for service
     * tier advisor.
     */
    String overallRecommendationServiceLevelObjective();

    /**
     * @return or sets overallRecommendationServiceLevelObjectiveId for service
     * tier advisor.
     */
    UUID overallRecommendationServiceLevelObjectiveId();

    /**
     * @return or sets confidence for service tier advisor.
     */
    double confidence();
}

