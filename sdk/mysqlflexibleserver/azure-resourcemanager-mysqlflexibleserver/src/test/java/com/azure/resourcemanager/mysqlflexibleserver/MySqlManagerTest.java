// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.mysqlflexibleserver;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.mysqlflexibleserver.models.Server;
import com.azure.resourcemanager.resources.ResourceManager;
import io.netty.util.internal.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

public class MySqlManagerTest extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST;
    private String resourceGroupName = "rg" + randomPadding();
    private MySqlManager mysqlManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        mysqlManager = MySqlManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(REGION)
                .create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void testCreateServer() {
        Server server = null;
        String randomPadding = randomPadding();
        try {
            String serverName = "mysql" + randomPadding;
            String adminName = "sqlAdmin" + randomPadding;
            String adminPwd = "sqlAdmin"
                + UUID.randomUUID().toString().replace("-", StringUtil.EMPTY_STRING).substring(0, 8);
            // @embedmeStart
            server = mysqlManager.servers()
                .define(serverName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withAdministratorLogin(adminName)
                .withAdministratorLoginPassword(adminPwd)
                .create();
            // @embedmeEnd
            server.refresh();
            Assertions.assertEquals(server.name(), serverName);
            Assertions.assertEquals(server.name(), mysqlManager.servers().getById(server.id()).name());
            Assertions.assertTrue(mysqlManager.servers().list().stream().count() > 0);
        } finally {
            if (server != null) {
                mysqlManager.servers().deleteById(server.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
