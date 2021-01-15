// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.ServiceLevelObjectiveUsageMetric;
import com.azure.resourcemanager.sql.models.ServiceTierAdvisor;
import com.azure.resourcemanager.sql.models.SloUsageMetric;
import com.azure.resourcemanager.sql.fluent.models.ServiceTierAdvisorInner;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;

/** Implementation for Azure SQL Database's service tier advisor. */
class ServiceTierAdvisorImpl extends RefreshableWrapperImpl<ServiceTierAdvisorInner, ServiceTierAdvisor>
    implements ServiceTierAdvisor {
    private final String sqlServerName;
    private final String resourceGroupName;
    private final SqlServerManager sqlServerManager;
    private final ResourceId resourceId;
    private List<ServiceLevelObjectiveUsageMetric> serviceLevelObjectiveUsageMetrics;

    protected ServiceTierAdvisorImpl(
        String resourceGroupName,
        String sqlServerName,
        ServiceTierAdvisorInner innerObject,
        SqlServerManager sqlServerManager) {
        super(innerObject);
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlServerManager = sqlServerManager;
        this.resourceId = ResourceId.fromString(this.innerModel().id());
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String sqlServerName() {
        return this.sqlServerName;
    }

    @Override
    public String databaseName() {
        return this.resourceId.parent().name();
    }

    @Override
    public OffsetDateTime observationPeriodStart() {
        return this.innerModel().observationPeriodStart();
    }

    @Override
    public OffsetDateTime observationPeriodEnd() {
        return this.innerModel().observationPeriodEnd();
    }

    @Override
    public double activeTimeRatio() {
        return this.innerModel().activeTimeRatio();
    }

    @Override
    public double minDtu() {
        return this.innerModel().minDtu();
    }

    @Override
    public double avgDtu() {
        return this.innerModel().avgDtu();
    }

    @Override
    public double maxDtu() {
        return this.innerModel().maxDtu();
    }

    @Override
    public double maxSizeInGB() {
        return this.innerModel().maxSizeInGB();
    }

    @Override
    public List<ServiceLevelObjectiveUsageMetric> serviceLevelObjectiveUsageMetric() {
        if (this.serviceLevelObjectiveUsageMetrics == null) {
            this.serviceLevelObjectiveUsageMetrics = new ArrayList<>();
            for (SloUsageMetric sloUsageMetricInner : this.innerModel().serviceLevelObjectiveUsageMetrics()) {
                this
                    .serviceLevelObjectiveUsageMetrics
                    .add(new ServiceLevelObjectiveUsageMetricImpl(sloUsageMetricInner));
            }
        }
        return serviceLevelObjectiveUsageMetrics;
    }

    @Override
    public String currentServiceLevelObjective() {
        return this.innerModel().currentServiceLevelObjective();
    }

    @Override
    public UUID currentServiceLevelObjectiveId() {
        return this.innerModel().currentServiceLevelObjectiveId();
    }

    @Override
    public String usageBasedRecommendationServiceLevelObjective() {
        return this.innerModel().usageBasedRecommendationServiceLevelObjective();
    }

    @Override
    public UUID usageBasedRecommendationServiceLevelObjectiveId() {
        return this.innerModel().currentServiceLevelObjectiveId();
    }

    @Override
    public String databaseSizeBasedRecommendationServiceLevelObjective() {
        return this.innerModel().databaseSizeBasedRecommendationServiceLevelObjective();
    }

    @Override
    public UUID databaseSizeBasedRecommendationServiceLevelObjectiveId() {
        return this.innerModel().databaseSizeBasedRecommendationServiceLevelObjectiveId();
    }

    @Override
    public String disasterPlanBasedRecommendationServiceLevelObjective() {
        return this.innerModel().disasterPlanBasedRecommendationServiceLevelObjective();
    }

    @Override
    public UUID disasterPlanBasedRecommendationServiceLevelObjectiveId() {
        return this.innerModel().disasterPlanBasedRecommendationServiceLevelObjectiveId();
    }

    @Override
    public String overallRecommendationServiceLevelObjective() {
        return this.innerModel().overallRecommendationServiceLevelObjective();
    }

    @Override
    public UUID overallRecommendationServiceLevelObjectiveId() {
        return this.innerModel().overallRecommendationServiceLevelObjectiveId();
    }

    @Override
    public double confidence() {
        return this.innerModel().confidence();
    }

    @Override
    protected Mono<ServiceTierAdvisorInner> getInnerAsync() {
        this.serviceLevelObjectiveUsageMetrics = null;

        return this
            .sqlServerManager
            .serviceClient()
            .getServiceTierAdvisors()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }
}
