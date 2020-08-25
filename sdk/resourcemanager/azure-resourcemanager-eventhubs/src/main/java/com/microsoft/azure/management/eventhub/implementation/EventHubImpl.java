/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.eventhub.CaptureDescription;
import com.microsoft.azure.management.eventhub.Destination;
import com.microsoft.azure.management.eventhub.EncodingCaptureDescription;
import com.microsoft.azure.management.eventhub.EventHub;
import com.microsoft.azure.management.eventhub.EventHubAuthorizationRule;
import com.microsoft.azure.management.eventhub.EventHubConsumerGroup;
import com.microsoft.azure.management.eventhub.EventHubNamespace;
import com.microsoft.azure.management.resources.fluentcore.dag.FunctionalTaskItem;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Implementation for {@link EventHub}.
 */
@LangDefinition
class EventHubImpl
        extends NestedResourceImpl<EventHub, EventhubInner, EventHubImpl>
        implements
        EventHub,
        EventHub.Definition,
        EventHub.Update {

    private Ancestors.OneAncestor ancestor;
    private CaptureSettings captureSettings;
    private StorageManager storageManager;

    EventHubImpl(String name, EventhubInner inner, EventHubManager manager, StorageManager storageManager) {
        super(name, inner, manager);
        this.ancestor = new Ancestors().new OneAncestor(inner.id());
        this.captureSettings = new CaptureSettings(this.inner());
        this.storageManager = storageManager;
    }

    EventHubImpl(String name, EventHubManager manager, StorageManager storageManager) {
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
        EventHubNamespaceImpl namespace = ((EventHubNamespaceImpl) namespaceCreatable);
        this.ancestor = new Ancestors().new OneAncestor(namespace.resourceGroupName(), namespaceCreatable.name());
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
    public EventHubImpl withNewStorageAccountForCapturedData(Creatable<StorageAccount> storageAccountCreatable, String containerName) {
        this.captureSettings.withNewStorageAccountForCapturedData(storageAccountCreatable, containerName);
        return this;
    }

    @Override
    public EventHubImpl withExistingStorageAccountForCapturedData(StorageAccount storageAccount, String containerName) {
        this.captureSettings.withExistingStorageAccountForCapturedData(storageAccount, containerName);
        return this;
    }

    @Override
    public EventHubImpl withExistingStorageAccountForCapturedData(String storageAccountId, String containerName) {
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
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(final Context context) {
                return manager.eventHubAuthorizationRules()
                        .define(ruleName)
                        .withExistingEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name())
                        .withSendAccess()
                        .createAsync();
            }
        });
        return this;
    }

    @Override
    public EventHubImpl withNewListenRule(final String ruleName) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(final Context context) {
                return manager.eventHubAuthorizationRules()
                        .define(ruleName)
                        .withExistingEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name())
                        .withListenAccess()
                        .createAsync();
            }
        });
        return this;
    }

    @Override
    public EventHubImpl withNewManageRule(final String ruleName) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(final Context context) {
                return manager.eventHubAuthorizationRules()
                        .define(ruleName)
                        .withExistingEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name())
                        .withManageAccess()
                        .createAsync();
            }
        });
        return this;
    }

    @Override
    public EventHubImpl withoutAuthorizationRule(final String ruleName) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(final Context context) {
                return manager.eventHubAuthorizationRules()
                        .deleteByNameAsync(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name(), ruleName)
                        .<Indexable>toObservable()
                        .concatWith(context.voidObservable());
            }
        });
        return this;
    }

    @Override
    public EventHubImpl withNewConsumerGroup(final String name) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(final Context context) {
                return manager.consumerGroups()
                        .define(name)
                        .withExistingEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name())
                        .createAsync();
            }
        });
        return this;
    }

    @Override
    public EventHubImpl withNewConsumerGroup(final String name, final String metadata) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(final Context context) {
                return manager.consumerGroups()
                        .define(name)
                        .withExistingEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name())
                        .withUserMetadata(metadata)
                        .createAsync();
            }
        });
        return this;
    }

    @Override
    public EventHubImpl withoutConsumerGroup(final String name) {
        addPostRunDependent(new FunctionalTaskItem() {
            @Override
            public Observable<Indexable> call(final Context context) {
                return manager.consumerGroups()
                        .deleteByNameAsync(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name(), name)
                        .<Indexable>toObservable()
                        .concatWith(context.voidObservable());
            }
        });
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
        this.inner().withCaptureDescription(this.captureSettings.validateAndGetSettings());
    }

    @Override
    public Observable<EventHub> createResourceAsync() {
        return this.manager.inner().eventHubs()
                .createOrUpdateAsync(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Observable<EventhubInner> getInnerAsync() {
        return this.manager.inner().eventHubs().getAsync(this.ancestor().resourceGroupName(),
                this.ancestor().ancestor1Name(),
                this.name());
    }

    @Override
    public Observable<EventHubConsumerGroup> listConsumerGroupsAsync() {
        return this.manager.consumerGroups()
                .listByEventHubAsync(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name());
    }

    @Override
    public Observable<EventHubAuthorizationRule> listAuthorizationRulesAsync() {
        return this.manager.eventHubAuthorizationRules()
                .listByEventHubAsync(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name());
    }

    @Override
    public PagedList<EventHubConsumerGroup> listConsumerGroups() {
        return this.manager.consumerGroups()
                .listByEventHub(ancestor().resourceGroupName(), ancestor().ancestor1Name(), name());
    }

    @Override
    public PagedList<EventHubAuthorizationRule> listAuthorizationRules() {
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

    private class CaptureSettings {
        private final CaptureDescription currentSettings;
        private CaptureDescription newSettings;

        CaptureSettings(final EventhubInner eventhubInner) {
            this.currentSettings = eventhubInner.captureDescription();
        }

        public CaptureSettings withNewStorageAccountForCapturedData(final Creatable<StorageAccount> creatableStorageAccount, final String containerName) {
            this.ensureSettings().destination().withStorageAccountResourceId("temp-id");
            this.ensureSettings().destination().withBlobContainer(containerName);
            //
            // Schedule task to create storage account and container.
            //
            addDependency(new FunctionalTaskItem() {
                @Override
                public Observable<Indexable> call(final Context context) {
                    return creatableStorageAccount.createAsync()
                            .last()
                            .flatMap(new Func1<Indexable, Observable<Indexable>>() {
                                @Override
                                public Observable<Indexable> call(Indexable indexable) {
                                    StorageAccount storageAccount = (StorageAccount) indexable;
                                    ensureSettings().destination().withStorageAccountResourceId(storageAccount.id());
                                    return createContainerIfNotExistsAsync(storageAccount, containerName)
                                            .flatMap(new Func1<Boolean, Observable<Indexable>>() {
                                                @Override
                                                public Observable<Indexable> call(Boolean aBoolean) {
                                                    return context.voidObservable();
                                                }
                                            });

                                }
                            });
                }
            });
            return this;
        }

        public CaptureSettings withExistingStorageAccountForCapturedData(final StorageAccount storageAccount, final String containerName) {
            this.ensureSettings().destination().withStorageAccountResourceId(storageAccount.id());
            this.ensureSettings().destination().withBlobContainer(containerName);
            //
            // Schedule task to create container if not exists.
            //
            addDependency(new FunctionalTaskItem() {
                @Override
                public Observable<Indexable> call(final Context context) {
                    return  createContainerIfNotExistsAsync(storageAccount, containerName)
                            .flatMap(new Func1<Boolean, Observable<Indexable>>() {
                                @Override
                                public Observable<Indexable> call(Boolean aBoolean) {
                                    return context.voidObservable();
                                }
                            });
                }
            });
            return this;
        }

        public CaptureSettings withExistingStorageAccountForCapturedData(final String storageAccountId, final String containerName) {
            this.ensureSettings().destination().withStorageAccountResourceId(storageAccountId);
            this.ensureSettings().destination().withBlobContainer(containerName);
            //
            // Schedule task to create container if not exists.
            //
            addDependency(new FunctionalTaskItem() {
                @Override
                public Observable<Indexable> call(final Context context) {
                    return storageManager.storageAccounts().getByIdAsync(storageAccountId)
                            .last()
                            .flatMap(new Func1<StorageAccount, Observable<Indexable>>() {
                                @Override
                                public Observable<Indexable> call(StorageAccount storageAccount) {
                                    if (storageAccount == null) {
                                        return Observable.error(new Throwable(String.format("Storage account with id: %s not found (storing captured data)", storageAccountId)));
                                    }
                                    ensureSettings().destination().withStorageAccountResourceId(storageAccount.id());
                                    return createContainerIfNotExistsAsync(storageAccount, containerName)
                                            .flatMap(new Func1<Boolean, Observable<Indexable>>() {
                                                @Override
                                                public Observable<Indexable> call(Boolean aBoolean) {
                                                    return context.voidObservable();
                                                }
                                            });

                                }
                            });
                }
            });
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
                throw new IllegalStateException("Setting any of the capture properties requires capture destination [StorageAccount, DataLake] to be specified");
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

        private Observable<Boolean> createContainerIfNotExistsAsync(final StorageAccount storageAccount,
                                                                    final String containerName) {
            return getCloudStorageAsync(storageAccount)
                    .flatMap(new Func1<CloudStorageAccount, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(final CloudStorageAccount cloudStorageAccount) {
                            return Observable.fromCallable(new Callable<Boolean>() {
                                @Override
                                public Boolean call() {
                                    CloudBlobClient blobClient = cloudStorageAccount.createCloudBlobClient();
                                    try {
                                        return blobClient.getContainerReference(containerName).createIfNotExists();
                                    } catch (StorageException stgException) {
                                        throw Exceptions.propagate(stgException);
                                    } catch (URISyntaxException syntaxException) {
                                        throw Exceptions.propagate(syntaxException);
                                    }
                                }
                            }).subscribeOn(SdkContext.getRxScheduler());
                        }
                    });
        }

        private Observable<CloudStorageAccount> getCloudStorageAsync(final StorageAccount storageAccount) {
            return storageAccount.getKeysAsync()
                    .flatMapIterable(new Func1<List<StorageAccountKey>, Iterable<StorageAccountKey>>() {
                        @Override
                        public Iterable<StorageAccountKey> call(List<StorageAccountKey> storageAccountKeys) {
                            return storageAccountKeys;
                        }
                    })
                    .last()
                    .map(new Func1<StorageAccountKey, CloudStorageAccount>() {
                        @Override
                        public CloudStorageAccount call(StorageAccountKey storageAccountKey) {
                            try {
                            return CloudStorageAccount.parse(Utils.getStorageConnectionString(storageAccount.name(), storageAccountKey.value(), manager().inner().restClient()));
                            } catch (URISyntaxException syntaxException) {
                                throw Exceptions.propagate(syntaxException);
                            } catch (InvalidKeyException keyException) {
                                throw Exceptions.propagate(keyException);
                            }
                        }
                    });
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
                clone.destination().withStorageAccountResourceId(this.currentSettings.destination().storageAccountResourceId());
            } else {
                clone.withDestination(new Destination());
            }
            return clone;
        }
    }
}
