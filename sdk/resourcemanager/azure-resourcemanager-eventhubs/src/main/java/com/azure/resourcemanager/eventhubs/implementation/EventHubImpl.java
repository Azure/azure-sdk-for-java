// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.eventhubs.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.fluent.inner.EventhubInner;
import com.azure.resourcemanager.eventhubs.models.CaptureDescription;
import com.azure.resourcemanager.eventhubs.models.Destination;
import com.azure.resourcemanager.eventhubs.models.EncodingCaptureDescription;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHubAuthorizationRule;
import com.azure.resourcemanager.eventhubs.models.EventHubConsumerGroup;
import com.azure.resourcemanager.eventhubs.models.EventHubNamespace;
import com.azure.resourcemanager.resources.fluentcore.dag.VoidIndexable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Implementation for {@link EventHub}.
 */
class EventHubImpl
    extends NestedResourceImpl<EventHub, EventhubInner, EventHubImpl>
    implements EventHub, EventHub.Definition, EventHub.Update {

    private Ancestors.OneAncestor ancestor;
    private CaptureSettings captureSettings;
    private StorageManager storageManager;
    private Flux<Indexable> postRunTasks;

    private final ClientLogger logger = new ClientLogger(EventHubImpl.class);

    EventHubImpl(String name, EventhubInner inner, EventHubsManager manager, StorageManager storageManager) {
        super(name, inner, manager);
        this.ancestor = new Ancestors().new OneAncestor(inner.id());
        this.captureSettings = new CaptureSettings(this.inner());
        this.storageManager = storageManager;
    }

    EventHubImpl(String name, EventHubsManager manager, StorageManager storageManager) {
        super(name, new EventhubInner(), manager);
        this.storageManager = storageManager;
        this.captureSettings = new CaptureSettings(this.inner());
    }

    @Override
    public String namespaceResourceGroupName() {
        return this.ancestor().resourceGroupName();
    }

    @Override
    public String namespaceName() {
        return this.ancestor().ancestor1Name();
    }

    @Override
    public boolean isDataCaptureEnabled() {
        if (this.inner().captureDescription() == null) {
            return false;
        }
        return Utils.toPrimitiveBoolean(this.inner().captureDescription().enabled());
    }

    @Override
    public int dataCaptureWindowSizeInSeconds() {
        if (this.inner().captureDescription() == null) {
            return 0;
        }
        return Utils.toPrimitiveInt(this.inner().captureDescription().intervalInSeconds());
    }

    @Override
    public int dataCaptureWindowSizeInMB() {
        if (this.inner().captureDescription() == null) {
            return 0;
        }
        int inBytes = Utils.toPrimitiveInt(this.inner().captureDescription().sizeLimitInBytes());
        if (inBytes != 0) {
            return inBytes / (1024 * 1024);
        } else {
            return 0;
        }
    }

    @Override
    public boolean dataCaptureSkipEmptyArchives() {
        if (this.inner().captureDescription() == null) {
            return false;
        }
        return this.inner().captureDescription().skipEmptyArchives();
    }

    @Override
    public String dataCaptureFileNameFormat() {
        if (this.inner().captureDescription() == null) {
            return null;
        } else if (this.inner().captureDescription().destination() == null) {
            return null;
        } else {
            return this.inner().captureDescription().destination().archiveNameFormat();
        }
    }

    @Override
    public Destination captureDestination() {
        if (this.inner().captureDescription() == null) {
            return null;
        } else {
            return this.inner().captureDescription().destination();
        }
    }

    @Override
    public Set<String> partitionIds() {
        if (this.inner().partitionIds() == null) {
            return Collections.unmodifiableSet(new HashSet<String>());
        } else {
            return Collections.unmodifiableSet(new HashSet<String>(this.inner().partitionIds()));
        }
    }

    @Override
    public int messageRetentionPeriodInDays() {
        return Utils.toPrimitiveInt(this.inner().messageRetentionInDays());
    }

    @Override
    public EventHubImpl withNewNamespace(Creatable<EventHubNamespace> namespaceCreatable) {
        this.addDependency(namespaceCreatable);
        if (namespaceCreatable instanceof EventHubNamespaceImpl) {
            EventHubNamespaceImpl namespace = ((EventHubNamespaceImpl) namespaceCreatable);
            this.ancestor = new Ancestors().new OneAncestor(namespace.resourceGroupName(), namespaceCreatable.name());
        } else {
            logger.logExceptionAsError(new IllegalArgumentException("The namespaceCreatable is invalid."));
        }
        return this;
    }

    @Override
    public EventHubImpl withExistingNamespace(EventHubNamespace namespace) {
        this.ancestor = new Ancestors().new OneAncestor(selfId(namespace.id()));
        return this;
    }

    @Override
    public EventHubImpl withExistingNamespace(String resourceGroupName, String namespaceName) {
        this.ancestor = new Ancestors().new OneAncestor(resourceGroupName, namespaceName);
        return this;
    }

    @Override
    public EventHubImpl withExistingNamespaceId(String namespaceId) {
        this.ancestor = new Ancestors().new OneAncestor(selfId(namespaceId));
        return this;
    }

    @Override
    public EventHubImpl withNewStorageAccountForCapturedData(
        Creatable<StorageAccount> storageAccountCreatable, String containerName) {
        this.captureSettings.withNewStorageAccountForCapturedData(storageAccountCreatable, containerName);
        return this;
    }

    @Override
    public EventHubImpl withExistingStorageAccountForCapturedData(
        StorageAccount storageAccount, String containerName) {
        this.captureSettings.withExistingStorageAccountForCapturedData(storageAccount, containerName);
        return this;
    }

    @Override
    public EventHubImpl withExistingStorageAccountForCapturedData(
        String storageAccountId, String containerName) {
        this.captureSettings.withExistingStorageAccountForCapturedData(storageAccountId, containerName);
        return this;
    }

    @Override
    public EventHubImpl withDataCaptureEnabled() {
        this.captureSettings.withDataCaptureEnabled();
        return this;
    }

    @Override
    public EventHubImpl withDataCaptureDisabled() {
        this.captureSettings.withDataCaptureDisabled();
        return this;
    }

    @Override
    public EventHubImpl withDataCaptureWindowSizeInSeconds(int sizeInSeconds) {
        this.captureSettings.withDataCaptureWindowSizeInSeconds(sizeInSeconds);
        return this;
    }

    @Override
    public EventHubImpl withDataCaptureSkipEmptyArchives(Boolean skipEmptyArchives) {
        this.captureSettings.withDataCaptureSkipEmptyArchives(skipEmptyArchives);
        return this;
    }

    @Override
    public EventHubImpl withDataCaptureWindowSizeInMB(int sizeInMB) {
        this.captureSettings.withDataCaptureWindowSizeInMB(sizeInMB);
        return this;
    }

    @Override
    public EventHubImpl withDataCaptureFileNameFormat(String format) {
        this.captureSettings.withDataCaptureFileNameFormat(format);
        return this;
    }

    @Override
    public EventHubImpl withNewSendRule(final String ruleName) {
        concatPostRunTask(manager().eventHubAuthorizationRules()
            .define(ruleName)
            .withExistingEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name())
            .withSendAccess()
            .createAsync()
            .last());
        return this;
    }

    @Override
    public EventHubImpl withNewListenRule(final String ruleName) {
        concatPostRunTask(manager().eventHubAuthorizationRules()
            .define(ruleName)
            .withExistingEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name())
            .withListenAccess()
            .createAsync()
            .last());
        return this;
    }

    @Override
    public EventHubImpl withNewSendAndListenRule(final String ruleName) {
        concatPostRunTask(manager().eventHubAuthorizationRules()
            .define(ruleName)
            .withExistingEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name())
            .withSendAndListenAccess()
            .createAsync()
            .last());
        return this;
    }

    @Override
    public EventHubImpl withNewManageRule(final String ruleName) {
        concatPostRunTask(manager().eventHubAuthorizationRules()
            .define(ruleName)
            .withExistingEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name())
            .withManageAccess()
            .createAsync()
            .last());
        return this;
    }

    @Override
    public EventHubImpl withoutAuthorizationRule(final String ruleName) {
        concatPostRunTask(manager().eventHubAuthorizationRules()
            .deleteByNameAsync(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name(), ruleName)
            .map(aVoid -> new VoidIndexable(UUID.randomUUID().toString())));
        return this;
    }

    @Override
    public EventHubImpl withNewConsumerGroup(final String name) {
        concatPostRunTask(manager().consumerGroups()
            .define(name)
            .withExistingEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name())
            .createAsync()
            .last());
        return this;
    }

    @Override
    public EventHubImpl withNewConsumerGroup(final String name, final String metadata) {
        concatPostRunTask(manager().consumerGroups()
            .define(name)
            .withExistingEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name())
            .withUserMetadata(metadata)
            .createAsync()
            .last());
        return this;
    }

    @Override
    public EventHubImpl withoutConsumerGroup(final String name) {
        concatPostRunTask(manager().consumerGroups()
            .deleteByNameAsync(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name(), name)
            .map(aVoid -> new VoidIndexable(UUID.randomUUID().toString())));
        return this;
    }

    @Override
    public EventHubImpl withPartitionCount(long count) {
        this.inner().withPartitionCount(count);
        return this;
    }

    @Override
    public EventHubImpl withRetentionPeriodInDays(long period) {
        this.inner().withMessageRetentionInDays(period);
        return this;
    }

    @Override
    public EventHubImpl update() {
        this.captureSettings = new CaptureSettings(this.inner());
        return super.update();
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
        if (postRunTasks != null) {
            addPostRunDependent(context -> postRunTasks.last());
        }
        this.inner().withCaptureDescription(this.captureSettings.validateAndGetSettings());
    }

    @Override
    public Mono<EventHub> createResourceAsync() {
        return this.manager.inner().getEventHubs()
                .createOrUpdateAsync(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        postRunTasks = null;
        return Mono.empty();
    }

    @Override
    protected Mono<EventhubInner> getInnerAsync() {
        return this.manager.inner().getEventHubs().getAsync(this.ancestor().resourceGroupName(),
                this.ancestor().ancestor1Name(),
                this.name());
    }

    @Override
    public PagedFlux<EventHubConsumerGroup> listConsumerGroupsAsync() {
        return this.manager.consumerGroups()
                .listByEventHubAsync(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name());
    }

    @Override
    public PagedFlux<EventHubAuthorizationRule> listAuthorizationRulesAsync() {
        return this.manager.eventHubAuthorizationRules()
                .listByEventHubAsync(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name());
    }

    @Override
    public PagedIterable<EventHubConsumerGroup> listConsumerGroups() {
        return this.manager.consumerGroups()
                .listByEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name());
    }

    @Override
    public PagedIterable<EventHubAuthorizationRule> listAuthorizationRules() {
        return this.manager.eventHubAuthorizationRules()
                .listByEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name());
    }

    private Ancestors.OneAncestor ancestor() {
        Objects.requireNonNull(this.ancestor);
        return this.ancestor;
    }

    private String selfId(String parentId) {
        return String.format("%s/eventhubs/%s", parentId, this.name());
    }

    private void concatPostRunTask(Mono<Indexable> task) {
        if (postRunTasks == null) {
            postRunTasks = Flux.empty();
        }
        postRunTasks = postRunTasks.concatWith(task);
    }

    private class CaptureSettings {
        private final CaptureDescription currentSettings;
        private CaptureDescription newSettings;

        CaptureSettings(final EventhubInner eventhubInner) {
            this.currentSettings = eventhubInner.captureDescription();
        }

        public CaptureSettings withNewStorageAccountForCapturedData(
            final Creatable<StorageAccount> creatableStorageAccount, final String containerName) {
            this.ensureSettings().destination().withStorageAccountResourceId("temp-id");
            this.ensureSettings().destination().withBlobContainer(containerName);
            //
            // Schedule task to create storage account and container.
            //
            addDependency(context -> creatableStorageAccount
                .createAsync()
                .last()
                .flatMap(indexable -> {
                    StorageAccount storageAccount = (StorageAccount) indexable;
                    ensureSettings().destination().withStorageAccountResourceId(storageAccount.id());
                    return createContainerIfNotExistsAsync(storageAccount, containerName);
                }));
            return this;
        }

        public CaptureSettings withExistingStorageAccountForCapturedData(
            final StorageAccount storageAccount, final String containerName) {
            this.ensureSettings().destination().withStorageAccountResourceId(storageAccount.id());
            this.ensureSettings().destination().withBlobContainer(containerName);
            //
            // Schedule task to create container if not exists.
            //
            addDependency(context -> createContainerIfNotExistsAsync(storageAccount, containerName));
            return this;
        }

        public CaptureSettings withExistingStorageAccountForCapturedData(
            final String storageAccountId, final String containerName) {
            this.ensureSettings().destination().withStorageAccountResourceId(storageAccountId);
            this.ensureSettings().destination().withBlobContainer(containerName);
            //
            // Schedule task to create container if not exists.
            //
            addDependency(context -> storageManager.storageAccounts()
                .getByIdAsync(storageAccountId)
                .flatMap(storageAccount -> {
                    ensureSettings().destination().withStorageAccountResourceId(storageAccount.id());
                    return createContainerIfNotExistsAsync(storageAccount, containerName);
                }));
            return this;
        }

        public CaptureSettings withDataCaptureEnabled() {
            this.ensureSettings().withEnabled(true);
            return this;
        }

        public CaptureSettings withDataCaptureDisabled() {
            this.ensureSettings().withEnabled(false);
            return this;
        }

        public CaptureSettings withDataCaptureSkipEmptyArchives(Boolean skipEmptyArchives) {
            this.ensureSettings().withSkipEmptyArchives(skipEmptyArchives);
            return this;
        }

        public CaptureSettings withDataCaptureWindowSizeInSeconds(int sizeInSeconds) {
            this.ensureSettings().withIntervalInSeconds(sizeInSeconds);
            return this;
        }

        public CaptureSettings withDataCaptureWindowSizeInMB(int sizeInMB) {
            this.ensureSettings().withSizeLimitInBytes(sizeInMB * 1024 * 1024);
            return this;
        }

        public CaptureSettings withDataCaptureFileNameFormat(String format) {
            this.ensureSettings().destination().withArchiveNameFormat(format);
            return this;
        }

        public CaptureDescription validateAndGetSettings() {
            if (this.newSettings == null) {
                return this.currentSettings;
            } else if (this.newSettings.destination() == null
                    || this.newSettings.destination().storageAccountResourceId() == null
                    || this.newSettings.destination().blobContainer() == null) {
                throw logger.logExceptionAsError(new IllegalStateException(
                    "Setting any of the capture properties requires "
                        + "capture destination [StorageAccount, DataLake] to be specified"));
            }
            if (this.newSettings.destination().name() == null) {
                this.newSettings.destination().withName("EventHubArchive.AzureBlockBlob");
            }
            if (this.newSettings.encoding() == null) {
                this.newSettings.withEncoding(EncodingCaptureDescription.AVRO);
            }
            return this.newSettings;
        }

        private CaptureDescription ensureSettings() {
            if (this.newSettings != null) {
                return this.newSettings;
            } else if (this.currentSettings == null) {
                this.newSettings = new CaptureDescription().withDestination(new Destination());
                return this.newSettings;
            } else {
                // Clone the current settings to new settings (one time)
                //
                this.newSettings = cloneCurrentSettings();
                return this.newSettings;
            }
        }

        private Mono<Indexable> createContainerIfNotExistsAsync(final StorageAccount storageAccount,
                                                                final String containerName) {
            return storageManager.blobContainers()
                .getAsync(storageAccount.resourceGroupName(), storageAccount.name(), containerName)
                .cast(Indexable.class)
                .onErrorResume(throwable -> storageManager.blobContainers()
                    .defineContainer(containerName)
                    .withExistingBlobService(storageAccount.resourceGroupName(), storageAccount.name())
                    .withPublicAccess(PublicAccess.CONTAINER)
                    .createAsync()
                    .last());
        }

        private CaptureDescription cloneCurrentSettings() {
            Objects.requireNonNull(this.currentSettings);
            CaptureDescription clone = new CaptureDescription();
            clone.withSizeLimitInBytes(this.currentSettings.sizeLimitInBytes());
            clone.withSkipEmptyArchives(this.currentSettings.skipEmptyArchives());
            clone.withIntervalInSeconds(this.currentSettings.intervalInSeconds());
            clone.withEnabled(this.currentSettings.enabled());
            clone.withEncoding(this.currentSettings.encoding());
            if (this.currentSettings.destination() != null) {
                clone.withDestination(new Destination());
                clone.destination().withArchiveNameFormat(this.currentSettings.destination().archiveNameFormat());
                clone.destination().withBlobContainer(this.currentSettings.destination().blobContainer());
                clone.destination().withName(this.currentSettings.destination().name());
                clone.destination().withStorageAccountResourceId(
                    this.currentSettings.destination().storageAccountResourceId());
            } else {
                clone.withDestination(new Destination());
            }
            return clone;
        }
    }
}
