/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.sql.ServiceTierAdvisor;
import com.microsoft.azure.management.sql.SloUsageMetric;
import com.microsoft.azure.management.sql.SloUsageMetricInterface;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

/**
 * Implementation for Azure SQL Database's service tier advisor.
 */
@LangDefinition
class ServiceTierAdvisorImpl
        extends WrapperImpl<ServiceTierAdvisorInner>
        implements ServiceTierAdvisor {
    private final ResourceId resourceId;
    private final DatabasesInner databasesInner;
    private List<SloUsageMetricInterface> sloUsageMetrics;

    protected ServiceTierAdvisorImpl(ServiceTierAdvisorInner innerObject, DatabasesInner databasesInner) {
        super(innerObject);
        this.resourceId = ResourceId.parseResourceId(this.inner().id());
        this.databasesInner = databasesInner;
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
        return this.resourceId.resourceGroupName();
    }

    @Override
    public String sqlServerName() {
        return this.resourceId.parent().parent().name();
    }

    @Override
    public String databaseName() {
        return this.resourceId.parent().name();
    }

    @Override
    public DateTime observationPeriodStart() {
        return this.inner().observationPeriodStart();
    }

    @Override
    public DateTime observationPeriodEnd() {
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
    public List<SloUsageMetricInterface> serviceLevelObjectiveUsageMetrics() {
        if (sloUsageMetrics == null) {
            PagedListConverter<SloUsageMetric, SloUsageMetricInterface> converter = new PagedListConverter<SloUsageMetric, SloUsageMetricInterface>() {
                @Override
                public SloUsageMetricInterface typeConvert(SloUsageMetric sloUsageMetricInner) {

                    return new SloUsageMetricImpl(sloUsageMetricInner);
                }
            };

            sloUsageMetrics = converter.convert(ReadableWrappersImpl.convertToPagedList(this.inner().serviceLevelObjectiveUsageMetrics()));
        }
        return sloUsageMetrics;
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
    public ServiceTierAdvisor refresh() {
        sloUsageMetrics = null;
        this.setInner(this.databasesInner.getServiceTierAdvisor(this.resourceGroupName(), this.sqlServerName(), this.databaseName(), this.name()));
        return this;
    }
}
