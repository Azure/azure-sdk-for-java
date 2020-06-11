// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.sql.models.ElasticPoolEdition;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServers;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Flux;

public class TestSql extends TestTemplate<SqlServer, SqlServers> {
    @Override
    public SqlServer createResource(SqlServers resources) throws Exception {
        final String sqlServerName = resources.manager().sdkContext().randomResourceName("sql", 10);
        final SqlServer[] sqlServers = new SqlServer[1];
        final SettableFuture<SqlServer> future = SettableFuture.create();
        Flux<Indexable> resourceStream =
            resources
                .define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withAdministratorLogin("admin32")
                .withAdministratorPassword("Password~1")
                .withNewDatabase("database1")
                .withNewElasticPool("elasticPool1", ElasticPoolEdition.STANDARD, "databaseInEP")
                .withNewFirewallRule("10.10.10.10")
                .withTag("mytag", "testtag")
                .createAsync();

        resourceStream.last().subscribe(sqlServer -> future.set((SqlServer) sqlServer));

        sqlServers[0] = future.get();

        Assertions.assertNotNull(sqlServers[0].inner());

        Assertions.assertNotNull(sqlServers[0].inner());
        // Including master database
        Assertions.assertEquals(sqlServers[0].databases().list().size(), 3);
        Assertions.assertEquals(sqlServers[0].elasticPools().list().size(), 1);
        Assertions.assertEquals(sqlServers[0].firewallRules().list().size(), 2);

        return sqlServers[0];
    }

    @Override
    public SqlServer updateResource(SqlServer sqlServer) throws Exception {
        sqlServer =
            sqlServer
                .update()
                .withoutDatabase("database1")
                .withoutDatabase("databaseInEP")
                .withoutElasticPool("elasticPool1")
                .apply();

        Assertions.assertNotNull(sqlServer.inner());
        // Just master database
        Assertions.assertEquals(1, sqlServer.databases().list().size());
        Assertions.assertEquals(0, sqlServer.elasticPools().list().size());
        Assertions.assertEquals(2, sqlServer.firewallRules().list().size());

        return sqlServer;
    }

    @Override
    public void print(SqlServer sqlServer) {
        System
            .out
            .println(
                new StringBuilder()
                    .append("SqlServer : ")
                    .append(sqlServer.id())
                    .append(", Name: ")
                    .append(sqlServer.name())
                    .toString());
        System
            .out
            .println(
                new StringBuilder()
                    .append("Number of databases : ")
                    .append(sqlServer.databases().list().size())
                    .toString());
        System
            .out
            .println(
                new StringBuilder()
                    .append("Number of elastic pools : ")
                    .append(sqlServer.elasticPools().list().size())
                    .toString());
    }
}
