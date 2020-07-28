// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.ServiceLevelObjectiveUsageMetric;
import com.azure.resourcemanager.sql.models.ServiceTierAdvisor;
import com.azure.resourcemanager.sql.models.SloUsageMetric;
import com.azure.resourcemanager.sql.fluent.inner.ServiceTierAdvisorInner;
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
        this.resourceId = ResourceId.fromString(this.inner().id());
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String id() {
        return this.inner().id();
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
        return this.inner().observationPeriodStart();
    }

    @Override
    public OffsetDateTime observationPeriodEnd() {
        return this.inner().observationPeriodEnd();
    }

    @Override
    public double activeTimeRatio() {
        return this.inner().activeTimeRatio();
    }

    @Override
    public double minDtu() {
        return this.inner().minDtu();
    }

    @Override
    public double avgDtu() {
        return this.inner().avgDtu();
    }

    @Override
    public double maxDtu() {
        return this.inner().maxDtu();
    }

    @Override
    public double maxSizeInGB() {
        return this.inner().maxSizeInGB();
    }

    @Override
    public List<ServiceLevelObjectiveUsageMetric> serviceLevelObjectiveUsageMetric() {
        if (this.serviceLevelObjectiveUsageMetrics == null) {
            this.serviceLevelObjectiveUsageMetrics = new ArrayList<>();
            for (SloUsageMetric sloUsageMetricInner : this.inner().serviceLevelObjectiveUsageMetrics()) {
                this
                    .serviceLevelObjectiveUsageMetrics
                    .add(new ServiceLevelObjectiveUsageMetricImpl(sloUsageMetricInner));
            }
        }
        return serviceLevelObjectiveUsageMetrics;
    }

    @Override
    public String currentServiceLevelObjective() {
        return this.inner().currentServiceLevelObjective();
    }

    @Override
    public UUID currentServiceLevelObjectiveId() {
        return this.inner().currentServiceLevelObjectiveId();
    }

    @Override
    public String usageBasedRecommendationServiceLevelObjective() {
        return this.inner().usageBasedRecommendationServiceLevelObjective();
    }

    @Override
    public UUID usageBasedRecommendationServiceLevelObjectiveId() {
        return this.inner().currentServiceLevelObjectiveId();
    }

    @Override
    public String databaseSizeBasedRecommendationServiceLevelObjective() {
        return this.inner().databaseSizeBasedRecommendationServiceLevelObjective();
    }

    @Override
    public UUID databaseSizeBasedRecommendationServiceLevelObjectiveId() {
        return this.inner().databaseSizeBasedRecommendationServiceLevelObjectiveId();
    }

    @Override
    public String disasterPlanBasedRecommendationServiceLevelObjective() {
        return this.inner().disasterPlanBasedRecommendationServiceLevelObjective();
    }

    @Override
    public UUID disasterPlanBasedRecommendationServiceLevelObjectiveId() {
        return this.inner().disasterPlanBasedRecommendationServiceLevelObjectiveId();
    }

    @Override
    public String overallRecommendationServiceLevelObjective() {
        return this.inner().overallRecommendationServiceLevelObjective();
    }

    @Override
    public UUID overallRecommendationServiceLevelObjectiveId() {
        return this.inner().overallRecommendationServiceLevelObjectiveId();
    }

    @Override
    public double confidence() {
        return this.inner().confidence();
    }

    @Override
    protected Mono<ServiceTierAdvisorInner> getInnerAsync() {
        this.serviceLevelObjectiveUsageMetrics = null;

        return this
            .sqlServerManager
            .inner()
            .getServiceTierAdvisors()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.databaseName(), this.name());
    }
}
