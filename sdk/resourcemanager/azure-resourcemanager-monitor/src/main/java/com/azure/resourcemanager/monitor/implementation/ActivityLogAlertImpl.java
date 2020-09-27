// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.implementation;

import com.azure.resourcemanager.monitor.MonitorManager;
import com.azure.resourcemanager.monitor.models.ActivityLogAlert;
import com.azure.resourcemanager.monitor.models.ActivityLogAlertActionGroup;
import com.azure.resourcemanager.monitor.models.ActivityLogAlertActionList;
import com.azure.resourcemanager.monitor.models.ActivityLogAlertAllOfCondition;
import com.azure.resourcemanager.monitor.models.ActivityLogAlertLeafCondition;
import com.azure.resourcemanager.monitor.fluent.models.ActivityLogAlertResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** Implementation for ActivityLogAlert. */
class ActivityLogAlertImpl
    extends GroupableResourceImpl<ActivityLogAlert, ActivityLogAlertResourceInner, ActivityLogAlertImpl, MonitorManager>
    implements ActivityLogAlert,
        ActivityLogAlert.Definition,
        ActivityLogAlert.Update,
        ActivityLogAlert.UpdateStages.WithActivityLogUpdate {

    private Map<String, String> conditions;

    ActivityLogAlertImpl(
        String name, final ActivityLogAlertResourceInner innerModel, final MonitorManager monitorManager) {
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
        return Collections.unmodifiableCollection(this.innerModel().scopes());
    }

    @Override
    public Boolean enabled() {
        return this.innerModel().enabled();
    }

    @Override
    public Map<String, String> equalsConditions() {
        return this.conditions;
    }

    @Override
    public Collection<String> actionGroupIds() {
        if (this.innerModel().actions() != null && this.innerModel().actions().actionGroups() != null) {
            List<String> ids = new ArrayList<>();
            for (ActivityLogAlertActionGroup alaag : this.innerModel().actions().actionGroups()) {
                ids.add(alaag.actionGroupId());
            }
            return Collections.unmodifiableCollection(ids);
        }
        return Collections.emptyList();
    }

    @Override
    public String description() {
        return this.innerModel().description();
    }

    @Override
    public Mono<ActivityLogAlert> createResourceAsync() {
        this.innerModel().withLocation("global");
        ActivityLogAlertAllOfCondition condition = new ActivityLogAlertAllOfCondition();
        condition.withAllOf(new ArrayList<ActivityLogAlertLeafCondition>());
        for (Map.Entry<String, String> cds : conditions.entrySet()) {
            ActivityLogAlertLeafCondition alalc = new ActivityLogAlertLeafCondition();
            alalc.withField(cds.getKey());
            alalc.withEquals(cds.getValue());
            condition.allOf().add(alalc);
        }
        this.innerModel().withCondition(condition);
        return this
            .manager()
            .serviceClient()
            .getActivityLogAlerts()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<ActivityLogAlertResourceInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getActivityLogAlerts()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public ActivityLogAlert.DefinitionStages.WithDescription withTargetResource(String resourceId) {
        this.innerModel().withScopes(new ArrayList<String>());
        this.innerModel().scopes().add(resourceId);
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
        this.innerModel().withDescription(description);
        return this;
    }

    @Override
    public ActivityLogAlertImpl withRuleEnabled() {
        this.innerModel().withEnabled(true);
        return this;
    }

    @Override
    public ActivityLogAlertImpl withRuleDisabled() {
        this.innerModel().withEnabled(false);
        return this;
    }

    @Override
    public ActivityLogAlertImpl withActionGroups(String... actionGroupId) {
        if (this.innerModel().actions() == null) {
            this.innerModel().withActions(new ActivityLogAlertActionList());
            this.innerModel().actions().withActionGroups(new ArrayList<ActivityLogAlertActionGroup>());
        }
        this.innerModel().actions().actionGroups().clear();

        for (String agid : actionGroupId) {
            ActivityLogAlertActionGroup aaa = new ActivityLogAlertActionGroup();
            aaa.withActionGroupId(agid);
            this.innerModel().actions().actionGroups().add(aaa);
        }
        return this;
    }

    @Override
    public ActivityLogAlertImpl withoutActionGroup(String actionGroupId) {
        if (this.innerModel().actions() != null && this.innerModel().actions().actionGroups() != null) {
            List<ActivityLogAlertActionGroup> toDelete = new ArrayList<>();
            for (ActivityLogAlertActionGroup aaa : this.innerModel().actions().actionGroups()) {
                if (aaa.actionGroupId().equalsIgnoreCase(actionGroupId)) {
                    toDelete.add(aaa);
                }
            }
            this.innerModel().actions().actionGroups().removeAll(toDelete);
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
