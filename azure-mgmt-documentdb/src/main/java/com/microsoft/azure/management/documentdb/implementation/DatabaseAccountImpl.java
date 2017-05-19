/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.documentdb.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.documentdb.DatabaseAccount;
import com.microsoft.azure.management.documentdb.Location;
import com.microsoft.azure.management.documentdb.DatabaseAccountKind;
import com.microsoft.azure.management.documentdb.DatabaseAccountOfferType;
import com.microsoft.azure.management.documentdb.ConsistencyPolicy;
import com.microsoft.azure.management.documentdb.KeyKind;
import com.microsoft.azure.management.documentdb.DefaultConsistencyLevel;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for DatabaseAccount.
 */
@LangDefinition
class DatabaseAccountImpl
        extends
        GroupableResourceImpl<
                DatabaseAccount,
                DatabaseAccountInner,
                DatabaseAccountImpl,
                DocumentDBManager>
        implements DatabaseAccount,
        DatabaseAccount.Definition,
        DatabaseAccount.Update {
    private List<FailoverPolicyInner> failoverPolicies;

    DatabaseAccountImpl(String name, DatabaseAccountInner innerObject, DocumentDBManager manager) {
        super(fixDBName(name), innerObject, manager);

        this.failoverPolicies = new ArrayList<FailoverPolicyInner>();
        if (this.inner().failoverPolicies() != null) {
            for (FailoverPolicyInner failoverPolicy : this.inner().failoverPolicies()) {
                this.failoverPolicies.add(failoverPolicy);
            }
        }
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
    public void failoverPriorityChange(List<Location> failoverPolicies) {
        this.failoverPriorityChangeAsync(failoverPolicies).toBlocking().last();
    }

    @Override
    public Observable<Void> failoverPriorityChangeAsync(List<Location> failoverPolicies) {
        List<FailoverPolicyInner> policyInners = new ArrayList<FailoverPolicyInner>();
        for (int i = 0; i < failoverPolicies.size(); i++) {
            Location location  = failoverPolicies.get(i);
            FailoverPolicyInner policyInner = new FailoverPolicyInner();
            policyInner.withLocationName(location.locationName());
            policyInner.withFailoverPriority(i);
            policyInners.add(policyInner);
        }

        return this.manager().inner().databaseAccounts().failoverPriorityChangeAsync(this.resourceGroupName(),
                this.name(), policyInners);
    }

    @Override
    public DatabaseAccountListKeysResultInner listKeys() {
        return this.listKeysAsync().toBlocking().last();
    }

    @Override
    public Observable<DatabaseAccountListKeysResultInner> listKeysAsync() {
        return this.manager().inner().databaseAccounts().listKeysAsync(this.resourceGroupName(),
                this.name());
    }

    @Override
    public DatabaseAccountListConnectionStringsResultInner listConnectionStrings() {
        return this.listConnectionStringsAsync().toBlocking().last();
    }

    @Override
    public Observable<DatabaseAccountListConnectionStringsResultInner> listConnectionStringsAsync() {
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
    public DatabaseAccountImpl withKind(DatabaseAccountKind kind) {
        this.inner().withKind(kind);
        return this;        
    }


    @Override
    public DatabaseAccountImpl withIpRangeFilter(String ipRangeFilter) {
        this.inner().withIpRangeFilter(ipRangeFilter);
        return this;        
    }

    @Override
    protected Observable<DatabaseAccountInner> getInnerAsync() {
        return this.manager().inner().databaseAccounts().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Observable<DatabaseAccount> createResourceAsync() {
        final DatabaseAccountImpl self = this;
        DatabaseAccountCreateUpdateParametersInner createUpdateParametersInner =
                this.createUpdateParametersInner(this.inner());
        return this.manager().inner().databaseAccounts().createOrUpdateAsync(
                resourceGroupName(),
                name(),
                createUpdateParametersInner)
                .flatMap(new Func1<DatabaseAccountInner, Observable<? extends DatabaseAccount>>() {
                    @Override
                    public Observable<? extends DatabaseAccount> call(DatabaseAccountInner databaseAccountInner) {
                        return manager().databaseAccounts().getByResourceGroupAsync(
                                resourceGroupName(),
                                name());
                    }
                });
    }

    private DatabaseAccountCreateUpdateParametersInner createUpdateParametersInner(DatabaseAccountInner inner) {
        DatabaseAccountCreateUpdateParametersInner createUpdateParametersInner =
                new DatabaseAccountCreateUpdateParametersInner();
        createUpdateParametersInner.withLocation(this.regionName().toLowerCase());
        createUpdateParametersInner.withConsistencyPolicy(inner.consistencyPolicy());
        createUpdateParametersInner.withDatabaseAccountOfferType(
                DatabaseAccountOfferType.STANDARD.toString());
        createUpdateParametersInner.withIpRangeFilter(inner.ipRangeFilter());
        createUpdateParametersInner.withKind(inner.kind());
        createUpdateParametersInner.withTags(inner.getTags());
        if (this.isInCreateMode()) {
            this.addLocationsForCreateUpdateParameters(createUpdateParametersInner, this.failoverPolicies);
        } else {
            this.addLocationsForCreateUpdateParameters(createUpdateParametersInner, this.inner().failoverPolicies());
        }

        return createUpdateParametersInner;
    }

    private static String fixDBName(String name) {
        return name.toLowerCase();
    }

    @Override
    public DatabaseAccountImpl withReadableFailover(Region region) {
        FailoverPolicyInner failoverPolicyInner = new FailoverPolicyInner();
        failoverPolicyInner.withLocationName(region.name());
        this.failoverPolicies.add(0, failoverPolicyInner);
        return this;
    }

    @Override
    public DatabaseAccountImpl withWritableFailover(Region region) {
        FailoverPolicyInner failoverPolicyInner = new FailoverPolicyInner();
        failoverPolicyInner.withLocationName(region.name());
        this.failoverPolicies.add(failoverPolicyInner);
        return this;
    }

    @Override
    public DatabaseAccountImpl withEventualConsistencyPolicy() {
        this.setConsistencyPolicy(DefaultConsistencyLevel.EVENTUAL, 0, 0);
        return this;
    }

    @Override
    public DatabaseAccountImpl withSessionConsistencyPolicy() {
        this.setConsistencyPolicy(DefaultConsistencyLevel.SESSION, 0, 0);
        return this;
    }

    @Override
    public DatabaseAccountImpl withBoundedStalenessConsistencyPolicy(int maxStalenessPrefix, int maxIntervalInSeconds) {
        this.setConsistencyPolicy(DefaultConsistencyLevel.BOUNDED_STALENESS,
                maxStalenessPrefix,
                maxIntervalInSeconds);
        return this;
    }

    @Override
    public DatabaseAccountImpl withStrongConsistencyPolicy() {
        this.setConsistencyPolicy(DefaultConsistencyLevel.STRONG, 0, 0);
        return this;
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
}
