// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.DynamicMetricCriteria;
import com.azure.resourcemanager.monitor.models.MetricAlert;
import com.azure.resourcemanager.monitor.models.MetricAlertAction;
import com.azure.resourcemanager.monitor.models.MetricAlertCondition;
import com.azure.resourcemanager.monitor.models.MetricAlertCriteria;
import com.azure.resourcemanager.monitor.models.MetricAlertMultipleResourceMultipleMetricCriteria;
import com.azure.resourcemanager.monitor.models.MetricAlertSingleResourceMultipleMetricCriteria;
import com.azure.resourcemanager.monitor.models.MetricCriteria;
import com.azure.resourcemanager.monitor.models.MetricDynamicAlertCondition;
import com.azure.resourcemanager.monitor.models.MultiMetricCriteria;
import com.azure.resourcemanager.monitor.fluent.models.MetricAlertResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** Implementation for MetricAlert. */
class MetricAlertImpl
    extends GroupableResourceImpl<MetricAlert, MetricAlertResourceInner, MetricAlertImpl, MonitorManager>
    implements MetricAlert,
        MetricAlert.Definition,
        MetricAlert.DefinitionMultipleResource,
        MetricAlert.Update,
        MetricAlert.UpdateStages.WithMetricUpdate {

    private final ClientLogger logger = new ClientLogger(getClass());

    // 2019/09 at present service support 2 static criteria, or 1 dynamic criteria
    // static criteria
    private Map<String, MetricAlertCondition> conditions;
    // dynamic criteria
    private Map<String, MetricDynamicAlertCondition> dynamicConditions;

    private boolean multipleResource = false;

    MetricAlertImpl(String name, final MetricAlertResourceInner innerModel, final MonitorManager monitorManager) {
        super(name, innerModel, monitorManager);
        this.conditions = new TreeMap<>();
        this.dynamicConditions = new TreeMap<>();
        if (innerModel.criteria() != null) {
            MetricAlertCriteria innerCriteria = innerModel.criteria();
            if (innerCriteria instanceof MetricAlertSingleResourceMultipleMetricCriteria) {
                multipleResource = false;
                // single resource with multiple static criteria
                MetricAlertSingleResourceMultipleMetricCriteria crits =
                    (MetricAlertSingleResourceMultipleMetricCriteria) innerCriteria;
                List<MetricCriteria> criteria = crits.allOf();
                if (criteria != null) {
                    for (MetricCriteria crit : criteria) {
                        this.conditions.put(crit.name(), new MetricAlertConditionImpl(crit.name(), crit, this));
                    }
                }
            } else if (innerCriteria instanceof MetricAlertMultipleResourceMultipleMetricCriteria) {
                multipleResource = true;
                // multiple resource with either multiple static criteria, or (currently single) dynamic criteria
                MetricAlertMultipleResourceMultipleMetricCriteria crits =
                    (MetricAlertMultipleResourceMultipleMetricCriteria) innerCriteria;
                List<MultiMetricCriteria> criteria = crits.allOf();
                if (criteria != null) {
                    for (MultiMetricCriteria crit : criteria) {
                        if (crit instanceof MetricCriteria) {
                            this
                                .conditions
                                .put(
                                    crit.name(),
                                    new MetricAlertConditionImpl(crit.name(), (MetricCriteria) crit, this));
                        } else if (crit instanceof DynamicMetricCriteria) {
                            this
                                .dynamicConditions
                                .put(
                                    crit.name(),
                                    new MetricDynamicAlertConditionImpl(
                                        crit.name(), (DynamicMetricCriteria) crit, this));
                        }
                    }
                }
            }
        }
    }

    @Override
    public Mono<MetricAlert> createResourceAsync() {
        if (this.conditions.isEmpty() && this.dynamicConditions.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Condition cannot be empty"));
        } else if (!this.conditions.isEmpty() && !this.dynamicConditions.isEmpty()) {
            throw logger.logExceptionAsError(
                new IllegalArgumentException("Static condition and dynamic condition cannot co-exist"));
        }

        this.innerModel().withLocation("global");
        if (!this.conditions.isEmpty()) {
            if (!multipleResource) {
                MetricAlertSingleResourceMultipleMetricCriteria crit =
                    new MetricAlertSingleResourceMultipleMetricCriteria();
                crit.withAllOf(new ArrayList<>());
                for (MetricAlertCondition mc : conditions.values()) {
                    crit.allOf().add(mc.innerModel());
                }
                this.innerModel().withCriteria(crit);
            } else {
                MetricAlertMultipleResourceMultipleMetricCriteria crit =
                    new MetricAlertMultipleResourceMultipleMetricCriteria();
                crit.withAllOf(new ArrayList<>());
                for (MetricAlertCondition mc : conditions.values()) {
                    crit.allOf().add(mc.innerModel());
                }
                this.innerModel().withCriteria(crit);
            }
        } else if (!this.dynamicConditions.isEmpty()) {
            MetricAlertMultipleResourceMultipleMetricCriteria crit =
                new MetricAlertMultipleResourceMultipleMetricCriteria();
            crit.withAllOf(new ArrayList<>());
            for (MetricDynamicAlertCondition mc : dynamicConditions.values()) {
                crit.allOf().add(mc.innerModel());
            }
            this.innerModel().withCriteria(crit);
        }
        return this
            .manager()
            .serviceClient()
            .getMetricAlerts()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<MetricAlertResourceInner> getInnerAsync() {
        return this.manager().serviceClient().getMetricAlerts()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public MetricAlertImpl withTargetResource(String resourceId) {
        multipleResource = false;

        this.innerModel().withScopes(new ArrayList<>());
        this.innerModel().scopes().add(resourceId);
        return this;
    }

    @Override
    public MetricAlertImpl withTargetResource(HasId resource) {
        multipleResource = false;

        return this.withTargetResource(resource.id());
    }

    @Override
    public MetricAlertImpl withPeriod(Duration size) {
        this.innerModel().withWindowSize(size);
        return this;
    }

    @Override
    public MetricAlertImpl withFrequency(Duration frequency) {
        this.innerModel().withEvaluationFrequency(frequency);
        return this;
    }

    @Override
    public MetricAlertImpl withSeverity(int severity) {
        this.innerModel().withSeverity(severity);
        return this;
    }

    @Override
    public MetricAlertImpl withAlertDetails(int severity, String description) {
        this.withSeverity(severity);
        return this.withDescription(description);
    }

    @Override
    public MetricAlertImpl withDescription(String description) {
        this.innerModel().withDescription(description);
        return this;
    }

    @Override
    public MetricAlertImpl withRuleEnabled() {
        this.innerModel().withEnabled(true);
        return this;
    }

    @Override
    public MetricAlertImpl withRuleDisabled() {
        this.innerModel().withEnabled(false);
        return this;
    }

    @Override
    public MetricAlertImpl withAutoMitigation() {
        this.innerModel().withAutoMitigate(true);
        return this;
    }

    @Override
    public MetricAlertImpl withoutAutoMitigation() {
        this.innerModel().withAutoMitigate(false);
        return this;
    }

    @Override
    public MetricAlertImpl withActionGroups(String... actionGroupId) {
        if (this.innerModel().actions() == null) {
            this.innerModel().withActions(new ArrayList<MetricAlertAction>());
        }
        this.innerModel().actions().clear();
        for (String agid : actionGroupId) {
            MetricAlertAction maa = new MetricAlertAction();
            maa.withActionGroupId(agid);
            this.innerModel().actions().add(maa);
        }
        return this;
    }

    @Override
    public MetricAlertImpl withoutActionGroup(String actionGroupId) {
        if (this.innerModel().actions() != null) {
            List<MetricAlertAction> toDelete = new ArrayList<>();
            for (MetricAlertAction maa : this.innerModel().actions()) {
                if (maa.actionGroupId().equalsIgnoreCase(actionGroupId)) {
                    toDelete.add(maa);
                }
            }
            this.innerModel().actions().removeAll(toDelete);
        }
        return this;
    }

    @Override
    public MetricAlertConditionImpl defineAlertCriteria(String name) {
        return new MetricAlertConditionImpl(name, new MetricCriteria(), this);
    }

    @Override
    public MetricDynamicAlertConditionImpl defineDynamicAlertCriteria(String name) {
        return new MetricDynamicAlertConditionImpl(name, new DynamicMetricCriteria(), this);
    }

    @Override
    public MetricAlertConditionImpl updateAlertCriteria(String name) {
        return (MetricAlertConditionImpl) this.conditions.get(name);
    }

    @Override
    public MetricDynamicAlertConditionImpl updateDynamicAlertCriteria(String name) {
        return (MetricDynamicAlertConditionImpl) this.dynamicConditions.get(name);
    }

    @Override
    public MetricAlertImpl withoutAlertCriteria(String name) {
        if (this.conditions.containsKey(name)) {
            this.conditions.remove(name);
        }
        if (this.dynamicConditions.containsKey(name)) {
            this.dynamicConditions.remove(name);
        }
        return this;
    }

    MetricAlertImpl withAlertCriteria(MetricAlertConditionImpl criteria) {
        this.withoutAlertCriteria(criteria.name());
        this.conditions.put(criteria.name(), criteria);
        return this;
    }

    MetricAlertImpl withDynamicAlertCriteria(MetricDynamicAlertConditionImpl criteria) {
        this.withoutAlertCriteria(criteria.name());
        this.dynamicConditions.put(criteria.name(), criteria);
        return this;
    }

    @Override
    public MetricAlertImpl withMultipleTargetResources(Collection<String> resourceIds, String type, String region) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Target resource cannot be empty"));
        }

        multipleResource = true;

        this.innerModel().withScopes(new ArrayList<>(resourceIds));
        this.innerModel().withTargetResourceType(type);
        this.innerModel().withTargetResourceRegion(region);
        return this;
    }

    @Override
    public MetricAlertImpl withMultipleTargetResources(Collection<? extends Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Target resource cannot be empty"));
        }

        multipleResource = true;

        List<String> resourceIds = new ArrayList<>();
        String type = resources.iterator().next().type();
        String regionName = resources.iterator().next().regionName();
        for (Resource resource : resources) {
            if (!type.equalsIgnoreCase(resource.type()) || !regionName.equalsIgnoreCase(resource.regionName())) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Target resource must be of the same resource type and in the same region"));
            }

            resourceIds.add(resource.id());
        }
        return this.withMultipleTargetResources(resourceIds, type, regionName);
    }

    @Override
    public String description() {
        return this.innerModel().description();
    }

    @Override
    public int severity() {
        return this.innerModel().severity();
    }

    @Override
    public boolean enabled() {
        return this.innerModel().enabled();
    }

    @Override
    public Duration evaluationFrequency() {
        return this.innerModel().evaluationFrequency();
    }

    @Override
    public Duration windowSize() {
        return this.innerModel().windowSize();
    }

    @Override
    public boolean autoMitigate() {
        return this.innerModel().autoMitigate();
    }

    @Override
    public OffsetDateTime lastUpdatedTime() {
        return this.innerModel().lastUpdatedTime();
    }

    @Override
    public Collection<String> scopes() {
        return Collections.unmodifiableCollection(this.innerModel().scopes());
    }

    @Override
    public Collection<String> actionGroupIds() {
        if (this.innerModel().actions() != null && this.innerModel().actions() != null) {
            List<String> ids = new ArrayList<>();
            for (MetricAlertAction maag : this.innerModel().actions()) {
                ids.add(maag.actionGroupId());
            }
            return Collections.unmodifiableCollection(ids);
        }
        return Collections.emptyList();
    }

    @Override
    public Map<String, MetricAlertCondition> alertCriterias() {
        return Collections.unmodifiableMap(this.conditions);
    }

    @Override
    public Map<String, MetricDynamicAlertCondition> dynamicAlertCriterias() {
        return Collections.unmodifiableMap(this.dynamicConditions);
    }
}
