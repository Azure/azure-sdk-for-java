/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.documentdb.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.documentdb.DatabaseAccountKind;
import com.microsoft.azure.management.documentdb.DatabaseAccountOfferType;
import com.microsoft.azure.management.documentdb.ConsistencyPolicy;
import com.microsoft.azure.management.documentdb.DefaultConsistencyLevel;
import com.microsoft.azure.management.documentdb.KeyKind;
import com.microsoft.azure.management.documentdb.Location;
import com.microsoft.azure.management.documentdb.DocumentDBAccount;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * The implementation for DocumentDBAccount.
 */
@LangDefinition
class DocumentDBAccountImpl
        extends
        GroupableResourceImpl<
                DocumentDBAccount,
                DatabaseAccountInner,
                DocumentDBAccountImpl,
                DocumentDBManager>
        implements DocumentDBAccount,
        DocumentDBAccount.Definition,
        DocumentDBAccount.Update {
    private List<FailoverPolicyInner> failoverPolicies;
    private boolean hasFailoverPolicyChanges;
    private final int maxDelayDueToMissingFailovers = 60 * 10;

    DocumentDBAccountImpl(String name, DatabaseAccountInner innerObject, DocumentDBManager manager) {
        super(fixDBName(name), innerObject, manager);
        this.failoverPolicies = new ArrayList<FailoverPolicyInner>();
    }

    @Override
    public DatabaseAccountKind kind() {
        return this.inner().kind();
    }

    @Override
    public String documentEndpoint() {
        return this.inner().documentEndpoint();
    }

    @Override
    public DatabaseAccountOfferType databaseAccountOfferType() {
        return this.inner().databaseAccountOfferType();
    }

    @Override
    public String ipRangeFilter() {
        return this.inner().ipRangeFilter();
    }

    @Override
    public ConsistencyPolicy consistencyPolicy() {
        return this.inner().consistencyPolicy();
    }

    @Override
    public DefaultConsistencyLevel defaultConsistencyLevel() {
        if (this.inner().consistencyPolicy() == null) {
            throw new RuntimeException("Consistency policy is missing!");
        }

        return this.inner().consistencyPolicy().defaultConsistencyLevel();
    }

    @Override
    public List<Location> writableReplications() {
        return this.inner().writeLocations();
    }

    @Override
    public List<Location> readableReplications() {
        return this.inner().readLocations();
    }

    @Override
    public DatabaseAccountListKeysResult listKeys() {
        return this.listKeysAsync().toBlocking().last();
    }

    @Override
    public Observable<DatabaseAccountListKeysResult> listKeysAsync() {
        return this.manager().inner().databaseAccounts().listKeysAsync(this.resourceGroupName(),
                this.name());
    }

    @Override
    public DatabaseAccountListConnectionStringsResult listConnectionStrings() {
        return this.listConnectionStringsAsync().toBlocking().last();
    }

    @Override
    public Observable<DatabaseAccountListConnectionStringsResult> listConnectionStringsAsync() {
        return this.manager().inner().databaseAccounts().listConnectionStringsAsync(this.resourceGroupName(),
                this.name());
    }

    @Override
    public void regenerateKey(KeyKind keyKind) {
        this.regenerateKeyAsync(keyKind).toBlocking().last();
    }

    @Override
    public Observable<Void> regenerateKeyAsync(KeyKind keyKind) {
        return this.manager().inner().databaseAccounts().regenerateKeyAsync(this.resourceGroupName(),
                this.name(), keyKind);
    }

    @Override
    public DocumentDBAccountImpl withKind(DatabaseAccountKind kind) {
        this.inner().withKind(kind);
        return this;        
    }


    @Override
    public DocumentDBAccountImpl withIpRangeFilter(String ipRangeFilter) {
        this.inner().withIpRangeFilter(ipRangeFilter);
        return this;        
    }

    @Override
    protected Observable<DatabaseAccountInner> getInnerAsync() {
        return this.manager().inner().databaseAccounts().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public DocumentDBAccountImpl withWriteReplication(Region region) {
        FailoverPolicyInner failoverPolicyInner = new FailoverPolicyInner();
        failoverPolicyInner.withLocationName(region.name());
        this.hasFailoverPolicyChanges = true;
        this.failoverPolicies.add(failoverPolicyInner);
        return this;
    }

    @Override
    public DocumentDBAccountImpl withReadReplication(Region region) {
        this.ensureFailoverIsInitialized();
        FailoverPolicyInner failoverPolicyInner = new FailoverPolicyInner();
        failoverPolicyInner.withLocationName(region.name());
        failoverPolicyInner.withFailoverPriority(this.failoverPolicies.size());
        this.hasFailoverPolicyChanges = true;
        this.failoverPolicies.add(failoverPolicyInner);
        return this;
    }

    @Override
    public DocumentDBAccountImpl withoutReadReplication(Region region) {
        this.ensureFailoverIsInitialized();
        for (int i = 1; i < this.failoverPolicies.size(); i++) {
            if (this.failoverPolicies.get(i).locationName() != null) {
                String locName = this.failoverPolicies.get(i).locationName().replace(" ", "").toLowerCase();
                if (locName.equals(region.name())) {
                    this.failoverPolicies.remove(i);
                }
            }
        }

        return this;
    }

    @Override
    public DocumentDBAccountImpl withEventualConsistency() {
        this.setConsistencyPolicy(DefaultConsistencyLevel.EVENTUAL, 0, 0);
        return this;
    }

    @Override
    public DocumentDBAccountImpl withSessionConsistency() {
        this.setConsistencyPolicy(DefaultConsistencyLevel.SESSION, 0, 0);
        return this;
    }

    @Override
    public DocumentDBAccountImpl withBoundedStalenessConsistency(int maxStalenessPrefix, int maxIntervalInSeconds) {
        this.setConsistencyPolicy(DefaultConsistencyLevel.BOUNDED_STALENESS,
                maxStalenessPrefix,
                maxIntervalInSeconds);
        return this;
    }

    @Override
    public DocumentDBAccountImpl withStrongConsistency() {
        this.setConsistencyPolicy(DefaultConsistencyLevel.STRONG, 0, 0);
        return this;
    }


    @Override
    public Observable<DocumentDBAccount> createResourceAsync() {
        return this.doDatabaseUpdateCreate();
    }

    private DatabaseAccountCreateUpdateParametersInner createUpdateParametersInner(DatabaseAccountInner inner) {
        this.ensureFailoverIsInitialized();
        DatabaseAccountCreateUpdateParametersInner createUpdateParametersInner =
                new DatabaseAccountCreateUpdateParametersInner();
        createUpdateParametersInner.withLocation(this.regionName().toLowerCase());
        createUpdateParametersInner.withConsistencyPolicy(inner.consistencyPolicy());
        createUpdateParametersInner.withDatabaseAccountOfferType(
                DatabaseAccountOfferType.STANDARD.toString());
        createUpdateParametersInner.withIpRangeFilter(inner.ipRangeFilter());
        createUpdateParametersInner.withKind(inner.kind());
        createUpdateParametersInner.withTags(inner.getTags());
        this.addLocationsForCreateUpdateParameters(createUpdateParametersInner, this.failoverPolicies);
        return createUpdateParametersInner;
    }

    private static String fixDBName(String name) {
        return name.toLowerCase();
    }

    private void setConsistencyPolicy(
            DefaultConsistencyLevel level,
            int maxIntervalInSeconds,
            long maxStalenessPrefix) {
        ConsistencyPolicy policy = new ConsistencyPolicy();
        policy.withDefaultConsistencyLevel(level);
        if (level == DefaultConsistencyLevel.BOUNDED_STALENESS) {
            policy.withMaxIntervalInSeconds(maxIntervalInSeconds);
            policy.withMaxStalenessPrefix((long) maxStalenessPrefix);
        }

        this.inner().withConsistencyPolicy(policy);
    }

    private void addLocationsForCreateUpdateParameters(
            DatabaseAccountCreateUpdateParametersInner createUpdateParametersInner,
            List<FailoverPolicyInner> failoverPolicies) {
        List<Location> locations = new ArrayList<Location>();
        for (int i = 0; i < failoverPolicies.size(); i++) {
            FailoverPolicyInner policyInner = failoverPolicies.get(i);
            Location location = new Location();
            location.withFailoverPriority(i);
            location.withLocationName(policyInner.locationName());
            locations.add(location);
        }

        if (locations.size() > 0) {
            createUpdateParametersInner.withLocations(locations);
        }
    }

    private Observable<DocumentDBAccount> updateFailoverPriorityAsync() {
        final DocumentDBAccountImpl self = this;
        return this.manager().inner().databaseAccounts().failoverPriorityChangeAsync(this.resourceGroupName(),
                this.name(), this.failoverPolicies).map(new Func1<Void, DocumentDBAccount>() {
            @Override
            public DocumentDBAccount call(Void voidInner) {
                if (self.inner().failoverPolicies() != null) {
                    self.inner().failoverPolicies().clear();
                    self.inner().failoverPolicies().addAll(self.failoverPolicies);
                }

                self.failoverPolicies.clear();
                return self;
            }
        });
    }

    private Observable<DocumentDBAccount> doDatabaseUpdateCreate() {
        final DocumentDBAccountImpl self = this;
        final List<Integer> data = new ArrayList<Integer>();
        data.add(0);
        final DatabaseAccountCreateUpdateParametersInner createUpdateParametersInner =
                this.createUpdateParametersInner(this.inner());
        return this.manager().inner().databaseAccounts().createOrUpdateAsync(
                resourceGroupName(),
                name(),
                createUpdateParametersInner)
                .flatMap(new Func1<DatabaseAccountInner, Observable<? extends DocumentDBAccount>>() {
                    @Override
                    public Observable<? extends DocumentDBAccount> call(DatabaseAccountInner databaseAccountInner) {
                        self.failoverPolicies.clear();
                        self.hasFailoverPolicyChanges = false;
                        return manager().databaseAccounts().getByResourceGroupAsync(
                                resourceGroupName(),
                                name()
                        ).repeatWhen(new Func1<Observable<? extends java.lang.Void>, Observable<?>>() {
                            @Override
                            public Observable<?> call(Observable<? extends Void> observable) {
                                data.set(0, data.get(0) + 5);
                                return observable.delay(5, TimeUnit.SECONDS);
                            }
                        })
                        .filter(new Func1<DocumentDBAccount, Boolean>() {
                            @Override
                            public Boolean call(DocumentDBAccount databaseAccount) {
                                if (maxDelayDueToMissingFailovers > data.get(0)
                                        && (databaseAccount.id() == null
                                        || databaseAccount.id().length() == 0
                                        || createUpdateParametersInner.locations().size()
                                        > databaseAccount.inner().failoverPolicies().size())) {
                                    data.set(0, data.get(0) + 5);
                                    return false;
                                }

                                if (isAFinalProvisioningState(databaseAccount.inner().provisioningState())) {
                                    for (Location location : databaseAccount.readableReplications()) {
                                        if (!isAFinalProvisioningState(location.provisioningState())) {
                                            return false;
                                        }

                                    }
                                } else {
                                    return false;
                                }

                                self.setInner(databaseAccount.inner());
                                return true;
                            }
                        })
                        .first();

                    }
                });
    }

    private void ensureFailoverIsInitialized() {
        if (this.isInCreateMode()) {
            return;
        }

        if (!this.hasFailoverPolicyChanges) {
            this.failoverPolicies.clear();
            FailoverPolicyInner[] policyInners = new FailoverPolicyInner[this.inner().failoverPolicies().size()];
            this.inner().failoverPolicies().toArray(policyInners);
            Arrays.sort(policyInners, new Comparator<FailoverPolicyInner>() {
                @Override
                public int compare(FailoverPolicyInner o1, FailoverPolicyInner o2) {
                    return o1.failoverPriority().compareTo(o2.failoverPriority());
                }
            });

            for (int i = 0; i < policyInners.length; i++) {
                this.failoverPolicies.add(policyInners[i]);
            }

            this.hasFailoverPolicyChanges = true;
        }
    }

    private boolean isAFinalProvisioningState(String state) {
        switch (state.toLowerCase()) {
            case "succeeded":
            case "canceled":
            case "failed":
                return true;
            default:
                return false;
        }
    }
}
