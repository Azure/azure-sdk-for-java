/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.sql.DatabaseEditions;
import com.microsoft.azure.management.sql.ServiceObjectiveName;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlServer;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.UUID;

/**
 * Implementation for SqlDatabase and its parent interfaces.
 */
class SqlDatabaseImpl
        extends IndependentChildResourceImpl<
                            SqlDatabase,
                            SqlServer,
                            DatabaseInner,
                            SqlDatabaseImpl>
        implements SqlDatabase,
            SqlDatabase.Definition,
            SqlDatabase.Update,
        IndependentChild.DefinitionStages.WithParentResource<SqlDatabase, SqlServer> {
    private final DatabasesInner innerCollection;
    private String elasticPoolCreatableKey;

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
        return Long.parseLong(this.inner().maxSizeBytes());
    }

    @Override
    public ServiceObjectiveName requestedServiceObjectiveName() {
        return this.inner().requestedServiceObjectiveName();
    }

    @Override
    public ServiceObjectiveName serviceLevelObjective() {
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
        final SqlDatabaseImpl self = this;
        if (this.elasticPoolCreatableKey != null) {
            SqlElasticPool sqlElasticPool = (SqlElasticPool) this.createdResource(this.elasticPoolCreatableKey);
            withExistingElasticPool(sqlElasticPool);
        }
        if (this.inner().elasticPoolName() != null && !this.inner().elasticPoolName().isEmpty()) {
            this.inner().withEdition(new DatabaseEditions(""));
            this.inner().withRequestedServiceObjectiveName(new ServiceObjectiveName(""));
            this.inner().withRequestedServiceObjectiveId(null);
        }

        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.sqlServerName(), this.name(), this.inner())
                .map(new Func1<DatabaseInner, SqlDatabase>() {
            @Override
            public SqlDatabase call(DatabaseInner databaseInner) {
                setInner(databaseInner);
                self.elasticPoolCreatableKey = null;
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
    public SqlDatabaseImpl withoutExistingElasticPool() {
        this.inner().withElasticPoolName("");
        return this;
    }

    @Override
    public SqlDatabaseImpl withExistingElasticPool(String elasticPoolName) {
        this.inner().withElasticPoolName(elasticPoolName);
        return this;
    }

    @Override
    public SqlDatabaseImpl withExistingElasticPool(SqlElasticPool sqlElasticPool) {
        return this.withExistingElasticPool(sqlElasticPool.name());
    }

    @Override
    public SqlDatabaseImpl withNewElasticPool(Creatable<SqlElasticPool> sqlElasticPool) {
        if (this.elasticPoolCreatableKey == null) {
            this.elasticPoolCreatableKey = sqlElasticPool.key();
            this.addCreatableDependency(sqlElasticPool);
        }
        return this;
    }

    @Override
    public SqlDatabaseImpl withMaxSizeBytes(long maxSizeBytes) {
        this.inner().withMaxSizeBytes(Long.toString(maxSizeBytes));
        return this;
    }

    @Override
    public SqlDatabaseImpl withServiceObjective(ServiceObjectiveName serviceLevelObjective) {
        this.inner().withRequestedServiceObjectiveName(serviceLevelObjective);
        this.inner().withRequestedServiceObjectiveId(null);
        return this;
    }
/*
    private UUID getServiceLevelObjectiveId(ServiceObjectiveName serviceObjectiveName)
    {
        if (serviceObjectiveName.toString().equalsIgnoreCase(ServiceObjectiveName.S0.toString()))
        {
            return UUID.fromString("dd6d99bb-f193-4ec1-86f2-43d3bccbc49c");
        }
        if (serviceObjectiveName.toString().equalsIgnoreCase(ServiceObjectiveName.S1.toString()))
        {
            return UUID.fromString("f1173c43-91bd-4aaa-973c-54e79e15235b");
        }
        if (serviceObjectiveName.toString().equalsIgnoreCase(ServiceObjectiveName.S2.toString()))
        {
            return UUID.fromString("1b1ebd4d-d903-4baa-97f9-4ea675f5e928");
        }
        if (serviceObjectiveName.toString().equalsIgnoreCase(ServiceObjectiveName.S3.toString()))
        {
            return UUID.fromString("455330e1-00cd-488b-b5fa-177c226f28b7");
        }
        if (serviceObjectiveName.toString().equalsIgnoreCase(ServiceObjectiveName.P1.toString()))
        {
            return UUID.fromString("7203483a-c4fb-4304-9e9f-17c71c904f5d");
        }
        if (serviceObjectiveName.toString().equalsIgnoreCase(ServiceObjectiveName.P2.toString()))
        {
            return UUID.fromString("789681b8-ca10-4eb0-bdf2-e0b050601b40");
        }
        if (serviceObjectiveName.toString().equalsIgnoreCase(ServiceObjectiveName.P3.toString()))
        {
            return UUID.fromString("a7d1b92d-c987-4375-b54d-2b1d0e0f5bb0");
        }
        if (serviceObjectiveName.toString().equalsIgnoreCase(ServiceObjectiveName.P3.toString()))
        {
            return UUID.fromString("a7c4c615-cfb1-464b-b252-925be0a19446");
        }

        return null;
    }*/
}
