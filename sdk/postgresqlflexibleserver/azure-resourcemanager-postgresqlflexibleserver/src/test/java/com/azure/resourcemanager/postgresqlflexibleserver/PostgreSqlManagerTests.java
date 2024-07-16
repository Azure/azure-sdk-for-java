// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.postgresqlflexibleserver;

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
import com.azure.resourcemanager.postgresqlflexibleserver.models.ActiveDirectoryAuthEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ArmServerKeyType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.AuthConfig;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Backup;
import com.azure.resourcemanager.postgresqlflexibleserver.models.DataEncryption;
import com.azure.resourcemanager.postgresqlflexibleserver.models.GeoRedundantBackupEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.HighAvailability;
import com.azure.resourcemanager.postgresqlflexibleserver.models.HighAvailabilityMode;
import com.azure.resourcemanager.postgresqlflexibleserver.models.IdentityType;
import com.azure.resourcemanager.postgresqlflexibleserver.models.PasswordAuthEnum;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ReplicationRole;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Server;
import com.azure.resourcemanager.postgresqlflexibleserver.models.ServerVersion;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Sku;
import com.azure.resourcemanager.postgresqlflexibleserver.models.SkuTier;
import com.azure.resourcemanager.postgresqlflexibleserver.models.Storage;
import com.azure.resourcemanager.postgresqlflexibleserver.models.UserAssignedIdentity;
import com.azure.resourcemanager.resources.ResourceManager;
import io.netty.util.internal.StringUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

public class PostgreSqlManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private PostgreSqlManager postgreSqlManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        postgreSqlManager = PostgreSqlManager
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
            String serverName = "postgresql" + randomPadding;
            String adminName = "sqlAdmin" + randomPadding;
            String adminPwd = "sqlAdmin"
                + UUID.randomUUID().toString().replace("-", StringUtil.EMPTY_STRING).substring(0, 8);
            // @embedmeStart
            server = postgreSqlManager.servers()
                .define(serverName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withAdministratorLogin(adminName)
                .withAdministratorLoginPassword(adminPwd)
                .withSku(new Sku().withName("Standard_D2ds_v4").withTier(SkuTier.GENERAL_PURPOSE))
                .withAuthConfig(new AuthConfig()
                    .withActiveDirectoryAuth(ActiveDirectoryAuthEnum.DISABLED)
                    .withPasswordAuth(PasswordAuthEnum.ENABLED))
                .withIdentity(new UserAssignedIdentity().withType(IdentityType.NONE))
                .withDataEncryption(new DataEncryption().withType(ArmServerKeyType.SYSTEM_MANAGED))
                .withVersion(ServerVersion.ONE_FOUR)
                .withAvailabilityZone("2")
                .withStorage(new Storage().withStorageSizeGB(128))
                .withBackup(new Backup()
                    .withGeoRedundantBackup(GeoRedundantBackupEnum.DISABLED)
                    .withBackupRetentionDays(7))
                .withHighAvailability(new HighAvailability().withMode(HighAvailabilityMode.DISABLED))
                .withReplicationRole(ReplicationRole.PRIMARY)
                .create();
            // @embedmeEnd
            server.refresh();
            Assertions.assertEquals(server.name(), serverName);
            Assertions.assertEquals(server.name(), postgreSqlManager.servers().getById(server.id()).name());
            Assertions.assertTrue(postgreSqlManager.servers().list().stream().count() > 0);
        } finally {
            if (server != null) {
                postgreSqlManager.servers().deleteById(server.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
