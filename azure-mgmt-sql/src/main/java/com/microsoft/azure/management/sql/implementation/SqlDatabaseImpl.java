/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.sql.DatabaseEditions;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlServer;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.UUID;

/**
 * Implementation for SqlDatabase and its parent interfaces.
 */
public class SqlDatabaseImpl
        extends IndependentChildResourceImpl<
                            SqlDatabase,
                            SqlServer,
                            DatabaseInner,
                            SqlDatabaseImpl>
        implements SqlDatabase,
            SqlDatabase.Definition,
            SqlDatabase.Update {
    private final DatabasesInner innerCollection;

    protected SqlDatabaseImpl(String name,
                            DatabaseInner innerObject,
                            DatabasesInner innerCollection) {
        super(name, innerObject);
        this.innerCollection = innerCollection;
    }

    @Override
    public String sqlServerName() {
        return this.parentName;
    }

    @Override
    public String collation() {
        return this.inner().collation();
    }

    @Override
    public DateTime creationDate() {
        return this.inner().creationDate();
    }

    @Override
    public UUID currentServiceObjectiveId() {
        return this.inner().currentServiceObjectiveId();
    }

    @Override
    public String databaseId() {
        return this.inner().databaseId();
    }

    @Override
    public DateTime earliestRestoreDate() {
        return this.inner().earliestRestoreDate();
    }

    @Override
    public DatabaseEditions edition() {
        return this.inner().edition();
    }

    @Override
    public UUID requestedServiceObjectiveId() {
        return this.inner().requestedServiceObjectiveId();
    }

    @Override
    public long maxSizeBytes() {
        return this.inner().maxSizeBytes();
    }

    @Override
    public String requestedServiceObjectiveName() {
        return this.inner().requestedServiceObjectiveName();
    }

    @Override
    public String serviceLevelObjective() {
        return this.inner().serviceLevelObjective();
    }

    @Override
    public String status() {
        return this.inner().status();
    }

    @Override
    public String elasticPoolName() {
        return this.inner().elasticPoolName();
    }

    @Override
    public String defaultSecondaryLocation() {
        return this.inner().defaultSecondaryLocation();
    }

    @Override
    public List<ServiceTierAdvisorInner> serviceTierAdvisors() {
        return this.inner().serviceTierAdvisors();
    }

    @Override
    public UpgradeHintInner upgradeHint() {
        return this.inner().upgradeHint();
    }

    @Override
    public List<SchemaInner> schemas() {
        return this.inner().schemas();
    }

    @Override
    public List<TransparentDataEncryptionInner> transparentDataEncryption() {
        return this.inner().transparentDataEncryption();
    }

    @Override
    public List<RecommendedIndexInner> recommendedIndex() {
        return this.inner().recommendedIndex();
    }

    @Override
    public SqlDatabase refresh() {
        this.innerCollection.get(this.resourceGroupName(), this.sqlServerName(), this.name());
        return this;
    }

    @Override
    protected Observable<SqlDatabase> createChildResourceAsync() {
        final SqlDatabase self = this;
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.sqlServerName(), this.name(), this.inner())
                .map(new Func1<DatabaseInner, SqlDatabase>() {
            @Override
            public SqlDatabase call(DatabaseInner databaseInner) {
                setInner(databaseInner);

                return self;
            }
        });
    }

    @Override
    public SqlDatabaseImpl withCollation(String collation) {
        this.inner().withCollation(collation);
        return this;
    }

    @Override
    public SqlDatabaseImpl withEdition(DatabaseEditions edition) {
        this.inner().withEdition(edition);
        return this;
    }

    @Override
    public SqlDatabaseImpl withExistingElasticPoolName(String elasticPoolName) {
        throw new NotImplementedException("Will be implemented once we put support for elastic pools");
    }
}
