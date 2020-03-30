/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.management.monitor.ActivityLogAlert;
import com.azure.management.monitor.ActivityLogAlertActionGroup;
import com.azure.management.monitor.ActivityLogAlertActionList;
import com.azure.management.monitor.ActivityLogAlertAllOfCondition;
import com.azure.management.monitor.ActivityLogAlertLeafCondition;
import com.azure.management.monitor.models.ActivityLogAlertResourceInner;
import com.azure.management.resources.fluentcore.arm.models.HasId;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Implementation for ActivityLogAlert.
 */
class ActivityLogAlertImpl
        extends GroupableResourceImpl<
            ActivityLogAlert,
            ActivityLogAlertResourceInner,
            ActivityLogAlertImpl,
            MonitorManager>
        implements
            ActivityLogAlert,
            ActivityLogAlert.Definition,
            ActivityLogAlert.Update,
            ActivityLogAlert.UpdateStages.WithActivityLogUpdate {

    private Map<String, String> conditions;

    ActivityLogAlertImpl(String name, final ActivityLogAlertResourceInner innerModel, final MonitorManager monitorManager) {
        super(name, innerModel, monitorManager);
        this.conditions = new TreeMap<>();
        if (innerModel.condition() != null && innerModel.condition().allOf() != null) {
            for (ActivityLogAlertLeafCondition aac : innerModel.condition().allOf()) {
                this.conditions.put(aac.field(), aac.equals());
            }
        }
    }

    @Override
    public Collection<String> scopes() {
        return Collections.unmodifiableCollection(this.inner().scopes());
    }

    @Override
    public Boolean enabled() {
        return this.inner().enabled();
    }

    @Override
    public Map<String, String> equalsConditions() {
        return this.conditions;
    }

    @Override
    public Collection<String> actionGroupIds() {
        if (this.inner().actions() != null
                && this.inner().actions().actionGroups() != null) {
            List<String> ids = new ArrayList<>();
            for (ActivityLogAlertActionGroup alaag : this.inner().actions().actionGroups()) {
                ids.add(alaag.actionGroupId());
            }
            return Collections.unmodifiableCollection(ids);
        }
        return Collections.emptyList();
    }

    @Override
    public String description() {
        return this.inner().description();
    }

    @Override
    public Mono<ActivityLogAlert> createResourceAsync() {
        this.inner().setLocation("global");
        ActivityLogAlertAllOfCondition condition = new ActivityLogAlertAllOfCondition();
        condition.withAllOf(new ArrayList<ActivityLogAlertLeafCondition>());
        for (Map.Entry<String, String> cds : conditions.entrySet()) {
            ActivityLogAlertLeafCondition alalc = new ActivityLogAlertLeafCondition();
            alalc.withField(cds.getKey());
            alalc.withEquals(cds.getValue());
            condition.allOf().add(alalc);
        }
        this.inner().withCondition(condition);
        return this.manager().inner().activityLogAlerts().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<ActivityLogAlertResourceInner> getInnerAsync() {
        return this.manager().inner().activityLogAlerts().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public ActivityLogAlert.DefinitionStages.WithDescription withTargetResource(String resourceId) {
        this.inner().withScopes(new ArrayList<String>());
        this.inner().scopes().add(resourceId);
        return this;
    }

    @Override
    public ActivityLogAlert.DefinitionStages.WithDescription withTargetResource(HasId resource) {
        return this.withTargetResource(resource.id());
    }

    @Override
    public ActivityLogAlert.DefinitionStages.WithDescription withTargetSubscription(String targetSubscriptionId) {
        return this.withTargetResource("/subscriptions/" + targetSubscriptionId);
    }

    @Override
    public ActivityLogAlertImpl withDescription(String description) {
        this.inner().withDescription(description);
        return this;
    }

    @Override
    public ActivityLogAlertImpl withRuleEnabled() {
        this.inner().withEnabled(true);
        return this;
    }

    @Override
    public ActivityLogAlertImpl withRuleDisabled() {
        this.inner().withEnabled(false);
        return this;
    }

    @Override
    public ActivityLogAlertImpl withActionGroups(String... actionGroupId) {
        if (this.inner().actions() == null) {
            this.inner().withActions(new ActivityLogAlertActionList());
            this.inner().actions().withActionGroups(new ArrayList<ActivityLogAlertActionGroup>());
        }
        this.inner().actions().actionGroups().clear();

        for (String agid : actionGroupId) {
            ActivityLogAlertActionGroup aaa = new ActivityLogAlertActionGroup();
            aaa.withActionGroupId(agid);
            this.inner().actions().actionGroups().add(aaa);
        }
        return this;
    }

    @Override
    public ActivityLogAlertImpl withoutActionGroup(String actionGroupId) {
        if (this.inner().actions() != null && this.inner().actions().actionGroups() != null) {
            List<ActivityLogAlertActionGroup> toDelete = new ArrayList<>();
            for (ActivityLogAlertActionGroup aaa : this.inner().actions().actionGroups()) {
                if (aaa.actionGroupId().equalsIgnoreCase(actionGroupId)) {
                    toDelete.add(aaa);
                }
            }
            this.inner().actions().actionGroups().removeAll(toDelete);
        }
        return this;
    }

    @Override
    public ActivityLogAlertImpl withEqualsCondition(String field, String equals) {
        this.withoutEqualsCondition(field);
        this.conditions.put(field, equals);
        return this;
    }

    @Override
    public ActivityLogAlertImpl withEqualsConditions(Map<String, String> fieldEqualsMap) {
        this.conditions.clear();
        this.conditions.putAll(fieldEqualsMap);
        return this;
    }

    @Override
    public ActivityLogAlertImpl withoutEqualsCondition(String field) {
        if (this.conditions.containsKey(field)) {
            this.conditions.remove(field);
        }
        return this;
    }
}

