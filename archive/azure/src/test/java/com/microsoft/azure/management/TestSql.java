/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management;

import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.sql.ElasticPoolEditions;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.management.sql.SqlServers;
import org.junit.Assert;
import rx.Observable;
import rx.functions.Action1;

public class TestSql extends TestTemplate<SqlServer, SqlServers>  {
    @Override
    public SqlServer createResource(SqlServers resources) throws Exception {
        final String sqlServerName = "sql" + this.testId;
        final SqlServer[] sqlServers = new SqlServer[1];
        final SettableFuture<SqlServer> future = SettableFuture.create();
        Observable<Indexable> resourceStream = resources.define(sqlServerName)
                .withRegion(Region.INDIA_CENTRAL)
                .withNewResourceGroup()
                .withAdministratorLogin("admin32")
                .withAdministratorPassword("Password~1")
                .withNewDatabase("database1")
                .withNewElasticPool("elasticPool1", ElasticPoolEditions.STANDARD, "databaseInEP")
                .withNewFirewallRule("10.10.10.10")
                .withTag("mytag", "testtag")
                .createAsync();

        Utils.<SqlServer>rootResource(resourceStream)
                .subscribe(new Action1<SqlServer>() {
                    @Override
                    public void call(SqlServer sqlServer) {
                        future.set(sqlServer);
                    }
                });

        sqlServers[0] = future.get();

        Assert.assertNotNull(sqlServers[0].inner());

        Assert.assertNotNull(sqlServers[0].inner());
        // Including master database
        Assert.assertEquals(sqlServers[0].databases().list().size(), 3);
        Assert.assertEquals(sqlServers[0].elasticPools().list().size(), 1);
        Assert.assertEquals(sqlServers[0].firewallRules().list().size(), 1);

        return sqlServers[0];
    }

    @Override
    public SqlServer updateResource(SqlServer sqlServer) throws Exception {
        sqlServer = sqlServer.update()
                .withoutDatabase("database1")
                .withoutDatabase("databaseInEP")
                .withoutElasticPool("elasticPool1")
                .apply();

        Assert.assertNotNull(sqlServer.inner());
        // Just master database
        Assert.assertEquals(1, sqlServer.databases().list().size());
        Assert.assertEquals(0, sqlServer.elasticPools().list().size());
        Assert.assertEquals(1, sqlServer.firewallRules().list().size());

        return sqlServer;
    }

    @Override
    public void print(SqlServer sqlServer) {
        System.out.println(new StringBuilder().append("SqlServer : ").append(sqlServer.id()).append(", Name: ").append(sqlServer.name()).toString());
        System.out.println(new StringBuilder().append("Number of databases : ").append(sqlServer.databases().list().size()).toString());
        System.out.println(new StringBuilder().append("Number of elastic pools : ").append(sqlServer.elasticPools().list().size()).toString());
    }
}
