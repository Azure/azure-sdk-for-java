/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice;

import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DiagnosticLogsTests extends AppServiceTest {
    private String RG_NAME_1 = "";
    private String WEBAPP_NAME_1 = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        WEBAPP_NAME_1 = generateRandomResourceName("java-webapp-", 20);
        RG_NAME_1 = generateRandomResourceName("javacsmrg", 20);

        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME_1);
    }

    @Test
    public void canCRUDWebAppWithDiagnosticLogs() throws Exception {
        // Create with new app service plan
        WebApp webApp1 = appServiceManager.webApps().define(WEBAPP_NAME_1)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(RG_NAME_1)
                .withNewWindowsPlan(PricingTier.BASIC_B1)
                .defineDiagnosticLogsConfiguration()
                    .withApplicationLogging()
                    .withLogLevel(LogLevel.INFORMATION)
                    .withApplicationLogsStoredOnFileSystem()
                    .attach()
                .defineDiagnosticLogsConfiguration()
                    .withWebServerLogging()
                    .withWebServerLogsStoredOnFileSystem()
                    .withWebServerFileSystemQuotaInMB(50)
                    .withUnlimitedLogRetentionDays()
                    .attach()
                .create();
        Assertions.assertNotNull(webApp1);
        Assertions.assertEquals(Region.US_WEST, webApp1.region());
        AppServicePlan plan1 = appServiceManager.appServicePlans().getById(webApp1.appServicePlanId());
        Assertions.assertNotNull(plan1);
        Assertions.assertEquals(Region.US_WEST, plan1.region());
        Assertions.assertEquals(PricingTier.BASIC_B1, plan1.pricingTier());

        Assertions.assertNotNull(webApp1.diagnosticLogsConfig());
        Assertions.assertEquals(LogLevel.INFORMATION, webApp1.diagnosticLogsConfig().applicationLoggingFileSystemLogLevel());
        Assertions.assertEquals(LogLevel.OFF, webApp1.diagnosticLogsConfig().applicationLoggingStorageBlobLogLevel());
        Assertions.assertNull(webApp1.diagnosticLogsConfig().applicationLoggingStorageBlobContainer());
        Assertions.assertEquals(0, webApp1.diagnosticLogsConfig().applicationLoggingStorageBlobRetentionDays());
        Assertions.assertEquals(50, webApp1.diagnosticLogsConfig().webServerLoggingFileSystemQuotaInMB());
        // 0 means unlimited
        Assertions.assertEquals(0, webApp1.diagnosticLogsConfig().webServerLoggingFileSystemRetentionDays());
        Assertions.assertNull(webApp1.diagnosticLogsConfig().webServerLoggingStorageBlobContainer());
        Assertions.assertEquals(0, webApp1.diagnosticLogsConfig().webServerLoggingStorageBlobRetentionDays());
        Assertions.assertFalse(webApp1.diagnosticLogsConfig().detailedErrorMessages());
        Assertions.assertFalse(webApp1.diagnosticLogsConfig().failedRequestsTracing());

        // Update
        webApp1.update()
                .updateDiagnosticLogsConfiguration()
                    .withoutApplicationLogging()
                    .parent()
                .updateDiagnosticLogsConfiguration()
                    .withWebServerLogging()
                    .withWebServerLogsStoredOnFileSystem()
                    .withWebServerFileSystemQuotaInMB(80)
                    .withLogRetentionDays(3)
                    .withDetailedErrorMessages(true)
                    .parent()
                .apply();

        Assertions.assertNotNull(webApp1.diagnosticLogsConfig());
        Assertions.assertEquals(LogLevel.OFF, webApp1.diagnosticLogsConfig().applicationLoggingFileSystemLogLevel());
        Assertions.assertEquals(LogLevel.OFF, webApp1.diagnosticLogsConfig().applicationLoggingStorageBlobLogLevel());
        Assertions.assertNull(webApp1.diagnosticLogsConfig().applicationLoggingStorageBlobContainer());
        Assertions.assertEquals(0, webApp1.diagnosticLogsConfig().applicationLoggingStorageBlobRetentionDays());
        Assertions.assertEquals(80, webApp1.diagnosticLogsConfig().webServerLoggingFileSystemQuotaInMB());
        Assertions.assertEquals(3, webApp1.diagnosticLogsConfig().webServerLoggingFileSystemRetentionDays());
        Assertions.assertNull(webApp1.diagnosticLogsConfig().webServerLoggingStorageBlobContainer());
        Assertions.assertEquals(3, webApp1.diagnosticLogsConfig().webServerLoggingStorageBlobRetentionDays());
        Assertions.assertTrue(webApp1.diagnosticLogsConfig().detailedErrorMessages());
        Assertions.assertFalse(webApp1.diagnosticLogsConfig().failedRequestsTracing());
    }
}