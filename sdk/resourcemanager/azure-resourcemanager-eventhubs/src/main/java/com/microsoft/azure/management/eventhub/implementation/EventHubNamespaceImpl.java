/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.management.eventhub.EventHubNamespaceAuthorizationRule;
import com.microsoft.azure.management.eventhub.EventHubNamespaceSkuType;
import com.microsoft.azure.management.eventhub.Sku;
import com.microsoft.azure.management.eventhub.SkuName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.dag.FunctionalTaskItem;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import org.joda.time.DateTime;
import rx.Observable;

/**
 * Implementation for {@link EventHubNamespace}.
 */
@LangDefinition
class EventHubNamespaceImpl
        extends GroupableResourceImpl<EventHubNamespace, EHNamespaceInner, EventHubNamespaceImpl, EventHubManager>
        implements
        EventHubNamespace,
        EventHubNamespace.Definition,
        EventHubNamespace.Update {

    protected EventHubNamespaceImpl(String name, EHNamespaceInner innerObject, EventHubManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    public EventHubNamespaceSkuType sku() {
        return new EventHubNamespaceSkuType(this.inner().sku());
    }

    @Override
    public String azureInsightMetricId() {
        return this.inner().metricId();
    }

    @Override
    public String serviceBusEndpoint() {
        return this.inner().serviceBusEndpoint();
    }

    @Override
    public DateTime createdAt() {
        return this.inner().createdAt();
    }

    @Override
    public DateTime updatedAt() {
        return this.inner().updatedAt();
    }

    @Override
    public String provisioningState() {
        return this.inner().provisioningState();
    }

    @Override
    public boolean isAutoScaleEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().isAutoInflateEnabled());
    }

    @Override
    public int currentThroughputUnits() {
        return Utils.toPrimitiveInt(this.inner().sku().capacity());
    }

    @Override
    public int throughputUnitsUpperLimit() {
        return Utils.toPrimitiveInt(this.inner().maximumThroughputUnits());
    }

    @Override
    public EventHubNamespaceImpl withNewEventHub(final String eventHubName) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(final Context context) {
                return manager().eventHubs()
                        .define(eventHubName)
                        .withExistingNamespace(resourceGroupName(), name())
                        .createAsync();
            }
        });
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewEventHub(final String eventHubName, final int partitionCount) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(final Context context) {
               return  manager().eventHubs()
                        .define(eventHubName)
                        .withExistingNamespace(resourceGroupName(), name())
                        .withPartitionCount(partitionCount)
                        .createAsync();
            }
        });
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewEventHub(final String eventHubName, final int partitionCount, final int retentionPeriodInDays) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(final Context context) {
                return  manager().eventHubs()
                        .define(eventHubName)
                        .withExistingNamespace(resourceGroupName(), name())
                        .withPartitionCount(partitionCount)
                        .withRetentionPeriodInDays(retentionPeriodInDays)
                        .createAsync();
            }
        });
        return this;
    }

    @Override
    public Update withoutEventHub(final String eventHubName) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(Context context) {
                return manager().eventHubs()
                        .deleteByNameAsync(resourceGroupName(), name(), eventHubName)
                        .<Indexable>toObservable()
                        .concatWith(context.voidObservable());
            }
        });
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewSendRule(final String ruleName) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(Context context) {
                return manager().namespaceAuthorizationRules()
                        .define(ruleName)
                        .withExistingNamespace(resourceGroupName(), name())
                        .withSendAccess()
                        .createAsync();
            }
        });
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewListenRule(final String ruleName) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(Context context) {
                return manager().namespaceAuthorizationRules()
                        .define(ruleName)
                        .withExistingNamespace(resourceGroupName(), name())
                        .withListenAccess()
                        .createAsync();
            }
        });
        return this;
    }

    @Override
    public EventHubNamespaceImpl withNewManageRule(final String ruleName) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(Context context) {
                return manager().namespaceAuthorizationRules()
                        .define(ruleName)
                        .withExistingNamespace(resourceGroupName(), name())
                        .withManageAccess()
                        .createAsync();
            }
        });
        return this;
    }

    @Override
    public EventHubNamespaceImpl withoutAuthorizationRule(final String ruleName) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(Context context) {
                return manager().namespaceAuthorizationRules()
                        .deleteByNameAsync(resourceGroupName(), name(), ruleName)
                        .<Indexable>toObservable()
                        .concatWith(context.voidObservable());
            }
        });
        return this;
    }

    @Override
    public EventHubNamespaceImpl withAutoScaling() {
        // Auto-inflate requires a Sku > 'Basic' with capacity.
        this.setDefaultSkuIfNotSet();
        this.inner().withIsAutoInflateEnabled(true);
        if (this.inner().maximumThroughputUnits() == null) {
            // Required when auto-inflate is set & use portal default.
            this.withThroughputUnitsUpperLimit(20);
        }
        return this;
    }

    @Override
    public EventHubNamespaceImpl withSku(EventHubNamespaceSkuType namespaceSku) {
        Sku newSkuInner = new Sku()
                .withName(namespaceSku.name())
                .withTier(namespaceSku.tier())
                .withCapacity(null);
        Sku currentSkuInner = this.inner().sku();

        boolean isDifferent = currentSkuInner == null || !currentSkuInner.name().equals(newSkuInner.name());
        if (isDifferent) {
            this.inner().withSku(newSkuInner);
            if (newSkuInner.name().equals(SkuName.STANDARD)) {
                newSkuInner.withCapacity(1);
            }
        }
        return this;
    }

    @Override
    public EventHubNamespaceImpl withCurrentThroughputUnits(int units) {
        this.setDefaultSkuIfNotSet();
        this.inner().sku().withCapacity(units);
        return this;
    }

    @Override
    public EventHubNamespaceImpl withThroughputUnitsUpperLimit(int units) {
        this.inner().withMaximumThroughputUnits(units);
        return this;
    }

    @Override
    public Observable<EventHubNamespace> createResourceAsync() {
        return this.manager().inner().namespaces()
                .createOrUpdateAsync(resourceGroupName(), name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public Observable<EventHub> listEventHubsAsync() {
        return this.manager().eventHubs().listByNamespaceAsync(resourceGroupName(), name());
    }

    @Override
    public Observable<EventHubNamespaceAuthorizationRule> listAuthorizationRulesAsync() {
        return this.manager().namespaceAuthorizationRules().listByNamespaceAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public PagedList<EventHub> listEventHubs() {
        return this.manager().eventHubs().listByNamespace(resourceGroupName(), name());
    }

    @Override
    public PagedList<EventHubNamespaceAuthorizationRule> listAuthorizationRules() {
        return this.manager().namespaceAuthorizationRules()
                .listByNamespace(this.resourceGroupName(), this.name());
    }

    @Override
    protected Observable<EHNamespaceInner> getInnerAsync() {
        return this.manager().inner().namespaces().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    private void setDefaultSkuIfNotSet() {
        if (this.inner().sku() == null) {
            this.withSku(EventHubNamespaceSkuType.STANDARD);
        }
    }
}
