/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.monitor.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.monitor.AutoscaleProfile;
import com.microsoft.azure.management.monitor.AutoscaleSetting;
import com.microsoft.azure.management.monitor.AutoscaleSettings;
import com.microsoft.azure.management.monitor.MetricTrigger;
import com.microsoft.azure.management.monitor.Recurrence;
import com.microsoft.azure.management.monitor.ScaleAction;
import com.microsoft.azure.management.monitor.ScaleRule;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelCrudableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;

import java.util.List;

/**
 * Implementation for {@link AutoscaleSettings}.
 */
@LangDefinition
class AutoscaleSettingsImpl
        extends TopLevelCrudableResourcesImpl<
                            AutoscaleSetting,
                            AutoscaleSettingImpl,
                            AutoscaleSettingResourceInner,
                            AutoscaleSettingsInner,
                            MonitorManager>
        implements AutoscaleSettings {

    AutoscaleSettingsImpl(final MonitorManager monitorManager) {
        super(monitorManager.inner().autoscaleSettings()), monitorManager);
    }

    @Override
    protected AutoscaleSettingImpl wrapModel(String name) {
        return new AutoscaleSettingImpl(name, new AutoscaleSettingResourceInner(), this.manager());
    }

    @Override
    protected AutoscaleSettingImpl wrapModel(AutoscaleSettingResourceInner inner) {
        return new AutoscaleSettingImpl(inner.name(), inner, this.manager());
    }

    @Override
    public AutoscaleSettingImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public PagedList<Operation> listOperations() {
        return (new PagedListConverter<OperationInner, Operation>() {
            @Override
            public Operation typeConvert(OperationInner inner) {
                return new Operation(inner);
            }
        }).convert(this.manager().inner().listOperations());
    }

    @Override
    public PagedList<ResourceUsage> listResourceUsage() {
        return (new PagedListConverter<ResourceUsageInner, ResourceUsage>() {
            @Override
            public ResourceUsage typeConvert(ResourceUsageInner inner) {
                return new ResourceUsage(inner);
            }
        }).convert(this.manager().inner().checkResourceUsage());
    }

    @Override
    public PagedList<EdgeNode> listEdgeNodes() {
        return (new PagedListConverter<EdgeNodeInner, EdgeNode>() {
            @Override
            public EdgeNode typeConvert(EdgeNodeInner inner) {
                return new EdgeNode(inner);
            }
        }).convert(this.manager().inner().edgeNodes().list());
    }

    @Override
    public ScaleRule.DefinitionStages.MetricTriggerDefinitionStages.WithMetricResourceUri<MetricTrigger> createMetricTrigger(String name) {
        return null;
    }

    @Override
    public ScaleRule.DefinitionStages.ScaleActionDefinitionStages.WithDirection<ScaleAction> createScaleAction() {
        return null;
    }

    @Override
    public AutoscaleProfile.DefinitionStages.RecurrenceDefinitionStages.WithRecurrenceFrequency<Recurrence> createRecurrence() {
        return null;
    }
}
