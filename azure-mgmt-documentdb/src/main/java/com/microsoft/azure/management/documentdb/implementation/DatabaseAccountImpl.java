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
import com.microsoft.azure.management.documentdb.FailoverPolicies;
import com.microsoft.azure.management.documentdb.KeyKind;
import com.microsoft.azure.management.documentdb.DefaultConsistencyLevel;
import com.microsoft.azure.management.documentdb.DBLocation;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private List<Location> allLocations;
    private Map<String, FailoverPolicyInner> failoverPolicies;

    DatabaseAccountImpl(String name, DatabaseAccountInner innerObject, DocumentDBManager manager) {
        super(fixDBName(name), innerObject, manager);
        this.allLocations = new ArrayList<Location>();
        if (this.inner().writeLocations() != null) {
            for (Location location : this.inner().writeLocations()) {
                this.allLocations.add(location);
            }
        }

        if (this.inner().readLocations() != null) {
            for (Location location : this.inner().readLocations()) {
                this.allLocations.add(location);
            }
        }

        this.failoverPolicies = new HashMap<String, FailoverPolicyInner>();
        if (this.inner().failoverPolicies() != null) {
            for (FailoverPolicyInner failoverPolicy : this.inner().failoverPolicies()) {
                this.failoverPolicies.put(failoverPolicy.id(), failoverPolicy);
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
    public Map<String, Location> locations() {
        Map<String, Location> locations = new HashMap<String, Location>();
        for (Location location : this.allLocations) {
            locations.put(location.id(), location);
        }
        return locations;
    }

    @Override
    public Map<String, FailoverPolicyInner> failoverPolicies() {
        return this.failoverPolicies;
    }

    @Override
    public void failoverPriorityChange(FailoverPolicies failoverPolicies) {
        this.failoverPriorityChangeAsync(failoverPolicies).toBlocking().last();
    }

    @Override
    public Observable<Void> failoverPriorityChangeAsync(FailoverPolicies failoverPolicies) {
        return this.manager().inner().databaseAccounts().failoverPriorityChangeAsync(this.resourceGroupName(),
                this.name(), failoverPolicies.failoverPolicies());
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
    public DatabaseAccountImpl withConsistencyPolicy(DefaultConsistencyLevel defaultConsistencyLevel,
                                                     int maxStalenessPrefix,
                                                     int maxIntervalInSeconds) {
        ConsistencyPolicy policy = new ConsistencyPolicy();
        policy.withDefaultConsistencyLevel(defaultConsistencyLevel);
        policy.withMaxIntervalInSeconds(maxIntervalInSeconds);
        policy.withMaxStalenessPrefix((long) maxStalenessPrefix);
        this.inner().withConsistencyPolicy(policy);
        return null;
    }

    @Override
    public DBLocationImpl defineLocation(Region region) {
        DBLocationImpl dbLocation =  new DBLocationImpl(new Location(), this);
        dbLocation.withLocation(region.name().toLowerCase());
        return dbLocation;
    }

    @Override
    public DatabaseAccountImpl removeLocation(Region region) {
        String regionName = region.name().toLowerCase();
        if (this.locations().containsKey(regionName)) {
            this.locations().remove(regionName);
        }

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
        return this.manager().inner().databaseAccounts().createOrUpdateAsync(
                resourceGroupName(),
                name(),
                this.createUpdateParametersInner())
                .map(new Func1<DatabaseAccountInner, DatabaseAccount>() {
                    @Override
                    public DatabaseAccount call(DatabaseAccountInner databaseAccountInner) {
                        self.setInner(databaseAccountInner);
                        return self;
                    }
                });
    }

    private DatabaseAccountCreateUpdateParametersInner createUpdateParametersInner() {
        DatabaseAccountCreateUpdateParametersInner createUpdateParametersInner =
                new DatabaseAccountCreateUpdateParametersInner();
        createUpdateParametersInner.withLocation(this.regionName().toLowerCase());
        createUpdateParametersInner.withConsistencyPolicy(this.inner().consistencyPolicy());
        createUpdateParametersInner.withLocation(this.regionName().toLowerCase());
        createUpdateParametersInner.withLocations(this.allLocations);
        if (this.inner().databaseAccountOfferType() != null) {
            createUpdateParametersInner.withDatabaseAccountOfferType(this.inner().databaseAccountOfferType().toString());
        }

        createUpdateParametersInner.withIpRangeFilter(this.inner().ipRangeFilter());
        createUpdateParametersInner.withKind((this.inner().kind()));
        createUpdateParametersInner.withTags(this.inner().getTags());
        return createUpdateParametersInner;
    }

    void addDBLocation(DBLocation dbLocation) {
        this.allLocations.add(dbLocation.inner());
    }

    private static String fixDBName(String name) {
        return name.toLowerCase();
    }
}
