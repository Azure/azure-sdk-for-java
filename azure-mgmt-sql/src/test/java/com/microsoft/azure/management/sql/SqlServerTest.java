/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.sql.implementation.SqlServerManager;
import com.microsoft.rest.RestClient;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public abstract class SqlServerTest extends TestBase {
    protected static ResourceManager resourceManager;
    protected static SqlServerManager sqlServerManager;
    protected static String RG_NAME = "";
    protected static String SQL_SERVER_NAME = "";

    @Override
    protected RestClient buildRestClient(RestClient.Builder builder, boolean isMocked) {
        if (!isMocked) {
        return super.buildRestClient(builder.withReadTimeout(150, TimeUnit.SECONDS) , isMocked);
    }
        return super.buildRestClient(builder, isMocked);
    }

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javasqlrg", 20);
        SQL_SERVER_NAME = generateRandomResourceName("javasqlserver", 20);

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);

        sqlServerManager = SqlServerManager
                .authenticate(restClient, defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME);
    }
}
