// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.management.Region;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServers;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Mono;

public class TestSql extends TestTemplate<SqlServer, SqlServers> {
    @Override
    public SqlServer createResource(SqlServers resources) throws Exception {
        final String sqlServerName = resources.manager().resourceManager().internalContext().randomResourceName("sql", 10);
        final SqlServer[] sqlServers = new SqlServer[1];
        Mono<SqlServer> resourceStream =
            resources
                .define(sqlServerName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withAdministratorLogin("admin32")
                .withAdministratorPassword("Password~1")
                .defineDatabase("database1").attach()
                .defineElasticPool("elasticPool1").withStandardPool().attach()
                .defineDatabase("databaseInEP").withExistingElasticPool("elasticPool1").attach()
                .defineFirewallRule("firewallRule1").withIpAddress("10.10.10.10").attach()
                .withTag("mytag", "testtag")
                .createAsync();


        sqlServers[0] = resourceStream.block();

        Assertions.assertNotNull(sqlServers[0].innerModel());

        Assertions.assertNotNull(sqlServers[0].innerModel());
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

        Assertions.assertNotNull(sqlServer.innerModel());
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
